package com.aguacero.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aguacero.app.data.AlmacenPreferencias
import com.aguacero.app.data.RepositorioDatos
import com.aguacero.app.model.Categoria
import com.aguacero.app.model.Evento
import com.aguacero.app.util.aLocalDate
import com.aguacero.app.util.esEsteFinde
import com.aguacero.app.util.esHoy
import com.aguacero.app.util.eventoFuturo
import com.aguacero.app.util.proximaFecha
import com.aguacero.app.util.fechasEfectivas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Rango de fecha seleccionable en los filtros. */
enum class RangoFecha { HOY, MANANA, ESTE_FINDE, ESPECIFICA, TODOS }

data class EstadoUI(
    val cargando: Boolean = true,
    val error: String? = null,
    val desdeCache: Boolean = false,
    val eventos: List<Evento> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    // Filtros y búsqueda
    val busqueda: String = "",
    val categoriaActiva: String? = null,   // null = todas
    val rangoFecha: RangoFecha = RangoFecha.TODOS,
    val fechaEspecifica: LocalDate? = null
)

/** ViewModel de la cartelera: carga, caché, filtros, búsqueda y favoritos. */
class ModeloCartelera(app: Application) : AndroidViewModel(app) {

    private val repositorio = RepositorioDatos(app)
    val preferencias = AlmacenPreferencias(app)

    private val _estado = MutableStateFlow(EstadoUI())
    val estado: StateFlow<EstadoUI> = _estado.asStateFlow()

    /** Novedades: eventos nuevos detectados y aún no revisados. */
    val novedades: StateFlow<Set<String>> = preferencias.novedades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /** Marca todas las novedades como vistas (al abrir la campana). */
    fun marcarNovedadesVistas() {
        viewModelScope.launch { preferencias.limpiarNovedades() }
    }

    /** Quita un evento de las novedades (al abrir su detalle). */
    fun quitarNovedad(id: String) {
        viewModelScope.launch { preferencias.quitarNovedad(id) }
    }

    /** Favoritos guardados localmente con DataStore. */
    val favoritos: StateFlow<Set<String>> = preferencias.favoritos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        cargar(false)
    }

    fun cargar(forzarRefresco: Boolean = false) {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, error = null)
            try {
                val resultado = repositorio.obtener()
                _estado.value = _estado.value.copy(
                    cargando = false,
                    error = null,
                    desdeCache = resultado.desdeCache,
                    eventos = resultado.datos.eventos
                        .filter { eventoFuturo(it) }
                        .sortedBy { it.proximaFecha() },
                    categorias = resultado.datos.categorias
                )
                if (!resultado.desdeCache) {
                    detectarNovedades(resultado.datos.eventos.map { it.id }.toSet())
                }
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(
                    cargando = false,
                    error = "No se pudo cargar la cartelera. Revisa tu conexión."
                )
            }
        }
    }

    /**
     * Al abrir la app con datos frescos: marca los eventos nuevos como
     * novedades (sin notificar) y actualiza los IDs vistos, para que el
     * trabajador en segundo plano no avise por lo que ya se vio aquí.
     */
    private suspend fun detectarNovedades(idsActuales: Set<String>) {
        try {
            val vistos = preferencias.leerIdsVistos()
            if (vistos.isEmpty()) {
                preferencias.guardarIdsVistos(idsActuales)
            } else {
                val nuevos = idsActuales - vistos
                if (nuevos.isNotEmpty()) preferencias.agregarNovedades(nuevos)
                preferencias.guardarIdsVistos(idsActuales)
            }
        } catch (_: Exception) {
            // La detección es secundaria: nunca debe tumbar la carga
        }
    }

    fun buscar(texto: String) {
        _estado.value = _estado.value.copy(busqueda = texto)
    }

    fun filtrarCategoria(id: String?) {
        _estado.value = _estado.value.copy(categoriaActiva = id)
    }

    fun filtrarRango(rango: RangoFecha) {
        _estado.value = _estado.value.copy(rangoFecha = rango, fechaEspecifica = null)
    }

    fun filtrarFechaEspecifica(fecha: LocalDate) {
        _estado.value = _estado.value.copy(rangoFecha = RangoFecha.ESPECIFICA, fechaEspecifica = fecha)
    }

    fun limpiarFechaEspecifica() {
        _estado.value = _estado.value.copy(rangoFecha = RangoFecha.TODOS, fechaEspecifica = null)
    }

    fun alternarFavorito(id: String) {
        viewModelScope.launch { preferencias.alternarFavorito(id) }
    }

    /** ¿La fecha ISO corresponde a mañana? */
    private fun esManana(fechaIso: String): Boolean =
        fechaIso.aLocalDate() == LocalDate.now().plusDays(1)

    /** ¿Alguna fecha del evento cumple el criterio? */
    private fun algunaFecha(evento: Evento, criterio: (String) -> Boolean): Boolean =
        evento.fechasEfectivas().any(criterio)

    /** Lista final aplicando categoría + rango de fecha + búsqueda. */
    fun eventosFiltrados(): List<Evento> {
        val e = _estado.value
        var lista = e.eventos

        e.categoriaActiva?.let { cat ->
            lista = lista.filter { it.categoriaId == cat }
        }

        lista = when (e.rangoFecha) {
            RangoFecha.HOY -> lista.filter { algunaFecha(it) { f -> esHoy(f) } }
            RangoFecha.MANANA -> lista.filter { algunaFecha(it) { f -> esManana(f) } }
            RangoFecha.ESTE_FINDE -> lista.filter { algunaFecha(it) { f -> esEsteFinde(f) } }
            RangoFecha.ESPECIFICA -> e.fechaEspecifica?.let { f ->
                lista.filter { it.fechasEfectivas().any { iso -> iso.aLocalDate() == f } }
            } ?: lista
            RangoFecha.TODOS -> lista
        }

        val consulta = e.busqueda.trim().lowercase()
        if (consulta.isNotEmpty()) {
            lista = lista.filter {
                it.titulo.lowercase().contains(consulta) ||
                    it.lugar.lowercase().contains(consulta) ||
                    it.descripcion.lowercase().contains(consulta)
            }
        }
        return lista
    }

    fun eventosFavoritos(): List<Evento> {
        val ids = favoritos.value
        return _estado.value.eventos.filter { it.id in ids }
    }

    fun categoriaDe(evento: Evento): Categoria? =
        _estado.value.categorias.find { it.id == evento.categoriaId }

    fun eventoPorId(id: String): Evento? =
        _estado.value.eventos.find { it.id == id }
}
