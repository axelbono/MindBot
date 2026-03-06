package com.bono.mentalbot.domain.model

data class Technique(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val mood: String = "",
    val aiSuggestion: String = "",
    val timestamp: Long = System.currentTimeMillis()
)