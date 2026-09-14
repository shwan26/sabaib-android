package com.smnc.sabaib.ui.participants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.model.BillStage
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiYellow
import com.smnc.sabaib.viewmodel.BillViewModel
import kotlinx.coroutines.delay

@Composable
fun ParticipantsScreen(
    billViewModel: BillViewModel,
    onContinue: () -> Unit
) {
    val bill by billViewModel.bill
    val participants by billViewModel.participants
    val currentParticipantId by billViewModel.currentParticipantId
    val self = participants.find { it.id == currentParticipantId }

    LaunchedEffect(bill.id) {
        while (true) {
            billViewModel.loadParticipants(bill.id)
            billViewModel.pollBillState(bill.id)
            billViewModel.loadBillItemsIfMissing(bill.id)
            delay(3000)
        }
    }

    LaunchedEffect(bill.stage) {
        if (bill.stage == BillStage.SPLITTING) {
            billViewModel.clearOwnReadyForNewStage(currentParticipantId, bill.id)
            onContinue()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Who's joining?",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "${participants.size} people joined"
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        participants.forEach { participant ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = participant.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (participant.id == currentParticipantId) SabaiYellow else SabaiBlack
                    )

                    if (participant.isHost) {
                        Text(
                            text = "Host",
                            style = MaterialTheme.typography.labelMedium
                        )
                    } else if (participant.isReady) {
                        Text(
                            text = "Ready",
                            style = MaterialTheme.typography.labelMedium,
                            color = SabaiYellow
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "Waiting for host to start splitting...",
            style = MaterialTheme.typography.bodySmall,
            color = SabaiGray
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Button(
            onClick = {
                currentParticipantId?.let { billViewModel.toggleReady(it, bill.id) }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentParticipantId != null
        ) {
            Text(if (self?.isReady == true) "Not ready" else "I'm ready")
        }
    }
}