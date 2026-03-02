package com.bono.mentalbot.ui.mood

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MoodViewModel : ViewModel() {

    private val _selectedMood = MutableStateFlow("")
    val selectedMood: StateFlow<String> = _selectedMood

    fun selectMood(mood: String) {
        _selectedMood.value = mood
    }
}