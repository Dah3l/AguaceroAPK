package com.aguacero.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aguacero.app.ui.ModeloCartelera
import com.aguacero.app.ui.RangoFecha
import com.aguacero.app.ui.components.ChipModerno
import com.aguacero.app.ui.components.TituloPestana
import com.aguacero.app.ui.components.MesCalendario
import com.aguacero.app.util.aLocalDate
import java.time.YearMonth

/**
 * Pestaña Calendario: filtros rápidos de fecha arriba y calendario con
 * scroll vertical (varios meses). Al tocar un día se filtra por esa fecha
 * y se salta a Inicio con los eventos del día.
 */
@Composable
fun PantallaCalendario(
    modelo: ModeloCartelera,
    alIrAInicio: () -> Unit
) {
    val estado by modelo.estado.collectAsState()

    val rangos = listOf(
        RangoFecha.TODOS to "Todos",
        RangoFecha.HOY to "Hoy",
        RangoFecha.MANANA to "Mañana",
        RangoFecha.ESTE_FINDE to "Este finde"
    )

    val fechasConEventos = estado.eventos.mapNotNull {
        try { it.fecha.aLocalDate() } catch (e: Exception) { null }
    }.toSet()

    // Meses dinámicos: desde hoy hasta el mes del evento más lejano
    // (mínimo 6 meses aunque no haya eventos tan lejos)
    val mesInicio = YearMonth.now()
    val mesMasLejano = fechasConEventos.maxOrNull()?.let { YearMonth.from(it) }
    val mesFin = maxOf(mesMasLejano ?: mesInicio, mesInicio.plusMonths(5))
    val meses = generateSequence(mesInicio) { it.plusMonths(1) }
        .takeWhile { it <= mesFin }
        .toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TituloPestana(
            "Calendario de eventos",
            modificador = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp)
        )

        // Filtros rápidos de fecha
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rangos) { (rango, nombre) ->
                ChipModerno(
                    texto = nombre,
                    seleccionado = estado.rangoFecha == rango,
                    alClic = {
                        modelo.filtrarRango(rango)
                        alIrAInicio()
                    }
                )
            }
        }

        // Calendario con scroll (sin flechas)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            items(meses, key = { it.toString() }) { mes ->
                MesCalendario(
                    mes = mes,
                    fechasConEventos = fechasConEventos,
                    alSeleccionar = { fecha ->
                        modelo.filtrarFechaEspecifica(fecha)
                        alIrAInicio()
                    }
                )
            }
        }
    }
}
