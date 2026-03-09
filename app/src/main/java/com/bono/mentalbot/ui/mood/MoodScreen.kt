package com.bono.mentalbot.ui.mood

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bono.mentalbot.ui.theme.BackgroundMedium
import com.bono.mentalbot.ui.theme.MoodAngry
import com.bono.mentalbot.ui.theme.MoodAnxious
import com.bono.mentalbot.ui.theme.MoodCalm
import com.bono.mentalbot.ui.theme.MoodHappy
import com.bono.mentalbot.ui.theme.MoodSad
import com.bono.mentalbot.ui.theme.MoodTired
import com.bono.mentalbot.ui.theme.Purple
import com.bono.mentalbot.ui.theme.TextSecondary

/**
 * Representa una opción de estado de ánimo que el usuario puede seleccionar.
 *
 * @property label Texto que describe el estado de ánimo.
 * @property emoji Emoji asociado.
 * @property color Color representativo del estado.
 */
data class MoodOption(
    val label: String,
    val emoji: String,
    val color: Color
)

val moodOptions = listOf(
    MoodOption("Feliz", "😊", MoodHappy),
    MoodOption("Triste", "😢", MoodSad),
    MoodOption("Ansioso", "😰", MoodAnxious),
    MoodOption("Tranquilo", "😌", MoodCalm),
    MoodOption("Enojado", "😠", MoodAngry),
    MoodOption("Cansado", "😴", MoodTired)
)

/**
 * Pantalla donde el usuario selecciona su estado de ánimo actual.
 *
 * @param isDarkTheme Indica si la app está en tema oscuro.
 * @param onToggleTheme Callback para alternar el tema.
 * @param userName Nombre del usuario, usado para personalizar el saludo.
 * @param onContinue Callback con el estado de ánimo seleccionado.
 * @param viewModel ViewModel que gestiona la selección de ánimo.
 */
@Composable
fun MoodScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    userName: String,
    onContinue: (String) -> Unit,
    viewModel: MoodViewModel = viewModel()
) {
    val selectedMood by viewModel.selectedMood.collectAsState()

    // Animaciones
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .alpha(alpha.value)
            .scale(scale.value),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onToggleTheme) {
                Text(
                    text = if (isDarkTheme) "☀️" else "🌙",
                    fontSize = 24.sp
                )
            }
        }

        Text(text = "🧠", fontSize = 64.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "MindBot",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tu espacio seguro para hablar",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = if (userName.isNotEmpty())
                "¿Cómo te sientes hoy, $userName?"
            else
                "¿Cómo te sientes hoy?",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            moodOptions.chunked(3).forEach { rowMoods ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowMoods.forEach { mood ->
                        MoodCard(
                            mood = mood,
                            isSelected = selectedMood == mood.label,
                            onClick = { viewModel.selectMood(mood.label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { if (selectedMood.isNotEmpty()) onContinue(selectedMood) },
            enabled = selectedMood.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple,
                disabledContainerColor = BackgroundMedium
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (selectedMood.isEmpty()) "Selecciona tu estado" else "Comenzar →",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Tarjeta que representa una opción de estado de ánimo seleccionable.
 *
 * @param mood Opción de estado de ánimo (emoji + etiqueta + color).
 * @param isSelected Indica si esta opción está actualmente seleccionada.
 * @param onClick Callback cuando el usuario selecciona esta opción.
 * @param modifier Modificador para personalizar la apariencia desde el llamador.
 */
@Composable
fun MoodCard(
    mood: MoodOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) mood.color.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) mood.color else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = mood.emoji, fontSize = 32.sp)
            Text(
                text = mood.label,
                color = if (isSelected) mood.color else TextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}