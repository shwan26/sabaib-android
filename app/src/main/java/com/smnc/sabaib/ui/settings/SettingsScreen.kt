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
import androidx.compose.material3.HorizontalDivider
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
import com.smnc.sabaib.ui.components.ConfirmDialog
import com.smnc.sabaib.ui.components.ProfileMenuRow
import com.smnc.sabaib.ui.profile.AccountEvent
import com.smnc.sabaib.ui.profile.ProfileUiState
import com.smnc.sabaib.ui.profile.ProfileViewModel
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiError
import com.smnc.sabaib.ui.theme.SabaiOffWhite
import com.smnc.sabaib.ui.theme.SabaiSuccess
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow

@Composable
fun SettingsScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val uiState by profileViewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var isChangeNameExpanded by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isStatusError by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Loaded) {
            name = (uiState as ProfileUiState.Loaded).displayName.orEmpty()
        }
    }

    LaunchedEffect(Unit) {
        profileViewModel.accountEvent.collect { event ->
            when (event) {
                is AccountEvent.DataCleared -> {
                    isStatusError = false
                    statusMessage = "Your data has been cleared."
                }
                is AccountEvent.AccountDeleted -> onAccountDeleted()
                is AccountEvent.Failed -> {
                    isStatusError = true
                    statusMessage = event.message
                }
            }
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
        ) {
            ProfileMenuRow(
                label = "Change name",
                onClick = { isChangeNameExpanded = !isChangeNameExpanded }
            )

            if (isChangeNameExpanded) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
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
                }
            }

            HorizontalDivider(color = SabaiOffWhite, thickness = 1.dp)

            ProfileMenuRow(
                label = "Clear data",
                labelColor = SabaiError,
                showArrow = false,
                onClick = { showClearDataDialog = true }
            )

            HorizontalDivider(color = SabaiOffWhite, thickness = 1.dp)

            ProfileMenuRow(
                label = "Delete account",
                labelColor = SabaiError,
                showArrow = false,
                onClick = { showDeleteAccountDialog = true }
            )
        }

        if (uiState is ProfileUiState.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = (uiState as ProfileUiState.Error).message,
                color = SabaiError,
                fontSize = 13.sp
            )
        }

        statusMessage?.let { message ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = if (isStatusError) SabaiError else SabaiSuccess,
                fontSize = 13.sp
            )
        }
    }

    if (showClearDataDialog) {
        ConfirmDialog(
            title = "Clear data?",
            message = "This will delete all of your bills and groups. This can't be undone.",
            confirmLabel = "Clear data",
            onConfirm = {
                showClearDataDialog = false
                profileViewModel.clearData()
            },
            onDismiss = { showClearDataDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        ConfirmDialog(
            title = "Delete account?",
            message = "This will permanently delete your account and all of your data. This can't be undone.",
            confirmLabel = "Delete account",
            onConfirm = {
                showDeleteAccountDialog = false
                profileViewModel.deleteAccount()
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }
}
