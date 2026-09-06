package com.smnc.sabaib.ui.groups

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smnc.sabaib.R
import com.smnc.sabaib.ui.home.RecentGroupUi
import com.smnc.sabaib.ui.home.RecentGroupsSection
import com.smnc.sabaib.ui.home.sampleRecentGroups
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiOffWhite

@Composable
fun GroupsScreen(
    groups: List<RecentGroupUi> = sampleRecentGroups,
    onGroupClick: (RecentGroupUi) -> Unit = {}
) {
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

        RecentGroupsSection(
            groups = groups,
            onGroupClick = onGroupClick,
            title = "Your Groups"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
