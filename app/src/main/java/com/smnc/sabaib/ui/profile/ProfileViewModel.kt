package com.smnc.sabaib.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smnc.sabaib.data.AuthRepository
import com.smnc.sabaib.data.ProfileRepository
import com.smnc.sabaib.data.toUserMessage
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Loaded(val displayName: String?, val plan: String = "free") : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.sessionStatusFlow().collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> loadProfile()
                    is SessionStatus.NotAuthenticated -> _uiState.value = ProfileUiState.Loaded(null)
                    else -> Unit
                }
            }
        }
    }

    fun loadProfile() {
        val userId = authRepository.currentUserId() ?: return
        _uiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            try {
                val profile = profileRepository.getProfile(userId)
                _uiState.value = ProfileUiState.Loaded(profile?.displayName, profile?.plan ?: "free")
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.toUserMessage())
            }
        }
    }

    fun updateDisplayName(newName: String) {
        val userId = authRepository.currentUserId() ?: return
        val currentPlan = (_uiState.value as? ProfileUiState.Loaded)?.plan ?: "free"
        viewModelScope.launch {
            try {
                profileRepository.updateDisplayName(userId, newName)
                _uiState.value = ProfileUiState.Loaded(newName, currentPlan)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.toUserMessage())
            }
        }
    }
}
