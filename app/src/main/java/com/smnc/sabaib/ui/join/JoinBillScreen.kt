package com.smnc.sabaib.ui.join

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.viewmodel.BillViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinBillScreen(
    billViewModel: BillViewModel,
    onJoined: () -> Unit,
    onBack: () -> Unit
) {
    var billCode by remember {
        mutableStateOf("")
    }

    var name by remember {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Join a Bill")
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Join your friends",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Enter the bill code shared by your friend."
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            OutlinedTextField(
                value = billCode,
                onValueChange = {
                    billCode = it.uppercase()
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Bill code")
                },
                singleLine = true
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Your name")
                },
                singleLine = true
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = {
                    billViewModel.addParticipant(
                        name = name
                    )

                    onJoined()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = billCode.isNotBlank() &&
                        name.isNotBlank()
            ) {
                Text("Join Bill")
            }
        }
    }
}