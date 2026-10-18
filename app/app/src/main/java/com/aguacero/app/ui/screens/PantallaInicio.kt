package com.aguacero.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aguacero.app.ui.ModeloCartelera
import com.aguacero.app.ui.RangoFecha
import com.aguacero.app.ui.components.EstadoVacio
import com.aguacero.app.ui.components.TarjetaEvento

/**
 * Pantalla de Inicio: lista de eventos futuros ordenados por cercanía,
 * con filtros por categoría y rango de fecha, y tirada para actualizar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(modelo: ModeloCartelera, alAbrirDetalle: (String) -> Unit) {
    val estado by modelo.estado.collectAsState()
    val favoritos by modelo.favoritos.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // ---- Chips de filtro por rango de fecha ----
        val rangos = listOf(
            RangoFecha.HOY to "Hoy",
            RangoFecha.MANANA to "Mañana",
            RangoFecha.ESTE_FINDE to "Este finde",
            RangoFecha.TODOS to "Todos"
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rangos) { (rango, nombre) ->
                FilterChip(
                    selected = estado.rangoFecha == rango,
                    onClick = { modelo.filtrarRango(rango) },
                    label = { Text(nombre) }
                )
            }
        }

        // ---- Chips de filtro por categoría ----
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = estado.categoriaActiva == null,
                    onClick = { modelo.filtrarCategoria(null) },
                    label = { Text("Todas") }
                )
            }
            items(estado.categorias) { categoria ->
                FilterChip(
                    selected = estado.categoriaActiva == categoria.id,
                    onClick = {
                        modelo.filtrarCategoria(
                            if (estado.categoriaActiva == categoria.id) null else categoria.id
                        )
                    },
                    label = { Text("${categoria.emoji} ${categoria.nombre}") }
                )
            }
        }

        if (estado.desdeCache && !estado.cargando) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .clickable { modelo.cargar(true) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📴", style = MaterialTheme.typography.titleMedium)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Sin conexión",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "Mostrando datos guardados · Toca para reintentar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Reintentar",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // ---- Lista con tirada para actualizar ----
        val eventos = modelo.eventosFiltrados()

        when {
            estado.cargando && eventos.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            estado.error != null && eventos.isEmpty() -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(estado.error ?: "", modifier = Modifier.padding(16.dp))
                Row {
                    androidx.compose.material3.Button(
                        onClick = { modelo.cargar(true) },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.height(0.dp))
                        Text("Reintentar")
                    }
                }
            }

            eventos.isEmpty() -> EstadoVacio(
                Icons.Filled.EventBusy,
                "No hay eventos con estos filtros 🌤️"
            )

            else -> Box(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(eventos, key = { it.id }) { evento ->
                        TarjetaEvento(
                            evento = evento,
                            categoria = modelo.categoriaDe(evento),
                            esFavorito = favoritos.contains(evento.id),
                            alClic = { alAbrirDetalle(evento.id) },
                            alAlternarFavorito = { modelo.alternarFavorito(evento.id) }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}
