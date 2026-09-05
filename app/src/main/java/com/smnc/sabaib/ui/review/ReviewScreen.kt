package com.smnc.sabaib.ui.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.smnc.sabaib.domain.charges.ChargeCalculator
import com.smnc.sabaib.model.ReceiptItem
import com.smnc.sabaib.model.sampleReceiptItems
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow
import com.smnc.sabaib.viewmodel.BillViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    billViewModel: BillViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    // Seed from whatever the scan step already produced (or from an
    // in-progress edit if the user navigated back here). Only fall back
    // to sample data if there's genuinely nothing yet, e.g. when jumping
    // straight to this screen during development.
    var receiptItems by remember {
        val existingItems = billViewModel.bill.value.items
        mutableStateOf(
            if (existingItems.isNotEmpty()) existingItems else sampleReceiptItems
        )
    }

    val defaultLabel = remember {
        SimpleDateFormat("d MMM", Locale.getDefault()).format(Date()) + " Receipt"
    }

    var restaurantName by remember {
        val existingName = billViewModel.bill.value.restaurantName
        mutableStateOf(existingName.ifBlank { defaultLabel })
    }

    var isEditingLabel by remember { mutableStateOf(false) }

    var vatIncluded by remember { mutableStateOf(false) }
    var vatPercent by remember { mutableStateOf("7") }
    var otherCharges by remember { mutableStateOf("0") }

    val subtotal = receiptItems.sumOf {
        it.price * it.quantity
    }

    val otherChargesAmount = otherCharges.toDoubleOrNull() ?: 0.0
    val vatRate = (vatPercent.toDoubleOrNull() ?: 0.0) / 100
    val otherChargesRate = if (subtotal > 0) otherChargesAmount / subtotal else 0.0

    val preview = ChargeCalculator.calculate(
        subtotal = subtotal,
        serviceChargeRate = otherChargesRate,
        vatRate = vatRate,
        discount = 0.0,
        isVatIncluded = vatIncluded
    )

    val inputFieldShape = RoundedCornerShape(12.dp)
    val inputFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = SabaiWhite,
        unfocusedContainerColor = SabaiWhite,
        focusedBorderColor = SabaiYellow,
        unfocusedBorderColor = SabaiBlack,
        cursorColor = SabaiYellow
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Confirm Items",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back_24),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        if (isEditingLabel) {
                            OutlinedTextField(
                                value = restaurantName,
                                onValueChange = { restaurantName = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = inputFieldShape,
                                colors = inputFieldColors
                            )

                            TextButton(onClick = { isEditingLabel = false }) {
                                Text("Done")
                            }
                        } else {
                            Text(
                                text = restaurantName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SabaiYellow
                            )

                            IconButton(
                                onClick = { isEditingLabel = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.edit_24),
                                    contentDescription = "Edit receipt name",
                                    tint = SabaiYellow
                                )
                            }
                        }
                    }
                }

                item {
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
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, SabaiBlack),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("+ Add Item", fontWeight = FontWeight.Bold)
                    }
                }

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

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Checkbox(
                            checked = vatIncluded,
                            onCheckedChange = { vatIncluded = it }
                        )

                        Text("Vat and other charges are already included.")
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text("VAT", style = MaterialTheme.typography.bodyLarge)

                        OutlinedTextField(
                            value = vatPercent,
                            onValueChange = { vatPercent = it },
                            suffix = { Text("%") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = inputFieldShape,
                            colors = inputFieldColors,
                            modifier = Modifier.width(100.dp)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text("Other charges", style = MaterialTheme.typography.bodyLarge)

                        OutlinedTextField(
                            value = otherCharges,
                            onValueChange = { otherCharges = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = inputFieldShape,
                            colors = inputFieldColors,
                            modifier = Modifier.width(100.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {

                HorizontalDivider()

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "฿${"%.1f".format(preview.total)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        billViewModel.updateRestaurantName(restaurantName)
                        billViewModel.updateItems(receiptItems)
                        billViewModel.updateCharges(
                            serviceChargeRate = otherChargesRate,
                            vatRate = vatRate,
                            discount = 0.0,
                            isVatIncluded = vatIncluded
                        )
                        billViewModel.createHost("You")
                        onContinue()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SabaiYellow,
                        contentColor = SabaiBlack
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Continue to create Group", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
