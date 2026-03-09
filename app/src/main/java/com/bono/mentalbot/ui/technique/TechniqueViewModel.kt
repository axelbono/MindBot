package com.bono.mentalbot.ui.technique

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bono.mentalbot.data.remote.api.GroqApiService
import com.bono.mentalbot.data.remote.firebase.FirestoreService
import com.bono.mentalbot.data.remote.model.ChatRequest
import com.bono.mentalbot.data.remote.model.MessageDto
import com.bono.mentalbot.domain.model.Technique
import com.bono.mentalbot.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ViewModel encargado de gestionar las técnicas de bienestar.
 *
 * - Carga técnicas guardadas desde Firestore.
 * - Genera nuevas técnicas usando la API de Groq (modelo de lenguaje).
 * - Permite guardar y borrar técnicas en Firestore.
 */
class TechniqueViewModel : ViewModel() {

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

    private val _techniques = MutableStateFlow<List<Technique>>(emptyList())
    val techniques: StateFlow<List<Technique>> = _techniques

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadTechniques()
    }

    /**
     * Carga las técnicas almacenadas en Firestore y las expone en [techniques].
     */
    private fun loadTechniques() {
        viewModelScope.launch {
            firestoreService.getTechniques().collect {
                _techniques.value = it
            }
        }
    }

    /**
     * Genera una técnica de bienestar usando el modelo de lenguaje (Groq/OpenAI).
     *
     * @param mood Estado de ánimo actual del usuario, usado para contextualizar la técnica.
     * @param userName Nombre del usuario (actualmente no se usa en el prompt, pero se puede extender).
     */
    fun generateTechnique(mood: String, userName: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            _error.value = null
            try {
                val prompt = "Sugiere una técnica de bienestar mental o relajación específica para alguien que se siente $mood. " +
                        "Responde SOLO en este formato exacto:\n" +
                        "TITULO: [nombre corto de la técnica]\n" +
                        "CATEGORIA: [Respiración/Mindfulness/Movimiento/Visualización/Grounding]\n" +
                        "DESCRIPCION: [descripción breve de máximo 2 oraciones]\n" +
                        "PASOS: [3 pasos simples separados por punto y coma]\n" +
                        "Responde en español."

                val response = api.sendMessage(
                    apiKey = Constants.GROQ_API_KEY,
                    request = ChatRequest(
                        model = Constants.MODEL,
                        messages = listOf(
                            MessageDto(role = "user", content = prompt)
                        )
                    )
                )

                val raw = response.choices[0].message.content
                val technique = parseTechnique(raw, mood)
                firestoreService.saveTechnique(technique)

            } catch (e: Exception) {
                if (e is retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    _error.value = "Error HTTP: $errorBody"
                } else {
                    _error.value = "Error: ${e.message}"
                }
            } finally {
                _isGenerating.value = false
            }
        }
    }

    /**
     * Guarda una técnica creada manualmente por el usuario en Firestore.
     *
     * @param title Título de la técnica.
     * @param description Descripción de la técnica.
     * @param category Categoría de la técnica (ej. Respiración, Mindfulness).
     * @param mood Estado de ánimo asociado.
     */
    fun saveTechnique(title: String, description: String, category: String, mood: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val technique = Technique(
                    title = title,
                    description = description,
                    category = category,
                    mood = mood,
                    aiSuggestion = ""
                )
                firestoreService.saveTechnique(technique)
            } catch (e: Exception) {
                _error.value = "Error al guardar técnica."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina una técnica identificada por su ID.
     */
    fun deleteTechnique(id: String) {
        viewModelScope.launch {
            try {
                firestoreService.deleteTechnique(id)
            } catch (e: Exception) {
                _error.value = "Error al eliminar técnica."
            }
        }
    }

    /**
     * Parsea el texto crudo retornado por el modelo para extraer los campos de técnica.
     *
     * El texto debe seguir un formato específico con prefijos como "TITULO:",
     * "CATEGORIA:", "DESCRIPCION:" y "PASOS:".
     */
    private fun parseTechnique(raw: String, mood: String): Technique {
        val lines = raw.lines()
        var title = "Técnica de bienestar"
        var category = "Mindfulness"
        var description = raw
        var pasos = ""

        lines.forEach { line ->
            when {
                line.startsWith("TITULO:") -> title = line.removePrefix("TITULO:").trim()
                line.startsWith("CATEGORIA:") -> category = line.removePrefix("CATEGORIA:").trim()
                line.startsWith("DESCRIPCION:") -> description = line.removePrefix("DESCRIPCION:").trim()
                line.startsWith("PASOS:") -> pasos = line.removePrefix("PASOS:").trim()
            }
        }

        val fullDescription = if (pasos.isNotEmpty()) "$description\n\nPasos: $pasos" else description

        return Technique(
            title = title,
            description = fullDescription,
            category = category,
            mood = mood,
            aiSuggestion = raw
        )
    }
}