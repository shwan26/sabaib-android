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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.viewmodel.BillViewModel

@Composable
fun ParticipantsScreen(
    billViewModel: BillViewModel,
    onContinue: () -> Unit
) {
    val participants = billViewModel.participants.value

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
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (participant.isHost) {
                        Text(
                            text = "Host",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            enabled = participants.isNotEmpty()
        ) {
            Text("Start splitting")
        }
    }
}