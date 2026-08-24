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
    onGroupCreated: () -> Unit
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
            .padding(24.dp)
    ) {

        Text(
            text = "Create your group",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Enter your name before inviting your friends."
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

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

                    onGroupCreated()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = hostName.isNotBlank()
        ) {
            Text("Create group")
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
    }
}