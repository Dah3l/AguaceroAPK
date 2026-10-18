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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aguacero.app.data.AlmacenPreferencias
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
        Spacer(Modifier.height(24.dp))
    }
}
