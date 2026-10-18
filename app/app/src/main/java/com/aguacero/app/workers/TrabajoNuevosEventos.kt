package com.aguacero.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import com.aguacero.app.data.AlmacenPreferencias
import com.aguacero.app.data.RepositorioDatos
import com.aguacero.app.util.notificarNuevoEvento

/**
 * Trabajo periódico (cada 6 horas) que revisa si hay eventos nuevos
 * en el JSON de GitHub Pages. Compara los IDs con la lista local de
 * eventos ya vistos y notifica: "Nuevo en Aguacero: [título]".
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

        // Si el usuario desactivó las notificaciones, no hacer nada
        val activas = preferencias.notificacionesActivas.first()
        if (!activas) return Result.success()

        return try {
            // Forzar descarga para detectar cambios
            val resultado = repositorio.obtener(forzarRefresco = true)
            val idsVistos = preferencias.leerIdsVistos()

            val nuevos = resultado.datos.eventos.filter { it.id !in idsVistos }

            if (idsVistos.isEmpty()) {
                // Primera ejecución: solo registrar la base actual, sin avisar
                preferencias.guardarIdsVistos(resultado.datos.eventos.map { it.id }.toSet())
            } else {
                nuevos.forEachIndexed { indice, evento ->
                    notificarNuevoEvento(
                        contexto,
                        "Nuevo en Aguacero",
                        evento.titulo
                    )
                    // Pequeña pausa para que el sistema no agrupe mal las alertas
                    if (indice < nuevos.size - 1) kotlinx.coroutines.delay(500)
                }
                if (nuevos.isNotEmpty()) {
                    preferencias.guardarIdsVistos(
                        idsVistos + nuevos.map { it.id }
                    )
                }
            }
            Result.success()
        } catch (_: Exception) {
            // Sin conexión u otro error: reintentar más tarde
            Result.retry()
        }
    }
}
