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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Rango de fecha seleccionable en los filtros. */
enum class RangoFecha { HOY, MANANA, ESTE_FINDE, TODOS }

data class EstadoUI(
    val cargando: Boolean = true,
    val error: String? = null,
    val desdeCache: Boolean = false,
    val eventos: List<Evento> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    // Filtros y búsqueda
    val busqueda: String = "",
    val categoriaActiva: String? = null,   // null = todas
    val rangoFecha: RangoFecha = RangoFecha.TODOS
)

/** ViewModel de la cartelera: carga, caché, filtros, búsqueda y favoritos. */
class ModeloCartelera(app: Application) : AndroidViewModel(app) {

    private val repositorio = RepositorioDatos(app)
    val preferencias = AlmacenPreferencias(app)

    private val _estado = MutableStateFlow(EstadoUI())
    val estado: StateFlow<EstadoUI> = _estado.asStateFlow()

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
                        .sortedBy { it.fecha },
                    categorias = resultado.datos.categorias
                )
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(
                    cargando = false,
                    error = "No se pudo cargar la cartelera. Revisa tu conexión."
                )
            }
        }
    }

    fun buscar(texto: String) {
        _estado.value = _estado.value.copy(busqueda = texto)
    }

    fun filtrarCategoria(id: String?) {
        _estado.value = _estado.value.copy(categoriaActiva = id)
    }

    fun filtrarRango(rango: RangoFecha) {
        _estado.value = _estado.value.copy(rangoFecha = rango)
    }

    fun alternarFavorito(id: String) {
        viewModelScope.launch { preferencias.alternarFavorito(id) }
    }

    /** ¿La fecha ISO corresponde a mañana? */
    private fun esManana(fechaIso: String): Boolean =
        fechaIso.aLocalDate() == LocalDate.now().plusDays(1)

    /** Lista final aplicando categoría + rango de fecha + búsqueda. */
    fun eventosFiltrados(): List<Evento> {
        val e = _estado.value
        var lista = e.eventos

        e.categoriaActiva?.let { cat ->
            lista = lista.filter { it.categoriaId == cat }
        }

        lista = when (e.rangoFecha) {
            RangoFecha.HOY -> lista.filter { esHoy(it.fecha) }
            RangoFecha.MANANA -> lista.filter { esManana(it.fecha) }
            RangoFecha.ESTE_FINDE -> lista.filter { esEsteFinde(it.fecha) }
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
