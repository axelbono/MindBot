package com.bono.mentalbot.domain.usecase

import com.bono.mentalbot.data.repository.ChatRepository
import com.bono.mentalbot.domain.model.Message
import kotlinx.coroutines.flow.Flow

class GetChatHistoryUseCase(private val repository: ChatRepository) {
    operator fun invoke(): Flow<List<Message>> {
        return repository.getMessages()
    }
}