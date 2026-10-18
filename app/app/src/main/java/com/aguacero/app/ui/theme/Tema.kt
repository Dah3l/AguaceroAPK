package com.aguacero.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = ClaroPrimario,
    onPrimary = ClaroSobrePrimario,
    primaryContainer = ClaroContenedorPrimario,
    onPrimaryContainer = ClaroSobreContenedorPrimario,
    secondary = ClaroSecundario,
    onSecondary = ClaroSobrePrimario,
    tertiary = ClaroTerciario
)

private val EsquemaOscuro = darkColorScheme(
    primary = OscuroPrimario,
    onPrimary = OscuroSobrePrimario,
    primaryContainer = OscuroContenedorPrimario,
    onPrimaryContainer = OscuroSobreContenedorPrimario,
    secondary = OscuroSecundario,
    tertiary = OscuroTerciario
)

/** Tema de Aguacero basado en Material3 con la paleta azul del logo. */
@Composable
fun TemaAguacero(
    esModoOscuro: Boolean = isSystemInDarkTheme(),
    contenido: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (esModoOscuro) EsquemaOscuro else EsquemaClaro,
        typography = androidx.compose.material3.Typography(),
        content = contenido
    )
}
