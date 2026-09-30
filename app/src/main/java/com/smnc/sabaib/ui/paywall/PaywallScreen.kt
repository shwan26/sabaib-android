package com.smnc.sabaib.ui.paywall

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revenuecat.purchases.Package
import com.smnc.sabaib.R
import com.smnc.sabaib.data.ProfileRepository
import com.smnc.sabaib.ui.profile.ProfileUiState
import com.smnc.sabaib.ui.profile.ProfileViewModel
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiError
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiLightGray
import com.smnc.sabaib.ui.theme.SabaiOffWhite
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow
import com.smnc.sabaib.ui.theme.SabaiYellowDark
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private val PRO_FEATURES = listOf(
    "Unlimited receipt scans",
    "No monthly scan limit resets to wait for"
)

private const val TERMS_URL = "https://sabaib.vercel.app/terms"
private const val PRIVACY_URL = "https://sabaib.vercel.app/privacy"
private const val MANAGE_SUBSCRIPTION_URL =
    "https://play.google.com/store/account/subscriptions?package=com.smnc.sabaib"

@Composable
fun PaywallScreen(
    profileViewModel: ProfileViewModel,
    paywallViewModel: PaywallViewModel,
    onBack: () -> Unit
) {
    val uiState by paywallViewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()
    val isPremium = (profileState as? ProfileUiState.Loaded)?.plan == "premium"
    val context = LocalContext.current
    val activity = context as? Activity
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(uiState) {
        if (uiState is PaywallUiState.PurchaseSuccess) {
            profileViewModel.loadProfile()
            delay(1200.milliseconds)
            onBack()
        }
    }

    val monthly: Package? = when (val state = uiState) {
        is PaywallUiState.Loaded -> state.monthly
        is PaywallUiState.Purchasing -> state.monthly
        is PaywallUiState.Error -> state.monthly
        else -> null
    }
    val isPurchasing = uiState is PaywallUiState.Purchasing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SabaiOffWhite)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_back_24),
                contentDescription = "Back",
                tint = SabaiBlack
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.penguin_jump),
                contentDescription = "SabaiB penguin mascot",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SabaiB+",
                color = SabaiBlack,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Scan every receipt. No limits.",
                color = SabaiGray,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
            PlanComparison()
            Spacer(modifier = Modifier.height(20.dp))

            when {
                isPremium -> PremiumActiveCard()

                uiState is PaywallUiState.PurchaseSuccess -> PremiumActiveCard(justPurchased = true)

                uiState is PaywallUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SabaiYellow)
                    }
                }

                uiState is PaywallUiState.NoOfferingsAvailable -> ComingSoonCard()

                monthly != null -> MonthlyPlanCard(pkg = monthly)
            }

            val errorState = uiState as? PaywallUiState.Error
            if (errorState != null && !isPremium) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorState.message,
                    color = SabaiError,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Pinned bottom actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SabaiOffWhite)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                isPremium -> PrimaryButton(
                    text = "Manage subscription",
                    onClick = { uriHandler.openUri(MANAGE_SUBSCRIPTION_URL) }
                )

                uiState is PaywallUiState.PurchaseSuccess -> Unit

                monthly != null -> PrimaryButton(
                    text = "Subscribe for ${monthly.product.price.formatted}/month",
                    loading = isPurchasing,
                    onClick = { activity?.let { paywallViewModel.purchaseMonthly(it) } }
                )

                uiState is PaywallUiState.Error -> PrimaryButton(
                    text = "Try again",
                    onClick = { paywallViewModel.loadOffering() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isPremium) {
                    FooterLink("Restore purchases", enabled = !isPurchasing) { paywallViewModel.restore() }
                    FooterDot()
                }
                FooterLink("Terms") { uriHandler.openUri(TERMS_URL) }
                FooterDot()
                FooterLink("Privacy") { uriHandler.openUri(PRIVACY_URL) }
            }
        }
    }
}

@Composable
private fun PlanComparison() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SabaiWhite)
            .padding(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(1.4f))
            Text(
                text = "Free",
                color = SabaiGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "SabaiB+",
                color = SabaiYellowDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        ComparisonRow(
            label = "Receipt scans",
            free = "${ProfileRepository.FREE_SCAN_LIMIT} / 30 days",
            plus = "Unlimited"
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SabaiLightGray)
        )
        Spacer(modifier = Modifier.height(16.dp))
        PRO_FEATURES.forEach { feature ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "✓", color = SabaiYellowDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = feature, color = SabaiBlack, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ComparisonRow(label: String, free: String, plus: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = SabaiBlack,
            fontSize = 14.sp,
            modifier = Modifier.weight(1.4f)
        )
        Text(
            text = free,
            color = SabaiGray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = plus,
            color = SabaiBlack,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MonthlyPlanCard(pkg: Package) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SabaiWhite)
            .border(2.dp, SabaiYellow, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Monthly",
                    color = SabaiBlack,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Billed every month",
                    color = SabaiGray,
                    fontSize = 13.sp
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = pkg.product.price.formatted,
                    color = SabaiBlack,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " / month",
                    color = SabaiGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Auto-renews monthly. Cancel anytime in Google Play.",
            color = SabaiGray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun PremiumActiveCard(justPurchased: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SabaiYellow)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "You're on SabaiB+! 🎉",
            color = SabaiBlack,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (justPurchased) "Thanks for subscribing. Enjoy unlimited scans." else "Unlimited scans are unlocked.",
            color = SabaiBlack,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ComingSoonCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SabaiWhite)
            .padding(24.dp)
    ) {
        Text(
            text = "SabaiB+ is coming soon!",
            color = SabaiBlack,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "We're finishing setup for subscriptions. Check back shortly.",
            color = SabaiGray,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun PrimaryButton(text: String, loading: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(SabaiYellow)
            .clickable(enabled = !loading, onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = SabaiBlack,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(text = text, color = SabaiBlack, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FooterLink(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Text(
        text = text,
        color = if (enabled) SabaiGray else SabaiGray.copy(alpha = 0.5f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    )
}

@Composable
private fun FooterDot() {
    Text(text = "·", color = SabaiGray, fontSize = 12.sp)
}
