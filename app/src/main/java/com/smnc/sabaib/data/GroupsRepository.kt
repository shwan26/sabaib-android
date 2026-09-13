package com.smnc.sabaib.data

import io.github.jan.supabase.postgrest.postgrest

class GroupsRepository {

    private val postgrest = SupabaseProvider.client.postgrest

    suspend fun fetchBillsForUser(userId: String): List<GroupBillRow> {
        val owned = postgrest["bills"].select {
            filter { eq("owner_id", userId) }
        }.decodeList<GroupBillRow>()

        val participantBillIds = postgrest["participants"].select {
            filter { eq("user_id", userId) }
        }.decodeList<ParticipantRow>().map { it.billId }.distinct()

        val joined = if (participantBillIds.isNotEmpty()) {
            postgrest["bills"].select {
                filter { isIn("id", participantBillIds) }
            }.decodeList<GroupBillRow>()
        } else {
            emptyList()
        }

        return (owned + joined).distinctBy { it.id }
    }

    suspend fun fetchParticipantCounts(billIds: List<String>): Map<String, Int> {
        if (billIds.isEmpty()) return emptyMap()
        return postgrest["participants"].select {
            filter { isIn("bill_id", billIds) }
        }.decodeList<ParticipantRow>().groupingBy { it.billId }.eachCount()
    }
}
