package com.smnc.sabaib.ui.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.R
import com.smnc.sabaib.model.ReceiptItem
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiLightGray

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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SabaiLightGray),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

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
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = item.quantity.toString(),
                        onValueChange = { value ->

                            val newQuantity = value.toIntOrNull()

                            if (newQuantity != null && newQuantity > 0) {
                                onItemChange(
                                    item.copy(quantity = newQuantity)
                                )
                            }
                        },
                        label = {
                            Text("Qty")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

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
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    IconButton(
                        onClick = { isEditing = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.edit_24),
                            contentDescription = "Edit item"
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {

                        Text(
                            text = item.englishName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (item.thaiName.isNotBlank()) {
                            Text(
                                text = item.thaiName,
                                style = MaterialTheme.typography.bodySmall,
                                color = SabaiGray
                            )
                        }
                    }

                    Text(
                        text = item.quantity.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Text(
                        text = "฿${"%.0f".format(item.price * item.quantity)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.delete_24),
                            contentDescription = "Remove item",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
