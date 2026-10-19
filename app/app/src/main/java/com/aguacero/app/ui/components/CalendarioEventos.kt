package com.aguacero.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.Color

private val AzulPrimario = Color(0xFF1786B4)
private val AzulOscuro = Color(0xFF0A3C55)
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Diálogo con calendario mensual para filtrar eventos por fecha específica.
 * Estilo basado en la referencia "Events Calendar", adaptado a la paleta Cielo.
 */
@Composable
fun CalendarioEventos(
    fechasConEventos: Set<LocalDate>,
    alSeleccionar: (LocalDate) -> Unit,
    alCerrar: () -> Unit
) {
    var mesActual by remember { mutableStateOf(YearMonth.now()) }
    var seleccionado by remember { mutableStateOf<LocalDate?>(null) }

    Dialog(onDismissRequest = alCerrar) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, MaterialTheme.shapes.large)
                .padding(20.dp)
        ) {
            // Cabecera: mes/año + flechas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { mesActual = mesActual.minusMonths(1) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Mes anterior",
                        tint = AzulOscuro
                    )
                }
                Text(
                    mesActual.month.getDisplayName(TextStyle.FULL, Locale("es"))
                        .replaceFirstChar { it.uppercase() } + " ${mesActual.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AzulOscuro
                )
                IconButton(onClick = { mesActual = mesActual.plusMonths(1) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Mes siguiente",
                        tint = AzulOscuro
                    )
                }
            }

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
            val primerDia = mesActual.atDay(1)
            // Ajustar para que domingo = 0
            val desplazamiento = (primerDia.dayOfWeek.value % 7)
            val diasEnMes = mesActual.lengthOfMonth()
            val hoy = LocalDate.now()

            Column {
                var dia = 1 - desplazamiento
                while (dia <= diasEnMes) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (i in 0..6) {
                            val fecha = if (dia in 1..diasEnMes) {
                                mesActual.atDay(dia)
                            } else null
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (fecha != null) {
                                    val esSeleccionado = fecha == seleccionado
                                    val tieneEventos = fecha in fechasConEventos
                                    val esHoy = fecha == hoy
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    esSeleccionado -> AzulPrimario
                                                    esHoy -> Color(0xFFE1F2FA)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable {
                                                seleccionado = fecha
                                                alSeleccionar(fecha)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "${fecha.dayOfMonth}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (esSeleccionado || esHoy) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    esSeleccionado -> Color.White
                                                    else -> AzulOscuro
                                                }
                                            )
                                            // Puntito si tiene eventos
                                            if (tieneEventos && !esSeleccionado) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(AzulPrimario)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            dia++
                        }
                    }
                }
            }
        }
    }
}
