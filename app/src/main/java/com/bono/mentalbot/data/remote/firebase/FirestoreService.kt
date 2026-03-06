package com.bono.mentalbot.data.remote.firebase

import com.bono.mentalbot.domain.model.Message
import com.bono.mentalbot.domain.model.Technique
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreService {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Colección de mensajes del usuario actual
    private fun messagesCollection() = db
        .collection("users")
        .document(auth.currentUser?.uid ?: "anonymous")
        .collection("messages")

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

    suspend fun clearMessages() {
        val snapshot = messagesCollection().get().await()
        snapshot.documents.forEach { it.reference.delete().await() }
    }

    // Guardar nombre del usuario
    suspend fun saveUserName(name: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .set(mapOf("name" to name), com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    // Obtener nombre del usuario
    suspend fun getUserName(): String {
        val uid = auth.currentUser?.uid ?: return ""
        val doc = db.collection("users").document(uid).get().await()
        return doc.getString("name") ?: ""
    }

    private fun techniquesCollection() = db
        .collection("users")
        .document(auth.currentUser?.uid ?: "anonymous")
        .collection("techniques")

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

    suspend fun deleteTechnique(id: String) {
        techniquesCollection().document(id).delete().await()
    }
}