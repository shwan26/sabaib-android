package com.smnc.sabaib.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smnc.sabaib.data.AuthRepository
import com.smnc.sabaib.data.GroupsRepository
import com.smnc.sabaib.data.toUserMessage
import com.smnc.sabaib.ui.home.RecentGroupUi
import com.smnc.sabaib.ui.home.toRecentGroupUi
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PaymentHistoryUiState {
    object Loading : PaymentHistoryUiState()
    data class Loaded(val bills: List<RecentGroupUi>) : PaymentHistoryUiState()
    data class Error(val message: String) : PaymentHistoryUiState()
}

class PaymentHistoryViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val groupsRepository: GroupsRepository = GroupsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentHistoryUiState>(PaymentHistoryUiState.Loading)
    val uiState: StateFlow<PaymentHistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.sessionStatusFlow().collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> loadHistory()
                    is SessionStatus.NotAuthenticated -> _uiState.value = PaymentHistoryUiState.Loaded(emptyList())
                    else -> Unit
                }
            }
        }
    }

    fun loadHistory() {
        val userId = authRepository.currentUserId() ?: return
        _uiState.value = PaymentHistoryUiState.Loading
        viewModelScope.launch {
            try {
                val bills = groupsRepository.fetchJoinedBills(userId)
                val counts = groupsRepository.fetchParticipantCounts(bills.map { it.id })
                val history = bills
                    .sortedByDescending { it.createdAt }
                    .map { it.toRecentGroupUi(counts[it.id] ?: 1) }
                _uiState.value = PaymentHistoryUiState.Loaded(history)
            } catch (e: Exception) {
                _uiState.value = PaymentHistoryUiState.Error(e.toUserMessage())
            }
        }
    }
}
