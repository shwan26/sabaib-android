package com.smnc.sabaib.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.smnc.sabaib.R
import com.smnc.sabaib.data.AuthRepository
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiError
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiLightGray
import com.smnc.sabaib.ui.theme.SabaiOffWhite
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    profileViewModel: ProfileViewModel,
    onUpgradeClick: () -> Unit = {},
    onPaymentHistoryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLoggedOut: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val email = authRepository.currentUserEmail()
    val fallbackName = email
        ?.substringBefore("@")
        ?.replaceFirstChar { it.uppercase() }
        ?: "Guest"
    val uiState by profileViewModel.uiState.collectAsState()
    val displayName = (uiState as? ProfileUiState.Loaded)?.displayName?.takeIf { it.isNotBlank() }
        ?: fallbackName
    val isPremium = (uiState as? ProfileUiState.Loaded)?.plan == "premium"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SabaiOffWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "SabaiB",
                color = SabaiBlack,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(R.drawable.penguin_wave),
                contentDescription = "SabaiB penguin mascot",
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SabaiWhite)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SabaiYellow),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.take(1).uppercase(),
                    color = SabaiBlack,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = displayName,
                color = SabaiBlack,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            if (email != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = email, color = SabaiGray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isPremium) SabaiYellow else SabaiLightGray)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isPremium) "SABAIB+" else "FREE PLAN",
                    color = if (isPremium) SabaiBlack else SabaiGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SabaiYellow)
                .clickable(onClick = onUpgradeClick)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Upgrade to SabaiB+",
                    color = SabaiBlack,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Unlimited scans & more",
                    color = SabaiBlack,
                    fontSize = 13.sp
                )
            }
            Text(text = "→", color = SabaiBlack, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SabaiWhite)
        ) {
            ProfileMenuRow(label = "Payment History", onClick = onPaymentHistoryClick)
            HorizontalDivider(color = SabaiOffWhite, thickness = 1.dp)
            ProfileMenuRow(label = "Settings", onClick = onSettingsClick)
            HorizontalDivider(color = SabaiOffWhite, thickness = 1.dp)
            ProfileMenuRow(
                label = "Log Out",
                labelColor = SabaiError,
                showArrow = false,
                onClick = {
                    coroutineScope.launch {
                        try {
                            authRepository.signOut()
                        } catch (e: Exception) {
                            // Remote invalidation failed/timed out; still treat the
                            // local session as ended so the user isn't stuck here.
                        }
                        onLoggedOut()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileMenuRow(
    label: String,
    onClick: () -> Unit,
    labelColor: androidx.compose.ui.graphics.Color = SabaiBlack,
    showArrow: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = labelColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (showArrow) {
            Text(text = "→", color = SabaiGray, fontSize = 16.sp)
        }
    }
}
