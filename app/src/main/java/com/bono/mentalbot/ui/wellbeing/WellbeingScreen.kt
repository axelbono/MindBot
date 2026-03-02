package com.bono.mentalbot.ui.wellbeing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bono.mentalbot.ui.theme.Purple
import com.bono.mentalbot.ui.theme.PurpleLight
import com.bono.mentalbot.ui.theme.TextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellbeingScreen(
    userName: String,
    mood: String,
    onBack: () -> Unit,
    onContinue: (String) -> Unit,
    viewModel: WellbeingViewModel = viewModel()
) {
    val data by viewModel.data.collectAsState()
    val scrollState = rememberScrollState()
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(targetValue = 1f, animationSpec = tween(800))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .alpha(alpha.value)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Evaluación emocional",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Purple
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "🧘", fontSize = 48.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hola ${if (userName.isNotEmpty()) userName else ""}. Antes de comenzar,\ncuéntame un poco más sobre ti.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader(
                emoji = "🏗️",
                title = "Necesidades básicas",
                subtitle = "Basado en la jerarquía de Maslow"
            )

            Spacer(modifier = Modifier.height(16.dp))

            ScaleQuestion(
                question = "¿Qué tan bien cubiertas están tus necesidades básicas? (alimentación, descanso, salud)",
                value = data.necesidadesBasicas,
                onValueChange = { viewModel.updateNecesidadesBasicas(it) }
            )
            ScaleQuestion(
                question = "¿Qué tan seguro/a te sientes en tu entorno actual?",
                value = data.seguridadVital,
                onValueChange = { viewModel.updateSeguridadVital(it) }
            )
            ScaleQuestion(
                question = "¿Cómo calificarías la calidad de tus relaciones personales?",
                value = data.relacionesSociales,
                onValueChange = { viewModel.updateRelacionesSociales(it) }
            )
            ScaleQuestion(
                question = "¿Cómo está tu autoestima en este momento?",
                value = data.autoestima,
                onValueChange = { viewModel.updateAutoestima(it) }
            )
            ScaleQuestion(
                question = "¿Sientes que tu vida tiene propósito y dirección?",
                value = data.propositoVida,
                onValueChange = { viewModel.updatePropositoVida(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            SectionDivider()
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                emoji = "🌍",
                title = "Tu circunstancia vital",
                subtitle = "Basado en Ortega y Gasset: \"Yo soy yo y mi circunstancia\""
            )

            Spacer(modifier = Modifier.height(16.dp))

            ScaleQuestion(
                question = "¿Qué tan satisfecho/a estás con las circunstancias de tu vida actual?",
                value = data.satisfaccionCircunstancias,
                onValueChange = { viewModel.updateSatisfaccionCircunstancias(it) }
            )
            ScaleQuestion(
                question = "¿Sientes que tienes control sobre las decisiones importantes de tu vida?",
                value = data.controlVida,
                onValueChange = { viewModel.updateControlVida(it) }
            )
            OpenQuestion(
                question = "¿Hay alguna circunstancia externa que esté afectando tu bienestar hoy?",
                value = data.textoCircunstancia,
                onValueChange = { viewModel.updateTextoCircunstancia(it) },
                placeholder = "Puedes escribir sobre tu trabajo, familia, entorno..."
            )

            Spacer(modifier = Modifier.height(24.dp))
            SectionDivider()
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                emoji = "✨",
                title = "Valores y propósito",
                subtitle = "Basado en el enfoque humanista de López de Llergo"
            )

            Spacer(modifier = Modifier.height(16.dp))

            ScaleQuestion(
                question = "¿Qué tan alineado/a te sientes con tus valores personales hoy?",
                value = data.vivenciaValores,
                onValueChange = { viewModel.updateVivenciaValores(it) }
            )
            OpenQuestion(
                question = "¿Qué es lo que más valoras en este momento de tu vida?",
                value = data.textoValores,
                onValueChange = { viewModel.updateTextoValores(it) },
                placeholder = "Familia, trabajo, salud, crecimiento personal..."
            )

            Spacer(modifier = Modifier.height(24.dp))
            SectionDivider()
            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                emoji = "💭",
                title = "Reflexión libre",
                subtitle = "Algo que quieras que MindBot sepa antes de comenzar"
            )

            Spacer(modifier = Modifier.height(16.dp))

            OpenQuestion(
                question = "¿Hay algo más que quieras compartir sobre cómo te sientes hoy?",
                value = data.textoLibre,
                onValueChange = { viewModel.updateTextoLibre(it) },
                placeholder = "Escribe lo que sientas...",
                minLines = 4
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    val context = viewModel.buildWellbeingContext()
                    onContinue(context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Ir al chat →",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionHeader(emoji: String, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 24.sp)
            Text(
                text = "  $title",
                color = PurpleLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = subtitle,
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun ScaleQuestion(
    question: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = question,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = Purple,
                activeTrackColor = Purple,
                inactiveTrackColor = PurpleLight.copy(alpha = 0.3f)
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "😔 Muy bajo", color = TextSecondary, fontSize = 11.sp)
            Text(
                text = "${value.roundToInt()}/5",
                color = Purple,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = "Muy alto 😊", color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun OpenQuestion(
    question: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 2
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = question,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextSecondary, fontSize = 13.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple,
                unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = Purple
            ),
            shape = RoundedCornerShape(12.dp),
            minLines = minLines,
            maxLines = 6
        )
    }
}

@Composable
fun SectionDivider() {
    Divider(
        color = PurpleLight.copy(alpha = 0.3f),
        thickness = 1.dp
    )
}