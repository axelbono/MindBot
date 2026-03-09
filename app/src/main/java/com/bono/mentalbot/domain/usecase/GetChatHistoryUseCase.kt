package com.bono.mentalbot.domain.usecase

import com.bono.mentalbot.data.repository.ChatRepository
import com.bono.mentalbot.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso que expone un flujo de historial de mensajes del chat.
 *
 * Este caso de uso delega al repositorio para obtener los mensajes almacenados.
 */
class GetChatHistoryUseCase(private val repository: ChatRepository) {

    /**
     * Obtiene un Flow que emite la lista actualizada de mensajes del chat.
     *
     * @return Flow que emite el historial de mensajes.
     */
    operator fun invoke(): Flow<List<Message>> {
        return repository.getMessages()
    }
}