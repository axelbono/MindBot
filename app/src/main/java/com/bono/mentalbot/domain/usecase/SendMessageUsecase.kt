package com.bono.mentalbot.domain.usecase

import com.bono.mentalbot.data.repository.ChatRepository

class SendMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(message: String, mood: String): String {
        return repository.sendMessage(message, mood)
    }
}