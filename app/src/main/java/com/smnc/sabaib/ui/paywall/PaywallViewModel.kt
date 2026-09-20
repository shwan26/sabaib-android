package com.smnc.sabaib.ui.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.PurchasesTransactionException
import com.smnc.sabaib.data.AuthRepository
import com.smnc.sabaib.data.BillingRepository
import com.smnc.sabaib.data.ProfileRepository
import com.smnc.sabaib.data.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PaywallUiState {
    object Loading : PaywallUiState()
    data class Loaded(val packages: List<Package>) : PaywallUiState()
    object NoOfferingsAvailable : PaywallUiState()
    object Purchasing : PaywallUiState()
    object PurchaseSuccess : PaywallUiState()
    data class Error(val message: String) : PaywallUiState()
}

class PaywallViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val billingRepository: BillingRepository = BillingRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaywallUiState>(PaywallUiState.Loading)
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        loadOffering()
    }

    fun loadOffering() {
        _uiState.value = PaywallUiState.Loading
        viewModelScope.launch {
            try {
                val offering = billingRepository.getCurrentOffering()
                _uiState.value = if (offering == null || offering.availablePackages.isEmpty()) {
                    PaywallUiState.NoOfferingsAvailable
                } else {
                    PaywallUiState.Loaded(offering.availablePackages)
                }
            } catch (e: Exception) {
                _uiState.value = PaywallUiState.Error(e.toUserMessage())
            }
        }
    }

    fun purchase(activity: Activity, pkg: Package) {
        val userId = authRepository.currentUserId() ?: return
        _uiState.value = PaywallUiState.Purchasing
        viewModelScope.launch {
            try {
                val customerInfo = billingRepository.purchasePackage(activity, pkg)
                if (billingRepository.isPremiumActive(customerInfo)) {
                    profileRepository.updatePlan(userId, "premium")
                }
                _uiState.value = PaywallUiState.PurchaseSuccess
            } catch (e: PurchasesTransactionException) {
                if (e.userCancelled) {
                    loadOffering()
                } else {
                    _uiState.value = PaywallUiState.Error(e.toUserMessage())
                }
            } catch (e: PurchasesException) {
                _uiState.value = PaywallUiState.Error(e.toUserMessage())
            }
        }
    }

    fun restore() {
        val userId = authRepository.currentUserId() ?: return
        _uiState.value = PaywallUiState.Purchasing
        viewModelScope.launch {
            try {
                val customerInfo = billingRepository.restorePurchases()
                if (billingRepository.isPremiumActive(customerInfo)) {
                    profileRepository.updatePlan(userId, "premium")
                    _uiState.value = PaywallUiState.PurchaseSuccess
                } else {
                    loadOffering()
                }
            } catch (e: Exception) {
                _uiState.value = PaywallUiState.Error(e.toUserMessage())
            }
        }
    }
}
