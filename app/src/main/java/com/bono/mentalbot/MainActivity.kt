package com.bono.mentalbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.bono.mentalbot.ui.navigation.NavGraph
import com.bono.mentalbot.ui.theme.MentalBotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme = remember { mutableStateOf(true) }
            MentalBotTheme(isDarkTheme = isDarkTheme.value) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph(
                        isDarkTheme = isDarkTheme.value,
                        onToggleTheme = { isDarkTheme.value = !isDarkTheme.value }
                    )
                }
            }
        }
    }
}