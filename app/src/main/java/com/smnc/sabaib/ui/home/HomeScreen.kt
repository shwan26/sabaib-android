package com.smnc.sabaib.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smnc.sabaib.R
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiLightGray
import com.smnc.sabaib.ui.theme.SabaiNavy
import com.smnc.sabaib.ui.theme.SabaiNavyDark
import com.smnc.sabaib.ui.theme.SabaiOffWhite
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow
import com.smnc.sabaib.ui.theme.SabaiYellowDark
import com.smnc.sabaib.ui.theme.SabaiYellowLight

enum class GroupStatus { ACTIVE, SETTLED }

data class RecentGroupUi(
    val id: String,
    val name: String,
    val peopleCount: Int,
    val totalAmount: Double,
    val status: GroupStatus
)

private val sampleRecentGroups = listOf(
    RecentGroupUi("1", "Dinner @ Thonglor", 4, 830.0, GroupStatus.ACTIVE),
    RecentGroupUi("2", "Weekend Trip", 3, 2450.0, GroupStatus.SETTLED)
)

private enum class HomeTab { HOME, GROUPS, PROFILE }

@Composable
fun HomeScreen(
    onScanClick: () -> Unit,
    onJoinBill: () -> Unit,
    userName: String = "Alex",
    recentGroups: List<RecentGroupUi> = sampleRecentGroups,
    onGroupClick: (RecentGroupUi) -> Unit = {},
    onGroupsTabClick: () -> Unit = {},
    onProfileTabClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }

    Scaffold(
        containerColor = SabaiOffWhite,
        bottomBar = {
            NavigationBar(containerColor = SabaiWhite) {
                NavigationBarItem(
                    selected = selectedTab == HomeTab.HOME,
                    onClick = { selectedTab = HomeTab.HOME },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.home_24),
                            contentDescription = "Home",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Home") },
                    colors = homeNavItemColors()
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.GROUPS,
                    onClick = {
                        selectedTab = HomeTab.GROUPS
                        onGroupsTabClick()
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ad_group_24),
                            contentDescription = "Groups",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Groups") },
                    colors = homeNavItemColors()
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.PROFILE,
                    onClick = {
                        selectedTab = HomeTab.PROFILE
                        onProfileTabClick()
                    },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.contacts_product_24),
                            contentDescription = "Profile",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Profile") },
                    colors = homeNavItemColors()
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SabaiOffWhite)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Welcome $userName!",
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
}

@Composable
private fun homeNavItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = SabaiYellowDark,
    selectedTextColor = SabaiYellowDark,
    indicatorColor = SabaiYellowLight.copy(alpha = 0.35f),
    unselectedIconColor = SabaiGray,
    unselectedTextColor = SabaiGray
)

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

@Composable
private fun RecentGroupsSection(
    groups: List<RecentGroupUi>,
    onGroupClick: (RecentGroupUi) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SabaiYellowLight.copy(alpha = 0.18f))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Recent Groups",
                color = SabaiBlack,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            groups.forEachIndexed { index, group ->
                RecentGroupRow(group = group, onClick = { onGroupClick(group) })
                if (index != groups.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun RecentGroupRow(group: RecentGroupUi, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SabaiWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.name,
                color = SabaiBlack,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${group.peopleCount} people · ฿${"%,.0f".format(group.totalAmount)}",
                color = SabaiGray,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        StatusBadge(status = group.status)
    }
}

@Composable
private fun StatusBadge(status: GroupStatus) {
    val (background, textColor, label) = when (status) {
        GroupStatus.ACTIVE -> Triple(SabaiYellowLight.copy(alpha = 0.6f), SabaiYellowDark, "ACTIVE")
        GroupStatus.SETTLED -> Triple(SabaiNavy.copy(alpha = 0.12f), SabaiNavy, "SETTLED")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
