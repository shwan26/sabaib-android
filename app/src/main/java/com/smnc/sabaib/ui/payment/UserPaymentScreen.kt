package com.smnc.sabaib.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.smnc.sabaib.R
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiYellow
import com.smnc.sabaib.viewmodel.BillViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPaymentScreen(
    billViewModel: BillViewModel,
    participantId: String,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onBackToHome: () -> Unit
) {

    val participants by billViewModel.participants
    val paidStatus by billViewModel.paidStatus
    val promptPayNumber by billViewModel.promptPayNumber

    val participant = participants.find { it.id == participantId }
    val total = billViewModel
        .calculateParticipantTotals()
        .find { it.participantId == participantId }
        ?.total ?: 0.0

    val isPaid = paidStatus[participantId] == true
    val name = participant?.name ?: "Participant"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$name's Payment") },
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
                .padding(16.dp)
        ) {

            if (isPaid) {
                Text(
                    text = "Paid",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
            } else {
                AsyncImage(
                    model = "https://promptpay.io/$promptPayNumber/${"%.0f".format(total)}.png",
                    contentDescription = "PromptPay QR code",
                    modifier = Modifier.size(220.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "฿${"%.0f".format(total)}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = name,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = if (participant?.isHost == true) "(host)" else if (isPaid) "Paid" else "Unpaid",
                color = SabaiGray,
                style = MaterialTheme.typography.bodySmall
            )

            if (!isPaid) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Left - ${"%.0f".format(total)}",
                    color = SabaiGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text("Back to Home")
                }

                if (!isPaid) {
                    Button(
                        onClick = onDone,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SabaiYellow,
                            contentColor = SabaiBlack
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
