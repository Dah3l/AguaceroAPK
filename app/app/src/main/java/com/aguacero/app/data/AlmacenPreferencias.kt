package com.aguacero.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

/**
 * Almacén local de preferencias con DataStore:
 * - Favoritos (conjunto de IDs de evento).
 * - Ajuste de notificaciones activadas/desactivadas.
 * - IDs de eventos ya vistos por el trabajador de notificaciones.
 */
private val Context.dataStore by preferencesDataStore(name = "aguacero_prefs")

class AlmacenPreferencias(private val contexto: Context) {

    companion object {
        private val CLAVE_FAVORITOS = stringSetPreferencesKey("favoritos")
        private val CLAVE_NOTIFICACIONES = booleanPreferencesKey("notificaciones_activas")
        private val CLAVE_VISTOS = stringSetPreferencesKey("ids_eventos_vistos")
    }

    /** Flujo de los IDs de eventos favoritos. */
    val favoritos: Flow<Set<String>> = contexto.dataStore.data
        .map { prefs -> prefs[CLAVE_FAVORITOS] ?: emptySet() }

    /** Alternar el estado de favorito de un evento. Devuelve el nuevo estado. */
    suspend fun alternarFavorito(idEvento: String): Boolean {
        var ahoraEsFavorito = false
        contexto.dataStore.edit { prefs ->
            val actuales = HashSet(prefs[CLAVE_FAVORITOS] ?: emptySet())
            ahoraEsFavorito = if (actuales.contains(idEvento)) {
                actuales.remove(idEvento); false
            } else {
                actuales.add(idEvento); true
            }
            prefs[CLAVE_FAVORITOS] = actuales
        }
        return ahoraEsFavorito
    }

    /** Ajuste de notificaciones de nuevos eventos (por defecto activadas). */
    val notificacionesActivas: Flow<Boolean> = contexto.dataStore.data
        .map { prefs -> prefs[CLAVE_NOTIFICACIONES] ?: true }

    suspend fun setNotificacionesActivas(activo: Boolean) {
        contexto.dataStore.edit { prefs ->
            prefs[CLAVE_NOTIFICACIONES] = activo
        }
    }

    /** Leer una vez el conjunto de IDs ya notificados (lo usa el WorkManager). */
    suspend fun leerIdsVistos(): Set<String> {
        val prefs = contexto.dataStore.data.firstOrNull() ?: return emptySet()
        return prefs[CLAVE_VISTOS] ?: emptySet()
    }

    suspend fun guardarIdsVistos(ids: Set<String>) {
        contexto.dataStore.edit { prefs ->
            prefs[CLAVE_VISTOS] = ids
        }
    }
}
