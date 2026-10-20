package com.aguacero.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aguacero.app.ui.ModeloCartelera
import com.aguacero.app.ui.RangoFecha
import com.aguacero.app.ui.components.EstadoVacio
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import com.aguacero.app.ui.components.ListaSkeletons
import com.aguacero.app.ui.components.ChipModerno
import com.aguacero.app.ui.components.TarjetaDestacado
import com.aguacero.app.ui.components.TarjetaEvento

/**
 * Pantalla de Inicio: lista de eventos futuros ordenados por cercanía,
 * con filtros por categoría y rango de fecha, y tirada para actualizar.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PantallaInicio(modelo: ModeloCartelera, alAbrirDetalle: (String) -> Unit) {
    val estado by modelo.estado.collectAsState()
    val favoritos by modelo.favoritos.collectAsState()

    val pullState = rememberPullRefreshState(
        refreshing = estado.cargando,
        onRefresh = { modelo.cargar(true) }
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullState)
    ) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // ---- Buscador integrado (píldora moderna) ----
        androidx.compose.material3.TextField(
            value = estado.busqueda,
            onValueChange = { modelo.buscar(it) },
            placeholder = { Text("Buscar eventos…") },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(28.dp)),
            singleLine = true,
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedContainerColor = androidx.compose.ui.graphics.Color(0xFFE8EEF2),
                unfocusedContainerColor = androidx.compose.ui.graphics.Color(0xFFE8EEF2)
            )
        )

        // ---- Pill de fecha específica activa (viene de la pestaña Calendario) ----
        if (estado.rangoFecha == RangoFecha.ESPECIFICA && estado.fechaEspecifica != null) {
            val f = estado.fechaEspecifica!!
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ChipModerno(
                        texto = "${f.dayOfMonth} ${f.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale("es"))} ✕",
                        seleccionado = true,
                        alClic = { modelo.limpiarFechaEspecifica() }
                    )
                }
            }
        }

        // ---- Chips de filtro por categoría ----
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ChipModerno(
                    texto = "Todas",
                    seleccionado = estado.categoriaActiva == null,
                    alClic = { modelo.filtrarCategoria(null) }
                )
            }
            items(estado.categorias) { categoria ->
                ChipModerno(
                    texto = "${categoria.emoji} ${categoria.nombre}",
                    seleccionado = estado.categoriaActiva == categoria.id,
                    alClic = {
                        modelo.filtrarCategoria(
                            if (estado.categoriaActiva == categoria.id) null else categoria.id
                        )
                    }
                )
            }
        }

        if (estado.desdeCache && !estado.cargando) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("📴", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Sin conexión · mostrando datos guardados",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ---- Lista con tirada para actualizar ----
        val eventos = modelo.eventosFiltrados()

        when {
            estado.cargando && eventos.isEmpty() -> ListaSkeletons()

            estado.error != null && eventos.isEmpty() -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(estado.error ?: "", modifier = Modifier.padding(16.dp))
                Row {
                    androidx.compose.material3.Button(
                        onClick = { modelo.cargar(true) },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.height(0.dp))
                        Text("Reintentar")
                    }
                }
            }

            eventos.isEmpty() -> EstadoVacio(
                Icons.Filled.EventBusy,
                "No hay eventos",
                "Prueba con otros filtros o categorías"
            )

            else -> Box(
                modifier = Modifier.fillMaxSize()
            ) {
                val destacados = eventos.filter { it.destacado }.take(5)
                val normales = eventos.filter { !it.destacado }
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Destacados: scroll vertical con el resto, scroll horizontal propio
                    if (destacados.isNotEmpty()) {
                        item(key = "destacados_header") {
                            Text(
                                "Destacados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        item(key = "destacados_carousel") {
                            // Carrusel: la tarjeta central grande, las laterales
                            // se achican; al soltar, se centra la más cercana (snap)
                            val estadoPager = androidx.compose.foundation.pager.rememberPagerState(
                                pageCount = { destacados.size }
                            )
                            androidx.compose.foundation.pager.HorizontalPager(
                                state = estadoPager,
                                contentPadding = PaddingValues(horizontal = 36.dp),
                                pageSpacing = 14.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                            ) { pagina ->
                                val evento = destacados[pagina]
                                val desplazamiento = kotlin.math.abs(
                                    estadoPager.currentPage - pagina +
                                        estadoPager.currentPageOffsetFraction
                                )
                                TarjetaDestacado(
                                    evento = evento,
                                    categoria = modelo.categoriaDe(evento),
                                    esFavorito = favoritos.contains(evento.id),
                                    alAlternarFavorito = { modelo.alternarFavorito(evento.id) },
                                    alClic = { alAbrirDetalle(evento.id) },
                                    modificador = Modifier.graphicsLayer {
                                        val escala = 1f - (desplazamiento * 0.10f)
                                            .coerceIn(0f, 0.18f)
                                        scaleX = escala
                                        scaleY = escala
                                        alpha = 1f - (desplazamiento * 0.25f)
                                            .coerceIn(0f, 0.35f)
                                    }
                                )
                            }
                        }
                    }
                    items(normales, key = { it.id }) { evento ->
                        TarjetaEvento(
                            evento = evento,
                            categoria = modelo.categoriaDe(evento),
                            esFavorito = favoritos.contains(evento.id),
                            alClic = { alAbrirDetalle(evento.id) },
                            alAlternarFavorito = { modelo.alternarFavorito(evento.id) }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
        PullRefreshIndicator(
            refreshing = estado.cargando,
            state = pullState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
