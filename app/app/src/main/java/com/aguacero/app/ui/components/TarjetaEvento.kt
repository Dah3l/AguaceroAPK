package com.aguacero.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.aguacero.app.R
import com.aguacero.app.model.Categoria
import com.aguacero.app.model.Evento
import com.aguacero.app.util.aLocalDate
import com.aguacero.app.util.proximaFecha
import com.aguacero.app.util.formatearFechaCorta
import java.time.format.TextStyle
import java.util.Locale

/** Badge de categoría con el color y emoji definidos en el JSON. */
@Composable
fun InsigniaCategoria(categoria: Categoria?, modificador: Modifier = Modifier) {
    if (categoria == null) return
    val colorFondo = try {
        Color(android.graphics.Color.parseColor(categoria.color))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }
    Box(
        modificador
            .clip(RoundedCornerShape(percent = 50))
            .background(colorFondo)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "${categoria.emoji} ${categoria.nombre}",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Tarjeta de evento para las listas de Inicio y Favoritos. */
@Composable
fun TarjetaEvento(
    evento: Evento,
    categoria: Categoria?,
    esFavorito: Boolean,
    alClic: () -> Unit,
    alAlternarFavorito: () -> Unit,
    modificador: Modifier = Modifier
) {
    Card(
        onClick = alClic,
        modifier = modificador
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Imagen del flyer con badge de fecha superpuesto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(evento.imagen)
                    .crossfade(true)
                    .build(),
                contentDescription = "Flyer de ${evento.titulo}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading,
                    is AsyncImagePainter.State.Error -> {
                        // Placeholder con el logo de Aguacero
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(R.drawable.ic_logo_frontal),
                                contentDescription = "Logo Aguacero",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }
                    else -> SubcomposeAsyncImageContent()
                }
            }

            // Badge de fecha (día + mes) superpuesto
            val f = evento.proximaFecha().aLocalDate()
            if (f != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.95f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = f.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = f.month.getDisplayName(TextStyle.SHORT, Locale("es"))
                            .replace(".", "").uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            } // fin Box imagen

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InsigniaCategoria(categoria)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = alAlternarFavorito) {
                    Icon(
                        imageVector = if (esFavorito) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (esFavorito) "Quitar de favoritos" else "Guardar en favoritos",
                        tint = if (esFavorito) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = evento.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "📅 ${formatearFechaCorta(evento.proximaFecha())} · 🕐 ${evento.hora}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            if (evento.lugar.isNotBlank()) {
                Text(
                    text = "📍 ${evento.lugar}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                )
            }

            if (evento.precio.isNotBlank()) {
                val esGratis = evento.precio.lowercase().contains("gratis") ||
                    evento.precio.lowercase().contains("libre") ||
                    evento.precio.trim() == "0"
                Box(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (esGratis) Color(0xFFE8F5E9)
                            else Color(0xFFFFF3E0)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "💰 ${evento.precio}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (esGratis) Color(0xFF2E7D32) else Color(0xFFE65100),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

/** Estado vacío pulido con icono en círculo y mensaje. */
@Composable
fun EstadoVacio(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    mensaje: String,
    subtitulo: String = ""
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            mensaje,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitulo.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/** Tarjeta grande vertical para Destacados, estilo foto de referencia:
 * imagen completa, corazón arriba-derecha, fecha y título abajo. */
@Composable
fun TarjetaDestacado(
    evento: Evento,
    categoria: Categoria?,
    esFavorito: Boolean = false,
    alAlternarFavorito: () -> Unit = {},
    alClic: () -> Unit,
    modificador: Modifier = Modifier
) {
    Card(
        onClick = alClic,
        modifier = modificador
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen de fondo
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(evento.imagen)
                    .crossfade(true)
                    .build(),
                contentDescription = "Flyer de ${evento.titulo}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading,
                    is AsyncImagePainter.State.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(R.drawable.ic_logo_frontal),
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                    else -> SubcomposeAsyncImageContent()
                }
            }
            // Degradado inferior
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 300f
                        )
                    )
            )
            // Placa de destacado (arriba a la izquierda)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFB300))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "Destacado",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            // Corazón de favorito (arriba a la derecha)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable { alAlternarFavorito() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (esFavorito) Color(0xFFFF5252) else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            // Fecha y título (abajo)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = formatearFechaCorta(evento.proximaFecha()),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
