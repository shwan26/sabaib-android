package com.smnc.sabaib.ui.payment

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.R
import com.smnc.sabaib.ui.components.PromptPayQrSection
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiCharcoal
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow
import com.smnc.sabaib.viewmodel.BillViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    billViewModel: BillViewModel,
    onParticipantClick: (String) -> Unit,
    onBackToHome: () -> Unit,
    onBack: () -> Unit = {}
) {

    val bill by billViewModel.bill
    val participants by billViewModel.participants
    val paidStatus by billViewModel.paidStatus
    val currentParticipantId by billViewModel.currentParticipantId
    val isUploadingPromptPayQr by billViewModel.isUploadingPromptPayQr

    val totals = billViewModel.calculateParticipantTotals()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val qrPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        coroutineScope.launch {
            val bytes = runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }.getOrNull()

            if (bytes != null) {
                val contentType = context.contentResolver.getType(uri)
                billViewModel.uploadPromptPayQr(bill.id, bytes, contentType)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Split Items",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back_24),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SabaiCharcoal, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Bill Total" +
                            if (bill.restaurantName.isNotBlank()) " · ${bill.restaurantName}" else "",
                    color = SabaiGray,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "฿${"%.0f".format(bill.total)}",
                    color = SabaiYellow,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            PromptPayQrSection(
                qrUrl = bill.promptPayQrUrl,
                isUploading = isUploadingPromptPayQr,
                onAddClick = { qrPickerLauncher.launch("image/*") },
                onRemoveClick = { billViewModel.removePromptPayQr(bill.id) }
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                totals.forEach { personTotal ->

                    val participant = participants.find {
                        it.id == personTotal.participantId
                    }

                    val statusLabel = when {
                        participant?.isHost == true -> "(host)"
                        paidStatus[personTotal.participantId] == true -> "Paid"
                        else -> "Unpaid"
                    }

                    val displayName = personTotal.participantName
                    val isSelf = participant?.id == currentParticipantId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SabaiWhite, RoundedCornerShape(16.dp))
                            .clickable {
                                onParticipantClick(personTotal.participantId)
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = displayName,
                                color = if (isSelf) SabaiYellow else SabaiBlack,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = statusLabel,
                                color = SabaiGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text(
                            text = "฿${"%.0f".format(personTotal.total)}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Button(
                onClick = onBackToHome,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SabaiYellow,
                    contentColor = SabaiBlack
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Back to Home", fontWeight = FontWeight.Bold)
            }
        }
    }
}
