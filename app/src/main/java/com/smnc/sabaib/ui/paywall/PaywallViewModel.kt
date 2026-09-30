package com.smnc.sabaib.ui.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchasesErrorCode
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
    data class Loaded(val monthly: Package) : PaywallUiState()
    object NoOfferingsAvailable : PaywallUiState()
    /** [monthly] is kept so the plan card stays on screen while the Play sheet is up. */
    data class Purchasing(val monthly: Package?) : PaywallUiState()
    object PurchaseSuccess : PaywallUiState()
    data class Error(val message: String, val monthly: Package?) : PaywallUiState()
}

class PaywallViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val billingRepository: BillingRepository = BillingRepository(),
    private val profileRepository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaywallUiState>(PaywallUiState.Loading)
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    /** SabaiB+ is sold as a monthly plan only; last package fetched from the current offering. */
    private var monthlyPackage: Package? = null

    init {
        loadOffering()
    }

    fun loadOffering() {
        _uiState.value = PaywallUiState.Loading
        viewModelScope.launch {
            try {
                val offering = billingRepository.getCurrentOffering()
                val monthly = offering?.monthly ?: offering?.availablePackages?.firstOrNull()
                monthlyPackage = monthly
                _uiState.value = if (monthly == null) {
                    PaywallUiState.NoOfferingsAvailable
                } else {
                    PaywallUiState.Loaded(monthly)
                }
            } catch (e: PurchasesException) {
                _uiState.value = PaywallUiState.Error(e.toPaywallMessage(), monthlyPackage)
            } catch (e: Exception) {
                _uiState.value = PaywallUiState.Error(e.toUserMessage(), monthlyPackage)
            }
        }
    }

    fun purchaseMonthly(activity: Activity) {
        val pkg = monthlyPackage ?: return
        val userId = authRepository.currentUserId() ?: return
        _uiState.value = PaywallUiState.Purchasing(pkg)
        viewModelScope.launch {
            try {
                val customerInfo = billingRepository.purchasePackage(activity, pkg)
                if (billingRepository.isPremiumActive(customerInfo)) {
                    profileRepository.updatePlan(userId, "premium")
                }
                _uiState.value = PaywallUiState.PurchaseSuccess
            } catch (e: PurchasesTransactionException) {
                _uiState.value = if (e.userCancelled) {
                    PaywallUiState.Loaded(pkg)
                } else {
                    PaywallUiState.Error(e.toPaywallMessage(), pkg)
                }
            } catch (e: PurchasesException) {
                _uiState.value = PaywallUiState.Error(e.toPaywallMessage(), pkg)
            }
        }
    }

    fun restore() {
        val userId = authRepository.currentUserId() ?: return
        _uiState.value = PaywallUiState.Purchasing(monthlyPackage)
        viewModelScope.launch {
            try {
                val customerInfo = billingRepository.restorePurchases()
                if (billingRepository.isPremiumActive(customerInfo)) {
                    profileRepository.updatePlan(userId, "premium")
                    _uiState.value = PaywallUiState.PurchaseSuccess
                } else {
                    _uiState.value = PaywallUiState.Error("No active SabaiB+ subscription found for this account.", monthlyPackage)
                }
            } catch (e: Exception) {
                _uiState.value = PaywallUiState.Error(e.toUserMessage(), monthlyPackage)
            }
        }
    }
}

private fun PurchasesException.toPaywallMessage(): String = when (code) {
    PurchasesErrorCode.PurchaseNotAllowedError ->
        "Google Play billing isn't available on this device. Make sure you're signed in to the Play Store."
    PurchasesErrorCode.NetworkError -> "No internet connection. Please try again."
    PurchasesErrorCode.ProductAlreadyPurchasedError -> "You already own SabaiB+. Tap Restore purchases."
    PurchasesErrorCode.ProductNotAvailableForPurchaseError -> "SabaiB+ isn't available for purchase right now."
    else -> toUserMessage()
}
