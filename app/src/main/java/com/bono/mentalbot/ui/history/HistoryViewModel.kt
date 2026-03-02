package com.bono.mentalbot.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bono.mentalbot.data.remote.api.GroqApiService
import com.bono.mentalbot.data.remote.firebase.FirestoreService
import com.bono.mentalbot.data.repository.ChatRepository
import com.bono.mentalbot.domain.model.Message
import com.bono.mentalbot.domain.usecase.GetChatHistoryUseCase
import com.bono.mentalbot.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HistoryViewModel : ViewModel() {

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
    private val getChatHistoryUseCase = GetChatHistoryUseCase(repository)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            getChatHistoryUseCase().collect { messages ->
                _messages.value = messages
            }
        }
    }

    fun clearMessages() {
        viewModelScope.launch {
            try {
                repository.clearMessages()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}