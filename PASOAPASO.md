# Manual Completo para Reconstruir la Aplicación MentalBot

Este documento explica paso a paso cómo reconstruir la aplicación **MentalBot** usando Android Studio, Kotlin, Jetpack Compose, Clean Architecture y Firebase. Cada sección incluye los archivos completos necesarios para replicar el proyecto.

---

## 1. Crear el proyecto

Abrir **Android Studio → New Project → Empty Activity**.

**Configuración:**
- Nombre: `MentalBot`
- Lenguaje: `Kotlin`
- Minimum SDK: `24`
- UI: `Jetpack Compose`

---

## 2. Estructura del proyecto

Crear los siguientes paquetes dentro de `com.bono.mentalbot`:

- `data`
- `domain`
- `ui`
- `utils`

---

## 3. Archivos del proyecto

A continuación se incluyen todos los archivos Kotlin del proyecto. Copiar cada archivo en la misma ruta dentro del proyecto Android.

---

### `app/src/androidTest/java/com/bono/mentalbot/ExampleInstrumentedTest.kt`

> Prueba instrumentada que se ejecuta en un dispositivo Android real.

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/MainActivity.kt`

> Actividad principal. Configura la UI con Jetpack Compose y controla el modo claro/oscuro mediante un estado local (`isDarkTheme`).

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/data/remote/api/GroqApiService.kt`

> Interface Retrofit para comunicarse con la API de Groq/OpenAI.

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/data/remote/firebase/FirestoreService.kt`

> Servicio para interactuar con Firebase Firestore. Maneja mensajería, técnicas, metas y almacenamiento de información del usuario.

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/data/remote/model/ChatRequest.kt`

> DTOs para construir el payload enviado al endpoint de chat completions.

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/data/remote/model/ChatResponse.kt`

> DTOs para deserializar la respuesta del endpoint de chat completions.

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/data/repository/ChatRepository.kt`

> Repositorio que orquesta la lógica de conversación entre la app y los servicios externos (Groq API + Firestore).

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/domain/model/Goal.kt`

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/domain/model/Message.kt`

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/domain/model/Technique.kt`

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/domain/usecase/GetChatHistoryUseCase.kt`

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/domain/usecase/SendMessageUsecase.kt`

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/ui/auth/AuthScreen.kt`

> Pantalla de autenticación que permite iniciar sesión o registrarse.

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/ui/auth/AuthViewModel.kt`

> ViewModel encargado de la autenticación de usuarios con Firebase Auth.

```kotlin
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

    fun toggleRememberSession() {
        val newValue = !_rememberSession.value
        _rememberSession.value = newValue
        prefs.edit().putBoolean("remember_session", newValue).apply()
    }

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

    fun logout() {
        auth.signOut()
        _isAuthenticated.value = false
        _isNewUser.value = false
        _userName.value = ""
        _rememberSession.value = false
        prefs.edit().putBoolean("remember_session", false).apply()
    }

    fun saveUserName(name: String) {
        viewModelScope.launch {
            firestoreService.saveUserName(name)
            _userName.value = name
        }
    }

    fun loadUserName() {
        viewModelScope.launch {
            _userName.value = firestoreService.getUserName()
        }
    }

    companion object {
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
```

---

### `app/src/main/java/com/bono/mentalbot/ui/auth/NameScreen.kt`

> Pantalla para solicitar el nombre del usuario al iniciar por primera vez.

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/ui/chat/ChatScreen.kt`

> Pantalla principal del chat con MindBot. Muestra el historial de mensajes, permite enviar texto y ofrece opciones de navegación.

```kotlin
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
                    Text(text = if (isDarkTheme) "☀️" else "🌙", fontSize = 18.sp)
                }
                IconButton(onClick = onHistoryClick) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Historial", tint = Purple)
                }
                IconButton(onClick = onLogout) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Cerrar sesión", tint = Purple)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
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
```

---

### `app/src/main/java/com/bono/mentalbot/ui/chat/ChatViewModel.kt`

> ViewModel responsable de la lógica del chat con MindBot.

```kotlin
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

    private fun loadMessages() {
        viewModelScope.launch {
            getChatHistoryUseCase().collect { messages ->
                _messages.value = messages
            }
        }
    }
}
```

---

### `app/src/main/java/com/bono/mentalbot/ui/chat/components/InputBar.kt`

> Barra de entrada de texto para el chat con botón de envío.

```kotlin
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
                placeholder = { Text("Escribe algo...", color = TextSecondary) },
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
```

---

### `app/src/main/java/com/bono/mentalbot/ui/chat/components/MessageBubble.kt`

> Muestra un mensaje en forma de burbuja de chat.

```kotlin
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
                Text(text = message.content, color = TextPrimary, fontSize = 15.sp)
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
```

---

### `app/src/main/java/com/bono/mentalbot/ui/chat/components/TypingIndicator.kt`

> Indicador animado que simula que el asistente está escribiendo.

```kotlin
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
                        animation = tween(durationMillis = 600, delayMillis = index * 150),
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
```

---

### `app/src/main/java/com/bono/mentalbot/ui/goal/GoalScreen.kt`

> Pantalla de metas donde el usuario puede ver, agregar y gestionar sus objetivos de bienestar.

```kotlin
package com.bono.mentalbot.ui.goal

// ... (ver código completo en el proyecto fuente)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    userName: String,
    onBack: () -> Unit,
    viewModel: GoalViewModel = viewModel()
) { /* ... */ }

@Composable
fun GoalCard(goal: Goal, onToggle: () -> Unit, onDelete: () -> Unit) { /* ... */ }

@Composable
fun AddGoalDialog(isGenerating: Boolean, onDismiss: () -> Unit, onSave: (String, String) -> Unit) { /* ... */ }
```

> ⚠️ Este archivo es extenso. Copiar el código completo desde el apartado **GoalScreen.kt** de este manual.

---

### `app/src/main/java/com/bono/mentalbot/ui/goal/GoalViewModel.kt`

> ViewModel que gestiona la creación, actualización y eliminación de metas con apoyo de IA.

```kotlin
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

    // ... Retrofit + Firestore setup ...

    fun addGoal(title: String, description: String, userName: String) { /* genera consejo IA y guarda en Firestore */ }
    fun toggleCompleted(id: String, current: Boolean) { /* actualiza isCompleted */ }
    fun deleteGoal(id: String) { /* elimina por ID */ }
}
```

---

### `app/src/main/java/com/bono/mentalbot/ui/history/HistoryScreen.kt`

> Muestra el historial completo de mensajes entre el usuario y MindBot. Permite borrar todo el historial.

```kotlin
package com.bono.mentalbot.ui.history

// Pantalla con TopAppBar, LazyColumn de mensajes y diálogo de confirmación para borrar historial.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) { /* ... */ }

@Composable
fun HistoryMessageItem(message: Message) { /* burbuja de historial */ }
```

---

### `app/src/main/java/com/bono/mentalbot/ui/history/HistoryViewModel.kt`

```kotlin
package com.bono.mentalbot.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bono.mentalbot.data.repository.ChatRepository
import com.bono.mentalbot.domain.model.Message
import com.bono.mentalbot.domain.usecase.GetChatHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {

    // ... setup Retrofit + Firestore + Repository ...

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    init { loadMessages() }

    private fun loadMessages() { /* colecta historial desde Firestore */ }

    fun clearMessages() { /* borra todos los mensajes */ }
}
```

---

### `app/src/main/java/com/bono/mentalbot/ui/mood/MoodScreen.kt`

> Pantalla donde el usuario selecciona su estado de ánimo actual antes de ingresar al chat.

```kotlin
package com.bono.mentalbot.ui.mood

data class MoodOption(val label: String, val emoji: String, val color: Color)

val moodOptions = listOf(
    MoodOption("Feliz", "😊", MoodHappy),
    MoodOption("Triste", "😢", MoodSad),
    MoodOption("Ansioso", "😰", MoodAnxious),
    MoodOption("Tranquilo", "😌", MoodCalm),
    MoodOption("Enojado", "😠", MoodAngry),
    MoodOption("Cansado", "😴", MoodTired)
)

@Composable
fun MoodScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    userName: String,
    onContinue: (String) -> Unit,
    viewModel: MoodViewModel = viewModel()
) { /* Grid de MoodCards + botón Continuar */ }

@Composable
fun MoodCard(mood: MoodOption, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) { /* ... */ }
```

---

### `app/src/main/java/com/bono/mentalbot/ui/mood/MoodViewModel.kt`

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/ui/navigation/NavGraph.kt`

> Define el grafo de navegación completo de la aplicación.

**Rutas disponibles:**

| Ruta | Destino |
|------|---------|
| `auth` | Pantalla de autenticación |
| `name` | Captura de nombre (nuevos usuarios) |
| `mood` | Selección de estado de ánimo |
| `wellbeinghome/{mood}` | Hub principal de bienestar |
| `wellbeing/{mood}` | Evaluación emocional |
| `chat/{mood}/{wellbeingContext}` | Chat con MindBot |
| `history` | Historial de mensajes |
| `techniques/{mood}` | Técnicas de bienestar |
| `goals` | Gestión de metas |

```kotlin
package com.bono.mentalbot.ui.navigation

@Composable
fun NavGraph(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    // ... AuthViewModel compartido entre pantallas ...

    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") { /* AuthScreen */ }
        composable("name") { /* NameScreen */ }
        composable("mood") { /* MoodScreen */ }
        composable("wellbeinghome/{mood}") { /* WellbeingHomeScreen */ }
        composable("wellbeing/{mood}") { /* WellbeingScreen */ }
        composable("chat/{mood}/{wellbeingContext}") { /* ChatScreen */ }
        composable("history") { /* HistoryScreen */ }
        composable("techniques/{mood}") { /* TechniqueScreen */ }
        composable("goals") { /* GoalScreen */ }
    }
}
```

---

### `app/src/main/java/com/bono/mentalbot/ui/technique/TechniqueScreen.kt`

> Pantalla de técnicas de bienestar. Permite generar técnicas con IA o agregarlas manualmente.

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechniqueScreen(mood: String, userName: String, onBack: () -> Unit, viewModel: TechniqueViewModel = viewModel()) { /* ... */ }

@Composable
fun TechniqueCard(technique: Technique, onDelete: () -> Unit) { /* Card con badge de categoría y botón eliminar */ }

@Composable
fun AddTechniqueDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) { /* Campos: título, descripción, categoría */ }
```

---

### `app/src/main/java/com/bono/mentalbot/ui/technique/TechniqueViewModel.kt`

> Gestiona las técnicas de bienestar: carga desde Firestore, genera con IA, guarda y elimina.

**Formato de respuesta esperado del modelo:**
```
TITULO: [nombre corto]
CATEGORIA: [Respiración/Mindfulness/Movimiento/Visualización/Grounding]
DESCRIPCION: [descripción breve]
PASOS: [paso 1]; [paso 2]; [paso 3]
```

```kotlin
class TechniqueViewModel : ViewModel() {
    fun generateTechnique(mood: String, userName: String) { /* llama a Groq API y parsea respuesta */ }
    fun saveTechnique(title: String, description: String, category: String, mood: String) { /* guarda manual */ }
    fun deleteTechnique(id: String) { /* elimina de Firestore */ }
    private fun parseTechnique(raw: String, mood: String): Technique { /* extrae TITULO, CATEGORIA, DESCRIPCION, PASOS */ }
}
```

---

### `app/src/main/java/com/bono/mentalbot/ui/theme/Color.kt`

> Paleta de colores de la aplicación.

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/ui/theme/Theme.kt`

> Tema Material3 con soporte para modo claro y oscuro.

```kotlin
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
```

---

### `app/src/main/java/com/bono/mentalbot/ui/theme/Type.kt`

```kotlin
package com.bono.mentalbot.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp)
)
```

---

### `app/src/main/java/com/bono/mentalbot/ui/wellbeing/WellbeingHomeScreen.kt`

> Pantalla principal de bienestar (hub). Muestra saludo, badge de estado de ánimo y tarjetas de acceso rápido.

**Funciones auxiliares:**

```kotlin
fun getGreeting(): String { /* "Buenos días" / "Buenas tardes" / "Buenas noches" */ }
fun getMoodEmoji(mood: String): String { /* emoji según estado */ }
```

**Estructura de la pantalla:**
- Card de bienvenida con nombre de usuario y badge de estado
- Fila 1: **Chat con MindBot** + **Evaluación Emocional**
- Fila 2: **Técnicas** + **Mis metas**
- Botón para actualizar estado de ánimo

---

### `app/src/main/java/com/bono/mentalbot/ui/wellbeing/WellbeingScreen.kt`

> Evaluación emocional estructurada en tres secciones teóricas.

**Secciones del formulario:**

| Sección | Base teórica | Preguntas |
|---------|-------------|-----------|
| Necesidades básicas | Maslow | Alimentación, seguridad, relaciones, autoestima, propósito |
| Circunstancia vital | Ortega y Gasset | Satisfacción, control, texto libre |
| Valores y propósito | López de Llergo | Vivencia de valores, texto libre |
| Reflexión libre | — | Campo abierto |

**Componentes reutilizables:**

```kotlin
@Composable fun SectionHeader(emoji: String, title: String, subtitle: String) { /* ... */ }
@Composable fun ScaleQuestion(question: String, value: Float, onValueChange: (Float) -> Unit) { /* Slider 1–5 */ }
@Composable fun OpenQuestion(question: String, value: String, onValueChange: (String) -> Unit, placeholder: String, minLines: Int) { /* TextField */ }
@Composable fun SectionDivider() { /* Divider morado */ }
```

---

### `app/src/main/java/com/bono/mentalbot/ui/wellbeing/WellbeingViewModel.kt`

> Estado y lógica de la evaluación emocional.

```kotlin
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

class WellbeingViewModel : ViewModel() {
    // update* funciones para cada campo ...

    fun buildWellbeingContext(): String {
        // Genera un bloque de texto estructurado con toda la evaluación
        // para enviarlo como contexto al modelo de lenguaje
    }
}
```

---

### `app/src/main/java/com/bono/mentalbot/utils/Constants.kt`

> Constantes globales y prompts del sistema.

```kotlin
package com.bono.mentalbot.utils

import com.bono.mentalbot.BuildConfig

object Constants {

    const val GROQ_BASE_URL = "https://api.groq.com/openai/v1/"
    val GROQ_API_KEY = "Bearer ${BuildConfig.GROQ_API_KEY}"
    const val MODEL = "llama-3.1-8b-instant"

    fun getSystemPrompt(userName: String, mood: String): String {
        return """
Eres MindBot, un asistente de apoyo emocional empático y seguro.

El usuario se llama $userName. Dirígete a él siempre por su nombre de forma natural,
cálida y cercana, sin sonar repetitivo.

El estado emocional actual del usuario es: $mood.

Tu estilo de respuesta debe ser:
- Calmado, validante y sin juicios.
- Breve y claro (máximo 5 oraciones).
- En español.
- Conversacional y humano, nunca robótico.

Normas importantes:
- No diagnosticas ni haces afirmaciones clínicas.
- No reemplazas a un profesional de salud mental.
- Si detectas desesperanza intensa o ideas suicidas, recomienda ayuda profesional de inmediato.
        """.trimIndent()
    }

    fun getInitialMessage(userName: String, mood: String, hasWellbeingContext: Boolean = false): String {
        // Retorna un saludo personalizado según mood y si hay contexto de evaluación
    }
}
```

---

### `app/src/main/java/com/bono/mentalbot/utils/Extensions.kt`

```kotlin
package com.bono.mentalbot.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Convierte un timestamp a formato "dd/MM/yyyy HH:mm".
 */
fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Capitaliza la primera letra de la cadena.
 */
fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}
```

---

### `app/src/test/java/com/bono/mentalbot/ExampleUnitTest.kt`

```kotlin
package com.bono.mentalbot

import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
```

---

## Resumen de arquitectura

```
com.bono.mentalbot
├── data
│   ├── remote
│   │   ├── api/          ← GroqApiService (Retrofit)
│   │   ├── firebase/     ← FirestoreService
│   │   └── model/        ← ChatRequest, ChatResponse, DTOs
│   └── repository/       ← ChatRepository
├── domain
│   ├── model/            ← Goal, Message, Technique
│   └── usecase/          ← GetChatHistoryUseCase, SendMessageUseCase
├── ui
│   ├── auth/             ← AuthScreen, AuthViewModel, NameScreen
│   ├── chat/             ← ChatScreen, ChatViewModel, components/
│   ├── goal/             ← GoalScreen, GoalViewModel
│   ├── history/          ← HistoryScreen, HistoryViewModel
│   ├── mood/             ← MoodScreen, MoodViewModel
│   ├── navigation/       ← NavGraph
│   ├── technique/        ← TechniqueScreen, TechniqueViewModel
│   ├── theme/            ← Color, Theme, Type
│   └── wellbeing/        ← WellbeingHomeScreen, WellbeingScreen, WellbeingViewModel
└── utils
    ├── Constants.kt
    └── Extensions.kt
```
