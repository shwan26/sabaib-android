package com.smnc.sabaib.ui.room

import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.util.generateQrCode
import com.smnc.sabaib.viewmodel.BillViewModel

@Composable
fun BillRoomScreen(
    billViewModel: BillViewModel,
    onStartSplitting: () -> Unit
) {
    val bill by billViewModel.bill
    val participants by billViewModel.participants
    val inviteUrl = "https://sabaib.app/join/${bill.code}"
    val qrBitmap = remember(inviteUrl) {
        generateQrCode(inviteUrl)
    }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Your Bill Room",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = if (bill.restaurantName.isNotBlank()) {
                bill.restaurantName
            } else {
                "Your Bill"
            }
        )

        Text(
            text = inviteUrl,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Group code",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = bill.code,
                    style = MaterialTheme.typography.displaySmall
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Bill invite QR code",
                    modifier = Modifier
                        .height(220.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Share this code with your friends"
                )
            }
            OutlinedButton(
                onClick = {

                    val sendIntent =
                        Intent(Intent.ACTION_SEND).apply {

                            type = "text/plain"

                            putExtra(
                                Intent.EXTRA_TEXT,
                                """
                    Join my SabaiB bill!

                    Group code: ${bill.code}

                    $inviteUrl
                    """.trimIndent()
                            )
                        }

                    val shareIntent =
                        Intent.createChooser(
                            sendIntent,
                            "Share SabaiB invite"
                        )

                    context.startActivity(shareIntent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Share invite")
            }
        }

        //temporary
        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedButton(
            onClick = {
                billViewModel.addParticipant(
                    name = "Alex"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Alex (Test)")
        }

        OutlinedButton(
            onClick = {
                billViewModel.addParticipant(
                    name = "Tulip"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Tulip (Test)")
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "${participants.size} joined",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        participants.forEach { participant ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text = participant.name
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
            onClick = onStartSplitting,
            modifier = Modifier.fillMaxWidth(),
            enabled = participants.isNotEmpty()
        ) {
            Text("Start splitting")
        }
    }
}