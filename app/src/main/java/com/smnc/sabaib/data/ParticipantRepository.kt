package com.smnc.sabaib.data

import io.github.jan.supabase.postgrest.postgrest

class ParticipantRepository {

    private val postgrest = SupabaseProvider.client.postgrest

    suspend fun insertParticipant(row: ParticipantRow): ParticipantRow =
        postgrest["participants"].insert(row) { select() }.decodeSingle()

    suspend fun listByBillId(billId: String): List<ParticipantRow> =
        postgrest["participants"].select {
            filter { eq("bill_id", billId) }
        }.decodeList()

    suspend fun deleteParticipant(id: String) {
        postgrest["participants"].delete {
            filter { eq("id", id) }
        }
    }

    suspend fun setReady(participantId: String, isReady: Boolean) {
        postgrest["participants"].update({
            ParticipantRow::isReady setTo isReady
        }) {
            filter { eq("id", participantId) }
        }
    }
}
