package com.aguacero.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de datos de Aguacero.
 * Refleja exactamente la estructura del archivo eventos.json publicado
 * en GitHub Pages.
 */

@Serializable
data class DatosAguacero(
    val categorias: List<Categoria> = emptyList(),
    val eventos: List<Evento> = emptyList()
)

@Serializable
data class Categoria(
    val id: String,
    val nombre: String,
    val emoji: String = "",
    val color: String = "#2E7CD6"
)

@Serializable
data class Evento(
    val id: String,
    val titulo: String,
    val descripcion: String = "",
    val fecha: String,                    // "2026-10-18"
    val hora: String,                     // "19:00"
    @SerialName("horaApertura") val horaApertura: String? = null,   // opcional
    @SerialName("horaFin") val horaFin: String? = null,             // opcional, culminación
    val lugar: String = "",
    val direccion: String = "",
    val precio: String = "",              // texto libre: "1000 CUP incluye trago"
    val contacto: List<String> = emptyList(),
    val correo: String = "",               // correo de contacto
    val fechas: List<String> = emptyList(), // todas las fechas (si vacío, se usa `fecha`)
    @SerialName("linkWeb") val linkWeb: String? = null,             // opcional
    val imagen: String = "",              // URL del flyer
    @SerialName("categoriaId") val categoriaId: String? = null,
    val organizador: String = "",         // opcional
    val destacado: Boolean = false,
    @SerialName("fechaPublicacion") val fechaPublicacion: String = ""
)
