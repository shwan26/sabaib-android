package com.smnc.sabaib.ui.charges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.domain.charges.ChargeCalculator
import com.smnc.sabaib.viewmodel.BillViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargesScreen(
    billViewModel: BillViewModel,
    onContinue: () -> Unit
) {

    val bill by billViewModel.bill

    var discount by remember {
        mutableStateOf("0")
    }

    val discountAmount =
        discount
            .toDoubleOrNull()
            ?: 0.0

    val preview =
        ChargeCalculator.calculate(
            subtotal = bill.subtotal,
            serviceChargeRate =
                bill.serviceChargeRate,
            vatRate =
                bill.vatRate,
            discount =
                discountAmount,
            isVatIncluded =
                bill.isVatIncluded
        )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Charges")
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Receipt charges",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Adjust any final discount before splitting."
            )

            Text(
                text = "Food subtotal: ฿${"%.2f".format(bill.subtotal)}"
            )

            OutlinedTextField(
                value = discount,
                onValueChange = {
                    discount = it
                },
                label = {
                    Text("Discount ฿")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            ChargeRow(
                label = "Subtotal",
                amount = preview.subtotal
            )

            ChargeRow(
                label = "Other charges",
                amount = preview.serviceCharge
            )

            ChargeRow(
                label = "VAT",
                amount = preview.vat
            )

            ChargeRow(
                label = "Discount",
                amount = -preview.discount
            )

            HorizontalDivider()

            ChargeRow(
                label = "Total",
                amount = preview.total
            )

            HorizontalDivider()

            // Final split
            Text(
                text = "Final split",
                style = MaterialTheme.typography.titleMedium
            )

            billViewModel
                .calculateParticipantTotals()
                .forEach { person ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = person.participantName
                        )

                        Text(
                            text = "฿${"%.2f".format(person.total)}"
                        )
                    }
                }

            // Continue button
            Button(
                onClick = {

                    billViewModel.updateCharges(
                        serviceChargeRate = bill.serviceChargeRate,
                        vatRate = bill.vatRate,
                        discount = discountAmount,
                        isVatIncluded = bill.isVatIncluded
                    )

                    onContinue()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }

}

@Composable
fun ChargeRow(
    label: String,
    amount: Double
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(label)

        Text(
            text =
                if (amount < 0) {
                    "-฿${"%.2f".format(-amount)}"
                } else {
                    "฿${"%.2f".format(amount)}"
                }
        )
    }
}