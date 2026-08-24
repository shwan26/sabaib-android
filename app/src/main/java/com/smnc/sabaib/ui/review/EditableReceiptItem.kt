package com.smnc.sabaib.ui.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.model.ReceiptItem

@Composable
fun EditableReceiptItem(
    item: ReceiptItem,
    onItemChange: (ReceiptItem) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {

                Column(modifier = Modifier.weight(1f)) {

                    if (!isEditing) {
                        Text(
                            text = item.englishName,
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (item.thaiName.isNotBlank()) {
                            Text(
                                text = item.thaiName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remove")
                }
            }

            if (isEditing) {

                OutlinedTextField(
                    value = item.englishName,
                    onValueChange = {
                        onItemChange(
                            item.copy(englishName = it)
                        )
                    },
                    label = {
                        Text("English name")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = item.thaiName,
                    onValueChange = {
                        onItemChange(
                            item.copy(thaiName = it)
                        )
                    },
                    label = {
                        Text("Thai name")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = item.price.toString(),
                    onValueChange = { value ->

                        val newPrice = value.toDoubleOrNull()

                        if (newPrice != null) {
                            onItemChange(
                                item.copy(price = newPrice)
                            )
                        }
                    },
                    label = {
                        Text("Price")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Button(
                        onClick = {
                            isEditing = false
                        }
                    ) {
                        Text("Done")
                    }
                }

            } else {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    OutlinedButton(
                        onClick = {
                            if (item.quantity > 1) {
                                onItemChange(
                                    item.copy(
                                        quantity = item.quantity - 1
                                    )
                                )
                            }
                        }
                    ) {
                        Text("-")
                    }

                    Text(
                        text = item.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedButton(
                        onClick = {
                            onItemChange(
                                item.copy(
                                    quantity = item.quantity + 1
                                )
                            )
                        }
                    ) {
                        Text("+")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "฿${"%.2f".format(item.price * item.quantity)}"
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        isEditing = true
                    }
                ) {
                    Text("Edit")
                }
            }
        }
    }
}