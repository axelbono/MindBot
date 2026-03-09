package com.bono.mentalbot.data.remote.api

import com.bono.mentalbot.data.remote.model.ChatRequest
import com.bono.mentalbot.data.remote.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * API de Groq/OpenAI para enviar mensajes y recibir respuestas del modelo de lenguaje.
 */
interface GroqApiService {

    /**
     * Envía un chat request al endpoint de completions.
     *
     * @param apiKey Clave de autorización con el prefijo "Bearer ".
     * @param request Payload con el modelo y los mensajes.
     */
    @POST("chat/completions")
    suspend fun sendMessage(
        @Header("Authorization") apiKey: String,
        @Body request: ChatRequest
    ): ChatResponse
}