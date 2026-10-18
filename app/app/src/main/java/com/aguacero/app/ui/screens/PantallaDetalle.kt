package com.aguacero.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aguacero.app.ui.ModeloCartelera
import com.aguacero.app.util.abrirTelefono
import com.aguacero.app.util.abrirWeb
import com.aguacero.app.util.compartirWhatsApp
import com.aguacero.app.util.formatearFechaLarga
import com.aguacero.app.util.textoParaCompartir

/**
 * Pantalla de detalle de un evento: imagen grande, toda la información
 * y acciones (llamar, abrir web, compartir por WhatsApp) + favorito.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalle(
    modelo: ModeloCartelera,
    idEvento: String,
    alVolver: () -> Unit
) {
    val estado by modelo.estado.collectAsState()
    val favoritos by modelo.favoritos.collectAsState()
    val contexto = LocalContext.current

    val evento = modelo.eventoPorId(idEvento) ?: estado.eventos.find { it.id == idEvento }

    if (evento == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Este evento ya no está disponible 😕")
            Spacer(Modifier.height(12.dp))
            Button(onClick = alVolver) { Text("Volver a la cartelera") }
        }
        return
    }

    val categoria = modelo.categoriaDe(evento)
    val esFav = favoritos.contains(evento.id)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ---- Cabecera inmersiva ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
        ) {
            AsyncImage(
                model = evento.imagen,
                contentDescription = "Flyer de ${evento.titulo}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Degradado para legibilidad
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
            // Botones superiores
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = alVolver,
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                IconButton(
                    onClick = { modelo.alternarFavorito(evento.id) },
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = if (esFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (esFav) Color(0xFFE53935) else Color.White
                    )
                }
            }
            // Título superpuesto abajo
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                if (categoria != null) {
                    Text(
                        "${categoria.emoji} ${categoria.nombre}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    evento.titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        Column(modifier = Modifier.padding(bottom = 24.dp)) {

            Spacer(Modifier.height(12.dp))

            val cuando = buildString {
                append(formatearFechaLarga(evento.fecha))
                if (evento.hora.isNotBlank()) append(" · ${evento.hora} h")
                if (!evento.horaApertura.isNullOrBlank()) append(" (abre puertas ${evento.horaApertura} h)")
            }
            FilaInfo("📅 Cuándo", cuando)

            if (evento.lugar.isNotBlank() || evento.direccion.isNotBlank()) {
                FilaInfo("📍 Dónde", listOf(evento.lugar, evento.direccion)
                    .filter { it.isNotBlank() }.joinToString("\n"))
            }

            if (evento.precio.isNotBlank()) FilaInfo("💰 Precio", evento.precio)
            if (evento.descripcion.isNotBlank()) FilaInfo("ℹ️ Detalles", evento.descripcion)
            if (evento.organizador.isNotBlank()) FilaInfo("🎟️ Organiza", evento.organizador)

            // ---- Botones de acción ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (evento.contacto.isNotEmpty()) {
                    Button(onClick = { abrirTelefono(contexto, evento.contacto.first()) }) {
                        Text("📞 Llamar")
                    }
                }
                if (!evento.linkWeb.isNullOrBlank()) {
                    Button(onClick = { abrirWeb(contexto, evento.linkWeb.orEmpty()) }) {
                        Text("🌐 Web")
                    }
                }
                Button(onClick = {
                    compartirWhatsApp(contexto, textoParaCompartir(evento))
                }) {
                    Text("💬 WhatsApp")
                }
            }

            // Contactos adicionales
            if (evento.contacto.size > 1) {
                Text(
                    "Otros contactos:",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                evento.contacto.drop(1).forEach { numero ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { abrirTelefono(contexto, numero) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(numero, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaInfo(etiqueta: String, valor: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(valor, style = MaterialTheme.typography.bodyMedium)
    }
}
