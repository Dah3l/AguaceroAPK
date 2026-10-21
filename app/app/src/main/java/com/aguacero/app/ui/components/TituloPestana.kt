package com.aguacero.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

/**
 * Título de pestaña, estilo unificado de la app:
 * titleLarge, ExtraBold, azul oscuro de la paleta Cielo.
 * Cada pantalla le da su margen con [modificador].
 */
@Composable
fun TituloPestana(texto: String, modificador: Modifier = Modifier) {
    Text(
        texto = texto,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF0A3C55),
        modifier = modificador
    )
}
