package com.bono.mentalbot.ui.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bono.mentalbot.data.remote.api.GroqApiService
import com.bono.mentalbot.data.remote.firebase.FirestoreService
import com.bono.mentalbot.data.remote.model.ChatRequest
import com.bono.mentalbot.data.remote.model.MessageDto
import com.bono.mentalbot.domain.model.Goal
import com.bono.mentalbot.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GoalViewModel : ViewModel() {

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

    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    val goals: StateFlow<List<Goal>> = _goals

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            firestoreService.getGoals().collect {
                _goals.value = it
            }
        }
    }

    fun addGoal(title: String, description: String, userName: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            _error.value = null
            try {
                val prompt = "El usuario se llama $userName y tiene esta meta de bienestar personal: \"$title\". " +
                        "Descripción adicional: \"$description\". " +
                        "Genera un consejo motivador y 3 pasos concretos para ayudarle a lograr esta meta. " +
                        "Responde SOLO en este formato:\n" +
                        "CONSEJO: [consejo motivador en 1 oración]\n" +
                        "PASOS: [paso 1]; [paso 2]; [paso 3]\n" +
                        "Responde en español, de forma cálida y empática."

                val response = api.sendMessage(
                    apiKey = Constants.GROQ_API_KEY,
                    request = ChatRequest(
                        model = Constants.MODEL,
                        messages = listOf(MessageDto(role = "user", content = prompt))
                    )
                )

                val aiAdvice = response.choices[0].message.content
                val goal = Goal(
                    title = title,
                    description = description,
                    aiAdvice = aiAdvice
                )
                firestoreService.saveGoal(goal)

            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun toggleCompleted(id: String, current: Boolean) {
        viewModelScope.launch {
            try {
                firestoreService.updateGoalCompleted(id, !current)
            } catch (e: Exception) {
                _error.value = "Error al actualizar meta."
            }
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            try {
                firestoreService.deleteGoal(id)
            } catch (e: Exception) {
                _error.value = "Error al eliminar meta."
            }
        }
    }
}