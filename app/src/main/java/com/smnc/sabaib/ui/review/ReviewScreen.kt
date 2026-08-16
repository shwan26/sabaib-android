package com.smnc.sabaib.ui.review

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReviewScreen(
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Review Receipt")

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text("Shrimp Fried Rice")
        Text("ข้าวผัดกุ้ง — ฿120")

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text("Thai Milk Tea")
        Text("ชาไทย — ฿65")

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = onContinue
        ) {
            Text("Create Group")
        }
    }
}