package com.smnc.sabaib.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smnc.sabaib.data.AccountRepository
import com.smnc.sabaib.data.AuthRepository
import com.smnc.sabaib.data.ProfileRepository
import com.smnc.sabaib.data.toUserMessage
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Loaded(val displayName: String?, val plan: String = "free") : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

/** Resolves the name to show for the current user: their saved profile name,
 * falling back to their email's local part, falling back to "Guest". */
fun resolveDisplayName(uiState: ProfileUiState, fallbackEmail: String?): String {
    val fallbackName = fallbackEmail
        ?.substringBefore("@")
        ?.replaceFirstChar { it.uppercase() }
        ?: "Guest"
    return (uiState as? ProfileUiState.Loaded)?.displayName?.takeIf { it.isNotBlank() }
        ?: fallbackName
}

/** One-off outcomes of account actions - a [SharedFlow] (not [ProfileUiState]) so a
 * replayed value can't re-trigger navigation or re-show a result on recomposition. */
sealed class AccountEvent {
    object DataCleared : AccountEvent()
    object AccountDeleted : AccountEvent()
    data class Failed(val message: String) : AccountEvent()
}

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val accountRepository: AccountRepository = AccountRepository(authRepository)
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _accountEvent = MutableSharedFlow<AccountEvent>()
    val accountEvent: SharedFlow<AccountEvent> = _accountEvent.asSharedFlow()

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

    fun clearData() {
        val userId = authRepository.currentUserId() ?: return
        viewModelScope.launch {
            try {
                accountRepository.clearData(userId)
                _accountEvent.emit(AccountEvent.DataCleared)
            } catch (e: Exception) {
                _accountEvent.emit(AccountEvent.Failed(e.toUserMessage()))
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                accountRepository.deleteAccount()
                _accountEvent.emit(AccountEvent.AccountDeleted)
            } catch (e: Exception) {
                _accountEvent.emit(AccountEvent.Failed(e.toUserMessage()))
            }
        }
    }
}
