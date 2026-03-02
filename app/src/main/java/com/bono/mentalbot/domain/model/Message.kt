package com.bono.mentalbot.domain.model

data class Message(
    val id: String = "",
    val sender: String,       // "user" o "bot"
    val content: String,
    val mood: String,
    val timestamp: Long = System.currentTimeMillis()
)