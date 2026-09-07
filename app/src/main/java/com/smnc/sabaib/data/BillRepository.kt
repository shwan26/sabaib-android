package com.smnc.sabaib.data

import com.smnc.sabaib.model.Bill
import com.smnc.sabaib.model.BillStage
import com.smnc.sabaib.model.ReceiptItem
import com.smnc.sabaib.util.generateGroupCode
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.postgrest
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

private const val UNIQUE_VIOLATION = "23505"
private const val BILL_RETENTION_DAYS = 7L

class BillRepository {

    private val postgrest = SupabaseProvider.client.postgrest

    suspend fun saveBill(ownerId: String, bill: Bill): BillRow {

        val billRow = try {
            insertBill(ownerId, bill, bill.code)
        } catch (e: PostgrestRestException) {
            if (e.code == UNIQUE_VIOLATION) {
                insertBill(ownerId, bill, generateGroupCode())
            } else {
                throw e
            }
        }

        if (bill.items.isNotEmpty()) {
            val itemRows = bill.items.map { it.toReceiptItemRow(billRow.id!!) }
            postgrest["receipt_items"].insert(itemRows)
        }

        return billRow
    }

    suspend fun findByCode(code: String): BillRow? =
        postgrest["bills"].select {
            filter { eq("code", code) }
        }.decodeSingleOrNull()

    suspend fun findItemsByBillId(billId: String): List<ReceiptItem> =
        postgrest["receipt_items"].select {
            filter { eq("bill_id", billId) }
        }.decodeList<ReceiptItemRow>().map { it.toReceiptItem() }

    suspend fun findById(billId: String): BillRow? =
        postgrest["bills"].select {
            filter { eq("id", billId) }
        }.decodeSingleOrNull()

    suspend fun updateStage(billId: String, stage: BillStage) {
        postgrest["bills"].update({
            BillRow::status setTo stage.dbValue
        }) {
            filter { eq("id", billId) }
        }
    }

    /** Records the host's one-time evenly-vs-by-item call for the bill. */
    suspend fun setSplitDecision(billId: String, isSplitEvenly: Boolean) {
        postgrest["bills"].update({
            BillRow::isSplitEvenly setTo isSplitEvenly
            BillRow::splitDecided setTo true
        }) {
            filter { eq("id", billId) }
        }
    }

    suspend fun updatePromptPayQrUrl(billId: String, url: String?) {
        postgrest["bills"].update({
            BillRow::promptPayQrUrl setTo url
        }) {
            filter { eq("id", billId) }
        }
    }

    private suspend fun insertBill(ownerId: String, bill: Bill, code: String): BillRow =
        postgrest["bills"].insert(
            BillRow(
                code = code,
                ownerId = ownerId,
                restaurantName = bill.restaurantName,
                subtotal = bill.subtotal,
                serviceChargePercent = bill.serviceChargeRate * 100,
                serviceChargeAmount = bill.serviceChargeAmount,
                vatPercent = bill.vatRate * 100,
                vatAmount = bill.vatAmount,
                discountAmount = bill.discount,
                totalAmount = bill.total,
                status = "waiting",
                isSplitEvenly = false,
                deleteAfter = Instant.now().plus(BILL_RETENTION_DAYS, ChronoUnit.DAYS)
            )
        ) { select() }.decodeSingle()

    private fun ReceiptItem.toReceiptItemRow(billId: String) = ReceiptItemRow(
        billId = billId,
        originalName = thaiName.ifBlank { englishName },
        translatedName = englishName.takeIf { thaiName.isNotBlank() },
        quantity = quantity.toDouble(),
        unitPrice = price,
        totalPrice = price * quantity
    )

    private fun ReceiptItemRow.toReceiptItem() = ReceiptItem(
        id = id ?: UUID.randomUUID().toString(),
        thaiName = if (translatedName != null) originalName else "",
        englishName = translatedName ?: originalName,
        quantity = quantity.toInt(),
        price = unitPrice
    )
}
