package com.smnc.sabaib.ui.split

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.model.Participant
import com.smnc.sabaib.model.ReceiptItem

@Composable
fun SplitItemCard(
    item: ReceiptItem,
    participants: List<Participant>,
    selectedParticipantIds: Set<String>,
    onParticipantToggle: (String) -> Unit,
    onSelectEveryone: () -> Unit,
    onClear: () -> Unit
) {

    val itemTotal =
        item.price * item.quantity

    val selectedCount =
        selectedParticipantIds.size

    val perPerson =
        if (selectedCount > 0) {
            itemTotal / selectedCount
        } else {
            0.0
        }

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                text = item.englishName,
                style =
                    MaterialTheme.typography
                        .titleMedium
            )

            Text(
                text = item.thaiName
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text =
                    "฿${"%.2f".format(itemTotal)}"
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onSelectEveryone
                ) {
                    Text("Everyone")
                }

                TextButton(
                    onClick = onClear,
                    enabled = selectedParticipantIds.isNotEmpty()
                ) {
                    Text("Clear")
                }
            }



            participants.forEach {
                    participant ->

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        text =
                            participant.name
                    )

                    Checkbox(
                        checked =
                            participant.id in
                                    selectedParticipantIds,

                        onCheckedChange = {
                            onParticipantToggle(
                                participant.id
                            )
                        }
                    )
                }
            }

            if (selectedCount > 0) {

                HorizontalDivider()

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Shared by $selectedCount"
                )

                Text(
                    text =
                        "฿${"%.2f".format(perPerson)} each"
                )

            } else {

                Text(
                    text =
                        "No one selected yet",
                    color =
                        MaterialTheme
                            .colorScheme
                            .error
                )
            }
        }
    }
}