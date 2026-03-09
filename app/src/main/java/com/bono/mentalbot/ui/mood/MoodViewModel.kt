package com.bono.mentalbot.ui.mood

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel que mantiene el estado de ánimo seleccionado por el usuario.
 */
class MoodViewModel : ViewModel() {

    private val _selectedMood = MutableStateFlow("")
    val selectedMood: StateFlow<String> = _selectedMood

    /**
     * Selecciona un estado de ánimo y actualiza el flujo expuesto.
     */
    fun selectMood(mood: String) {
        _selectedMood.value = mood
    }
}