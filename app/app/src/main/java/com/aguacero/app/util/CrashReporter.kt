package com.aguacero.app.util

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Reporte de crashes: guarda el stack trace en un archivo local
 * cuando la app se cierra inesperadamente. Se puede ver y copiar
 * desde Ajustes → Registro de errores.
 */
object CrashReporter {

    private const val ARCHIVO = "crash_log.txt"

    fun instalar(contexto: Context) {
        val manejadorPrevio = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { hilo, error ->
            try {
                guardar(contexto, hilo, error)
            } catch (_: Exception) {
            }
            manejadorPrevio?.uncaughtException(hilo, error)
        }
    }

    private fun guardar(contexto: Context, hilo: Thread, error: Throwable) {
        val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("es"))
        val sb = StringBuilder()
        sb.appendLine("=== CRASH ${formato.format(Date())} ===")
        sb.appendLine("Hilo: ${hilo.name}")
        sb.appendLine("Error: ${error.javaClass.name}: ${error.message}")
        sb.appendLine("--- Stack trace ---")
        for (elemento in error.stackTrace.take(40)) {
            sb.appendLine("  at $elemento")
        }
        val causa = error.cause
        if (causa != null) {
            sb.appendLine("--- Causa ---")
            sb.appendLine("${causa.javaClass.name}: ${causa.message}")
            for (elemento in causa.stackTrace.take(20)) {
                sb.appendLine("  at $elemento")
            }
        }
        sb.appendLine()
        File(contexto.filesDir, ARCHIVO).writeText(sb.toString())
    }

    /** Leer el último crash guardado, o null si no hay. */
    fun leer(contexto: Context): String? {
        return try {
            val archivo = File(contexto.filesDir, ARCHIVO)
            if (!archivo.exists()) null
            else {
                val texto = archivo.readText()
                if (texto.isBlank()) null else texto
            }
        } catch (_: Exception) {
            null
        }
    }

    /** ¿Hay un crash guardado? */
    fun hayReporte(contexto: Context): Boolean = leer(contexto) != null

    /** Borrar el reporte. */
    fun limpiar(contexto: Context) {
        try {
            File(contexto.filesDir, ARCHIVO).delete()
        } catch (_: Exception) {
        }
    }
}
