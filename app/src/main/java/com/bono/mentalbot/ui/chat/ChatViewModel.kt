package com.bono.mentalbot.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bono.mentalbot.data.remote.api.GroqApiService
import com.bono.mentalbot.data.remote.firebase.FirestoreService
import com.bono.mentalbot.data.repository.ChatRepository
import com.bono.mentalbot.domain.model.Message
import com.bono.mentalbot.domain.usecase.GetChatHistoryUseCase
import com.bono.mentalbot.domain.usecase.SendMessageUseCase
import com.bono.mentalbot.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ViewModel responsable de la lógica del chat con MindBot.
 *
 * - Inicializa la conversación y mantiene el historial.
 * - Envía mensajes al modelo de lenguaje.
 * - Expone estados de carga y errores para la UI.
 */
class ChatViewModel : ViewModel() {

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.GROQ_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
        )
        .build()

    private val api = retrofit.create(GroqApiService::class.java)
    private val firestoreService = FirestoreService()
    private val repository = ChatRepository(api, firestoreService)
    private val sendMessageUseCase = SendMessageUseCase(repository)
    private val getChatHistoryUseCase = GetChatHistoryUseCase(repository)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var chatInitialized = false

    /**
     * Inicializa la conversación en el repositorio y envía el mensaje inicial del asistente.
     *
     * @param userName Nombre del usuario (para personalizar el prompt).
     * @param mood Estado de ánimo actual.
     * @param wellbeingContext Contexto adicional generado por la evaluación de bienestar.
     */
    fun initializeChat(userName: String, mood: String, wellbeingContext: String) {
        if (chatInitialized) return
        chatInitialized = true
        repository.initializeChat(userName, mood, wellbeingContext)
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.sendInitialMessage(userName, mood)
            } catch (e: Exception) {
                _error.value = "Error al iniciar el chat"
            } finally {
                _isLoading.value = false
            }
        }
        loadMessages()
    }

    /**
     * Envía un mensaje del usuario a MindBot usando el caso de uso correspondiente.
     *
     * Maneja el estado de carga y errores para que la UI pueda mostrar indicaciones.
     *
     * @param text Mensaje del usuario.
     * @param mood Estado de ánimo actual (se envía como contexto al modelo).
     */
    fun sendMessage(text: String, mood: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                sendMessageUseCase(text, mood)
            } catch (e: Exception) {
                if (e is retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    _error.value = "Error: $errorBody"
                } else {
                    _error.value = "Error al enviar el mensaje. Verifica tu conexión."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Escucha el historial de mensajes y actualiza el estado interno de la UI.
     */
    private fun loadMessages() {
        viewModelScope.launch {
            getChatHistoryUseCase().collect { messages ->
                _messages.value = messages
            }
        }
    }
}