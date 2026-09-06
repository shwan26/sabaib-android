package com.smnc.sabaib.ui.settings

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smnc.sabaib.ui.profile.ProfileUiState
import com.smnc.sabaib.ui.profile.ProfileViewModel
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiError
import com.smnc.sabaib.ui.theme.SabaiOffWhite
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow

@Composable
fun SettingsScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val uiState by profileViewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Loaded) {
            name = (uiState as ProfileUiState.Loaded).displayName.orEmpty()
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
                text = "Settings",
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
                text = "Change name",
                color = SabaiBlack,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState is ProfileUiState.Loading) {
                CircularProgressIndicator()
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(SabaiYellow)
                        .clickable(
                            onClick = { profileViewModel.updateDisplayName(name.trim()) }
                        )
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Save",
                        color = SabaiBlack,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (uiState is ProfileUiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (uiState as ProfileUiState.Error).message,
                    color = SabaiError,
                    fontSize = 13.sp
                )
            }
        }
    }
}
