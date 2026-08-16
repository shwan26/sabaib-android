package com.smnc.sabaib.ui.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.model.sampleReceiptItems
import androidx.compose.runtime.*
import com.smnc.sabaib.model.ReceiptItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    onContinue: () -> Unit
) {
    var receiptItems by remember {
        mutableStateOf(sampleReceiptItems)
    }

    val subtotal = receiptItems.sumOf {
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
                items(
                    items = receiptItems,
                    key = { it.id }
                ) { item ->

                    EditableReceiptItem(
                        item = item,

                        onItemChange = { updatedItem ->

                            receiptItems = receiptItems.map {

                                if (it.id == updatedItem.id) {
                                    updatedItem
                                } else {
                                    it
                                }
                            }
                        },

                        onDelete = {

                            receiptItems = receiptItems.filter {
                                it.id != item.id
                            }
                        }
                    )
                }
            }

            OutlinedButton(
                onClick = {

                    val newItem = ReceiptItem(
                        id = System.currentTimeMillis().toString(),
                        thaiName = "",
                        englishName = "New item",
                        quantity = 1,
                        price = 0.0
                    )

                    receiptItems = receiptItems + newItem
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Add item")
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

