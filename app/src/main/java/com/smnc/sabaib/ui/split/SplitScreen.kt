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
import com.smnc.sabaib.model.BillStage
import com.smnc.sabaib.model.Participant
import com.smnc.sabaib.ui.theme.SabaiBeakOrange
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiLightGray
import com.smnc.sabaib.ui.theme.SabaiNavy
import com.smnc.sabaib.ui.theme.SabaiNavyLight
import com.smnc.sabaib.ui.theme.SabaiOffWhite
import com.smnc.sabaib.ui.theme.SabaiSuccess
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow
import com.smnc.sabaib.viewmodel.BillViewModel
import kotlinx.coroutines.delay

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

    LaunchedEffect(bill.id) {
        while (true) {
            billViewModel.loadParticipants(bill.id)
            billViewModel.pollBillState(bill.id)
            billViewModel.loadItemClaims(bill.id)
            billViewModel.loadBillItemsIfMissing(bill.id)
            delay(2000)
        }
    }

    LaunchedEffect(bill.stage) {
        if (bill.stage == BillStage.PAYMENT) onContinue()
    }

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

        when {
            !bill.splitDecided -> SplitDecisionSection(
                paddingValues = paddingValues,
                viewerIsHost = viewerIsHost,
                onChooseEvenly = { billViewModel.chooseSplitEvenly(bill.id) },
                onChooseItems = { billViewModel.chooseSplitByItems(bill.id) }
            )

            bill.isSplitEvenly -> SplitEvenlyTransitionSection(paddingValues = paddingValues)

            else -> Column(
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
                        text = if (viewerIsHost) {
                            "Tap a name, then tap the dishes that are theirs."
                        } else {
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
                                displayName = participant.name,
                                isSelf = participant.id == viewerId,
                                color = participantColors[participant.id] ?: SabaiGray,
                                amount = billViewModel.calculateParticipantSubtotal(participant.id),
                                isActive = activeParticipantId == participant.id,
                                isEnabled = canAct,
                                isConfirmed = participant.isReady,
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

                            val canEditThisItem = activeParticipantId != null &&
                                    billViewModel.canControlParticipant(viewerId, activeParticipantId!!)

                            SplitItemCard(
                                item = item,
                                effectivePrice = billViewModel.itemEffectivePrice(item),
                                participants = participants,
                                participantColors = participantColors,
                                selectedParticipantIds = selectedIds,
                                isSplitEvenly = false,
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

                val selfConfirmed = viewer?.isReady == true

                Button(
                    onClick = {
                        if (viewerIsHost) {
                            billViewModel.advanceStage(bill.id, BillStage.PAYMENT)
                        } else {
                            viewerId?.let { billViewModel.toggleReady(it, bill.id) }
                        }
                    },
                    enabled = if (viewerIsHost) {
                        !hasUnclaimedItems
                    } else {
                        !hasUnclaimedItems && viewerId != null && !selfConfirmed
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
                    Text(
                        text = when {
                            viewerIsHost -> "Continue"
                            selfConfirmed -> "Waiting for host..."
                            else -> "Confirm"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ParticipantSplitChip(
    participant: Participant,
    displayName: String,
    isSelf: Boolean,
    color: Color,
    amount: Double,
    isActive: Boolean,
    isEnabled: Boolean,
    isConfirmed: Boolean,
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
            color = if (isSelf) SabaiYellow else SabaiBlack,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )

        if (participant.isHost) {
            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "Host",
                color = SabaiGray,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "฿${"%.0f".format(amount)}",
            style = MaterialTheme.typography.bodyMedium,
            color = SabaiGray
        )

        if (isConfirmed) {
            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(SabaiSuccess, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = SabaiWhite,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/**
 * Shown before the host has made the evenly-vs-by-item call - see
 * [com.smnc.sabaib.model.Bill.splitDecided]. Only the host can act here;
 * everyone else just waits, so nobody can start tapping items before the
 * host has actually decided how the bill is being split at all.
 */
@Composable
private fun SplitDecisionSection(
    paddingValues: PaddingValues,
    viewerIsHost: Boolean,
    onChooseEvenly: () -> Unit,
    onChooseItems: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "How should this bill be split?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SabaiBlack
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (viewerIsHost) {
            Text(
                text = "Choose how everyone pays. This can't be changed later.",
                style = MaterialTheme.typography.bodySmall,
                color = SabaiGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onChooseEvenly,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SabaiYellow,
                    contentColor = SabaiBlack
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Split Evenly", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onChooseItems,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Split by Items", fontWeight = FontWeight.Bold)
            }
        } else {
            CircularProgressIndicator(color = SabaiYellow)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Waiting for the host to choose how to split the bill...",
                style = MaterialTheme.typography.bodySmall,
                color = SabaiGray
            )
        }
    }
}

/**
 * Brief transitional state after the host picks "Split Evenly": there's
 * nothing to divide up per-item, so the bill's stage is already moving to
 * PAYMENT (see [com.smnc.sabaib.viewmodel.BillViewModel.chooseSplitEvenly]).
 * Guests see this for up to one poll interval while their own device
 * catches up to that stage change.
 */
@Composable
private fun SplitEvenlyTransitionSection(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = SabaiYellow)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Splitting evenly - heading to payment...",
            style = MaterialTheme.typography.bodySmall,
            color = SabaiGray
        )
    }
}
