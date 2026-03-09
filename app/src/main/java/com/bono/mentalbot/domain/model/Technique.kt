package com.bono.mentalbot.domain.model

/**
 * Representa una técnica de bienestar guardada en Firestore.
 *
 * @param id Identificador único de la técnica.
 * @param title Nombre de la técnica.
 * @param description Descripción y pasos para realizarla.
 * @param category Categoría (por ejemplo, Respiración, Mindfulness).
 * @param mood Estado anímico al que está dirigida.
 * @param aiSuggestion Texto original generado por IA (para referencia).
 * @param timestamp Marca de tiempo en milisegundos.
 */
data class Technique(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val mood: String = "",
    val aiSuggestion: String = "",
    val timestamp: Long = System.currentTimeMillis()
)