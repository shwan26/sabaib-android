package com.smnc.sabaib.data

import android.app.Activity
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitLogIn
import com.revenuecat.purchases.awaitLogOut
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore

/** RevenueCat entitlement identifier for the SabaiB+ premium plan (placeholder until the RevenueCat dashboard is configured). */
const val ENTITLEMENT_PREMIUM = "premium"

class BillingRepository {

    /** Null when Purchases.configure() was skipped — expected pre-launch state with no RevenueCat key set. */
    private val purchases: Purchases?
        get() = if (Purchases.isConfigured) Purchases.sharedInstance else null

    /** Null/empty when no offering is configured yet in the RevenueCat dashboard — expected pre-launch state. */
    suspend fun getCurrentOffering(): Offering? =
        purchases?.awaitOfferings()?.current

    suspend fun purchasePackage(activity: Activity, packageToBuy: Package): CustomerInfo =
        requirePurchases().awaitPurchase(
            PurchaseParams.Builder(activity, packageToBuy).build()
        ).customerInfo

    suspend fun restorePurchases(): CustomerInfo = requirePurchases().awaitRestore()

    suspend fun getCustomerInfo(): CustomerInfo = requirePurchases().awaitCustomerInfo()

    fun isPremiumActive(customerInfo: CustomerInfo): Boolean =
        customerInfo.entitlements[ENTITLEMENT_PREMIUM]?.isActive == true

    /** Aliases RevenueCat's appUserID to the Supabase user id after sign-in. No-ops if not configured. */
    suspend fun logIn(userId: String) {
        purchases?.awaitLogIn(userId)
    }

    /** Resets RevenueCat back to an anonymous user after sign-out. No-ops if already anonymous or not configured. */
    suspend fun logOut() {
        val purchases = purchases ?: return
        if (!purchases.isAnonymous) {
            purchases.awaitLogOut()
        }
    }

    private fun requirePurchases(): Purchases =
        purchases ?: throw IllegalStateException("RevenueCat is not configured")
}
