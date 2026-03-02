package com.bono.mentalbot.data.remote.api

import com.bono.mentalbot.data.remote.model.ChatRequest
import com.bono.mentalbot.data.remote.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApiService {

    @POST("chat/completions")
    suspend fun sendMessage(
        @Header("Authorization") apiKey: String,
        @Body request: ChatRequest
    ): ChatResponse
}