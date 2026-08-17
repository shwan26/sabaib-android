package com.smnc.sabaib.ui.split

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.viewmodel.BillViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitScreen(
    billViewModel: BillViewModel,
    onContinue: () -> Unit
) {
    val bill by billViewModel.bill
    val participants by billViewModel.participants
    val selections by billViewModel.itemSelections
    val hasUnclaimedItems =
        billViewModel.hasUnclaimedItems()
    val splitTotal = participants.sumOf { participant ->
        billViewModel.calculateParticipantSubtotal(participant.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Split Items")
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

            Text(
                text = "Who had what?",
                style =
                    MaterialTheme.typography
                        .headlineSmall
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text =
                    "Select everyone who shared each item."
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            LazyColumn(
                modifier =
                    Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = bill.items,
                    key = { it.id }
                ) { item ->

                    val selection =
                        selections.find {
                            it.itemId == item.id
                        }


                    SplitItemCard(
                        item = item,
                        onSelectEveryone = {

                            billViewModel
                                .selectEveryoneForItem(
                                    item.id
                                )
                        },
                        onClear = {
                            billViewModel
                                .clearItemSelection(
                                    item.id
                                )
                        },
                        participants = participants,
                        selectedParticipantIds =
                            selection
                                ?.participantIds
                                ?: emptySet(),

                        onParticipantToggle = {
                                participantId ->

                            billViewModel
                                .toggleItemSelection(
                                    itemId = item.id,
                                    participantId =
                                        participantId
                                )
                        }
                    )
                }
            }

            Text(
                text = "Current Split",
                style =
                    MaterialTheme.typography
                        .titleMedium
            )

            participants.forEach {
                    participant ->

                val total =
                    billViewModel
                        .calculateParticipantSubtotal(
                            participant.id
                        )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        text = participant.name
                    )

                    Text(
                        text =
                            "฿${"%.2f".format(total)}"
                    )
                }
            }



            if (hasUnclaimedItems) {

                Text(
                    text =
                        "Assign every item before continuing.",
                    color =
                        MaterialTheme
                            .colorScheme
                            .error
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            HorizontalDivider()

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "฿${"%.2f".format(splitTotal)}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Button(
                onClick = onContinue,
                enabled = !hasUnclaimedItems,
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }
}
