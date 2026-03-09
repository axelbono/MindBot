package com.bono.mentalbot.domain.model

/**
 * Representa una meta de bienestar creada por el usuario.
 *
 * @param id Identificador único de la meta.
 * @param title Título de la meta.
 * @param description Descripción detallada de la meta.
 * @param isCompleted Indica si la meta está completada.
 * @param aiAdvice Consejo generado por IA para ayudar a alcanzarla.
 * @param timestamp Marca de tiempo en milisegundos.
 */
data class Goal(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val aiAdvice: String = "",
    val timestamp: Long = System.currentTimeMillis()
)