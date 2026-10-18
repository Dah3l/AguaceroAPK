package com.aguacero.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = ClaroPrimario,
    onPrimary = ClaroSobrePrimario,
    primaryContainer = ClaroContenedorPrimario,
    onPrimaryContainer = ClaroSobreContenedorPrimario,
    secondary = ClaroSecundario,
    onSecondary = ClaroSobrePrimario,
    tertiary = ClaroTerciario,
    background = androidx.compose.ui.graphics.Color(0xFFF8F9FA),
    onBackground = androidx.compose.ui.graphics.Color(0xFF04202E),
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color(0xFF04202E),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE1F2FA),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF0A3C55)
)

/** Tema de Aguacero: siempre modo claro para uniformidad. */
@Composable
fun TemaAguacero(
    contenido: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = androidx.compose.material3.Typography(),
        content = contenido
    )
}
