package com.smnc.sabaib.ui.paywall

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revenuecat.purchases.Package
import com.smnc.sabaib.ui.profile.ProfileViewModel
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiError
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiOffWhite
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow
import com.smnc.sabaib.ui.theme.SabaiYellowDark
import kotlinx.coroutines.delay

private val PRO_FEATURES = listOf(
    "Unlimited receipt scans",
    "No monthly scan limit resets to wait for"
)

@Composable
fun PaywallScreen(
    profileViewModel: ProfileViewModel,
    paywallViewModel: PaywallViewModel,
    onBack: () -> Unit
) {
    val uiState by paywallViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(uiState) {
        if (uiState is PaywallUiState.PurchaseSuccess) {
            profileViewModel.loadProfile()
            delay(1200)
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SabaiOffWhite)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "←",
                color = SabaiBlack,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onBack)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "SabaiB+",
                color = SabaiBlack,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SabaiWhite)
                .padding(24.dp)
        ) {
            Text(
                text = "Go unlimited",
                color = SabaiBlack,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            PRO_FEATURES.forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✓", color = SabaiYellowDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = feature, color = SabaiBlack, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (val state = uiState) {
            is PaywallUiState.Loading, is PaywallUiState.Purchasing -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(color = SabaiYellow)
                }
            }

            is PaywallUiState.NoOfferingsAvailable -> {
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

            is PaywallUiState.Loaded -> {
                state.packages.forEach { pkg ->
                    PackageCard(
                        pkg = pkg,
                        onClick = { activity?.let { paywallViewModel.purchase(it, pkg) } }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { paywallViewModel.restore() }),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Restore Purchases",
                        color = SabaiGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            is PaywallUiState.PurchaseSuccess -> {
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
                }
            }

            is PaywallUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SabaiWhite)
                        .padding(24.dp)
                ) {
                    Text(text = state.message, color = SabaiError, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(SabaiYellow)
                            .clickable(onClick = { paywallViewModel.loadOffering() })
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Try Again", color = SabaiBlack, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PackageCard(pkg: Package, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SabaiYellow)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pkg.product.title,
                color = SabaiBlack,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = pkg.product.price.formatted,
                color = SabaiBlack,
                fontSize = 13.sp
            )
        }
        Text(text = "→", color = SabaiBlack, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}
