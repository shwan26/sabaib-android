package com.smnc.sabaib.ui.charges

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChargesScreen(
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp)
    ) {
        Text("Charges")

        Button(
            onClick = onContinue
        ) {
            Text("Continue to Payment")
        }
    }
}