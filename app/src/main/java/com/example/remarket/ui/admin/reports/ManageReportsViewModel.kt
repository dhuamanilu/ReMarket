package com.example.remarket.ui.admin.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.model.Report
import com.example.remarket.data.network.ApiService
import com.example.remarket.data.repository.IReportRepository
import com.example.remarket.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportItem(
    val report: Report,
    val productName: String = "...",
    val reporterEmail: String = "..."
)

data class ManageReportsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val items: List<ReportItem> = emptyList(),
    val deletingId: String? = null
)

@HiltViewModel
class ManageReportsViewModel @Inject constructor(
    private val repo: IReportRepository,
    private val api: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageReportsUiState())
    val uiState: StateFlow<ManageReportsUiState> = _uiState

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            repo.getReports().collect { res ->
                when (res) {
                    is Resource.Loading  -> _uiState.update { it.copy(isLoading = true , error = null) }
                    is Resource.Error    -> _uiState.update { it.copy(isLoading = false, error = res.message) }
                    is Resource.Success  -> {
                        val raw = res.data

                        // ⬇️ en paralelo obtenemos nombre de producto y email
                        val enriched = raw.map { r ->
                            async {
                                val pName = runCatching { api.getProductById(r.productId).model }.getOrDefault("???")
                                val email = runCatching { api.getUserById(r.reporterId).email }.getOrDefault("???")
                                ReportItem(r, pName, email)
                            }
                        }.awaitAll()

                        _uiState.update { it.copy(isLoading = false, items = enriched) }
                    }

                    Resource.Idle -> TODO()
                }
            }
        }
    }

    fun delete(r: Report) {
        viewModelScope.launch {
            _uiState.update { it.copy(deletingId = r.id) }
            when (val res = repo.deleteReport(r.id)) {
                is Resource.Success -> refresh()
                is Resource.Error   -> _uiState.update { it.copy(error = res.message) }
                Resource.Idle -> TODO()
                Resource.Loading -> TODO()
            }
            _uiState.update { it.copy(deletingId = null) }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
