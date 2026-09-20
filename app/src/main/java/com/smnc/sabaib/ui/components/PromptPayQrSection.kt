package com.smnc.sabaib.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiOffWhite
import com.smnc.sabaib.ui.theme.SabaiWhite

/**
 * Host-only card for optionally attaching a PromptPay QR image to the bill.
 * Shown on the Payment screen once the host has confirmed how the bill is
 * split - see [com.smnc.sabaib.viewmodel.BillViewModel.uploadPromptPayQr].
 */
@Composable
fun PromptPayQrSection(
    qrUrl: String?,
    isUploading: Boolean,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SabaiWhite, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "PromptPay QR code",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = SabaiBlack
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Optional - add your QR so participants can pay you directly.",
            style = MaterialTheme.typography.bodySmall,
            color = SabaiGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isUploading -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Uploading...",
                        color = SabaiGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            qrUrl != null -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = qrUrl,
                        contentDescription = "Your PromptPay QR code",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .background(SabaiOffWhite, RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "QR code added",
                        color = SabaiBlack,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(onClick = onAddClick) {
                        Text("Change")
                    }

                    TextButton(onClick = onRemoveClick) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            else -> {
                OutlinedButton(
                    onClick = onAddClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add PromptPay QR")
                }
            }
        }
    }
}
