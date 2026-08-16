package com.smnc.sabaib.ui.payment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PaymentScreen() {
    Column(
        modifier = Modifier.padding(24.dp)
    ) {
        Text("Payment")

        Text("Alex owes ฿194.16")
    }
}