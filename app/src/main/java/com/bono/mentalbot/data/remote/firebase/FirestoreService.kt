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