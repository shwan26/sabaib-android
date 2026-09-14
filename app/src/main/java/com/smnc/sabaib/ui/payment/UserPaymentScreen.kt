package com.smnc.sabaib.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.smnc.sabaib.R
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiError
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiOffWhite
import com.smnc.sabaib.ui.theme.SabaiSuccess
import com.smnc.sabaib.ui.theme.SabaiYellow
import com.smnc.sabaib.util.shareQrCode
import com.smnc.sabaib.viewmodel.BillViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPaymentScreen(
    billViewModel: BillViewModel,
    participantId: String,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onUndo: () -> Unit,
    onBackToHome: () -> Unit,
    // True for a participant viewing their own payment - they can see
    // their amount/QR and whether the host has marked them paid, but only
    // the host (via the full list in PaymentScreen) can toggle that status.
    readOnly: Boolean = false
) {

    val bill by billViewModel.bill
    val participants by billViewModel.participants
    val paidStatus by billViewModel.paidStatus

    val participant = participants.find { it.id == participantId }
    val total = billViewModel
        .calculateParticipantTotals()
        .find { it.participantId == participantId }
        ?.total ?: 0.0

    val isPaid = paidStatus[participantId] == true
    val name = participant?.name ?: "Participant"
    val amountText = "%.0f".format(total)
    val qrUrl = bill.promptPayQrUrl

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (readOnly) "Your Payment" else "$name's Payment") },
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
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            if (!isPaid) {
                Text(
                    text = if (qrUrl != null) {
                        "Scan this QR code to receive money."
                    } else {
                        "Host hasn't added a PromptPay QR yet - arrange payment with them directly."
                    },
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SabaiOffWhite, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                if (isPaid) {
                    Text(
                        text = "Paid",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                } else if (qrUrl != null) {
                    var qrState by remember {
                        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(220.dp)
                    ) {
                        AsyncImage(
                            model = qrUrl,
                            contentDescription = "PromptPay QR code",
                            onState = { qrState = it },
                            modifier = Modifier.size(220.dp)
                        )

                        when (qrState) {
                            is AsyncImagePainter.State.Loading -> CircularProgressIndicator()
                            is AsyncImagePainter.State.Error -> Text(
                                text = "Couldn't load QR",
                                color = SabaiGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                            else -> Unit
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "฿$amountText",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                } else {
                    Text(
                        text = "No QR code yet",
                        color = SabaiGray,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "฿$amountText",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            if (!isPaid && qrUrl != null) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            shareQrCode(
                                context = context,
                                qrUrl = qrUrl,
                                caption = "$name's PromptPay — ฿$amountText"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Share this Promptpay")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text(
                    text = "Status: ",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = if (isPaid) "Paid" else "Unpaid",
                    color = if (isPaid) SabaiSuccess else SabaiError,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    readOnly && isPaid -> "The host has marked this as paid."
                    readOnly -> "The host will mark this as paid once they receive it."
                    isPaid -> "Tapped by mistake? Tap \"Undo\" to revert."
                    else -> "Once you received payment, tap \"Done\"."
                },
                color = SabaiGray,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text("Back to Home")
                }

                if (!readOnly) {
                    if (isPaid) {
                        OutlinedButton(
                            onClick = onUndo,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Text("Undo", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onDone,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SabaiYellow,
                                contentColor = SabaiBlack
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
