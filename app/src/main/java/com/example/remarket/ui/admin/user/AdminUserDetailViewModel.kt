package com.example.remarket.ui.admin.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.model.User
import com.example.remarket.data.model.toDomain
import com.example.remarket.data.repository.UserRepository
import com.example.remarket.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val state: StateFlow<UserDetailUiState> = _state.asStateFlow()

    fun load(id: String) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        when (val res = repo.getUserById(id)) {
            is Resource.Success -> _state.update {
                it.copy(user = res.data.toDomain(), isLoading = false)
            }
            is Resource.Error -> _state.update {
                it.copy(error = res.message, isLoading = false)
            }
            else -> {}
        }
    }
}
