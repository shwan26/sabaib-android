package com.smnc.sabaib.data

import io.github.jan.supabase.postgrest.postgrest

class ItemClaimRepository {

    private val postgrest = SupabaseProvider.client.postgrest

    suspend fun insertClaim(row: ItemClaimRow) {
        postgrest["item_claims"].upsert(row) {
            onConflict = "item_id,participant_id"
        }
    }

    suspend fun deleteClaim(itemId: String, participantId: String) {
        postgrest["item_claims"].delete {
            filter {
                eq("item_id", itemId)
                eq("participant_id", participantId)
            }
        }
    }

    suspend fun listClaimsForItems(itemIds: List<String>): List<ItemClaimRow> {
        if (itemIds.isEmpty()) return emptyList()

        return postgrest["item_claims"].select {
            filter { isIn("item_id", itemIds) }
        }.decodeList()
    }
}
