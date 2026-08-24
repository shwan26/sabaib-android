package com.smnc.sabaib.domain.scan

import com.smnc.sabaib.model.ReceiptItem
import java.util.UUID

/**
 * Turns raw OCR text from a photographed receipt into a best-effort list
 * of [ReceiptItem]s.
 *
 * This is a heuristic, line-based parser - not a full receipt-understanding
 * model. Receipts vary wildly in layout, and on-device OCR of Thai text is
 * unreliable (see [com.smnc.sabaib.util.recognizeTextFrom]), so the goal
 * here is a reasonable first draft that the user then corrects on the
 * Review screen, not a perfect read.
 */
object ReceiptParser {

    // Lines containing these words are treated as totals/metadata, not
    // purchasable items, and are dropped.
    private val ignoredLineKeywords = listOf(
        "total", "subtotal", "sub total", "vat", "tax", "service charge",
        "svc chg", "change", "cash", "amount due", "discount", "tip",
        "thank you", "receipt no", "table", "order no", "guest",
        "bill no", "cashier", "date", "time", "qty"
    )

    // Optional leading quantity, e.g. "2x ", "2 x ", "x2 "
    private val leadingQuantityRegex = Regex("""^(\d{1,2})\s*[xX]\s+""")

    // Trailing price, e.g. "120.00", "120", "1,200.50", with an optional
    // trailing currency marker.
    private val trailingPriceRegex = Regex(
        """([0-9][0-9,]*(?:\.\d{1,2})?)\s*(?:฿|บาท|thb)?\s*$""",
        RegexOption.IGNORE_CASE
    )

    fun parse(rawText: String): List<ReceiptItem> {
        return rawText
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { line ->
                ignoredLineKeywords.any { keyword ->
                    line.contains(keyword, ignoreCase = true)
                }
            }
            .mapNotNull { line -> parseLine(line) }
    }

    private fun parseLine(line: String): ReceiptItem? {
        val priceMatch = trailingPriceRegex.find(line) ?: return null

        val priceText = priceMatch.groupValues[1].replace(",", "")
        val price = priceText.toDoubleOrNull() ?: return null

        // A price of 0, or a line that's just a bare number with nothing
        // else on it, is almost never a real item.
        if (price <= 0.0) return null

        var namePart = line.substring(0, priceMatch.range.first).trim()

        var quantity = 1
        val qtyMatch = leadingQuantityRegex.find(namePart)
        if (qtyMatch != null) {
            quantity = qtyMatch.groupValues[1].toIntOrNull() ?: 1
            namePart = namePart.substring(qtyMatch.range.last + 1).trim()
        }

        if (namePart.isBlank()) return null

        return ReceiptItem(
            id = UUID.randomUUID().toString(),
            thaiName = "",
            englishName = namePart,
            quantity = quantity,
            price = price
        )
    }
}