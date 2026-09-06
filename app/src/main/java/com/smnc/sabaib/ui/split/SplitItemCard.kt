package com.smnc.sabaib.ui.split

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smnc.sabaib.model.Participant
import com.smnc.sabaib.model.ReceiptItem
import com.smnc.sabaib.ui.theme.SabaiBlack
import com.smnc.sabaib.ui.theme.SabaiGray
import com.smnc.sabaib.ui.theme.SabaiLightGray
import com.smnc.sabaib.ui.theme.SabaiWhite
import com.smnc.sabaib.ui.theme.SabaiYellow

@Composable
fun SplitItemCard(
    item: ReceiptItem,
    effectivePrice: Double,
    participants: List<Participant>,
    participantColors: Map<String, Color>,
    selectedParticipantIds: Set<String>,
    isSplitEvenly: Boolean,
    isHighlighted: Boolean,
    isInteractive: Boolean,
    onTap: () -> Unit
) {

    val assignedParticipants =
        if (isSplitEvenly) {
            participants
        } else {
            participants.filter { it.id in selectedParticipantIds }
        }

    val selectedCount = assignedParticipants.size

    val perPerson =
        if (selectedCount > 0) {
            effectivePrice / selectedCount
        } else {
            0.0
        }

    val highlight = isHighlighted && !isSplitEvenly

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (highlight) SabaiYellow.copy(alpha = 0.16f) else SabaiWhite
            )
            .border(
                width = if (highlight) 1.5.dp else 1.dp,
                color = if (highlight) SabaiYellow else SabaiLightGray,
                shape = RoundedCornerShape(16.dp)
            )
            .let { base ->
                if (isInteractive) base.clickable { onTap() } else base
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)) {

            Text(
                text = item.englishName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = SabaiBlack
            )

            Spacer(modifier = Modifier.width(2.dp))

            Text(
                text = when {
                    isSplitEvenly ->
                        "Split evenly · all ${participants.size}"

                    selectedCount > 0 ->
                        "Split $selectedCount way${if (selectedCount == 1) "" else "s"} · ฿${"%.0f".format(perPerson)} each"

                    else ->
                        "Tap to claim"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (selectedCount == 0 && !isSplitEvenly) MaterialTheme.colorScheme.error else SabaiGray
            )
        }

        if (assignedParticipants.isNotEmpty()) {

            Row(
                horizontalArrangement = Arrangement.spacedBy((-8).dp)
            ) {
                assignedParticipants.take(4).forEach { participant ->
                    AvatarDot(
                        initial = participant.name.take(1).uppercase(),
                        color = participantColors[participant.id] ?: SabaiGray
                    )
                }

                if (assignedParticipants.size > 4) {
                    AvatarDot(
                        initial = "+${assignedParticipants.size - 4}",
                        color = SabaiGray
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
        }

        Text(
            text = "฿${"%.0f".format(effectivePrice)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = SabaiBlack
        )
    }
}

@Composable
private fun AvatarDot(
    initial: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .border(2.dp, SabaiWhite, CircleShape)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = SabaiWhite,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
