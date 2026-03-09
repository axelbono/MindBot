Manual Completo para Reconstruir la Aplicación MentalBot
Este documento explica paso a paso cómo reconstruir la aplicación MentalBot usando Android Studio, Kotlin, Jetpack Compose, Clean Architecture y Firebase. Cada sección incluye los archivos completos necesarios para replicar el proyecto.
1. Crear el proyecto
Abrir Android Studio → New Project → Empty Activity.
Configuración:
Nombre: MentalBot
Lenguaje: Kotlin
Minimum SDK: 24
UI: Jetpack Compose
2. Estructura del proyecto
Crear los siguientes paquetes dentro de com.bono.mentalbot:
data
domain
ui
utils
3. Archivos del proyecto
A continuación se incluyen todos los archivos Kotlin del proyecto. Copiar cada archivo en la misma ruta dentro del proyecto Android.
app/src/androidTest/java/com/bono/mentalbot/ExampleInstrumentedTest.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.bono.mentalbot", appContext.packageName)
    }
}
app/src/main/java/com/bono/mentalbot/MainActivity.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
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
app/src/main/java/com/bono/mentalbot/data/remote/api/GroqApiService.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.data.remote.api

import com.bono.mentalbot.data.remote.model.ChatRequest
import com.bono.mentalbot.data.remote.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * API de Groq/OpenAI para enviar mensajes y recibir respuestas del modelo de lenguaje.
 */
interface GroqApiService {

    /**
     * Envía un chat request al endpoint de completions.
     *
     * @param apiKey Clave de autorización con el prefijo "Bearer ".
     * @param request Payload con el modelo y los mensajes.
     */
    @POST("chat/completions")
    suspend fun sendMessage(
        @Header("Authorization") apiKey: String,
        @Body request: ChatRequest
    ): ChatResponse
}
app/src/main/java/com/bono/mentalbot/data/remote/firebase/FirestoreService.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.data.remote.firebase

import com.bono.mentalbot.domain.model.Goal
import com.bono.mentalbot.domain.model.Message
import com.bono.mentalbot.domain.model.Technique
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Servicio para interactuar con Firebase Firestore.
 *
 * Maneja mensajería, técnicas, metas y almacenamiento de información del usuario.
 */
class FirestoreService {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Colección de mensajes del usuario actual
    private fun messagesCollection() = db
        .collection("users")
        .document(auth.currentUser?.uid ?: "anonymous")
        .collection("messages")

    /**
     * Guarda un mensaje en Firestore.
     */
    suspend fun saveMessage(message: Message) {
        messagesCollection().add(
            mapOf(
                "id" to message.id,
                "sender" to message.sender,
                "content" to message.content,
                "mood" to message.mood,
                "timestamp" to message.timestamp
            )
        ).await()
    }

    /**
     * Obtiene un flujo que emite el historial de mensajes ordenado cronológicamente.
     */
    fun getMessages(): Flow<List<Message>> = callbackFlow {
        val listener = messagesCollection()
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(Exception(error.message))
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    Message(
                        id = doc.id,
                        sender = doc.getString("sender") ?: "",
                        content = doc.getString("content") ?: "",
                        mood = doc.getString("mood") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Elimina todos los mensajes del historial del usuario.
     */
    suspend fun clearMessages() {
        val snapshot = messagesCollection().get().await()
        snapshot.documents.forEach { it.reference.delete().await() }
    }

    // Guardar nombre del usuario
    /**
     * Guarda el nombre del usuario en Firestore.
     */
    suspend fun saveUserName(name: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .set(mapOf("name" to name), com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    // Obtener nombre del usuario
    /**
     * Recupera el nombre del usuario almacenado en Firestore.
     */
    suspend fun getUserName(): String {
        val uid = auth.currentUser?.uid ?: return ""
        val doc = db.collection("users").document(uid).get().await()
        return doc.getString("name") ?: ""
    }

    private fun techniquesCollection() = db
        .collection("users")
        .document(auth.currentUser?.uid ?: "anonymous")
        .collection("techniques")

    /**
     * Guarda una técnica en Firestore.
     */
    suspend fun saveTechnique(technique: Technique) {
        val doc = techniquesCollection().document()
        doc.set(
            mapOf(
                "id" to doc.id,
                "title" to technique.title,
                "description" to technique.description,
                "category" to technique.category,
                "mood" to technique.mood,
                "aiSuggestion" to technique.aiSuggestion,
                "timestamp" to technique.timestamp
            )
        ).await()
    }

    /**
     * Obtiene un flujo con las técnicas almacenadas, ordenadas por fecha.
     */
    fun getTechniques(): Flow<List<Technique>> = callbackFlow {
        val listener = techniquesCollection()
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(Exception(error.message))
                    return@addSnapshotListener
                }
                val techniques = snapshot?.documents?.mapNotNull { doc ->
                    Technique(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "",
                        mood = doc.getString("mood") ?: "",
                        aiSuggestion = doc.getString("aiSuggestion") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                } ?: emptyList()
                trySend(techniques)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Elimina una técnica por su ID.
     */
    suspend fun deleteTechnique(id: String) {
        techniquesCollection().document(id).delete().await()
    }

    private fun goalsCollection() = db
        .collection("users")
        .document(auth.currentUser?.uid ?: "anonymous")
        .collection("goals")

    /**
     * Guarda una meta en Firestore.
     */
    suspend fun saveGoal(goal: Goal) {
        val doc = goalsCollection().document()
        doc.set(
            mapOf(
                "id" to doc.id,
                "title" to goal.title,
                "description" to goal.description,
                "isCompleted" to goal.isCompleted,
                "aiAdvice" to goal.aiAdvice,
                "timestamp" to goal.timestamp
            )
        ).await()
    }

    /**
     * Obtiene un flujo con las metas almacenadas, ordenadas por fecha.
     */
    fun getGoals(): Flow<List<Goal>> = callbackFlow {
        val listener = goalsCollection()
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(Exception(error.message))
                    return@addSnapshotListener
                }
                val goals = snapshot?.documents?.mapNotNull { doc ->
                    Goal(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                        aiAdvice = doc.getString("aiAdvice") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                } ?: emptyList()
                trySend(goals)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Actualiza el campo "completado" de una meta.
     */
    suspend fun updateGoalCompleted(id: String, isCompleted: Boolean) {
        goalsCollection().document(id)
            .update("isCompleted", isCompleted)
            .await()
    }

    /**
     * Elimina una meta por su ID.
     */
    suspend fun deleteGoal(id: String) {
        goalsCollection().document(id).delete().await()
    }
}
app/src/main/java/com/bono/mentalbot/data/remote/model/ChatRequest.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.data.remote.model

/**
 * Payload enviado al endpoint de chat completions.
 *
 * @param model Identificador del modelo de lenguaje.
 * @param messages Lista de mensajes que componen el contexto de la conversación.
 */
data class ChatRequest(
    val model: String,
    val messages: List<MessageDto>
)

/**
 * DTO que representa un mensaje dentro del request/response de Groq/OpenAI.
 *
 * @param role Rol del emisor (p. ej. "user" o "assistant").
 * @param content Texto del mensaje.
 */
data class MessageDto(
    val role: String,
    val content: String
)
app/src/main/java/com/bono/mentalbot/data/remote/model/ChatResponse.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.data.remote.model

/**
 * Respuesta del endpoint de chat completions de Groq/OpenAI.
 *
 * Contiene una lista de opciones (choices) con posibles respuestas.
 */
data class ChatResponse(
    val choices: List<Choice>
)

/**
 * Opción de respuesta dentro del objeto [ChatResponse].
 *
 * @param message Mensaje generado por el modelo.
 */
data class Choice(
    val message: MessageDto
)
app/src/main/java/com/bono/mentalbot/data/repository/ChatRepository.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.data.repository

import com.bono.mentalbot.data.remote.api.GroqApiService
import com.bono.mentalbot.data.remote.firebase.FirestoreService
import com.bono.mentalbot.data.remote.model.ChatRequest
import com.bono.mentalbot.data.remote.model.MessageDto
import com.bono.mentalbot.domain.model.Message
import com.bono.mentalbot.utils.Constants
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que orquestra la lógica de conversación entre la app y los servicios
 * externos (Groq API + Firestore).
 *
 * Mantiene un historial local de la conversación con el modelo y persiste los
 * mensajes en Firestore para permitir recuperar el historial del chat.
 */
class ChatRepository(
    private val api: GroqApiService,
    private val firestoreService: FirestoreService
) {
    private var conversationHistory = mutableListOf<MessageDto>()

    /**
     * Inicializa el historial de conversación con el prompt del sistema.
     *
     * @param userName Nombre del usuario (para personalizar el prompt).
     * @param mood Estado de ánimo actual del usuario.
     * @param wellbeingContext Contexto adicional proveniente de la evaluación de bienestar.
     */
    fun initializeChat(userName: String, mood: String, wellbeingContext: String) {
        val systemPrompt = Constants.getSystemPrompt(userName, mood) +
                if (wellbeingContext.isNotEmpty()) "\n\n$wellbeingContext" else ""

        conversationHistory = mutableListOf(
            MessageDto(role = "system", content = systemPrompt)
        )
    }

    /**
     * Genera y guarda el primer mensaje inicial del asistente (antes de llamar al modelo).
     *
     * @param userName Nombre del usuario.
     * @param mood Estado de ánimo actual.
     * @return El mensaje inicial del asistente.
     */
    suspend fun sendInitialMessage(userName: String, mood: String): String {
        val hasWellbeingContext = conversationHistory.any {
            it.role == "system" && it.content.contains("EVALUACIÓN EMOCIONAL")
        }
        val initialMessage = Constants.getInitialMessage(userName, mood, hasWellbeingContext)
        conversationHistory.add(MessageDto(role = "assistant", content = initialMessage))
        firestoreService.saveMessage(
            Message(sender = "bot", content = initialMessage, mood = mood)
        )
        return initialMessage
    }

    /**
     * Envía un mensaje del usuario al modelo de lenguaje y persiste el intercambio.
     *
     * @param userMessage Texto enviado por el usuario.
     * @param mood Estado de ánimo actual (para contexto y registro).
     * @return Respuesta generada por el asistente.
     */
    suspend fun sendMessage(userMessage: String, mood: String): String {
        conversationHistory.add(MessageDto(role = "user", content = userMessage))

        val response = api.sendMessage(
            apiKey = Constants.GROQ_API_KEY,
            request = ChatRequest(
                model = Constants.MODEL,
                messages = conversationHistory
            )
        )

        val botReply = response.choices[0].message.content
        conversationHistory.add(MessageDto(role = "assistant", content = botReply))

        firestoreService.saveMessage(
            Message(sender = "user", content = userMessage, mood = mood)
        )
        firestoreService.saveMessage(
            Message(sender = "bot", content = botReply, mood = mood)
        )

        return botReply
    }

    /**
     * Obtiene el historial de mensajes almacenado en Firestore.
     */
    fun getMessages(): Flow<List<Message>> = firestoreService.getMessages()

    /**
     * Elimina todos los mensajes almacenados en Firestore.
     */
    suspend fun clearMessages() = firestoreService.clearMessages()
}
app/src/main/java/com/bono/mentalbot/domain/model/Goal.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.domain.model

/**
 * Representa una meta de bienestar creada por el usuario.
 *
 * @param id Identificador único de la meta.
 * @param title Título de la meta.
 * @param description Descripción detallada de la meta.
 * @param isCompleted Indica si la meta está completada.
 * @param aiAdvice Consejo generado por IA para ayudar a alcanzarla.
 * @param timestamp Marca de tiempo en milisegundos.
 */
data class Goal(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val aiAdvice: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
app/src/main/java/com/bono/mentalbot/domain/model/Message.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.domain.model

/**
 * Representa un mensaje en la conversación entre el usuario y MindBot.
 *
 * @param id Identificador único del mensaje.
 * @param sender Emisor del mensaje ("user" o "bot").
 * @param content Texto del mensaje.
 * @param mood Estado de ánimo asociado al mensaje.
 * @param timestamp Marca de tiempo en milisegundos.
 */
data class Message(
    val id: String = "",
    val sender: String,       // "user" o "bot"
    val content: String,
    val mood: String,
    val timestamp: Long = System.currentTimeMillis()
)
app/src/main/java/com/bono/mentalbot/domain/model/Technique.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
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
app/src/main/java/com/bono/mentalbot/domain/usecase/GetChatHistoryUseCase.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.domain.usecase

import com.bono.mentalbot.data.repository.ChatRepository
import com.bono.mentalbot.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso que expone un flujo de historial de mensajes del chat.
 *
 * Este caso de uso delega al repositorio para obtener los mensajes almacenados.
 */
class GetChatHistoryUseCase(private val repository: ChatRepository) {

    /**
     * Obtiene un Flow que emite la lista actualizada de mensajes del chat.
     *
     * @return Flow que emite el historial de mensajes.
     */
    operator fun invoke(): Flow<List<Message>> {
        return repository.getMessages()
    }
}
app/src/main/java/com/bono/mentalbot/domain/usecase/SendMessageUsecase.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.domain.usecase

import com.bono.mentalbot.data.repository.ChatRepository

/**
 * Caso de uso que envía un mensaje del usuario al repositorio de chat y devuelve la respuesta del asistente.
 *
 * Encapsula la lógica de negocio para interactuar con el modelo de conversación.
 */
class SendMessageUseCase(private val repository: ChatRepository) {

    /**
     * Envía un mensaje al backend/chat y obtiene la respuesta del asistente.
     *
     * @param message Mensaje del usuario.
     * @param mood Estado de ánimo actual del usuario (se envía para mantener contexto).
     * @return Respuesta generada por el asistente.
     */
    suspend operator fun invoke(message: String, mood: String): String {
        return repository.sendMessage(message, mood)
    }
}
app/src/main/java/com/bono/mentalbot/ui/auth/AuthScreen.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bono.mentalbot.ui.theme.BackgroundDark
import com.bono.mentalbot.ui.theme.BackgroundMedium
import com.bono.mentalbot.ui.theme.Purple
import com.bono.mentalbot.ui.theme.PurpleLight
import com.bono.mentalbot.ui.theme.TextPrimary
import com.bono.mentalbot.ui.theme.TextSecondary

/**
 * Pantalla de autenticación que permite iniciar sesión o registrarse.
 *
 * @param onAuthSuccess Callback que se ejecuta cuando el usuario se autentica correctamente.
 *                       El parámetro indica si es un usuario nuevo.
 * @param viewModel ViewModel que gestiona la lógica de autenticación.
 */
@Composable
fun AuthScreen(
    onAuthSuccess: (Boolean) -> Unit,
    viewModel: AuthViewModel
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val isNewUser by viewModel.isNewUser.collectAsState()
    val rememberSession by viewModel.rememberSession.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) onAuthSuccess(isNewUser)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🧠", fontSize = 72.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "MindBot",
            color = TextPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Tu espacio seguro para hablar",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = if (isLoginMode) "Iniciar Sesión" else "Crear Cuenta",
            color = PurpleLight,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple,
                unfocusedBorderColor = TextSecondary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Purple
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple,
                unfocusedBorderColor = TextSecondary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Purple
            ),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        error?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = rememberSession,
                onCheckedChange = { viewModel.toggleRememberSession() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Purple,
                    uncheckedColor = TextSecondary
                )
            )
            Text(
                text = "Recordar sesión",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (isLoginMode) viewModel.login(email, password)
                else viewModel.register(email, password)
            },
            enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple,
                disabledContainerColor = BackgroundMedium
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = if (isLoginMode) "Iniciar Sesión" else "Registrarse",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { isLoginMode = !isLoginMode }) {
            Text(
                text = if (isLoginMode) "¿No tienes cuenta? Regístrate"
                else "¿Ya tienes cuenta? Inicia sesión",
                color = PurpleLight,
                fontSize = 14.sp
            )
        }
    }
}
app/src/main/java/com/bono/mentalbot/ui/auth/AuthViewModel.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bono.mentalbot.data.remote.firebase.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel encargado de la autenticación de usuarios con Firebase Auth.
 *
 * Gestiona el estado de carga, errores y el almacenamiento local de la sesión.
 */
class AuthViewModel(context: Context) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mindbot_prefs", Context.MODE_PRIVATE)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    private val _isNewUser = MutableStateFlow(false)
    val isNewUser: StateFlow<Boolean> = _isNewUser

    private val firestoreService = FirestoreService()

    private val _rememberSession = MutableStateFlow(
        prefs.getBoolean("remember_session", false)
    )
    val rememberSession: StateFlow<Boolean> = _rememberSession

    init {
        val remember = prefs.getBoolean("remember_session", false)
        if (auth.currentUser != null && remember) {
            _isAuthenticated.value = true
            loadUserName()
        } else {
            auth.signOut()
        }
    }

    /**
     * Alterna la preferencia de recordar la sesión y la persiste en SharedPreferences.
     */
    fun toggleRememberSession() {
        val newValue = !_rememberSession.value
        _rememberSession.value = newValue
        prefs.edit().putBoolean("remember_session", newValue).apply()
    }

    /**
     * Intenta iniciar sesión con correo y contraseña usando Firebase Auth.
     *
     * Actualiza los estados de carga, autenticación y errores para la UI.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _isNewUser.value = false
                _isAuthenticated.value = true
                loadUserName()
            } catch (e: Exception) {
                _error.value = when {
                    e.message?.contains("password") == true -> "Contraseña incorrecta"
                    e.message?.contains("user") == true -> "Usuario no encontrado"
                    e.message?.contains("email") == true -> "Correo inválido"
                    else -> "Error al iniciar sesión"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Crea una nueva cuenta con correo y contraseña usando Firebase Auth.
     *
     * Si la operación es exitosa, se marca al usuario como nuevo y autenticado.
     */
    fun register(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _isNewUser.value = true
                _isAuthenticated.value = true
            } catch (e: Exception) {
                _error.value = when {
                    e.message?.contains("email") == true -> "Correo ya registrado o inválido"
                    e.message?.contains("password") == true -> "La contraseña debe tener al menos 6 caracteres"
                    else -> "Error al registrarse"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cierra la sesión del usuario y borra el estado local asociado.
     */
    fun logout() {
        auth.signOut()
        _isAuthenticated.value = false
        _isNewUser.value = false
        _userName.value = ""
        _rememberSession.value = false
        prefs.edit().putBoolean("remember_session", false).apply()
    }

    /**
     * Guarda el nombre del usuario en Firestore.
     */
    fun saveUserName(name: String) {
        viewModelScope.launch {
            firestoreService.saveUserName(name)
            _userName.value = name
        }
    }

    /**
     * Carga el nombre del usuario desde Firestore y lo expone en [userName].
     */
    fun loadUserName() {
        viewModelScope.launch {
            _userName.value = firestoreService.getUserName()
        }
    }

    companion object {
        /**
         * Fábrica para crear el ViewModel con el contexto necesario.
         */
        fun factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return AuthViewModel(context) as T
                }
            }
        }
    }
}
app/src/main/java/com/bono/mentalbot/ui/auth/NameScreen.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bono.mentalbot.ui.theme.Purple
import com.bono.mentalbot.ui.theme.TextSecondary

/**
 * Pantalla para solicitar el nombre del usuario al iniciar por primera vez.
 *
 * @param onNameSaved Callback con el nombre ingresado cuando el usuario continúa.
 */
@Composable
fun NameScreen(
    onNameSaved: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "👋", fontSize = 64.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¡Bienvenido a MindBot!",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¿Cómo te llamas?\nQuiero conocerte mejor.",
            color = TextSecondary,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tu nombre", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple,
                unfocusedBorderColor = TextSecondary,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = Purple
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if (name.isNotBlank()) onNameSaved(name.trim()) },
            enabled = name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple,
                disabledContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Continuar →",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
app/src/main/java/com/bono/mentalbot/ui/chat/ChatScreen.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bono.mentalbot.ui.chat.components.InputBar
import com.bono.mentalbot.ui.chat.components.MessageBubble
import com.bono.mentalbot.ui.chat.components.TypingIndicator
import com.bono.mentalbot.ui.theme.Purple

/**
 * Pantalla principal del chat con MindBot.
 *
 * Muestra el historial de mensajes, permite enviar texto y ofrece opciones de navegación.
 *
 * @param mood Estado de ánimo actual para contextualizar la conversación.
 * @param userName Nombre del usuario (se usa para inicializar el chat).
 * @param wellbeingContext Contexto adicional generado por la evaluación emocional.
 * @param isDarkTheme Indica si el tema actual es oscuro.
 * @param onToggleTheme Callback para alternar entre modo claro/oscuro.
 * @param onBack Callback para volver a la pantalla anterior.
 * @param onHistoryClick Callback para mostrar el historial de mensajes.
 * @param onLogout Callback para cerrar sesión o salir.
 * @param viewModel ViewModel que gestiona la lógica del chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    mood: String,
    userName: String,
    wellbeingContext: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.initializeChat(userName, mood, wellbeingContext)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            title = {
                Column {
                    Text(
                        text = "MindBot",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Me siento $mood hoy",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            },
            actions = {
                IconButton(onClick = onToggleTheme) {
                    Text(
                        text = if (isDarkTheme) "☀️" else "🌙",
                        fontSize = 18.sp
                    )
                }
                IconButton(onClick = onHistoryClick) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Historial",
                        tint = Purple
                    )
                }
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Cerrar sesión",
                        tint = Purple
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message = message)
            }
            if (isLoading) {
                item {
                    Box(modifier = Modifier.padding(8.dp)) {
                        TypingIndicator()
                    }
                }
            }
        }

        error?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        InputBar(
            text = inputText,
            onTextChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank()) {
                    viewModel.sendMessage(inputText, mood)
                    inputText = ""
                }
            }
        )
    }
}
app/src/main/java/com/bono/mentalbot/ui/chat/ChatViewModel.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bono.mentalbot.data.remote.api.GroqApiService
import com.bono.mentalbot.data.remote.firebase.FirestoreService
import com.bono.mentalbot.data.repository.ChatRepository
import com.bono.mentalbot.domain.model.Message
import com.bono.mentalbot.domain.usecase.GetChatHistoryUseCase
import com.bono.mentalbot.domain.usecase.SendMessageUseCase
import com.bono.mentalbot.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ViewModel responsable de la lógica del chat con MindBot.
 *
 * - Inicializa la conversación y mantiene el historial.
 * - Envía mensajes al modelo de lenguaje.
 * - Expone estados de carga y errores para la UI.
 */
class ChatViewModel : ViewModel() {

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.GROQ_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
        )
        .build()

    private val api = retrofit.create(GroqApiService::class.java)
    private val firestoreService = FirestoreService()
    private val repository = ChatRepository(api, firestoreService)
    private val sendMessageUseCase = SendMessageUseCase(repository)
    private val getChatHistoryUseCase = GetChatHistoryUseCase(repository)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var chatInitialized = false

    /**
     * Inicializa la conversación en el repositorio y envía el mensaje inicial del asistente.
     *
     * @param userName Nombre del usuario (para personalizar el prompt).
     * @param mood Estado de ánimo actual.
     * @param wellbeingContext Contexto adicional generado por la evaluación de bienestar.
     */
    fun initializeChat(userName: String, mood: String, wellbeingContext: String) {
        if (chatInitialized) return
        chatInitialized = true
        repository.initializeChat(userName, mood, wellbeingContext)
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.sendInitialMessage(userName, mood)
            } catch (e: Exception) {
                _error.value = "Error al iniciar el chat"
            } finally {
                _isLoading.value = false
            }
        }
        loadMessages()
    }

    /**
     * Envía un mensaje del usuario a MindBot usando el caso de uso correspondiente.
     *
     * Maneja el estado de carga y errores para que la UI pueda mostrar indicaciones.
     *
     * @param text Mensaje del usuario.
     * @param mood Estado de ánimo actual (se envía como contexto al modelo).
     */
    fun sendMessage(text: String, mood: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                sendMessageUseCase(text, mood)
            } catch (e: Exception) {
                if (e is retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    _error.value = "Error: $errorBody"
                } else {
                    _error.value = "Error al enviar el mensaje. Verifica tu conexión."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Escucha el historial de mensajes y actualiza el estado interno de la UI.
     */
    private fun loadMessages() {
        viewModelScope.launch {
            getChatHistoryUseCase().collect { messages ->
                _messages.value = messages
            }
        }
    }
}
app/src/main/java/com/bono/mentalbot/ui/chat/components/InputBar.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.bono.mentalbot.ui.theme.BackgroundMedium
import com.bono.mentalbot.ui.theme.Purple
import com.bono.mentalbot.ui.theme.TextPrimary
import com.bono.mentalbot.ui.theme.TextSecondary

/**
 * Barra de entrada de texto para el chat con botón de envío.
 *
 * @param text Texto actual del campo.
 * @param onTextChange Callback cuando cambia el texto.
 * @param onSend Callback cuando se pulsa el botón de enviar o se presiona "Enter".
 */
@Composable
fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = BackgroundMedium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Escribe algo...", color = TextSecondary)
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E1E2E),
                    unfocusedContainerColor = Color(0xFF1E1E2E),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Botón enviar con fondo visible
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (text.isNotBlank()) Purple else Color(0xFF2E2E3E))
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = if (text.isNotBlank()) Color.White else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
app/src/main/java/com/bono/mentalbot/ui/chat/components/MessageBubble.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bono.mentalbot.domain.model.Message
import com.bono.mentalbot.ui.theme.BubbleBot
import com.bono.mentalbot.ui.theme.BubbleUser
import com.bono.mentalbot.ui.theme.TextPrimary
import com.bono.mentalbot.ui.theme.TextSecondary
import com.bono.mentalbot.utils.toFormattedDate

/**
 * Muestra un mensaje en forma de burbuja de chat.
 *
 * @param message Mensaje a mostrar (puede ser del usuario o del asistente).
 */
@Composable
fun MessageBubble(message: Message) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(if (isUser) BubbleUser else BubbleBot)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.content,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
            }
            Text(
                text = message.timestamp.toFormattedDate(),
                color = TextSecondary,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
app/src/main/java/com/bono/mentalbot/ui/chat/components/TypingIndicator.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.chat.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bono.mentalbot.ui.theme.BubbleBot
import com.bono.mentalbot.ui.theme.PurpleLight

/**
 * Indicador animado que simula que el asistente está escribiendo.
 */
@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BubbleBot)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 600,
                            delayMillis = index * 150
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot_$index"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PurpleLight.copy(alpha = alpha))
                )
            }
        }
    }
}
app/src/main/java/com/bono/mentalbot/ui/goal/GoalScreen.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.goal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bono.mentalbot.domain.model.Goal
import com.bono.mentalbot.ui.theme.Purple
import com.bono.mentalbot.ui.theme.PurpleLight
import com.bono.mentalbot.ui.theme.TextSecondary

/**
 * Pantalla de metas donde el usuario puede ver, agregar y gestionar sus objetivos.
 *
 * @param userName Nombre del usuario usado para personalizar los consejos generados por IA.
 * @param onBack Callback para regresar a la pantalla anterior.
 * @param viewModel ViewModel que gestiona las metas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    userName: String,
    onBack: () -> Unit,
    viewModel: GoalViewModel = viewModel()
) {
    val goals by viewModel.goals.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val error by viewModel.error.collectAsState()
    val alpha = remember { Animatable(0f) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        alpha.animateTo(targetValue = 1f, animationSpec = tween(800))
    }

    if (showAddDialog) {
        AddGoalDialog(
            isGenerating = isGenerating,
            onDismiss = { showAddDialog = false },
            onSave = { title, description ->
                viewModel.addGoal(title, description, userName)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis metas",
                        color = MaterialTheme.colorScheme.onBackground
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Purple
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nueva meta",
                    tint = Color.White
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .alpha(alpha.value)
                .padding(16.dp)
        ) {
            error?.let {
                Text(text = it, color = Color.Red, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (goals.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🎯", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tienes metas aún",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toca + para agregar una nueva meta",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                val pending = goals.filter { !it.isCompleted }
                val completed = goals.filter { it.isCompleted }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    if (pending.isNotEmpty()) {
                        item {
                            Text(
                                text = "En progreso",
                                color = PurpleLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(pending) { goal ->
                            GoalCard(
                                goal = goal,
                                onToggle = { viewModel.toggleCompleted(goal.id, goal.isCompleted) },
                                onDelete = { viewModel.deleteGoal(goal.id) }
                            )
                        }
                    }

                    if (completed.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Completadas ✅",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(completed) { goal ->
                            GoalCard(
                                goal = goal,
                                onToggle = { viewModel.toggleCompleted(goal.id, goal.isCompleted) },
                                onDelete = { viewModel.deleteGoal(goal.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente que muestra una meta con acciones para completarla o eliminarla.
 *
 * @param goal Meta a mostrar.
 * @param onToggle Callback para alternar el estado completado.
 * @param onDelete Callback para eliminar la meta.
 */
@Composable
fun GoalCard(
    goal: Goal,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAdvice by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar meta", color = MaterialTheme.colorScheme.onBackground) },
            text = { Text("¿Quieres eliminar \"${goal.title}\"?", color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = Purple)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (goal.isCompleted)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onToggle) {
                        Icon(
                            imageVector = if (goal.isCompleted)
                                Icons.Filled.CheckCircle
                            else
                                Icons.Outlined.CheckCircle,
                            contentDescription = "Completar",
                            tint = if (goal.isCompleted) Purple else TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = goal.title,
                        color = if (goal.isCompleted)
                            TextSecondary
                        else
                            MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (goal.isCompleted)
                            TextDecoration.LineThrough
                        else
                            TextDecoration.None
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = TextSecondary
                    )
                }
            }

            if (goal.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = goal.description,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (goal.aiAdvice.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { showAdvice = !showAdvice },
                    modifier = Modifier.padding(start = 0.dp)
                ) {
                    Text(
                        text = if (showAdvice) "Ocultar consejo IA ▲" else "Ver consejo IA ✨ ▼",
                        color = PurpleLight,
                        fontSize = 12.sp
                    )
                }
                if (showAdvice) {
                    Text(
                        text = goal.aiAdvice,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .background(
                                Purple.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Diálogo para crear una nueva meta.
 *
 * @param isGenerating Indica si se está generando el consejo de IA.
 * @param onDismiss Callback para cerrar el diálogo sin guardar.
 * @param onSave Callback con el título y descripción cuando se guarda la meta.
 */
@Composable
fun AddGoalDialog(
    isGenerating: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Nueva meta 🎯",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "La IA generará un consejo personalizado para ayudarte a lograrla.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("¿Cuál es tu meta?", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        cursorColor = Purple
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    placeholder = { Text("Ej: Dormir mejor, reducir ansiedad...", color = TextSecondary, fontSize = 12.sp) }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        cursorColor = Purple
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onSave(title, description) },
                enabled = title.isNotBlank() && !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = PurpleLight)
            }
        }
    )
}
app/src/main/java/com/bono/mentalbot/ui/goal/GoalViewModel.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bono.mentalbot.data.remote.api.GroqApiService
import com.bono.mentalbot.data.remote.firebase.FirestoreService
import com.bono.mentalbot.data.remote.model.ChatRequest
import com.bono.mentalbot.data.remote.model.MessageDto
import com.bono.mentalbot.domain.model.Goal
import com.bono.mentalbot.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GoalViewModel : ViewModel() {

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.GROQ_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
        )
        .build()

    private val api = retrofit.create(GroqApiService::class.java)
    private val firestoreService = FirestoreService()

    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    val goals: StateFlow<List<Goal>> = _goals

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            firestoreService.getGoals().collect {
                _goals.value = it
            }
        }
    }

    fun addGoal(title: String, description: String, userName: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            _error.value = null
            try {
                val prompt = "El usuario se llama $userName y tiene esta meta de bienestar personal: \"$title\". " +
                        "Descripción adicional: \"$description\". " +
                        "Genera un consejo motivador y 3 pasos concretos para ayudarle a lograr esta meta. " +
                        "Responde SOLO en este formato:\n" +
                        "CONSEJO: [consejo motivador en 1 oración]\n" +
                        "PASOS: [paso 1]; [paso 2]; [paso 3]\n" +
                        "Responde en español, de forma cálida y empática."

                val response = api.sendMessage(
                    apiKey = Constants.GROQ_API_KEY,
                    request = ChatRequest(
                        model = Constants.MODEL,
                        messages = listOf(MessageDto(role = "user", content = prompt))
                    )
                )

                val aiAdvice = response.choices[0].message.content
                val goal = Goal(
                    title = title,
                    description = description,
                    aiAdvice = aiAdvice
                )
                firestoreService.saveGoal(goal)

            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun toggleCompleted(id: String, current: Boolean) {
        viewModelScope.launch {
            try {
                firestoreService.updateGoalCompleted(id, !current)
            } catch (e: Exception) {
                _error.value = "Error al actualizar meta."
            }
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            try {
                firestoreService.deleteGoal(id)
            } catch (e: Exception) {
                _error.value = "Error al eliminar meta."
            }
        }
    }
}
app/src/main/java/com/bono/mentalbot/ui/history/HistoryScreen.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bono.mentalbot.domain.model.Message
import com.bono.mentalbot.ui.theme.BubbleUser
import com.bono.mentalbot.ui.theme.Purple
import com.bono.mentalbot.utils.toFormattedDate

/**
 * Pantalla que muestra el historial completo de mensajes entre el usuario y MindBot.
 *
 * Permite borrar todo el historial.
 *
 * @param isDarkTheme Indica si el tema actual es oscuro.
 * @param onToggleTheme Callback para alternar el tema.
 * @param onBack Callback para volver a la pantalla anterior.
 * @param viewModel ViewModel que gestiona el historial.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Dialogo de confirmacion
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Limpiar historial",
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que quieres borrar todos los mensajes?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearMessages()
                    showDeleteDialog = false
                }) {
                    Text(text = "Borrar", color = androidx.compose.ui.graphics.Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "Cancelar", color = Purple)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Historial",
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
            actions = {
                IconButton(onClick = onToggleTheme) {
                    Text(
                        text = if (isDarkTheme) "☀️" else "🌙",
                        fontSize = 18.sp
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Limpiar historial",
                        tint = Purple
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "💬", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay conversaciones aún",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    HistoryMessageItem(message = message)
                }
            }
        }
    }
}

/**
 * Componente que representa un mensaje dentro del historial.
 *
 * @param message Mensaje a mostrar.
 */
@Composable
fun HistoryMessageItem(message: Message) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
            ) {
                if (!isUser) {
                    Text(text = "🧠", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = if (isUser) "Tú" else "MindBot",
                    color = if (isUser) Purple else MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (isUser) BubbleUser
                        else MaterialTheme.colorScheme.surface
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.content,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            }

            Text(
                text = message.timestamp.toFormattedDate(),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
app/src/main/java/com/bono/mentalbot/ui/history/HistoryViewModel.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bono.mentalbot.data.remote.api.GroqApiService
import com.bono.mentalbot.data.remote.firebase.FirestoreService
import com.bono.mentalbot.data.repository.ChatRepository
import com.bono.mentalbot.domain.model.Message
import com.bono.mentalbot.domain.usecase.GetChatHistoryUseCase
import com.bono.mentalbot.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HistoryViewModel : ViewModel() {

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.GROQ_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
        )
        .build()

    private val api = retrofit.create(GroqApiService::class.java)
    private val firestoreService = FirestoreService()
    private val repository = ChatRepository(api, firestoreService)
    private val getChatHistoryUseCase = GetChatHistoryUseCase(repository)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            getChatHistoryUseCase().collect { messages ->
                _messages.value = messages
            }
        }
    }

    fun clearMessages() {
        viewModelScope.launch {
            try {
                repository.clearMessages()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
app/src/main/java/com/bono/mentalbot/ui/mood/MoodScreen.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
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
app/src/main/java/com/bono/mentalbot/ui/mood/MoodViewModel.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
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
app/src/main/java/com/bono/mentalbot/ui/navigation/NavGraph.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bono.mentalbot.ui.auth.AuthScreen
import com.bono.mentalbot.ui.auth.AuthViewModel
import com.bono.mentalbot.ui.auth.NameScreen
import com.bono.mentalbot.ui.chat.ChatScreen
import com.bono.mentalbot.ui.goal.GoalScreen
import com.bono.mentalbot.ui.history.HistoryScreen
import com.bono.mentalbot.ui.mood.MoodScreen
import com.bono.mentalbot.ui.technique.TechniqueScreen
import com.bono.mentalbot.ui.wellbeing.WellbeingHomeScreen
import com.bono.mentalbot.ui.wellbeing.WellbeingScreen

/**
 * Define el grafo de navegación de la aplicación.
 *
 * Contiene todas las rutas y parámetros necesarios para moverse entre pantallas.
 *
 * @param isDarkTheme Estado actual del tema para pasarlo a las pantallas que lo usan.
 * @param onToggleTheme Callback para alternar el tema en las pantallas que lo permitan.
 */
@Composable
fun NavGraph(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.factory(context)
    )
    val userName by authViewModel.userName.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        composable("auth") {
            AuthScreen(
                onAuthSuccess = { isNewUser ->
                    if (isNewUser) {
                        navController.navigate("name") {
                            popUpTo("auth") { inclusive = true }
                        }
                    } else {
                        authViewModel.loadUserName()
                        navController.navigate("mood") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable("name") {
            NameScreen(
                onNameSaved = { name ->
                    authViewModel.saveUserName(name)
                    navController.navigate("mood") {
                        popUpTo("name") { inclusive = true }
                    }
                }
            )
        }

        composable("mood") {
            MoodScreen(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                userName = userName,
                onContinue = { mood ->
                    navController.navigate("wellbeinghome/$mood")
                }
            )
        }

        composable("wellbeinghome/{mood}") { backStackEntry ->
            val mood = backStackEntry.arguments?.getString("mood") ?: "neutral"
            WellbeingHomeScreen(
                userName = userName,
                mood = mood,
                onGoToChat = {
                    navController.navigate("chat/$mood/${Uri.encode("sin evaluación")}")
                },
                onGoToEvaluation = {
                    navController.navigate("wellbeing/$mood")
                },
                onGoToTechniques = {
                    navController.navigate("techniques/$mood")
                },
                onGoToGoals = {
                    navController.navigate("goals")
                },
                onChangeMood = {
                    navController.navigate("mood") {
                        popUpTo("wellbeinghome/$mood") { inclusive = true }
                    }
                }
            )
        }

        composable("wellbeing/{mood}") { backStackEntry ->
            val mood = backStackEntry.arguments?.getString("mood") ?: "neutral"
            WellbeingScreen(
                userName = userName,
                mood = mood,
                onBack = { navController.popBackStack() },
                onContinue = { wellbeingContext ->
                    navController.navigate("chat/$mood/${Uri.encode(wellbeingContext)}")
                }
            )
        }

        composable(
            route = "chat/{mood}/{wellbeingContext}",
            arguments = listOf(
                navArgument("mood") { type = NavType.StringType },
                navArgument("wellbeingContext") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mood = backStackEntry.arguments?.getString("mood") ?: "neutral"
            val wellbeingContext = Uri.decode(
                backStackEntry.arguments?.getString("wellbeingContext") ?: ""
            )
            ChatScreen(
                mood = mood,
                userName = userName,
                wellbeingContext = wellbeingContext,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onBack = { navController.popBackStack() },
                onHistoryClick = { navController.navigate("history") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("history") {
            HistoryScreen(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onBack = { navController.popBackStack() }
            )
        }

        composable("techniques/{mood}") { backStackEntry ->
            val mood = backStackEntry.arguments?.getString("mood") ?: "neutral"
            TechniqueScreen(
                mood = mood,
                userName = userName,
                onBack = { navController.popBackStack() }
            )
        }

        composable("goals") {
            GoalScreen(
                userName = userName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
app/src/main/java/com/bono/mentalbot/ui/technique/TechniqueScreen.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.technique

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bono.mentalbot.domain.model.Technique
import com.bono.mentalbot.ui.theme.Purple
import com.bono.mentalbot.ui.theme.PurpleLight
import com.bono.mentalbot.ui.theme.TextSecondary

/**
 * Pantalla de técnicas de bienestar donde el usuario puede generar o registrar técnicas.
 *
 * @param mood Estado de ánimo actual para personalizar las sugerencias.
 * @param userName Nombre del usuario (se puede usar en prompts de IA).
 * @param onBack Callback para volver a la pantalla anterior.
 * @param viewModel ViewModel que gestiona el listado y generación de técnicas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechniqueScreen(
    mood: String,
    userName: String,
    onBack: () -> Unit,
    viewModel: TechniqueViewModel = viewModel()
) {
    val techniques by viewModel.techniques.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val error by viewModel.error.collectAsState()
    val alpha = remember { Animatable(0f) }

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        alpha.animateTo(targetValue = 1f, animationSpec = tween(800))
    }

    if (showAddDialog) {
        AddTechniqueDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, description, category ->
                viewModel.saveTechnique(title, description, category, mood)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Técnicas de bienestar",
                        color = MaterialTheme.colorScheme.onBackground
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Purple
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar técnica",
                    tint = Color.White
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .alpha(alpha.value)
                .padding(16.dp)
        ) {
            // Botón generar con IA
            Button(
                onClick = { viewModel.generateTechnique(mood, userName) },
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generando técnica...", color = Color.White)
                } else {
                    Text(
                        text = "✨ Generar técnica con IA",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "La IA sugerirá una técnica según tu estado de ánimo actual ($mood)",
                color = TextSecondary,
                fontSize = 12.sp
            )

            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (techniques.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🌿", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tienes técnicas guardadas",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Genera una con IA o agrégala manualmente",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(techniques) { technique ->
                        TechniqueCard(
                            technique = technique,
                            onDelete = { viewModel.deleteTechnique(technique.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente que muestra una técnica y permite eliminarla.
 *
 * @param technique Técnica a mostrar.
 * @param onDelete Callback al pulsar eliminar.
 */
@Composable
fun TechniqueCard(
    technique: Technique,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar técnica", color = MaterialTheme.colorScheme.onBackground) },
            text = { Text("¿Quieres eliminar \"${technique.title}\"?", color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = Purple)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Categoria badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Purple.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = technique.category,
                            color = PurpleLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = technique.title,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = technique.description,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Para cuando te sientes: ${technique.mood}",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Diálogo para agregar una técnica de forma manual.
 *
 * @param onDismiss Callback para cerrar el diálogo sin guardar.
 * @param onSave Callback con título, descripción y categoría cuando el usuario guarda.
 */
@Composable
fun AddTechniqueDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Mindfulness") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Nueva técnica",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre de la técnica", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        cursorColor = Purple
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción y pasos", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        cursorColor = Purple
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 5
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        cursorColor = Purple
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    placeholder = { Text("Respiración, Mindfulness, Grounding...", color = TextSecondary, fontSize = 12.sp) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank() && description.isNotBlank()) onSave(title, description, category) },
                enabled = title.isNotBlank() && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = PurpleLight)
            }
        }
    )
}
app/src/main/java/com/bono/mentalbot/ui/technique/TechniqueViewModel.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.technique

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bono.mentalbot.data.remote.api.GroqApiService
import com.bono.mentalbot.data.remote.firebase.FirestoreService
import com.bono.mentalbot.data.remote.model.ChatRequest
import com.bono.mentalbot.data.remote.model.MessageDto
import com.bono.mentalbot.domain.model.Technique
import com.bono.mentalbot.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ViewModel encargado de gestionar las técnicas de bienestar.
 *
 * - Carga técnicas guardadas desde Firestore.
 * - Genera nuevas técnicas usando la API de Groq (modelo de lenguaje).
 * - Permite guardar y borrar técnicas en Firestore.
 */
class TechniqueViewModel : ViewModel() {

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.GROQ_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
        )
        .build()

    private val api = retrofit.create(GroqApiService::class.java)
    private val firestoreService = FirestoreService()

    private val _techniques = MutableStateFlow<List<Technique>>(emptyList())
    val techniques: StateFlow<List<Technique>> = _techniques

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadTechniques()
    }

    /**
     * Carga las técnicas almacenadas en Firestore y las expone en [techniques].
     */
    private fun loadTechniques() {
        viewModelScope.launch {
            firestoreService.getTechniques().collect {
                _techniques.value = it
            }
        }
    }

    /**
     * Genera una técnica de bienestar usando el modelo de lenguaje (Groq/OpenAI).
     *
     * @param mood Estado de ánimo actual del usuario, usado para contextualizar la técnica.
     * @param userName Nombre del usuario (actualmente no se usa en el prompt, pero se puede extender).
     */
    fun generateTechnique(mood: String, userName: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            _error.value = null
            try {
                val prompt = "Sugiere una técnica de bienestar mental o relajación específica para alguien que se siente $mood. " +
                        "Responde SOLO en este formato exacto:\n" +
                        "TITULO: [nombre corto de la técnica]\n" +
                        "CATEGORIA: [Respiración/Mindfulness/Movimiento/Visualización/Grounding]\n" +
                        "DESCRIPCION: [descripción breve de máximo 2 oraciones]\n" +
                        "PASOS: [3 pasos simples separados por punto y coma]\n" +
                        "Responde en español."

                val response = api.sendMessage(
                    apiKey = Constants.GROQ_API_KEY,
                    request = ChatRequest(
                        model = Constants.MODEL,
                        messages = listOf(
                            MessageDto(role = "user", content = prompt)
                        )
                    )
                )

                val raw = response.choices[0].message.content
                val technique = parseTechnique(raw, mood)
                firestoreService.saveTechnique(technique)

            } catch (e: Exception) {
                if (e is retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    _error.value = "Error HTTP: $errorBody"
                } else {
                    _error.value = "Error: ${e.message}"
                }
            } finally {
                _isGenerating.value = false
            }
        }
    }

    /**
     * Guarda una técnica creada manualmente por el usuario en Firestore.
     *
     * @param title Título de la técnica.
     * @param description Descripción de la técnica.
     * @param category Categoría de la técnica (ej. Respiración, Mindfulness).
     * @param mood Estado de ánimo asociado.
     */
    fun saveTechnique(title: String, description: String, category: String, mood: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val technique = Technique(
                    title = title,
                    description = description,
                    category = category,
                    mood = mood,
                    aiSuggestion = ""
                )
                firestoreService.saveTechnique(technique)
            } catch (e: Exception) {
                _error.value = "Error al guardar técnica."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina una técnica identificada por su ID.
     */
    fun deleteTechnique(id: String) {
        viewModelScope.launch {
            try {
                firestoreService.deleteTechnique(id)
            } catch (e: Exception) {
                _error.value = "Error al eliminar técnica."
            }
        }
    }

    /**
     * Parsea el texto crudo retornado por el modelo para extraer los campos de técnica.
     *
     * El texto debe seguir un formato específico con prefijos como "TITULO:",
     * "CATEGORIA:", "DESCRIPCION:" y "PASOS:".
     */
    private fun parseTechnique(raw: String, mood: String): Technique {
        val lines = raw.lines()
        var title = "Técnica de bienestar"
        var category = "Mindfulness"
        var description = raw
        var pasos = ""

        lines.forEach { line ->
            when {
                line.startsWith("TITULO:") -> title = line.removePrefix("TITULO:").trim()
                line.startsWith("CATEGORIA:") -> category = line.removePrefix("CATEGORIA:").trim()
                line.startsWith("DESCRIPCION:") -> description = line.removePrefix("DESCRIPCION:").trim()
                line.startsWith("PASOS:") -> pasos = line.removePrefix("PASOS:").trim()
            }
        }

        val fullDescription = if (pasos.isNotEmpty()) "$description\n\nPasos: $pasos" else description

        return Technique(
            title = title,
            description = fullDescription,
            category = category,
            mood = mood,
            aiSuggestion = raw
        )
    }
}
app/src/main/java/com/bono/mentalbot/ui/theme/Color.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.theme

import androidx.compose.ui.graphics.Color

// Colores principales
val Purple = Color(0xFF7C6FCD)
val PurpleLight = Color(0xFFB39DDB)
val PurpleDark = Color(0xFF4A3F8F)

// Fondos oscuros
val BackgroundDark = Color(0xFF1A1A2E)
val BackgroundMedium = Color(0xFF16213E)
val SurfaceDark = Color(0xFF0F3460)

// Burbujas chat
val BubbleUser = Color(0xFF7C6FCD)
val BubbleBot = Color(0xFF1E1E2E)

// Textos oscuros
val TextPrimary = Color(0xFFEEEEEE)
val TextSecondary = Color(0xFFAAAAAA)

// Fondos claros
val LightBackground = Color(0xFFF5F5F5)
val LightSurface = Color(0xFFFFFFFF)
val LightBubbleBot = Color(0xFFE8E8E8)

// Textos claros
val LightTextPrimary = Color(0xFF1A1A2E)
val LightTextSecondary = Color(0xFF666666)

// Moods
val MoodHappy = Color(0xFFFFD700)
val MoodSad = Color(0xFF64B5F6)
val MoodAnxious = Color(0xFFFF8A65)
val MoodCalm = Color(0xFF81C784)
val MoodAngry = Color(0xFFE57373)
val MoodTired = Color(0xFFB0BEC5)
app/src/main/java/com/bono/mentalbot/ui/theme/Theme.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Purple,
    secondary = PurpleLight,
    tertiary = PurpleDark,
    background = BackgroundDark,
    surface = BackgroundMedium,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = Purple,
    secondary = PurpleLight,
    tertiary = PurpleDark,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary
)

/**
 * Tema de la aplicación que aplica una paleta clara u oscura.
 *
 * @param isDarkTheme Si es `true` se aplica el tema oscuro, de lo contrario el tema claro.
 * @param content Composable que se renderiza con el tema aplicado.
 */
@Composable
fun MentalBotTheme(
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
app/src/main/java/com/bono/mentalbot/ui/theme/Type.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)
app/src/main/java/com/bono/mentalbot/ui/wellbeing/WellbeingHomeScreen.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.wellbeing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bono.mentalbot.ui.theme.Purple
import com.bono.mentalbot.ui.theme.PurpleLight
import com.bono.mentalbot.ui.theme.TextSecondary
import java.util.Calendar

/**
 * Devuelve un saludo según la hora del día.
 *
 * @return "Buenos días", "Buenas tardes" o "Buenas noches" según la hora actual.
 */
fun getGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Buenos días"
        in 12..17 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

/**
 * Obtiene un emoji representativo del estado de ánimo.
 *
 * @param mood Texto con el estado de ánimo (ej. "feliz", "triste").
 * @return Emoji asociado al estado proporcionado.
 */
fun getMoodEmoji(mood: String): String {
    return when (mood.lowercase()) {
        "feliz" -> "😊"
        "triste" -> "😢"
        "ansioso" -> "😰"
        "tranquilo" -> "😌"
        "enojado" -> "😠"
        "cansado" -> "😴"
        else -> "🙂"
    }
}

/**
 * Pantalla principal de bienestar que muestra un resumen y accesos directos.
 *
 * Permite al usuario navegar rápidamente al chat, evaluación emocional, técnicas y metas.
 *
 * @param userName Nombre del usuario mostrado en el saludo.
 * @param mood Estado de ánimo actual, usado en el badge.
 * @param onGoToChat Callback para navegar al chat.
 * @param onGoToEvaluation Callback para iniciar la evaluación emocional.
 * @param onGoToTechniques Callback para navegar a las técnicas.
 * @param onGoToGoals Callback para navegar a la sección de metas.
 * @param onChangeMood Callback opcional para actualizar el estado de ánimo.
 */
@Composable
fun WellbeingHomeScreen(
    userName: String,
    mood: String,
    onGoToChat: () -> Unit,
    onGoToEvaluation: () -> Unit,
    onGoToTechniques: () -> Unit,
    onGoToGoals: () -> Unit,
    onChangeMood: () -> Unit = {}
) {
    val alpha = remember { Animatable(0f) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        alpha.animateTo(targetValue = 1f, animationSpec = tween(800))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .alpha(alpha.value)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // TopBar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Purple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🧠", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MindBot",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card de bienvenida
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = getGreeting(),
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userName.ifEmpty { "usuario" },
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "👋", fontSize = 24.sp)
                        }
                    }

                    // Mood badge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(12.dp)
                    ) {
                        Text(text = getMoodEmoji(mood), fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mood,
                            color = PurpleLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Estoy aquí para escucharte y apoyarte. ¿Qué quieres hacer hoy?",
                    color = TextSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onChangeMood,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Purple.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = PurpleLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Actualizar estado de ánimo",
                        color = PurpleLight,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fila 1 — Chat y Evaluación
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Chat
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Purple, PurpleLight.copy(alpha = 0.6f))
                        )
                    )
                    .clickable { onGoToChat() }
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .align(Alignment.TopEnd)
                )
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💬", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Hablar con MindBot",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Comparte cómo te sientes, estoy aquí.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }

            // Card Evaluación
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onGoToEvaluation() }
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Purple.copy(alpha = 0.1f))
                        .align(Alignment.TopEnd)
                )
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Purple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🧘", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Evaluación Emocional",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Responde unas preguntas y obtén un análisis.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fila 2 — Técnicas y Metas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Técnicas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                PurpleLight.copy(alpha = 0.4f),
                                Purple.copy(alpha = 0.6f)
                            )
                        )
                    )
                    .clickable { onGoToTechniques() }
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .align(Alignment.TopEnd)
                )
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌿", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Técnicas",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Relajación y mindfulness.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }

            // Card Metas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onGoToGoals() }
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Purple.copy(alpha = 0.1f))
                        .align(Alignment.TopEnd)
                )
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Purple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎯", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mis metas",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Logra tus objetivos.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
app/src/main/java/com/bono/mentalbot/ui/wellbeing/WellbeingScreen.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
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

/**
 * Pantalla de evaluación emocional donde el usuario completa un breve cuestionario.
 *
 * El resultado se usa para construir un contexto que se envía al asistente antes
 * del chat para personalizar la respuesta.
 *
 * @param userName Nombre del usuario (se muestra en el saludo).
 * @param mood Estado de ánimo actual (se usa para adaptar mensajes y contexto).
 * @param onBack Callback para volver a la pantalla anterior.
 * @param onContinue Callback que recibe el contexto generado y continúa al chat.
 * @param viewModel ViewModel que almacena y actualiza los valores del formulario.
 */
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

/**
 * Encabezado de sección usado en el formulario de evaluación.
 *
 * Muestra un emoji, un título y un subtítulo descriptivo.
 *
 * @param emoji Emoji representativo de la sección.
 * @param title Título principal de la sección.
 * @param subtitle Texto secundario explicativo.
 */
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

/**
 * Componente de pregunta de escala que permite al usuario puntuar un ítem de 1 a 5.
 *
 * @param question Texto de la pregunta.
 * @param value Valor actual de la escala (1-5).
 * @param onValueChange Callback para actualizar el valor de la escala.
 */
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

/**
 * Componente de pregunta abierta con un campo de texto multi-línea.
 *
 * @param question Texto de la pregunta.
 * @param value Valor actual del campo.
 * @param onValueChange Callback que se invoca cuando cambia el texto.
 * @param placeholder Texto de ayuda mostrado cuando el campo está vacío.
 * @param minLines Mínimo de líneas visibles en el campo de texto.
 */
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

/**
 * Divider personalizado usado para separar secciones en la pantalla de bienestar.
 */
@Composable
fun SectionDivider() {
    Divider(
        color = PurpleLight.copy(alpha = 0.3f),
        thickness = 1.dp
    )
}
app/src/main/java/com/bono/mentalbot/ui/wellbeing/WellbeingViewModel.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.ui.wellbeing

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Estado de la evaluación de bienestar emocional que el usuario completa.
 *
 * Los valores están diseñados para ofrecer una foto rápida del estado emocional
 * en distintas dimensiones (Maslow, Ortega, López de Llergo).
 */
data class WellbeingData(
    // Maslow
    val necesidadesBasicas: Float = 3f,
    val seguridadVital: Float = 3f,
    val relacionesSociales: Float = 3f,
    val autoestima: Float = 3f,
    val propositoVida: Float = 3f,
    // Ortega
    val satisfaccionCircunstancias: Float = 3f,
    val controlVida: Float = 3f,
    val textoCircunstancia: String = "",
    // Lopez de Llergo
    val vivenciaValores: Float = 3f,
    val textoValores: String = "",
    val textoLibre: String = ""
)

/**
 * ViewModel que almacena y actualiza los valores de la evaluación emocional.
 *
 * Permite a la UI reaccionar automáticamente a los cambios mediante StateFlow.
 */
class WellbeingViewModel : ViewModel() {

    private val _data = MutableStateFlow(WellbeingData())
    val data: StateFlow<WellbeingData> = _data

    /**
     * Actualiza el valor de "necesidades básicas" (Maslow).
     */
    fun updateNecesidadesBasicas(value: Float) {
        _data.value = _data.value.copy(necesidadesBasicas = value)
    }

    /**
     * Actualiza el valor de "seguridad vital" (Maslow).
     */
    fun updateSeguridadVital(value: Float) {
        _data.value = _data.value.copy(seguridadVital = value)
    }

    /**
     * Actualiza el valor de "relaciones sociales" (Maslow).
     */
    fun updateRelacionesSociales(value: Float) {
        _data.value = _data.value.copy(relacionesSociales = value)
    }

    /**
     * Actualiza el valor de "autoestima" (Maslow).
     */
    fun updateAutoestima(value: Float) {
        _data.value = _data.value.copy(autoestima = value)
    }

    /**
     * Actualiza el valor de "propósito de vida" (Maslow).
     */
    fun updatePropositoVida(value: Float) {
        _data.value = _data.value.copy(propositoVida = value)
    }

    /**
     * Actualiza el valor de "satisfacción con las circunstancias" (Ortega).
     */
    fun updateSatisfaccionCircunstancias(value: Float) {
        _data.value = _data.value.copy(satisfaccionCircunstancias = value)
    }

    /**
     * Actualiza la percepción de "control sobre la vida" (Ortega).
     */
    fun updateControlVida(value: Float) {
        _data.value = _data.value.copy(controlVida = value)
    }

    /**
     * Actualiza el texto descriptivo de la circunstancia actual (Ortega).
     */
    fun updateTextoCircunstancia(value: String) {
        _data.value = _data.value.copy(textoCircunstancia = value)
    }

    /**
     * Actualiza la vivencia de los valores personales (López de Llergo).
     */
    fun updateVivenciaValores(value: Float) {
        _data.value = _data.value.copy(vivenciaValores = value)
    }

    /**
     * Actualiza el texto que describe qué valores son relevantes en este momento.
     */
    fun updateTextoValores(value: String) {
        _data.value = _data.value.copy(textoValores = value)
    }

    /**
     * Actualiza el texto libre que el usuario quiere compartir.
     */
    fun updateTextoLibre(value: String) {
        _data.value = _data.value.copy(textoLibre = value)
    }

    /**
     * Construye un bloque de texto (contexto) que resume la evaluación emocional.
     *
     * Este contexto se puede enviar al asistente para que tenga más información
     * del usuario antes de generar una respuesta.
     */
    fun buildWellbeingContext(): String {
        val d = _data.value
        return """
            EVALUACIÓN EMOCIONAL DEL USUARIO:
            
            [MASLOW - Necesidades]
            - Necesidades básicas cubiertas: ${d.necesidadesBasicas}/5
            - Sensación de seguridad vital: ${d.seguridadVital}/5
            - Calidad de relaciones sociales: ${d.relacionesSociales}/5
            - Nivel de autoestima: ${d.autoestima}/5
            - Sentido de propósito: ${d.propositoVida}/5
            
            [ORTEGA - Circunstancias vitales]
            - Satisfacción con sus circunstancias actuales: ${d.satisfaccionCircunstancias}/5
            - Sensación de control sobre su vida: ${d.controlVida}/5
            - El usuario describe su situación así: "${d.textoCircunstancia}"
            
            [LÓPEZ DE LLERGO - Valores]
            - Vivencia de sus valores personales: ${d.vivenciaValores}/5
            - Lo que más valora en este momento: "${d.textoValores}"
            
            [REFLEXIÓN LIBRE]
            - El usuario quiere que sepas: "${d.textoLibre}"
            
            Usa toda esta información para personalizar tu respuesta de forma empática y profunda.
        """.trimIndent()
    }
}
app/src/main/java/com/bono/mentalbot/utils/Constants.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.utils

import com.bono.mentalbot.BuildConfig

/**
 * Constantes globales usadas en la comunicación con la API de Groq/OpenAI y en la
 * configuración inicial del asistente conversacional.
 *
 * Estas constantes se usan para construir solicitudes al modelo de lenguaje y para
 * generar mensajes iniciales que personalizan la experiencia según el usuario.
 */
object Constants {

    /**
     * URL base para las llamadas a la API de Groq/OpenAI.
     */
    const val GROQ_BASE_URL = "https://api.groq.com/openai/v1/"

    /**
     * Clave API (Bearer) usada para autenticar las peticiones.
     *
     * Importante: el valor se construye con un espacio después de "Bearer".
     */
    val GROQ_API_KEY = "Bearer ${BuildConfig.GROQ_API_KEY}"

    /**
     * Identificador del modelo de lenguaje utilizado para generar respuestas.
     */
    const val MODEL = "llama-3.1-8b-instant"

    /**
     * Genera el prompt del sistema que se envía al modelo de lenguaje.
     *
     * El prompt define el rol de MindBot, su estilo de respuesta y las reglas de
     * seguridad que debe seguir (evitar diagnósticos, priorizar la seguridad, etc.).
     *
     * @param userName Nombre del usuario para personalizar el saludo y las instrucciones.
     * @param mood Estado emocional actual del usuario para adaptar la sugerencia de tono.
     * @return Texto completo que se incluirá en la petición al modelo como "system" prompt.
     */
    fun getSystemPrompt(userName: String, mood: String): String {
        return """
Eres MindBot, un asistente de apoyo emocional empático y seguro.

El usuario se llama $userName. Dirígete a él siempre por su nombre de forma natural, cálida y cercana, sin sonar repetitivo.

El estado emocional actual del usuario es: $mood. Tenlo en cuenta, pero permite que cambie durante la conversación.

Tu estilo de respuesta debe ser:
- Calmado, validante y sin juicios.
- Breve y claro (máximo 5 oraciones).
- En español.
- Conversacional y humano, nunca robótico.
- Centrado en escuchar, razonar y pensar en lo mejor para el usuario antes de aconsejar.

Normas importantes:
- No diagnosticas ni haces afirmaciones clínicas.
- No reemplazas a un profesional de salud mental.
- Ofreces apoyo emocional, técnicas simples (respiración, grounding, mindfulness) o preguntas suaves cuando sea apropiado.
- No minimizas emociones ni usas frases vacías como "todo estará bien".
- Si detectas desesperanza intensa, autolesión o ideas suicidas, responde con empatía y recomienda buscar ayuda profesional o contactar un servicio de emergencia local de inmediato.
- En situaciones de riesgo, prioriza la seguridad por encima de la brevedad.

Tu objetivo es que $userName se sienta escuchado, comprendido y acompañado.
""".trimIndent()
    }

    /**
     * Genera el mensaje inicial que se muestra cuando el usuario comienza la interacción.
     *
     * Este mensaje varía según el estado de ánimo reportado y si hay un contexto de
     * bienestar disponible (por ejemplo, después de una evaluación de bienestar).
     *
     * @param userName Nombre del usuario para personalizar el saludo.
     * @param mood Estado emocional actual (ej. "triste", "ansioso").
     * @param hasWellbeingContext Indica si se dispone de contexto adicional de bienestar.
     * @return Mensaje de bienvenida personalizado para el usuario.
     */
    fun getInitialMessage(
        userName: String,
        mood: String,
        hasWellbeingContext: Boolean = false
    ): String {
        val name = if (userName.isNotEmpty()) ", $userName" else ""

        return if (hasWellbeingContext) {
            when (mood.lowercase()) {
                "triste" -> "Hola$name 💙 Gracias por compartir cómo te sientes y por completar la evaluación. He podido conocerte un poco mejor y quiero que sepas que estoy aquí para ti. Noto que hoy estás triste... ¿quieres contarme qué está pasando?"
                "ansioso" -> "Hola$name 🌿 Gracias por tomarte el tiempo de completar la evaluación, eso dice mucho de ti. He notado aspectos importantes de tu situación actual y quiero ayudarte. Sé que hoy te sientes ansioso... respira profundo, estoy aquí contigo. ¿Qué te tiene preocupado?"
                "enojado" -> "Hola$name 🔴 Gracias por compartir cómo estás a través de la evaluación. Entiendo que hoy estás enojado y es completamente válido sentirte así. Con lo que me has contado, puedo entender mejor tu situación. ¿Quieres hablar sobre lo que pasó?"
                "cansado" -> "Hola$name 🌙 Gracias por completar la evaluación, sé que cuando uno está cansado hacer cualquier cosa requiere esfuerzo. He visto tu situación actual y quiero apoyarte. ¿Ha sido un período muy pesado para ti?"
                "feliz" -> "Hola$name 🌟 ¡Qué bueno verte por aquí! Gracias por compartir tu evaluación, me ayuda a conocerte mejor. Me alegra mucho que hoy te sientas feliz. ¿Qué es lo que te tiene con esa energía tan bonita hoy?"
                "tranquilo" -> "Hola$name 🍃 Gracias por completar la evaluación, es un gesto de autocuidado hermoso. He podido conocer un poco más sobre ti y tu momento actual. Me alegra que hoy estés tranquilo. ¿Cómo te gustaría aprovechar esta calma?"
                else -> "Hola$name 👋 Gracias por completar la evaluación emocional, eso me permite conocerte mejor y darte un apoyo más personalizado. ¿Cómo te sientes en este momento?"
            }
        } else {
            when (mood.lowercase()) {
                "triste" -> "Hola$name 💙 He notado que hoy te sientes triste. ¿Quieres contarme qué ha pasado? Estoy aquí para escucharte."
                "ansioso" -> "Hola$name 🌿 Veo que hoy te sientes ansioso. Respira profundo... estoy aquí contigo. ¿Qué te tiene preocupado?"
                "enojado" -> "Hola$name 🔴 Entiendo que hoy estás enojado. Es completamente válido sentirte así. ¿Qué fue lo que pasó?"
                "cansado" -> "Hola$name 🌙 Parece que hoy estás muy cansado. El descanso es importante. ¿Ha sido un día muy pesado?"
                "feliz" -> "Hola$name 🌟 ¡Me alegra mucho que hoy te sientas feliz! ¿Qué te tiene con esa energía tan bonita?"
                "tranquilo" -> "Hola$name 🍃 Qué bien que hoy te sientes tranquilo. ¿Cómo te gustaría aprovechar esta calma hoy?"
                else -> "Hola$name 👋 ¿Cómo estás hoy? Cuéntame, estoy aquí para escucharte."
            }
        }
    }
}
app/src/main/java/com/bono/mentalbot/utils/Extensions.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Convierte un timestamp (milisegundos desde epoch) a una cadena con formato legible.
 *
 * @receiver Timestamp en milisegundos.
 * @return Fecha y hora formateadas en el formato "dd/MM/yyyy HH:mm".
 */
fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Capitaliza la primera letra de la cadena según la configuración regional actual.
 *
 * @receiver Cadena de texto a capitalizar.
 * @return La misma cadena con la primera letra en mayúscula (si es minúscula).
 */
fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}
app/src/test/java/com/bono/mentalbot/ExampleUnitTest.kt
Explicación: Este archivo forma parte del proyecto. A continuación se muestra el código completo. Se recomienda copiarlo exactamente en la misma ruta dentro del proyecto Android Studio. Lee el código primero y luego revisa los comentarios internos, ya que explican qué hace cada clase, función y componente.
package com.bono.mentalbot

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
