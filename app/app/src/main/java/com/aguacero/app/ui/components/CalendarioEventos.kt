package com.aguacero.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val AzulPrimario = Color(0xFF1786B4)
private val AzulOscuro = Color(0xFF0A3C55)
private val AzulFondo = Color(0xFFE1F2FA)

/**
 * Grilla de un mes del calendario de eventos, en la paleta Cielo.
 * Los días con eventos llevan un puntito; hoy se resalta suave.
 * Al tocar un día se invoca [alSeleccionar].
 */
@Composable
fun MesCalendario(
    mes: YearMonth,
    fechasConEventos: Set<LocalDate>,
    alSeleccionar: (LocalDate) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Nombre del mes
        Text(
            mes.month.getDisplayName(TextStyle.FULL, Locale("es"))
                .replaceFirstChar { it.uppercase() } + " ${mes.year}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AzulOscuro,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Días de la semana
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb").forEach { dia ->
                Text(
                    dia,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }
        }

        // Grilla de días
        val primerDia = mes.atDay(1)
        val desplazamiento = primerDia.dayOfWeek.value % 7 // domingo = 0
        val diasEnMes = mes.lengthOfMonth()
        val hoy = LocalDate.now()

        var dia = 1 - desplazamiento
        while (dia <= diasEnMes) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (i in 0..6) {
                    val fecha = if (dia in 1..diasEnMes) mes.atDay(dia) else null
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (fecha != null) {
                            val tieneEventos = fecha in fechasConEventos
                            val esHoy = fecha == hoy
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (esHoy) AzulFondo else Color.Transparent)
                                    .then(
                                        if (tieneEventos)
                                            Modifier.border(1.5.dp, AzulPrimario, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { alSeleccionar(fecha) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${fecha.dayOfMonth}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (esHoy || tieneEventos) FontWeight.Bold else FontWeight.Normal,
                                    color = if (tieneEventos) AzulPrimario else AzulOscuro
                                )
                            }
                        }
                    }
                    dia++
                }
            }
        }
    }
}
