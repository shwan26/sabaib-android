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
 * Reads a photographed receipt with Gemini's vision API: translates Thai
 * item names to English (keeping the Thai original), and separates each
 * line item from its price - see [ReceiptParser] for the regex-based
 * fallback used when this fails.
 */
object GeminiReceiptScanner {

    private val json = Json { ignoreUnknownKeys = true }

    private val PROMPT = """
        You are reading a photo of a restaurant receipt from Thailand. Item
        names may be in Thai, English, or both.

        Extract every purchasable line item - do not include totals,
        subtotal, VAT, tax, service charge, discount, change, or any
        table/order/receipt/cashier metadata lines.

        For each item return:
        - englishName: the item's name translated into English (translate
          from Thai if needed)
        - thaiName: the item's original Thai name exactly as printed, or an
          empty string if the receipt only shows an English name
        - quantity: the quantity as printed, or 1 if not shown
        - price: the unit price as printed on the receipt (not the line
          total, if quantity and unit price are both shown separately)

        Return only JSON matching the given schema - no extra text.
    """.trimIndent()

    private val responseSchema = buildJsonObject {
        put("type", JsonPrimitive("ARRAY"))
        put("items", buildJsonObject {
            put("type", JsonPrimitive("OBJECT"))
            put("properties", buildJsonObject {
                put("englishName", buildJsonObject { put("type", JsonPrimitive("STRING")) })
                put("thaiName", buildJsonObject { put("type", JsonPrimitive("STRING")) })
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
                    thaiName = parsed.thaiName,
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
    val thaiName: String = "",
    val quantity: Int = 1,
    val price: Double
)
