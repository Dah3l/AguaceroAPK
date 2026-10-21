package com.aguacero.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aguacero.app.ui.ModeloCartelera
import com.aguacero.app.ui.components.EstadoVacio
import com.aguacero.app.ui.components.TarjetaEvento

/**
 * Centro de novedades: lista los eventos recién publicados que el
 * sistema detectó (trabajador en segundo plano o al abrir la app).
 * Recibe una foto (snapshot) de los IDs, porque al abrirla las
 * novedades se marcan como vistas y el contador se limpia.
 */
@Composable
fun PantallaNovedades(
    modelo: ModeloCartelera,
    ids: List<String>,
    alVolver: () -> Unit,
    alAbrirDetalle: (String) -> Unit
) {
    val estado by modelo.estado.collectAsState()
    val favoritos by modelo.favoritos.collectAsState()
    val eventos = ids.mapNotNull { id -> estado.eventos.find { it.id == id } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = alVolver) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF0A3C55)
                )
            }
            Text(
                "Novedades",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0A3C55)
            )
            if (eventos.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Text(
                    "(${eventos.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
            }
        }

        if (eventos.isEmpty()) {
            EstadoVacio(
                Icons.Filled.Notifications,
                "Sin novedades por ahora",
                "Cuando se publique un evento nuevo, aparecerá aquí"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 14.dp, end = 14.dp, top = 6.dp, bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
            }
        }
    }
}
