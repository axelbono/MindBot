package com.bono.mentalbot.data.remote.model

/**
 * Payload enviado al endpoint de chat completions.
 *
 * @param model Identificador del modelo de lenguaje.
 * @param messages Lista de mensajes que componen el contexto de la conversación.
 */
data class ChatRequest(
    val model: String,
    val messages: List<MessageDto>
)

/**
 * DTO que representa un mensaje dentro del request/response de Groq/OpenAI.
 *
 * @param role Rol del emisor (p. ej. "user" o "assistant").
 * @param content Texto del mensaje.
 */
data class MessageDto(
    val role: String,
    val content: String
)