package com.smnc.sabaib.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smnc.sabaib.R
import com.smnc.sabaib.ui.groups.GroupsUiState
import com.smnc.sabaib.ui.groups.GroupsViewModel
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiLightGray
import com.smnc.sabaib.ui.theme.SabaiNavyDark
import com.smnc.sabaib.ui.theme.SabaiOffWhite
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow

private const val RECENT_GROUPS_LIMIT = 5

@Composable
fun HomeScreen(
    onScanClick: () -> Unit,
    onJoinBill: () -> Unit,
    userName: String = "Guest",
    groupsViewModel: GroupsViewModel = viewModel(),
    onGroupClick: (RecentGroupUi) -> Unit = {}
) {
    val uiState by groupsViewModel.uiState.collectAsState()
    val recentGroups = (uiState as? GroupsUiState.Loaded)?.groups.orEmpty().take(RECENT_GROUPS_LIMIT)
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
                text = "Welcome, $userName!",
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

        ScanBillCard()

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onScanClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = SabaiYellow,
                contentColor = SabaiBlack
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.photo_camera_24),
                contentDescription = null,
                tint = SabaiBlack,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Scan Bill", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = onJoinBill,
            border = BorderStroke(1.dp, SabaiLightGray),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SabaiBlack),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ad_group_24),
                contentDescription = null,
                tint = SabaiBlack,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Join a Group", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(28.dp))

        RecentGroupsSection(
            groups = recentGroups,
            onGroupClick = onGroupClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ScanBillCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(SabaiNavyDark)
    ) {
        Text(
            text = "🧾",
            fontSize = 64.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SabaiBlack.copy(alpha = 0f), SabaiBlack.copy(alpha = 0.85f))
                    )
                )
                .padding(vertical = 18.dp, horizontal = 16.dp)
        ) {
            Text(
                text = "Scan a bill, split it in seconds",
                color = SabaiWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
