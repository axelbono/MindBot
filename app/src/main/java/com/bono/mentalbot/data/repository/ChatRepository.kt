package com.bono.mentalbot.data.repository

import com.bono.mentalbot.data.remote.api.GroqApiService
import com.bono.mentalbot.data.remote.firebase.FirestoreService
import com.bono.mentalbot.data.remote.model.ChatRequest
import com.bono.mentalbot.data.remote.model.MessageDto
import com.bono.mentalbot.domain.model.Message
import com.bono.mentalbot.utils.Constants
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val api: GroqApiService,
    private val firestoreService: FirestoreService
) {
    private var conversationHistory = mutableListOf<MessageDto>()

    fun initializeChat(userName: String, mood: String, wellbeingContext: String) {
        val systemPrompt = Constants.getSystemPrompt(userName, mood) +
                if (wellbeingContext.isNotEmpty()) "\n\n$wellbeingContext" else ""

        conversationHistory = mutableListOf(
            MessageDto(role = "system", content = systemPrompt)
        )
    }

    suspend fun sendInitialMessage(userName: String, mood: String): String {
        val hasWellbeingContext = conversationHistory.any {
            it.role == "system" && it.content.contains("EVALUACIÓN EMOCIONAL")
        }
        val initialMessage = Constants.getInitialMessage(userName, mood, hasWellbeingContext)
        conversationHistory.add(MessageDto(role = "assistant", content = initialMessage))
        firestoreService.saveMessage(
            Message(sender = "bot", content = initialMessage, mood = mood)
        )
        return initialMessage
    }

    suspend fun sendMessage(userMessage: String, mood: String): String {
        conversationHistory.add(MessageDto(role = "user", content = userMessage))

        val response = api.sendMessage(
            apiKey = Constants.GROQ_API_KEY,
            request = ChatRequest(
                model = Constants.MODEL,
                messages = conversationHistory
            )
        )

        val botReply = response.choices[0].message.content
        conversationHistory.add(MessageDto(role = "assistant", content = botReply))

        firestoreService.saveMessage(
            Message(sender = "user", content = userMessage, mood = mood)
        )
        firestoreService.saveMessage(
            Message(sender = "bot", content = botReply, mood = mood)
        )

        return botReply
    }

    fun getMessages(): Flow<List<Message>> = firestoreService.getMessages()

    suspend fun clearMessages() = firestoreService.clearMessages()
}