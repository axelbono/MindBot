package com.bono.mentalbot.data.remote.model

/**
 * Respuesta del endpoint de chat completions de Groq/OpenAI.
 *
 * Contiene una lista de opciones (choices) con posibles respuestas.
 */
data class ChatResponse(
    val choices: List<Choice>
)

/**
 * Opción de respuesta dentro del objeto [ChatResponse].
 *
 * @param message Mensaje generado por el modelo.
 */
data class Choice(
    val message: MessageDto
)