package com.aguacero.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Barra de navegación inferior estilo "pill" con la paleta de Aguacero.
 * El círculo blanco del ítem activo se DESLIZA entre pestañas con rebote,
 * y el icono activo crece con un bounce suave.
 */
@Composable
fun BarraNavegacion(
    pestanaActual: Int,
    alCambiar: (Int) -> Unit,
    modificador: Modifier = Modifier
) {
    val items = listOf(
        Triple(0, Icons.Filled.Home, "Inicio"),
        Triple(1, Icons.Filled.CalendarMonth, "Calendario"),
        Triple(2, Icons.Filled.Favorite, "Favoritos"),
        Triple(3, Icons.Filled.Settings, "Ajustes"),
    )

    Box(
        modifier = modificador
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val anchoSegmento = maxWidth / items.size
            val desplazamiento by animateDpAsState(
                targetValue = anchoSegmento * pestanaActual + (anchoSegmento - 56.dp) / 2,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "indicadorNav"
            )

            // Fondo pill con degradado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
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
            )

            // Círculo blanco deslizante
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = desplazamiento)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )

            // Iconos
            Row(modifier = Modifier.fillMaxWidth().height(76.dp)) {
                items.forEach { (indice, icono, descripcion) ->
                    val activo = pestanaActual == indice
                    val escala by animateFloatAsState(
                        targetValue = if (activo) 1.18f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "escalaIcono$indice"
                    )
                    val tinte by animateColorAsState(
                        targetValue = if (activo) Color(0xFF0F5E83)
                        else Color.White.copy(alpha = 0.85f),
                        label = "tinteIcono$indice"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { alCambiar(indice) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icono,
                            contentDescription = descripcion,
                            tint = tinte,
                            modifier = Modifier
                                .size(26.dp)
                                .scale(escala)
                        )
                    }
                }
            }
        }
    }
}
