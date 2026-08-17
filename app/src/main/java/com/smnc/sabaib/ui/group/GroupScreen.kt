package com.smnc.sabaib.ui.group

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.model.Participant
import com.smnc.sabaib.viewmodel.BillViewModel

@Composable
fun GroupScreen(
    billViewModel: BillViewModel,
    onContinue: () -> Unit
) {
    val participants by billViewModel.participants
    val bill by billViewModel.bill
    var hostName by remember {
        mutableStateOf("")
    }

    //fake friends
    var fakeUserNumber by remember {
        mutableIntStateOf(1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = hostName,
            onValueChange = {
                hostName = it
            },
            label = {
                Text("Your name")
            },
            placeholder = {
                Text("Biab")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {

                if (hostName.isNotBlank()) {

                    billViewModel.createHost(
                        hostName.trim()
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create group")
        }

        OutlinedButton(
            onClick = {

                billViewModel.addParticipant(
                    Participant(
                        id = System.currentTimeMillis()
                            .toString(),

                        name = "Alex"
                    )
                )
            }
        ) {
            Text("Add Alex (Test)")
        }

        OutlinedButton(
            onClick = {

                billViewModel.addParticipant(
                    Participant(
                        id = System.currentTimeMillis()
                            .toString(),

                        name = "Friend $fakeUserNumber"
                    )
                )

                fakeUserNumber++
            }
        ) {
            Text("+ Add test friend")
        }

        Text(
            text = if (bill.restaurantName.isNotBlank()) {
                bill.restaurantName
            } else {
                "Your Bill"
            },
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "${bill.items.size} items"
        )

        Text(
            text = "Subtotal ฿${"%.2f".format(bill.subtotal)}"
        )

        Text(
            text = "Group code"
        )

        Text(
            text = bill.code,
            style =
                MaterialTheme.typography.headlineMedium
        )

        participants.forEach { participant ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 4.dp
                    )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        text = participant.name
                    )

                    if (participant.isHost) {
                        Text(
                            text = "Host"
                        )
                    }
                }
            }
        }

        Button(
            onClick = onContinue,
            enabled = participants.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue to Split")
        }
    }
}