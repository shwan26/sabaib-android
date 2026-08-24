package com.smnc.sabaib.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onScanClick: () -> Unit,
    onJoinBill: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("SabaiB")

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = onScanClick
        ) {
            Text("Scan Receipt")
        }

        OutlinedButton(
            onClick = onJoinBill,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Join Bill")
        }
    }
}