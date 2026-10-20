package com.aguacero.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aguacero.app.ui.ModeloCartelera
import com.aguacero.app.ui.components.BarraNavegacion
import com.aguacero.app.ui.screens.PantallaAjustes
import com.aguacero.app.ui.screens.PantallaCalendario
import com.aguacero.app.ui.screens.PantallaDetalle
import com.aguacero.app.ui.screens.PantallaFavoritos
import com.aguacero.app.ui.screens.PantallaInicio
import com.aguacero.app.ui.theme.TemaAguacero
import kotlinx.coroutines.launch

/**
 * Actividad principal de Aguacero. Navegación por pestañas:
 * Inicio, Calendario, Favoritos y Ajustes; el detalle se abre como capa.
 * Menú lateral con Licencia y Acerca de; campana reservada para
 * el futuro sistema de notificaciones.
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
    val estadoCajon = rememberDrawerState(DrawerValue.Closed)
    val alcance = rememberCoroutineScope()
    var dialogoMenu by remember { mutableStateOf<String?>(null) }

    // ---- Vista de detalle (capa completa) ----
    val idDetalle = eventoAbierto
    if (idDetalle != null) {
        // Botón/gesto atrás vuelve a la lista
        androidx.activity.compose.BackHandler {
            eventoAbierto = null
        }
        PantallaDetalle(
            modelo = modelo,
            idEvento = idDetalle,
            alVolver = { eventoAbierto = null }
        )
        return
    }

    // Doble toque atrás para salir
    val contexto = LocalContext.current
    var ultimoToque by remember { mutableStateOf(0L) }
    androidx.activity.compose.BackHandler {
        val ahora = System.currentTimeMillis()
        if (ahora - ultimoToque < 2000) {
            (contexto as? android.app.Activity)?.finish()
        } else {
            ultimoToque = ahora
            android.widget.Toast.makeText(
                contexto,
                "Toque de nuevo para salir",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Diálogos del menú lateral
    when (dialogoMenu) {
        "acerca" -> AlertDialog(
            onDismissRequest = { dialogoMenu = null },
            title = { Text("Acerca de Aguacero") },
            text = {
                Text(
                    "La cartelera cultural de Cuba en tu bolsillo: conciertos, " +
                        "teatro, cine y más, curados por la comunidad de Aguacero."
                )
            },
            confirmButton = {
                TextButton(onClick = { dialogoMenu = null }) { Text("Cerrar") }
            }
        )
        "licencia" -> AlertDialog(
            onDismissRequest = { dialogoMenu = null },
            title = { Text("Licencia") },
            text = {
                Text(
                    "Aguacero es un proyecto comunitario. El contenido de los " +
                        "eventos pertenece a sus organizadores y artistas."
                )
            },
            confirmButton = {
                TextButton(onClick = { dialogoMenu = null }) { Text("Cerrar") }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = estadoCajon,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1786B4),
                                    Color(0xFF0F5E83),
                                    Color(0xFF0A3C55)
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Text(
                        "Aguacero",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.padding(vertical = 6.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                    label = { Text("Acerca de") },
                    selected = false,
                    onClick = {
                        alcance.launch { estadoCajon.close() }
                        dialogoMenu = "acerca"
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Description, contentDescription = null) },
                    label = { Text("Licencia") },
                    selected = false,
                    onClick = {
                        alcance.launch { estadoCajon.close() }
                        dialogoMenu = "licencia"
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1786B4),
                                    Color(0xFF0F5E83),
                                    Color(0xFF0A3C55)
                                )
                            )
                        )
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { alcance.launch { estadoCajon.open() } }) {
                            Icon(
                                Icons.Filled.Menu,
                                contentDescription = "Menú",
                                tint = Color.White
                            )
                        }
                        Text(
                            "Aguacero",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.weight(1f))
                        // Campana: futura sede del sistema de notificaciones
                        IconButton(onClick = { }) {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = "Notificaciones",
                                tint = Color.White
                            )
                        }
                    }
                }
            },
            bottomBar = {
                Box(modifier = Modifier.background(Color.Transparent)) {
                    BarraNavegacion(
                        pestanaActual = pestana,
                        alCambiar = { pestana = it }
                    )
                }
            }
        ) { valoresInternos ->
            Box(modifier = Modifier.padding(valoresInternos)) {
                when (pestana) {
                    0 -> PantallaInicio(modelo, alAbrirDetalle = { eventoAbierto = it })
                    1 -> PantallaCalendario(modelo, alIrAInicio = { pestana = 0 })
                    2 -> PantallaFavoritos(modelo, alAbrirDetalle = { eventoAbierto = it })
                    3 -> PantallaAjustes(modelo.preferencias)
                }
            }
        }
    }
}
