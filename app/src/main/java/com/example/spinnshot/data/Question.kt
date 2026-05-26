package com.example.spinnshot.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Question(
    @SerialName("categoria") val categoria: String,
    @SerialName("pregunta") val pregunta: String,
    @SerialName("respuesta") val respuesta: String
)

object Categories {
    const val ALL = "Todas"
    val available: List<String> = listOf(
        "Deportes",
        "Cine",
        "Series",
        "Biología",
        "Historia",
        "Geografía",
        "Cultura Colombiana",
        "Videojuegos",
        "Cómics"
    )
}
