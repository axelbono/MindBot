package com.bono.mentalbot.domain.model

/**
 * Representa un mensaje en la conversación entre el usuario y MindBot.
 *
 * @param id Identificador único del mensaje.
 * @param sender Emisor del mensaje ("user" o "bot").
 * @param content Texto del mensaje.
 * @param mood Estado de ánimo asociado al mensaje.
 * @param timestamp Marca de tiempo en milisegundos.
 */
data class Message(
    val id: String = "",
    val sender: String,       // "user" o "bot"
    val content: String,
    val mood: String,
    val timestamp: Long = System.currentTimeMillis()
)