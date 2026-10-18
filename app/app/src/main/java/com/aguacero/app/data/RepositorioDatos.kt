package com.aguacero.app.data

import android.content.Context
import com.aguacero.app.model.DatosAguacero
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Repositorio de datos de Aguacero.
 *
 * - Descarga eventos.json desde GitHub Pages.
 * - Guarda una caché local en un archivo durante 24 horas,
 *   de modo que la app funciona sin conexión con los últimos datos.
 */
class RepositorioDatos(private val contexto: Context) {

    companion object {
        const val URL_EVENTOS = "https://dah3l.github.io/AguaceroAPK/docs/eventos.json"
        private const val ARCHIVO_CACHA = "eventos_cache.json"
        private const val DURACION_CACHE_MS = 24L * 60L * 60L * 1000L // 24 horas
    }

    private val clienteHttp = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun archivoCache(): File = File(contexto.filesDir, ARCHIVO_CACHA)

    /** ¿La caché local todavía es válida (menos de 24h)? */
    private fun cacheValida(): Boolean {
        val archivo = archivoCache()
        if (!archivo.exists()) return false
        return System.currentTimeMillis() - archivo.lastModified() < DURACION_CACHE_MS
    }

    /** Leer la caché local (puede ser antigua). */
    private fun leerCache(): DatosAguacero? {
        return try {
            val archivo = archivoCache()
            if (!archivo.exists()) null
            else json.decodeFromString(DatosAguacero.serializer(), archivo.readText())
        } catch (_: Exception) {
            null
        }
    }

    /** Descargar el JSON y actualizar la caché. Lanza excepción si falla la red. */
    private suspend fun descargar(): DatosAguacero = withContext(Dispatchers.IO) {
        val peticion = Request.Builder().url(URL_EVENTOS).build()
        clienteHttp.newCall(peticion).execute().use { respuesta ->
            if (!respuesta.isSuccessful) {
                throw java.io.IOException("Error HTTP ${respuesta.code} al descargar eventos")
            }
            val cuerpo = respuesta.body?.string()
                ?: throw java.io.IOException("Respuesta vacía del servidor")
            val datos = json.decodeFromString(DatosAguacero.serializer(), cuerpo)
            archivoCache().writeText(cuerpo)
            datos
        }
    }

    /**
     * Obtener los datos (estrategia "red primero"):
     * 1. Siempre intenta descargar de la red (salvo que se pida solo caché).
     * 2. Si la descarga falla, usa la caché aunque sea vieja (modo offline).
     * 3. Solo lanza excepción si no hay red NI caché.
     */
    suspend fun obtener(soloCache: Boolean = false): ResultadoDatos {
        if (!soloCache) {
            try {
                val fresca = descargar()
                return ResultadoDatos(fresca, desdeCache = false)
            } catch (_: Exception) {
                // Cae a caché abajo
            }
        }
        val cacheada = leerCache()
        if (cacheada != null) {
            return ResultadoDatos(cacheada, desdeCache = true)
        }
        // Sin red ni caché: intentar de todos modos para dar error descriptivo
        val fresca = descargar()
        return ResultadoDatos(fresca, desdeCache = false)
    }

    /** ¿La caché existe? (para saber si hay datos offline) */
    fun hayCache(): Boolean = archivoCache().exists()

    /** IDs actualmente en caché — usado por el trabajador de notificaciones. */
    fun idsEnCache(): Set<String> = leerCache()?.eventos?.map { it.id }?.toSet() ?: emptySet()

    data class ResultadoDatos(val datos: DatosAguacero, val desdeCache: Boolean)
}
