package com.smnc.sabaib.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.GifDecoder
import com.smnc.sabaib.ui.theme.SabaiNavy
import com.smnc.sabaib.ui.theme.SabaiOffWhite
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow
import com.smnc.sabaib.ui.theme.SabaiYellowLight

@Composable
fun HomeScreen(
    onScanClick: () -> Unit,
    onJoinBill: () -> Unit,
    onDashboardClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SabaiYellow)
    ) {
        // Decorative background circles
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopStart)
                .offset(x = (-90).dp, y = (-40).dp)
                .clip(CircleShape)
                .background(SabaiYellowLight.copy(alpha = 0.6f))
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 40.dp)
                .clip(CircleShape)
                .background(SabaiYellowLight.copy(alpha = 0.5f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Sabai",
                color = SabaiNavy,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Split bills, stay friends",
                color = SabaiNavy.copy(alpha = 0.75f),
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(SabaiOffWhite),
                contentAlignment = Alignment.Center
            ) {
                WavingPenguinGif(
                    modifier = Modifier
                        .width(130.dp)
                        .height(165.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "What do you want to do?",
                color = SabaiNavy.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HomeFeatureCard(
                    icon = "📷",
                    label = "Scan Bill",
                    onClick = onScanClick,
                    modifier = Modifier.weight(1f)
                )
                HomeFeatureCard(
                    icon = "👥",
                    label = "Join Group",
                    onClick = onJoinBill,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Dashboard",
                color = SabaiNavy,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onDashboardClick)
            )

            Spacer(modifier = Modifier.weight(1f))

        }
    }
}

@Composable
private fun HomeFeatureCard(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(SabaiWhite)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 34.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                color = SabaiNavy,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WavingPenguinGif(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val gifLoader = remember(context) {
        ImageLoader.Builder(context)
            .components { add(GifDecoder.Factory()) }
            .build()
    }

    AsyncImage(
        model = "file:///android_asset/penguin_wave.gif",
        contentDescription = "Waving penguin",
        imageLoader = gifLoader,
        modifier = modifier
    )
}
