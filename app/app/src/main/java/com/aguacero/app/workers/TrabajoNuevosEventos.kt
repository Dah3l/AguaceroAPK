package com.aguacero.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import com.aguacero.app.data.AlmacenPreferencias
import com.aguacero.app.data.RepositorioDatos
import com.aguacero.app.util.formatearFechaCorta
import com.aguacero.app.util.notificarNuevoEvento
import com.aguacero.app.util.proximaFecha

/**
 * Trabajo periódico (cada 15 minutos) que revisa si hay eventos nuevos
 * en el JSON de GitHub Pages.
 *
 * Flujo: descarga condicional con ETag (si el servidor responde 304,
 * no hay cambios y termina sin gastar datos) → compara los IDs con los
 * ya vistos → la primera vez solo registra la base, sin avisar → si hay
 * novedades las marca en el almacén y, con las notificaciones activas,
 * avisa: 1 evento = notificación con su detalle (al tocarla abre el
 * evento); varios = una sola notificación resumen (abre la app).
 * Con las notificaciones desactivadas igual actualiza los vistos, para
 * que al reactivarlas no caigan avisos viejos.
 */
class TrabajoNuevosEventos(
    contextoAplicacion: Context,
    parametros: WorkerParameters
) : CoroutineWorker(contextoAplicacion, parametros) {

    companion object {
        const val NOMBRE_TRABAJO = "revision_nuevos_eventos"
    }

    override suspend fun doWork(): Result {
        val contexto = applicationContext
        val repositorio = RepositorioDatos(contexto)
        val preferencias = AlmacenPreferencias(contexto)
        val activas = preferencias.notificacionesActivas.first()

        return try {
            val resultado = repositorio.obtenerConETag(preferencias.leerETag())
            val datos = resultado.datos ?: return Result.success() // 304: sin cambios
            preferencias.guardarETag(resultado.etag)

            val idsActuales = datos.eventos.map { it.id }.toSet()
            val idsVistos = preferencias.leerIdsVistos()

            if (idsVistos.isEmpty()) {
                // Primera ejecución: registrar la base actual, sin avisar
                preferencias.guardarIdsVistos(idsActuales)
                return Result.success()
            }

            val nuevos = datos.eventos.filter { it.id !in idsVistos }
            // Los vistos siempre reflejan el catálogo actual
            preferencias.guardarIdsVistos(idsActuales)
            if (nuevos.isEmpty()) return Result.success()

            preferencias.agregarNovedades(nuevos.map { it.id }.toSet())
            if (!activas) return Result.success()

            if (nuevos.size == 1) {
                val ev = nuevos.first()
                notificarNuevoEvento(
                    contexto,
                    "🌧️ Nuevo evento en Aguacero",
                    "${ev.titulo} — ${formatearFechaCorta(ev.proximaFecha())} · ${ev.lugar}",
                    idEvento = ev.id
                )
            } else {
                notificarNuevoEvento(
                    contexto,
                    "🌧️ Novedades en Aguacero",
                    "${nuevos.size} eventos nuevos en la cartelera"
                )
            }
            Result.success()
        } catch (_: Exception) {
            // Sin conexión u otro error: reintentar en la próxima ventana
            Result.retry()
        }
    }
}
