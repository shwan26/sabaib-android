package com.smnc.sabaib.ui.split

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.R
import com.smnc.sabaib.model.Participant
import com.smnc.sabaib.ui.theme.SabaiBeakOrange
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiLightGray
import com.smnc.sabaib.ui.theme.SabaiNavy
import com.smnc.sabaib.ui.theme.SabaiNavyLight
import com.smnc.sabaib.ui.theme.SabaiOffWhite
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow
import com.smnc.sabaib.viewmodel.BillViewModel

private val avatarColors = listOf(SabaiYellow, SabaiBeakOrange, SabaiNavy, SabaiNavyLight)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitScreen(
    billViewModel: BillViewModel,
    onBack: () -> Unit = {},
    onContinue: () -> Unit
) {
    val bill by billViewModel.bill
    val participants by billViewModel.participants
    val selections by billViewModel.itemSelections
    val currentParticipantId by billViewModel.currentParticipantId

    val viewerId = currentParticipantId
        ?: participants.firstOrNull { it.isHost }?.id
        ?: participants.firstOrNull()?.id

    val viewer = participants.find { it.id == viewerId }
    val viewerIsHost = viewer?.isHost == true

    val participantColors = remember(participants) {
        participants.mapIndexed { index, participant ->
            participant.id to avatarColors[index % avatarColors.size]
        }.toMap()
    }

    var activeParticipantId by remember(viewerId) {
        mutableStateOf(viewerId)
    }

    val hasUnclaimedItems = billViewModel.hasUnclaimedItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Split Items",
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(SabaiOffWhite, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {

                Text(
                    text = when {
                        bill.isSplitEvenly ->
                            "The bill is split evenly between everyone."
                        viewerIsHost ->
                            "Tap a name, then tap the dishes that are theirs."
                        else ->
                            "Tap a dish that's yours."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = SabaiGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = participants,
                        key = { it.id }
                    ) { participant ->

                        val canAct = billViewModel.canControlParticipant(
                            viewerId,
                            participant.id
                        )

                        ParticipantSplitChip(
                            participant = participant,
                            displayName = if (participant.id == viewerId) "You" else participant.name,
                            color = participantColors[participant.id] ?: SabaiGray,
                            amount = billViewModel.calculateParticipantSubtotal(participant.id),
                            isActive = !bill.isSplitEvenly && activeParticipantId == participant.id,
                            isEnabled = canAct,
                            onClick = {
                                if (canAct) {
                                    activeParticipantId = participant.id
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    items(
                        items = bill.items,
                        key = { it.id }
                    ) { item ->

                        val selection = selections.find {
                            it.itemId == item.id
                        }

                        val selectedIds = selection?.participantIds ?: emptySet()

                        val canEditThisItem = !bill.isSplitEvenly &&
                                activeParticipantId != null &&
                                billViewModel.canControlParticipant(viewerId, activeParticipantId!!)

                        SplitItemCard(
                            item = item,
                            effectivePrice = billViewModel.itemEffectivePrice(item),
                            participants = participants,
                            participantColors = participantColors,
                            selectedParticipantIds = selectedIds,
                            isSplitEvenly = bill.isSplitEvenly,
                            isHighlighted = activeParticipantId != null &&
                                    activeParticipantId in selectedIds,
                            isInteractive = canEditThisItem,
                            onTap = {
                                activeParticipantId?.let { id ->
                                    billViewModel.toggleItemSelection(
                                        itemId = item.id,
                                        participantId = id
                                    )
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))

                        SplitEvenlyRow(
                            checked = bill.isSplitEvenly,
                            enabled = viewerIsHost,
                            onToggle = {
                                billViewModel.setSplitEvenly(!bill.isSplitEvenly)
                            }
                        )
                    }
                }
            }

            if (hasUnclaimedItems) {

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Assign every item before continuing.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Your total",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SabaiGray
                )

                Text(
                    text = "฿${"%.0f".format(
                        viewerId?.let { billViewModel.calculateParticipantSubtotal(it) } ?: 0.0
                    )}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SabaiBlack
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinue,
                enabled = !hasUnclaimedItems && viewerId != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SabaiYellow,
                    contentColor = SabaiBlack
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Continue", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ParticipantSplitChip(
    participant: Participant,
    displayName: String,
    color: Color,
    amount: Double,
    isActive: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .alpha(if (isEnabled) 1f else 0.45f)
            .background(SabaiWhite, RoundedCornerShape(50))
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) SabaiBlack else SabaiLightGray,
                shape = RoundedCornerShape(50)
            )
            .let { base ->
                if (isEnabled) base.clickable { onClick() } else base
            }
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
            text = displayName,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "฿${"%.0f".format(amount)}",
            style = MaterialTheme.typography.bodyMedium,
            color = SabaiGray
        )
    }
}

@Composable
private fun SplitEvenlyRow(
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.6f)
            .let { base ->
                if (enabled) base.clickable { onToggle() } else base
            }
            .padding(vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .border(
                    width = 1.5.dp,
                    color = if (checked) SabaiYellow else SabaiLightGray,
                    shape = CircleShape
                )
                .background(
                    if (checked) SabaiYellow else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(SabaiWhite, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Split all evenly to everyone",
            style = MaterialTheme.typography.bodyMedium,
            color = SabaiBlack
        )
    }
}
