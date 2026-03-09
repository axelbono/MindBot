package com.bono.mentalbot.domain.usecase

import com.bono.mentalbot.data.repository.ChatRepository

/**
 * Caso de uso que envía un mensaje del usuario al repositorio de chat y devuelve la respuesta del asistente.
 *
 * Encapsula la lógica de negocio para interactuar con el modelo de conversación.
 */
class SendMessageUseCase(private val repository: ChatRepository) {

    /**
     * Envía un mensaje al backend/chat y obtiene la respuesta del asistente.
     *
     * @param message Mensaje del usuario.
     * @param mood Estado de ánimo actual del usuario (se envía para mantener contexto).
     * @return Respuesta generada por el asistente.
     */
    suspend operator fun invoke(message: String, mood: String): String {
        return repository.sendMessage(message, mood)
    }
}