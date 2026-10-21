package com.aguacero.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aguacero.app.ui.ModeloCartelera
import com.aguacero.app.ui.components.EstadoVacio
import com.aguacero.app.ui.components.TituloPestana
import com.aguacero.app.ui.components.TarjetaEvento

/**
 * Pestaña de Favoritos: muestra solo los eventos guardados localmente.
 */
@Composable
fun PantallaFavoritos(modelo: ModeloCartelera, alAbrirDetalle: (String) -> Unit) {
    val favoritos by modelo.favoritos.collectAsState()
    val eventos = modelo.eventosFavoritos()

    Column(modifier = Modifier.fillMaxSize()) {
        TituloPestana(
            "Tus eventos guardados ❤️",
            modificador = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp)
        )

        if (eventos.isEmpty()) {
            if (favoritos.isEmpty()) {
                EstadoVacio(
                    Icons.Filled.FavoriteBorder,
                    "Sin favoritos 🤍",
                    "Toca el corazón en cualquier evento para guardarlo aquí"
                )
            } else {
                EstadoVacio(
                    Icons.Filled.Favorite,
                    "Tus favoritos ya pasaron",
                    "Se retiraron de la cartelera"
                )
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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
}