package com.smnc.sabaib.data

import com.smnc.sabaib.model.Bill
import com.smnc.sabaib.model.ReceiptItem
import com.smnc.sabaib.util.generateGroupCode
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.postgrest

private const val UNIQUE_VIOLATION = "23505"

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
                status = "waiting"
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
}
