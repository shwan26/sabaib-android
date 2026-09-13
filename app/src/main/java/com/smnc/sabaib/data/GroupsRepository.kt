package com.smnc.sabaib.data

import io.github.jan.supabase.postgrest.postgrest

class GroupsRepository {

    private val postgrest = SupabaseProvider.client.postgrest

    suspend fun fetchHostedBills(userId: String): List<GroupBillRow> {
        return postgrest["bills"].select {
            filter { eq("owner_id", userId) }
        }.decodeList<GroupBillRow>()
    }

    suspend fun fetchJoinedBills(userId: String): List<GroupBillRow> {
        val joinedBillIds = postgrest["participants"].select {
            filter {
                eq("user_id", userId)
                eq("role", "member")
            }
        }.decodeList<ParticipantRow>().map { it.billId }.distinct()

        if (joinedBillIds.isEmpty()) return emptyList()

        return postgrest["bills"].select {
            filter { isIn("id", joinedBillIds) }
        }.decodeList<GroupBillRow>()
    }

    suspend fun fetchParticipantCounts(billIds: List<String>): Map<String, Int> {
        if (billIds.isEmpty()) return emptyMap()
        return postgrest["participants"].select {
            filter { isIn("bill_id", billIds) }
        }.decodeList<ParticipantRow>().groupingBy { it.billId }.eachCount()
    }
}
