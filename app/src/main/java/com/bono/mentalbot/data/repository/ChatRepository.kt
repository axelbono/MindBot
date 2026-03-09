package com.bono.mentalbot.data.repository

import com.bono.mentalbot.data.remote.api.GroqApiService
import com.bono.mentalbot.data.remote.firebase.FirestoreService
import com.bono.mentalbot.data.remote.model.ChatRequest
import com.bono.mentalbot.data.remote.model.MessageDto
import com.bono.mentalbot.domain.model.Message
import com.bono.mentalbot.utils.Constants
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que orquestra la lógica de conversación entre la app y los servicios
 * externos (Groq API + Firestore).
 *
 * Mantiene un historial local de la conversación con el modelo y persiste los
 * mensajes en Firestore para permitir recuperar el historial del chat.
 */
class ChatRepository(
    private val api: GroqApiService,
    private val firestoreService: FirestoreService
) {
    private var conversationHistory = mutableListOf<MessageDto>()

    /**
     * Inicializa el historial de conversación con el prompt del sistema.
     *
     * @param userName Nombre del usuario (para personalizar el prompt).
     * @param mood Estado de ánimo actual del usuario.
     * @param wellbeingContext Contexto adicional proveniente de la evaluación de bienestar.
     */
    fun initializeChat(userName: String, mood: String, wellbeingContext: String) {
        val systemPrompt = Constants.getSystemPrompt(userName, mood) +
                if (wellbeingContext.isNotEmpty()) "\n\n$wellbeingContext" else ""

        conversationHistory = mutableListOf(
            MessageDto(role = "system", content = systemPrompt)
        )
    }

    /**
     * Genera y guarda el primer mensaje inicial del asistente (antes de llamar al modelo).
     *
     * @param userName Nombre del usuario.
     * @param mood Estado de ánimo actual.
     * @return El mensaje inicial del asistente.
     */
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

    /**
     * Envía un mensaje del usuario al modelo de lenguaje y persiste el intercambio.
     *
     * @param userMessage Texto enviado por el usuario.
     * @param mood Estado de ánimo actual (para contexto y registro).
     * @return Respuesta generada por el asistente.
     */
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

    /**
     * Obtiene el historial de mensajes almacenado en Firestore.
     */
    fun getMessages(): Flow<List<Message>> = firestoreService.getMessages()

    /**
     * Elimina todos los mensajes almacenados en Firestore.
     */
    suspend fun clearMessages() = firestoreService.clearMessages()
}