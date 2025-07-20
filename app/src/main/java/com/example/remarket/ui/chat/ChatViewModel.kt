package com.example.remarket.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.model.Chat
import com.example.remarket.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val chatHeader: String = "Chat",
    val currentMessage: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatId: String = savedStateHandle.get<String>("chatId") ?: ""
    private val currentUserId: String = auth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    init {
        if (chatId.isNotEmpty()) {
            loadChatMetadata()
            listenForMessages()
        } else {
            _uiState.update { it.copy(isLoading = false, error = "ID de chat no válido.") }
        }
    }

    private fun loadChatMetadata() {
        firestore.collection("chats").document(chatId).get()
            .addOnSuccessListener { document ->
                val chat = document.toObject(Chat::class.java)
                _uiState.update { it.copy(chatHeader = chat?.productTitle ?: "Chat") }
            }
    }

    private fun listenForMessages() {
        firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update { it.copy(isLoading = false, error = "No se pudieron cargar los mensajes.") }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    // --- INICIO DE LA MODIFICACIÓN ---
                    // Ahora mapeamos cada documento a un objeto Mensaje,
                    // y nos aseguramos de incluir el ID del documento.
                    val messageList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Message::class.java)?.copy(id = doc.id)
                    }
                    // --- FIN DE LA MODIFICACIÓN ---
                    _uiState.update { it.copy(isLoading = false, messages = messageList) }
                }
            }
    }

    // El resto del archivo (onMessageChanged, sendMessage, etc.) no cambia
    fun onMessageChanged(newMessage: String) {
        _uiState.update { it.copy(currentMessage = newMessage) }
    }

    fun sendMessage() {
        val messageText = _uiState.value.currentMessage.trim()
        if (messageText.isBlank() || currentUserId.isBlank()) {
            return
        }

        val message = Message(
            senderId = currentUserId,
            text = messageText,
            timestamp = null
        )

        _uiState.update { it.copy(currentMessage = "") }

        val chatRef = firestore.collection("chats").document(chatId)
        val messageRef = chatRef.collection("messages").document()

        firestore.batch()
            .set(messageRef, message)
            .update(chatRef, mapOf(
                "lastMessage" to messageText,
                "lastMessageTimestamp" to FieldValue.serverTimestamp()
            ))
            .commit()
            .addOnFailureListener { e ->
                _uiState.update { it.copy(currentMessage = messageText, error = "Error al enviar mensaje.") }
            }
    }

    fun getCurrentUserId(): String = currentUserId
}