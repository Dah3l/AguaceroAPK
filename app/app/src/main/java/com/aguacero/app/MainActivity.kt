package com.aguacero.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.aguacero.app.ui.components.BarraNavegacion
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aguacero.app.ui.ModeloCartelera
import com.aguacero.app.ui.screens.PantallaAjustes
import com.aguacero.app.ui.screens.PantallaDetalle
import com.aguacero.app.ui.screens.PantallaFavoritos
import com.aguacero.app.ui.screens.PantallaInicio
import com.aguacero.app.ui.theme.TemaAguacero

/**
 * Actividad principal de Aguacero. Navegación por pestañas:
 * Inicio, Favoritos, Búsqueda y Ajustes; el detalle se abre como capa.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TemaAguacero {
                AppAguacero()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppAguacero(modelo: ModeloCartelera = viewModel()) {
    var pestana by rememberSaveable { mutableStateOf(0) }
    var eventoAbierto by rememberSaveable { mutableStateOf<String?>(null) }

    // ---- Vista de detalle (capa completa) ----
    val idDetalle = eventoAbierto
    if (idDetalle != null) {
        PantallaDetalle(
            modelo = modelo,
            idEvento = idDetalle,
            alVolver = { eventoAbierto = null }
        )
        return
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF2E7CD6),
                                Color(0xFF1B5FB0),
                                Color(0xFF0F3D75)
                            )
                        )
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(
                            id = com.aguacero.app.R.drawable.ic_logo_frontal
                        ),
                        contentDescription = "Logo Aguacero",
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        "Aguacero",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        bottomBar = {
            BarraNavegacion(
                pestanaActual = pestana,
                alCambiar = { pestana = it }
            )
        }
    ) { valoresInternos ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(valoresInternos)) {
            when (pestana) {
                0 -> PantallaInicio(modelo, alAbrirDetalle = { eventoAbierto = it })
                1 -> PantallaFavoritos(modelo, alAbrirDetalle = { eventoAbierto = it })
                2 -> PantallaBusqueda(modelo, alAbrirDetalle = { eventoAbierto = it })
                3 -> PantallaAjustes(modelo.preferencias)
            }
        }
    }
}

/** Pestaña de búsqueda por título, lugar o descripción. */
@androidx.compose.runtime.Composable
private fun PantallaBusqueda(
    modelo: com.aguacero.app.ui.ModeloCartelera,
    alAbrirDetalle: (String) -> Unit
) {
    val estado by modelo.estado.collectAsState()
    val favoritos by modelo.favoritos.collectAsState()
    val resultados = modelo.eventosFiltrados()

    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize()
    ) {
        androidx.compose.material3.OutlinedTextField(
            value = estado.busqueda,
            onValueChange = { modelo.buscar(it) },
            placeholder = { androidx.compose.material3.Text("Buscar por título, lugar o descripción…") },
            leadingIcon = {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Filled.Search,
                    contentDescription = null
                )
            },
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(14.dp),
            singleLine = true
        )

        if (resultados.isEmpty() && estado.busqueda.isNotBlank()) {
            com.aguacero.app.ui.components.EstadoVacio(
                androidx.compose.material.icons.Icons.Filled.Search,
                "Sin resultados para “${estado.busqueda}” 🔍"
            )
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp)
            ) {
                items(resultados, key = { it.id }) { evento ->
                    com.aguacero.app.ui.components.TarjetaEvento(
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
