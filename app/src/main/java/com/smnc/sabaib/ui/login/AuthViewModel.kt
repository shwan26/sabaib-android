package com.smnc.sabaib.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smnc.sabaib.data.AuthRepository
import com.smnc.sabaib.data.ConsentRepository
import com.smnc.sabaib.data.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository(),
    private val consentRepository: ConsentRepository = ConsentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                repository.signIn(email, password)
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.toUserMessage())
            }
        }
    }

    fun signUp(email: String, password: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                repository.signUp(email, password)
                repository.currentUserId()?.let { userId ->
                    try {
                        consentRepository.recordConsent(userId)
                    } catch (e: Exception) {
                        // Best-effort audit log; don't block account creation on this.
                    }
                }
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.toUserMessage())
            }
        }
    }

    fun isLoggedIn(): Boolean = repository.isLoggedIn()
}