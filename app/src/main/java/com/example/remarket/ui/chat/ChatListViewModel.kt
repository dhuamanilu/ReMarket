package com.example.remarket.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListUiState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadChats()
    }

    private fun loadChats() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.update { it.copy(isLoading = false, error = "Debes iniciar sesión para ver tus chats.") }
            return
        }

        firestore.collection("chats")
            .whereArrayContains("participantIds", currentUser.uid)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatListViewModel", "Error al escuchar chats", error)
                    _uiState.update { it.copy(isLoading = false, error = "No se pudieron cargar los chats.") }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // --- INICIO DE LA CORRECCIÓN ---
                    // Procesamos cada documento de chat individualmente para evitar que
                    // uno malo rompa toda la lista.
                    val chatList = snapshot.documents.mapNotNull { document ->
                        try {
                            // Intenta convertir el documento a un objeto Chat
                            document.toObject(Chat::class.java)?.copy(id = document.id)
                        } catch (e: Exception) {
                            // Si falla la conversión de un documento, lo reportamos y lo ignoramos.
                            Log.e("ChatListViewModel", "Error al parsear el documento: ${document.id}", e)
                            null // Devuelve nulo para que mapNotNull lo descarte
                        }
                    }
                    _uiState.update { it.copy(isLoading = false, chats = chatList, error = null) }
                    // --- FIN DE LA CORRECCIÓN ---
                }
            }
    }
}