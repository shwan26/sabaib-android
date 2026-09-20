package com.smnc.sabaib.data

import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage

private const val PROMPTPAY_QR_BUCKET = "promptpay-qrs"

class AccountRepository(
    private val authRepository: AuthRepository = AuthRepository()
) {

    private val postgrest = SupabaseProvider.client.postgrest
    private val bucket = SupabaseProvider.client.storage.from(PROMPTPAY_QR_BUCKET)

    /**
     * Deletes [userId]'s bill/group history (bills they own and their
     * participant rows in bills they joined) while leaving their `profiles`
     * row - and therefore their login and display name - untouched.
     */
    suspend fun clearData(userId: String) {
        val ownedBillIds = postgrest["bills"].select {
            filter { eq("owner_id", userId) }
        }.decodeList<GroupBillRow>().map { it.id }

        if (ownedBillIds.isNotEmpty()) {
            postgrest["participants"].delete {
                filter { isIn("bill_id", ownedBillIds) }
            }
            postgrest["receipt_items"].delete {
                filter { isIn("bill_id", ownedBillIds) }
            }
            ownedBillIds.forEach { billId ->
                runCatching { bucket.delete("$billId/qr") }
            }
            postgrest["bills"].delete {
                filter { isIn("id", ownedBillIds) }
            }
        }

        postgrest["participants"].delete {
            filter { eq("user_id", userId) }
        }
    }

    /**
     * Invokes the `delete-account` Edge Function, which cascade-deletes all
     * of the caller's data server-side (using the service-role key, never
     * present in the app) and removes the Supabase Auth user itself. Local
     * sign-out just clears the now-invalid client session.
     */
    suspend fun deleteAccount() {
        SupabaseProvider.client.functions.invoke("delete-account")
        runCatching { authRepository.signOut() }
    }
}
