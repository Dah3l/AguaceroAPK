package com.aguacero.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aguacero.app.ui.ModeloCartelera
import com.aguacero.app.ui.components.EstadoVacio
import com.aguacero.app.ui.components.TarjetaEvento

/**
 * Pestaña de Favoritos: muestra solo los eventos guardados localmente.
 */
@Composable
fun PantallaFavoritos(modelo: ModeloCartelera, alAbrirDetalle: (String) -> Unit) {
    val favoritos by modelo.favoritos.collectAsState()
    val eventos = modelo.eventosFavoritos()

    if (eventos.isEmpty()) {
        EstadoVacio(
            Icons.Filled.Favorite,
            if (favoritos.isEmpty())
                "Aún no tienes eventos favoritos.\nToca el corazón 🤍 en cualquier evento."
            else
                "Tus favoritos ya pasaron o se retiraron de la cartelera."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "❤️ Tus eventos guardados",
                style = MaterialTheme.typography.titleMedium
            )
        }
        items(eventos, key = { it.id }) { evento ->
            TarjetaEvento(
                evento = evento,
                categoria = modelo.categoriaDe(evento),
                esFavorito = true,
                alClic = { alAbrirDetalle(evento.id) },
                alAlternarFavorito = { modelo.alternarFavorito(evento.id) }
            )
        }
    }
}
