package com.smnc.sabaib.data

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class ClaimItemParams(
    @SerialName("p_item_id") val itemId: String,
    @SerialName("p_participant_id") val participantId: String
)

class ItemClaimRepository {

    private val postgrest = SupabaseProvider.client.postgrest

    /**
     * Claims/unclaims an item through the `claim_item`/`unclaim_item` Postgres
     * functions rather than writing to `item_claims` directly. Those functions
     * are `SECURITY DEFINER` and re-implement the same acting-on-behalf-of rule
     * as [com.smnc.sabaib.viewmodel.BillViewModel.canControlParticipant] server
     * side, which plain table RLS policies (self-only) can't express: a host
     * needs to write claims for participants they added manually, who have no
     * `auth.uid()` of their own to match against.
     */
    suspend fun claimItem(itemId: String, participantId: String) {
        postgrest.rpc("claim_item", ClaimItemParams(itemId, participantId))
    }

    suspend fun unclaimItem(itemId: String, participantId: String) {
        postgrest.rpc("unclaim_item", ClaimItemParams(itemId, participantId))
    }

    suspend fun listClaimsForItems(itemIds: List<String>): List<ItemClaimRow> {
        if (itemIds.isEmpty()) return emptyList()

        return postgrest["item_claims"].select {
            filter { isIn("item_id", itemIds) }
        }.decodeList()
    }
}
