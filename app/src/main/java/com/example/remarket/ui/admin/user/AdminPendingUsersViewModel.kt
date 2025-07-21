package com.example.remarket.ui.admin.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.model.User
import com.example.remarket.data.network.TokenManager
import com.example.remarket.data.repository.UserRepository
import com.example.remarket.util.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PendingUsersUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminPendingUsersViewModel @Inject constructor(
    private val repo: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(PendingUsersUiState())
    val state: StateFlow<PendingUsersUiState> = _state.asStateFlow()

    init { load() }

    fun onLogout() {
        firebaseAuth.signOut()
        tokenManager.clearToken()
    }

    fun load() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        when (val res = repo.getPendingUsers()) {
            is Resource.Success -> _state.update {
                it.copy(users = res.data, isLoading = false, error = null)
            }
            is Resource.Error -> _state.update {
                it.copy(isLoading = false, error = res.message)
            }
            else -> {}
        }
    }

    fun approve(id: String)  = setStatus(id, true)
    fun reject(id: String)   = setStatus(id, false)

    private fun setStatus(id: String, approved: Boolean) = viewModelScope.launch {
        Log.d("PENDING_USERS", "update $id → $approved")
        when (val res = repo.setUserApproved(id, approved)) {
            is Resource.Success -> {
                Log.d("PENDING_USERS", "✔ actualizado correctamente")
                load()
            }                      // refresca lista
            is Resource.Error   -> {
                Log.e("PENDING_USERS", "✘ falló: ${res.message}")
                _state.update { it.copy(error = res.message) }
            }
            else -> {}
        }
    }
}
