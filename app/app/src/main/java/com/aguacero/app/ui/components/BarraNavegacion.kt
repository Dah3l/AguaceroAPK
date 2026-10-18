package com.aguacero.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Barra de navegación inferior estilo "pill" con la paleta de Aguacero.
 * El ítem activo se resalta con un círculo blanco.
 */
@Composable
fun BarraNavegacion(
    pestanaActual: Int,
    alCambiar: (Int) -> Unit,
    modificador: Modifier = Modifier
) {
    val items = listOf(
        Triple(0, Icons.Filled.Home, "Inicio"),
        Triple(1, Icons.Filled.Favorite, "Favoritos"),
        Triple(2, Icons.Filled.Search, "Buscar"),
        Triple(3, Icons.Filled.Settings, "Ajustes"),
    )

    Box(
        modifier = modificador
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(percent = 50))
                .clip(RoundedCornerShape(percent = 50))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1786B4),
                            Color(0xFF0F5E83),
                            Color(0xFF0A3C55)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (indice, icono, descripcion) ->
                val activo = pestanaActual == indice
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (activo) Color.White
                            else Color.Transparent
                        )
                        .clickable { alCambiar(indice) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = descripcion,
                        tint = if (activo) Color(0xFF0F5E83) else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
