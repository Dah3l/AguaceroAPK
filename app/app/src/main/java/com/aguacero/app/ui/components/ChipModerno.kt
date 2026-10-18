package com.aguacero.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Chip moderno para filtros: pill redondeada con buen contraste.
 * Seleccionado: fondo azul sólido, texto blanco.
 * No seleccionado: fondo blanco, borde azul claro, texto azul oscuro.
 */
@Composable
fun ChipModerno(
    texto: String,
    seleccionado: Boolean,
    alClic: () -> Unit,
    modificador: Modifier = Modifier
) {
    val forma = RoundedCornerShape(20.dp)
    Box(
        modifier = modificador
            .then(
                if (seleccionado) Modifier.shadow(4.dp, forma)
                else Modifier
            )
            .clip(forma)
            .background(
                if (seleccionado) Color(0xFF1786B4)
                else Color.White
            )
            .then(
                if (!seleccionado) Modifier.border(
                    width = 1.dp,
                    color = Color(0xFF98D5EE),
                    shape = forma
                )
                else Modifier
            )
            .clickable(onClick = alClic)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
            color = if (seleccionado) Color.White else Color(0xFF0A3C55)
        )
    }
}
