package com.smnc.sabaib.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smnc.sabaib.data.AuthRepository
import com.smnc.sabaib.data.ConsentRepository
import com.smnc.sabaib.data.ProfileRepository
import com.smnc.sabaib.data.toUserMessage
import com.smnc.sabaib.util.AgeGate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "AuthViewModel"

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository(),
    private val consentRepository: ConsentRepository = ConsentRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository()
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

    fun signUp(email: String, password: String, name: String, birthYearText: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            _uiState.value = AuthUiState.Error("Enter your name.")
            return
        }

        val birthYear = birthYearText.trim().toIntOrNull()
        if (birthYear == null || !AgeGate.isValidYear(birthYear)) {
            _uiState.value = AuthUiState.Error("Enter a valid 4-digit year of birth.")
            return
        }
        if (!AgeGate.isEligible(birthYear)) {
            _uiState.value = AuthUiState.Error(
                "You must be at least ${AgeGate.MIN_AGE} years old to create an account."
            )
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                repository.signUp(email, password, trimmedName, birthYear)
                repository.currentUserId()?.let { userId ->
                    try {
                        consentRepository.recordConsent(userId)
                    } catch (e: Exception) {
                        // Best-effort audit log; don't block account creation on this.
                        Log.e(TAG, "Failed to record consent for $userId", e)
                    }
                    try {
                        profileRepository.updateDisplayName(userId, trimmedName)
                    } catch (e: Exception) {
                        // Best-effort; don't block account creation on this. The pending
                        // name saved to ProfilePrefs is applied again on first login.
                        Log.e(TAG, "Failed to set display name for $userId", e)
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