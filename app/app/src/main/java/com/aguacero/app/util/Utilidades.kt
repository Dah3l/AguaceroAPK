package com.aguacero.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aguacero.app.R
import com.aguacero.app.model.Evento
import com.aguacero.app.workers.TrabajoNuevosEventos
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Utilidades de fecha, notificaciones y acciones para Aguacero.
 */

// ---------- Fechas ----------

private val FORMATEADOR_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale("es"))

fun String.aLocalDate(): LocalDate? =
    try { LocalDate.parse(this.trim(), FORMATEADOR_FECHA) } catch (_: Exception) { null }

/** "2026-10-18" -> "dom 18 oct 2026" */
fun formatearFechaCorta(fechaIso: String): String {
    val f = fechaIso.aLocalDate() ?: return fechaIso
    val diaSemana = f.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es"))
        .replace(".", "")
    return "$diaSemana ${f.dayOfMonth} ${f.month.getDisplayName(TextStyle.SHORT, Locale("es"))} ${f.year}"
}

/** "2026-10-18" -> "domingo 18 de octubre de 2026" */
fun formatearFechaLarga(fechaIso: String): String {
    val f = fechaIso.aLocalDate() ?: return fechaIso
    return "${f.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es"))} ${f.dayOfMonth} " +
        "de ${f.month.getDisplayName(TextStyle.FULL, Locale("es"))} de ${f.year}"
}

/** ¿El evento es hoy? */
fun esHoy(fechaIso: String): Boolean = fechaIso.aLocalDate() == LocalDate.now()

/** ¿El evento cae en el próximo fin de semana (viernes, sábado o domingo)? */
fun esEsteFinde(fechaIso: String): Boolean {
    val f = fechaIso.aLocalDate() ?: return false
    val hoy = LocalDate.now()
    if (f.isBefore(hoy)) return false
    // Viernes de esta semana (si hoy ya pasó el viernes, el de la próxima)
    val viernesEstaSemana = when (hoy.dayOfWeek.value) {
        5 -> hoy            // hoy es viernes
        6 -> hoy.minusDays(1)
        7 -> hoy.minusDays(2)
        else -> hoy.plusDays((5 - hoy.dayOfWeek.value).toLong()) // LUN..JUE
    }
    val findeInicio = viernesEstaSemana
    val findeFin = viernesEstaSemana.plusDays(2)
    return !f.isBefore(findeInicio) && !f.isAfter(findeFin)
}

/** Todas las fechas ISO del evento (usa `fecha` si no hay lista). */
fun Evento.fechasEfectivas(): List<String> =
    if (fechas.isNotEmpty()) fechas.sorted() else listOf(fecha)

/** La próxima fecha del evento: la más cercana que no haya pasado. */
fun Evento.proximaFecha(): String {
    val hoy = LocalDate.now().toString()
    val lista = fechasEfectivas()
    return lista.firstOrNull { it >= hoy } ?: lista.lastOrNull() ?: fecha
}

/** ¿El evento sigue siendo futuro (alguna fecha + hora)? */
fun eventoFuturo(evento: Evento): Boolean {
    val hoy = LocalDate.now()
    return evento.fechasEfectivas().any { fechaIso ->
        val f = fechaIso.aLocalDate() ?: return@any false
        when {
            f.isAfter(hoy) -> true
            f.isBefore(hoy) -> false
            else -> try {
                val hora = LocalDateTime.parse(
                    "${fechaIso}T${evento.hora.ifBlank { "23:59" }}",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale("es"))
                )
                hora.isAfter(LocalDateTime.now())
            } catch (_: Exception) {
                true
            }
        }
    }
}

// ---------- Acciones sobre eventos ----------

fun abrirTelefono(contexto: Context, numero: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${numero.filter { !it.isWhitespace() }}"))
    try { contexto.startActivity(intent) } catch (_: Exception) { }
}

fun abrirWeb(contexto: Context, url: String) {
    if (url.isBlank()) return
    val corregida = if (url.startsWith("http")) url else "https://$url"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(corregida))
    try { contexto.startActivity(intent) } catch (_: Exception) { }
}

fun chatearWhatsApp(contexto: Context, numero: String, mensaje: String) {
    val limpio = numero.filter { it.isDigit() || it == '+' }
    if (limpio.isBlank()) return
    // wa.me requiere el número sin + ni espacios
    val sinMas = limpio.replace("+", "")
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://wa.me/$sinMas?text=${Uri.encode(mensaje)}")
        setPackage("com.whatsapp")
    }
    try {
        contexto.startActivity(intent)
    } catch (_: Exception) {
        // Sin WhatsApp, intentar genérico
        val generico = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$sinMas?text=${Uri.encode(mensaje)}"))
        try { contexto.startActivity(generico) } catch (_: Exception) { }
    }
}


/** Convierte "17:28" a "5:28 PM". */
fun formatearHoraAMPM(hora: String): String {
    return try {
        val partes = hora.trim().split(":")
        if (partes.size < 2) return hora
        var h = partes[0].toInt()
        val m = partes[1].take(2)
        val ampm = if (h >= 12) "PM" else "AM"
        h = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        "$h:$m $ampm"
    } catch (_: Exception) { hora }
}

fun compartirGenerico(contexto: Context, mensaje: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, mensaje)
    }
    try {
        contexto.startActivity(Intent.createChooser(intent, "Compartir"))
    } catch (_: Exception) { }
}

fun compartirWhatsApp(contexto: Context, mensaje: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(mensaje)}")
        setPackage("com.whatsapp")
    }
    try {
        contexto.startActivity(intent)
    } catch (_: Exception) {
        // Si no hay WhatsApp, compartir genérico
        val generico = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }
        try { contexto.startActivity(Intent.createChooser(generico, "Compartir")) }
        catch (_: Exception) { }
    }
}

/** Abre el cliente de correo para escribir a la dirección dada. */
fun abrirCorreo(contexto: Context, correo: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$correo"))
        contexto.startActivity(intent)
    } catch (_: Exception) {
    }
}

fun textoParaCompartir(evento: Evento): String = buildString {
    append("🌧️ *Aguacero* te presenta:\n\n")
    append("*${evento.titulo}* 🎉\n")
    append("📅 ${formatearFechaLarga(evento.proximaFecha())}")
    if (evento.hora.isNotBlank()) append(" 🕐 ${evento.hora}")
    if (!evento.horaFin.isNullOrBlank()) append(" – ${evento.horaFin}")
    append("\n")
    if (evento.lugar.isNotBlank()) append("📍 ${evento.lugar}\n")
    if (evento.direccion.isNotBlank()) append("   ${evento.direccion}\n")
    if (evento.precio.isNotBlank()) append("💰 ${evento.precio}\n")
    if (evento.contacto.isNotEmpty()) append("📞 ${evento.contacto.joinToString(", ")}\n")
    if (evento.correo.isNotBlank()) append("✉️ ${evento.correo}\n")
    if (!evento.linkWeb.isNullOrBlank()) append("\n🔗 ${evento.linkWeb}")
    append("\n\n#Aguacero #LaHabana")
}

// ---------- Notificaciones ----------

fun crearCanalNotificaciones(contexto: Context) {
    val canal = android.app.NotificationChannel(
        contexto.getString(R.string.canal_notificaciones),
        contexto.getString(R.string.canal_notificaciones_nombre),
        android.app.NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = contexto.getString(R.string.canal_notificaciones_desc)
    }
    val gestor = contexto.getSystemService(Context.NOTIFICATION_SERVICE)
        as android.app.NotificationManager
    gestor.createNotificationChannel(canal)
}

fun notificarNuevoEvento(
    contexto: Context,
    titulo: String,
    cuerpo: String,
    idEvento: String? = null
) {
    // Al tocar la notificación se abre la app (y el detalle si hay evento)
    val intent = Intent(contexto, com.aguacero.app.MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or
            Intent.FLAG_ACTIVITY_SINGLE_TOP
        if (idEvento != null) putExtra("evento_id", idEvento)
    }
    val pendiente = android.app.PendingIntent.getActivity(
        contexto,
        idEvento?.hashCode() ?: 0,
        intent,
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
    )

    val notificacion = NotificationCompat.Builder(
        contexto,
        contexto.getString(R.string.canal_notificaciones)
    )
        .setSmallIcon(R.drawable.ic_logo_frontal)
        .setContentTitle(titulo)
        .setContentText(cuerpo)
        .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
        .setContentIntent(pendiente)
        .setAutoCancel(true)
        .build()

    val gestor = NotificationManagerCompat.from(contexto)
    try {
        gestor.notify(idEvento?.hashCode() ?: titulo.hashCode(), notificacion)
    } catch (_: SecurityException) {
        // Sin permiso de notificaciones concedido
    }
}

// ---------- Programación del trabajo periódico ----------

fun programarRevisionEventos(contexto: Context) {
    val restricciones = Constraints.Builder()
        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
        .build()

    WorkManager.getInstance(contexto).enqueueUniquePeriodicWork(
        TrabajoNuevosEventos.NOMBRE_TRABAJO,
        ExistingPeriodicWorkPolicy.UPDATE,
        PeriodicWorkRequestBuilder<TrabajoNuevosEventos>(
            15, java.util.concurrent.TimeUnit.MINUTES,
            5, java.util.concurrent.TimeUnit.MINUTES
        )
            .setConstraints(restricciones)
            .build()
    )
}

fun cancelarRevisionEventos(contexto: Context) {
    WorkManager.getInstance(contexto).cancelUniqueWork(TrabajoNuevosEventos.NOMBRE_TRABAJO)
}
