package com.smnc.sabaib.ui.room

import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.R
import com.smnc.sabaib.model.Participant
import com.smnc.sabaib.ui.theme.SabaiBeakOrange
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiLightGray
import com.smnc.sabaib.ui.theme.SabaiNavy
import com.smnc.sabaib.ui.theme.SabaiNavyLight
import com.smnc.sabaib.ui.theme.SabaiOffWhite
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow
import com.smnc.sabaib.util.generateQrCode
import com.smnc.sabaib.viewmodel.BillViewModel

private val avatarColors = listOf(SabaiYellow, SabaiBeakOrange, SabaiNavy, SabaiNavyLight)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillRoomScreen(
    billViewModel: BillViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val bill by billViewModel.bill
    val participants by billViewModel.participants
    val inviteUrl = "https://sabaib.app/join/${bill.code}"
    val qrBitmap = remember(inviteUrl) {
        generateQrCode(inviteUrl)
    }
    val context = LocalContext.current

    var friendName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bill Room",
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
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SabaiYellow, RoundedCornerShape(28.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Group code",
                    fontWeight = FontWeight.Bold,
                    color = SabaiBlack
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = bill.code,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = SabaiBlack
                )

                Text(
                    text = if (bill.restaurantName.isNotBlank()) bill.restaurantName else "Your Bill",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = inviteUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = SabaiBlack
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .background(SabaiWhite, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Bill invite QR code",
                        modifier = Modifier
                            .height(150.dp)
                            .fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Share this code with your friends",
                    color = SabaiBlack
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Join my SabaiB bill!\n\nGroup code: ${bill.code}\n\n$inviteUrl"
                            )
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share SabaiB invite")
                        context.startActivity(shareIntent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SabaiWhite,
                        contentColor = SabaiBlack
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Share invite", fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SabaiOffWhite, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {

                Text(
                    text = "Who's splitting?",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    OutlinedTextField(
                        value = friendName,
                        onValueChange = { friendName = it },
                        placeholder = { Text("Add a friend's name...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SabaiWhite,
                            unfocusedContainerColor = SabaiWhite,
                            focusedBorderColor = SabaiYellow,
                            unfocusedBorderColor = SabaiLightGray,
                            cursorColor = SabaiYellow
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            val trimmed = friendName.trim()
                            if (trimmed.isNotEmpty()) {
                                billViewModel.addParticipant(name = trimmed)
                                friendName = ""
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(SabaiYellow, RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = "+",
                            color = SabaiBlack,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                if (participants.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        participants.forEachIndexed { index, participant ->
                            ParticipantChip(
                                participant = participant,
                                color = avatarColors[index % avatarColors.size]
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SabaiYellow,
                    contentColor = SabaiBlack
                ),
                shape = RoundedCornerShape(20.dp),
                enabled = participants.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .height(56.dp)
            ) {
                Text("Continue", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ParticipantChip(
    participant: Participant,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(SabaiWhite, RoundedCornerShape(50))
            .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = participant.name.take(1).uppercase(),
                color = SabaiWhite,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = if (participant.isHost) "You" else participant.name
        )
    }
}
