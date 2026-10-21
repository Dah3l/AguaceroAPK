package com.aguacero.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aguacero.app.data.AlmacenPreferencias
import com.aguacero.app.ui.components.TituloPestana
import com.aguacero.app.util.CrashReporter
import com.aguacero.app.util.cancelarRevisionEventos
import com.aguacero.app.util.programarRevisionEventos
import kotlinx.coroutines.launch

/**
 * Pantalla de Ajustes: activar/desactivar notificaciones de nuevos eventos
 * y sección "Acerca de".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAjustes(preferencias: AlmacenPreferencias) {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    val activas by preferencias.notificacionesActivas.collectAsState(initial = true)

    val permisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) programarRevisionEventos(contexto)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TituloPestana("Ajustes")

        // ---- Notificaciones ----
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.padding(horizontal = 6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Notificaciones de nuevos eventos", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Te avisamos cada 6 horas si hay eventos nuevos en la cartelera.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = activas,
                    onCheckedChange = { encendida ->
                        alcance.launch {
                            preferencias.setNotificacionesActivas(encendida)
                            if (encendida) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    programarRevisionEventos(contexto)
                                }
                            } else {
                                cancelarRevisionEventos(contexto)
                            }
                        }
                    }
                )
            }
        }

        Divider()

        // ---- Acerca de ----
        Text("Acerca de Aguacero", style = MaterialTheme.typography.titleMedium)
        Text(
            "🌧️ Aguacero es una cartelera colaborativa de eventos culturales " +
                "en La Habana: música, teatro, danza, cine y más.\n\n" +
                "Los datos se publican como un JSON estático en GitHub Pages y " +
                "la app los guarda en caché para funcionar sin conexión.\n\n" +
                "Versión 1.0.0",
            style = MaterialTheme.typography.bodyMedium
        )
        Divider()

        // ---- Registro de errores ----
        Text("Registro de errores", style = MaterialTheme.typography.titleMedium)
        var mostrarDialogo by remember { mutableStateOf(false) }
        var textoCrash by remember { mutableStateOf<String?>(null) }
        val hayReporte = remember { CrashReporter.hayReporte(contexto) }

        Text(
            "Si la app se cierra sola, aquí puedes ver y copiar el registro del error para reportarlo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(
            onClick = {
                textoCrash = CrashReporter.leer(contexto)
                mostrarDialogo = true
            },
            enabled = hayReporte || textoCrash != null
        ) {
            Text(if (hayReporte) "📋 Ver último error" else "Sin errores registrados")
        }

        if (mostrarDialogo) {
            AlertDialog(
                onDismissRequest = { mostrarDialogo = false },
                title = { Text("Registro de error") },
                text = {
                    Text(
                        textoCrash ?: "No hay registro.",
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val cm = contexto.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("crash", textoCrash ?: ""))
                        Toast.makeText(contexto, "Copiado ✓", Toast.LENGTH_SHORT).show()
                    }) { Text("Copiar") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        CrashReporter.limpiar(contexto)
                        textoCrash = null
                        mostrarDialogo = false
                    }) { Text("Borrar") }
                }
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
