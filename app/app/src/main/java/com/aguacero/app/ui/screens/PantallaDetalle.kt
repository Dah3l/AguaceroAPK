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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aguacero.app.ui.ModeloCartelera
import com.aguacero.app.util.abrirTelefono
import com.aguacero.app.util.abrirWeb
import com.aguacero.app.util.compartirWhatsApp
import com.aguacero.app.util.formatearFechaLarga
import com.aguacero.app.util.textoParaCompartir

// Paleta Cielo
private val AzulPrimario = Color(0xFF1786B4)
private val AzulOscuro = Color(0xFF0A3C55)
private val AzulClaro = Color(0xFFE1F2FA)

/**
 * Pantalla de detalle estilo referencia: imagen full-bleed con botones flotantes
 * y tarjeta blanca inferior con la información.
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
            Text("Este evento ya no está disponible")
            Spacer(Modifier.height(12.dp))
            Button(onClick = alVolver) { Text("Volver") }
        }
        return
    }

    val categoria = modelo.categoriaDe(evento)
    val esFav = favoritos.contains(evento.id)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ---- Imagen full-bleed ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                AsyncImage(
                    model = evento.imagen,
                    contentDescription = "Imagen de ${evento.titulo}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Degradado sutil arriba para los botones
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                // Botones flotantes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BotonCircular(
                        icono = Icons.AutoMirrored.Filled.ArrowBack,
                        descripcion = "Volver",
                        alClic = alVolver
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BotonCircular(
                            icono = Icons.Filled.Share,
                            descripcion = "Compartir",
                            alClic = { compartirWhatsApp(contexto, textoParaCompartir(evento)) }
                        )
                        BotonCircular(
                            icono = if (esFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            descripcion = "Favorito",
                            alClic = { modelo.alternarFavorito(evento.id) },
                            tinte = if (esFav) Color(0xFFE53935) else AzulOscuro
                        )
                    }
                }
            }

            // ---- Tarjeta blanca superpuesta ----
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Badge categoría
                    if (categoria != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AzulClaro)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "${categoria.emoji} ${categoria.nombre}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AzulPrimario
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // Título
                    Text(
                        evento.titulo,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AzulOscuro
                    )
                    Spacer(Modifier.height(12.dp))

                    // Ubicación y fecha
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (evento.lugar.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = AzulPrimario,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    evento.lugar,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        val fecha = formatearFechaLarga(evento.fecha)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📅", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                fecha,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (evento.hora.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "🕐 ${evento.hora} h" + (if (!evento.horaApertura.isNullOrBlank()) " (apertura ${evento.horaApertura} h)" else ""),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Acerca del evento
                    if (evento.descripcion.isNotBlank()) {
                        Text(
                            "Acerca del evento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AzulOscuro
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            evento.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                        Spacer(Modifier.height(20.dp))
                    }

                    // Organizador
                    if (evento.organizador.isNotBlank()) {
                        Text(
                            "Organizador",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AzulOscuro
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(AzulClaro),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        evento.organizador.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulPrimario
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        evento.organizador,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AzulOscuro
                                    )
                                    Text(
                                        "Organizador",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (evento.contacto.isNotEmpty()) {
                                    BotonCircularPequeno(
                                        icono = Icons.Filled.Call,
                                        descripcion = "Llamar",
                                        alClic = { abrirTelefono(contexto, evento.contacto.first()) }
                                    )
                                }
                                BotonCircularPequeno(
                                    icono = Icons.Filled.Message,
                                    descripcion = "WhatsApp",
                                    alClic = { compartirWhatsApp(contexto, textoParaCompartir(evento)) }
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    // Dirección
                    if (evento.direccion.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Dirección",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AzulOscuro
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            evento.direccion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                        Spacer(Modifier.height(20.dp))
                    }

                    // Precio y botón
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Precio",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Text(
                                if (evento.precio.isNotBlank()) evento.precio else "Gratis",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AzulOscuro
                            )
                        }
                        Button(
                            onClick = { compartirWhatsApp(contexto, textoParaCompartir(evento)) },
                            colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            Text("Compartir", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Contactos adicionales
                    if (evento.contacto.size > 1) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Otros contactos:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = AzulOscuro
                        )
                        evento.contacto.drop(1).forEach { numero ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { abrirTelefono(contexto, numero) }
                                    .padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Call,
                                    contentDescription = null,
                                    tint = AzulPrimario,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(numero, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // Link web
                    if (!evento.linkWeb.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { abrirWeb(contexto, evento.linkWeb.orEmpty()) },
                            colors = ButtonDefaults.buttonColors(containerColor = AzulClaro),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🌐 Visitar sitio web", color = AzulPrimario, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun BotonCircular(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    descripcion: String,
    alClic: () -> Unit,
    tinte: Color = Color(0xFF0A3C55)
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = alClic),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = descripcion,
            tint = tinte,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun BotonCircularPequeno(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    descripcion: String,
    alClic: () -> Unit
) {
    IconButton(
        onClick = alClic,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(AzulClaro)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = descripcion,
            tint = AzulPrimario,
            modifier = Modifier.size(20.dp)
        )
    }
}
