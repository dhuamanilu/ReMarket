package com.example.remarket.ui.admin.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.model.User
import com.example.remarket.data.model.toDomain
import com.example.remarket.data.repository.UserRepository
import com.example.remarket.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserDetailUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminUserDetailViewModel @Inject constructor(
    private val repo: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserDetailUiState())
    val state: StateFlow<UserDetailUiState> = _state

    private var currentId: String? = null

    fun load(id: String) = viewModelScope.launch {
        currentId = id
        _state.update { it.copy(isLoading = true, error = null) }

        when (val res = repo.getUserById(id)) {
            is Resource.Success -> {
                // 💡  IMPRIME AQUÍ LO QUE DEVUELVE LA API
                Log.d("API_DEBUG", "UserDTO recibido: ${res.data}")

                _state.update {
                it.copy(user = res.data.toDomain(), isLoading = false)
                }
            }
            is Resource.Error   -> _state.update {
                it.copy(isLoading = false, error = res.message)
            }
            else -> {}
        }
    }

    fun approve(approved: Boolean, onFinish: () -> Unit) = viewModelScope.launch {
        val id = currentId ?: return@launch
        when (repo.setUserApproved(id, approved)) {
            is Resource.Success -> onFinish()
            is Resource.Error   -> _state.update { it.copy(error = "No se pudo actualizar el usuario") }
            else -> {}
        }
    }
}
