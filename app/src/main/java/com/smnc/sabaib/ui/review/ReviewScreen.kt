package com.smnc.sabaib.ui.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.model.sampleReceiptItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    onContinue: () -> Unit
) {
    val subtotal = sampleReceiptItems.sumOf {
        it.price * it.quantity
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Review Receipt")
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            Text(
                text = "Check your receipt",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Make sure the items and prices are correct."
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sampleReceiptItems) { item ->

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = item.englishName,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = item.thaiName,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "฿${"%.2f".format(item.price)}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            ReceiptSummary(
                subtotal = subtotal
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
fun ReceiptSummary(
    subtotal: Double
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider()

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Subtotal")

            Text(
                text = "฿${"%.2f".format(subtotal)}"
            )
        }
    }
}

