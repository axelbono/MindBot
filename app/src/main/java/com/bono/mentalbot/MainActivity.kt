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

/**
 * Actividad principal de la aplicación MentalBot.
 *
 * Configura la UI con Jetpack Compose y controla el modo claro/oscuro mediante
 * un estado local (`isDarkTheme`).
 *
 * - Habilita la representación edge-to-edge para que la UI pueda dibujarse
 *   debajo de las barras de sistema.
 * - Inicializa el tema de la app con `MentalBotTheme`.
 * - Inicia el grafo de navegación (`NavGraph`) pasando el estado de tema y una
 *   acción para alternar entre claro y oscuro.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Estado local para el modo oscuro, se comparte dentro del árbol Compose.
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