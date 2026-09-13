package com.smnc.sabaib.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smnc.sabaib.data.AuthRepository
import com.smnc.sabaib.data.GroupBillRow
import com.smnc.sabaib.data.GroupsRepository
import com.smnc.sabaib.data.toUserMessage
import com.smnc.sabaib.ui.home.GroupStatus
import com.smnc.sabaib.ui.home.RecentGroupUi
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GroupsUiState {
    object Loading : GroupsUiState()
    data class Loaded(val groups: List<RecentGroupUi>) : GroupsUiState()
    data class Error(val message: String) : GroupsUiState()
}

class GroupsViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val groupsRepository: GroupsRepository = GroupsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<GroupsUiState>(GroupsUiState.Loading)
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.sessionStatusFlow().collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> loadGroups()
                    is SessionStatus.NotAuthenticated -> _uiState.value = GroupsUiState.Loaded(emptyList())
                    else -> Unit
                }
            }
        }
    }

    fun loadGroups() {
        val userId = authRepository.currentUserId() ?: return
        _uiState.value = GroupsUiState.Loading
        viewModelScope.launch {
            try {
                val bills = groupsRepository.fetchBillsForUser(userId)
                val counts = groupsRepository.fetchParticipantCounts(bills.map { it.id })
                val groups = bills
                    .sortedByDescending { it.createdAt }
                    .map { it.toRecentGroupUi(counts[it.id] ?: 1) }
                _uiState.value = GroupsUiState.Loaded(groups)
            } catch (e: Exception) {
                _uiState.value = GroupsUiState.Error(e.toUserMessage())
            }
        }
    }
}

private fun GroupBillRow.toRecentGroupUi(peopleCount: Int) = RecentGroupUi(
    id = id,
    name = restaurantName.orEmpty(),
    peopleCount = peopleCount,
    totalAmount = totalAmount,
    status = if (settledAt == null) GroupStatus.ACTIVE else GroupStatus.SETTLED
)
