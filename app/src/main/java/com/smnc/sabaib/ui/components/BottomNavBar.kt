package com.smnc.sabaib.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.R
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellowDark
import com.smnc.sabaib.ui.theme.SabaiYellowLight

enum class SabaiTab { HOME, GROUPS, PROFILE }

@Composable
fun SabaiBottomNavBar(
    selectedTab: SabaiTab,
    onHomeClick: () -> Unit,
    onGroupsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(containerColor = SabaiWhite) {
        NavigationBarItem(
            selected = selectedTab == SabaiTab.HOME,
            onClick = onHomeClick,
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
            selected = selectedTab == SabaiTab.GROUPS,
            onClick = onGroupsClick,
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
            selected = selectedTab == SabaiTab.PROFILE,
            onClick = onProfileClick,
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

@Composable
fun homeNavItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = SabaiYellowDark,
    selectedTextColor = SabaiYellowDark,
    indicatorColor = SabaiYellowLight.copy(alpha = 0.35f),
    unselectedIconColor = SabaiGray,
    unselectedTextColor = SabaiGray
)
