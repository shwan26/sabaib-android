package com.smnc.sabaib.domain.scan

import android.graphics.Bitmap
import com.smnc.sabaib.data.GeminiClient
import com.smnc.sabaib.model.ReceiptItem
import com.smnc.sabaib.util.toJpegBase64
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

/**
 * Reads a photographed receipt with Gemini's vision API: translates item
 * names from any language to English (keeping the original), and separates each
 * line item from its price - see [ReceiptParser] for the regex-based
 * fallback used when this fails.
 */
object GeminiReceiptScanner {

    private val json = Json { ignoreUnknownKeys = true }

    private val PROMPT = """
        You are reading a photo of a restaurant or shop receipt. It may be
        from any country, and item names may be in any language or script
        (for example Thai, Japanese, Chinese, Korean, Vietnamese, Arabic,
        or a European language), or mix several languages with English.

        Extract every purchasable line item - do not include totals,
        subtotal, VAT, tax, service charge, discount, change, or any
        table/order/receipt/cashier metadata lines.

        For each item return:
        - englishName: the item's name translated into natural English, as a
          menu would describe the dish or product (keep it short)
        - originalName: the item's name exactly as printed in its original
          language and script, or an empty string if the receipt only shows
          an English name
        - quantity: the quantity as printed, or 1 if not shown
        - price: the unit price as printed on the receipt (not the line
          total, if quantity and unit price are both shown separately), as a
          plain number in the receipt's currency - read the local number
          format correctly (e.g. "1.200" or "1 200" can mean one thousand
          two hundred) and drop currency symbols

        Return only JSON matching the given schema - no extra text.
    """.trimIndent()

    private val responseSchema = buildJsonObject {
        put("type", JsonPrimitive("ARRAY"))
        put("items", buildJsonObject {
            put("type", JsonPrimitive("OBJECT"))
            put("properties", buildJsonObject {
                put("englishName", buildJsonObject { put("type", JsonPrimitive("STRING")) })
                put("originalName", buildJsonObject { put("type", JsonPrimitive("STRING")) })
                put("quantity", buildJsonObject { put("type", JsonPrimitive("INTEGER")) })
                put("price", buildJsonObject { put("type", JsonPrimitive("NUMBER")) })
            })
            put("required", buildJsonArray {
                add(JsonPrimitive("englishName"))
                add(JsonPrimitive("price"))
            })
        })
    }

    suspend fun scan(bitmap: Bitmap): List<ReceiptItem> {
        val imageBase64 = bitmap.toJpegBase64()

        val responseText = GeminiClient.generateContent(
            prompt = PROMPT,
            imageBase64 = imageBase64,
            mimeType = "image/jpeg",
            responseSchema = responseSchema
        )

        val parsedItems = json.decodeFromString(
            ListSerializer(GeminiParsedItem.serializer()),
            responseText
        )

        return parsedItems
            .filter { it.englishName.isNotBlank() && it.price > 0.0 }
            .map { parsed ->
                ReceiptItem(
                    id = UUID.randomUUID().toString(),
                    originalName = parsed.originalName,
                    englishName = parsed.englishName,
                    quantity = parsed.quantity.coerceAtLeast(1),
                    price = parsed.price
                )
            }
    }
}

@Serializable
private data class GeminiParsedItem(
    val englishName: String,
    val originalName: String = "",
    val quantity: Int = 1,
    val price: Double
)
