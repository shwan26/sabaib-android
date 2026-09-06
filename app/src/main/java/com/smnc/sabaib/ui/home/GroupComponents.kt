package com.smnc.sabaib.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiNavy
import com.smnc.sabaib.ui.theme.SabaiWhite
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

val sampleRecentGroups = listOf(
    RecentGroupUi("1", "Dinner @ Thonglor", 4, 830.0, GroupStatus.ACTIVE),
    RecentGroupUi("2", "Weekend Trip", 3, 2450.0, GroupStatus.SETTLED)
)

@Composable
fun RecentGroupsSection(
    groups: List<RecentGroupUi>,
    onGroupClick: (RecentGroupUi) -> Unit,
    title: String = "Recent Groups"
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
                text = title,
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
fun RecentGroupRow(group: RecentGroupUi, onClick: () -> Unit) {
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
fun StatusBadge(status: GroupStatus) {
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
