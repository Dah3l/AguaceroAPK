package com.aguacero.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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

    // Mes actual + los próximos 5
    val meses = (0..5).map { YearMonth.now().plusMonths(it.toLong()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
        LazyColumn(modifier = Modifier.fillMaxSize()) {
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
