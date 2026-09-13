package com.smnc.sabaib.data

import android.util.Log
import com.smnc.sabaib.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.io.IOException
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Duration.Companion.milliseconds

/**
 * Thin wrapper around the Gemini `generateContent` REST endpoint.
 *
 * Mirrors [SupabaseProvider]'s shape: a singleton `object` owning a
 * lazily-built Ktor [HttpClient] and reading its secret from [BuildConfig].
 */
object GeminiClient {

    private const val TAG = "GeminiClient"

    // A floating alias (rather than a pinned version like "gemini-3-flash")
    // so this keeps working as Google retires specific model versions.
    private const val MODEL = "gemini-3.8-flash"
    private const val ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    // Gemini occasionally returns 503 (model overloaded) or 429 (rate limited)
    // for reasons unrelated to the request itself - these are worth a couple
    // of quick retries before giving up and falling back to on-device OCR.
    private const val MAX_ATTEMPTS = 3
    private val retryableStatuses = setOf(
        HttpStatusCode.ServiceUnavailable,
        HttpStatusCode.TooManyRequests
    )

    private val json = Json { ignoreUnknownKeys = true }

    private val httpClient = HttpClient(Android) {
        install(HttpTimeout) {
            requestTimeoutMillis = 20_000
        }
    }

    /**
     * POSTs [requestBodyJson], retrying on transient failures - a
     * [retryableStatuses] response or a request timeout - with a short
     * backoff between attempts. Any other status is returned immediately
     * for the caller to handle.
     */
    private suspend fun postWithRetry(requestBodyJson: String): HttpResponse {
        lateinit var lastResponse: HttpResponse
        var lastTimeout: HttpRequestTimeoutException? = null

        for (attempt in 1..MAX_ATTEMPTS) {
            try {
                val response = httpClient.post(ENDPOINT) {
                    parameter("key", BuildConfig.GEMINI_API_KEY)
                    contentType(ContentType.Application.Json)
                    setBody(requestBodyJson)
                }

                if (response.status.isSuccess() || response.status !in retryableStatuses) {
                    return response
                }

                lastResponse = response
                lastTimeout = null
                Log.w(TAG, "Gemini request got ${response.status}, attempt $attempt/$MAX_ATTEMPTS")
            } catch (e: HttpRequestTimeoutException) {
                lastTimeout = e
                Log.w(TAG, "Gemini request timed out, attempt $attempt/$MAX_ATTEMPTS")
            }

            if (attempt < MAX_ATTEMPTS) {
                delay((500L * attempt).milliseconds)
            }
        }

        lastTimeout?.let { throw it }
        return lastResponse
    }

    /**
     * Sends [prompt] alongside an inline image ([imageBase64], [mimeType]) to
     * Gemini and returns the model's raw text response (expected to be a
     * JSON string matching [responseSchema]).
     */
    suspend fun generateContent(
        prompt: String,
        imageBase64: String,
        mimeType: String,
        responseSchema: JsonObject
    ): String {
        val requestBody = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = prompt),
                        GeminiPart(inlineData = GeminiInlineData(mimeType = mimeType, data = imageBase64))
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(responseSchema = responseSchema)
        )

        val response = postWithRetry(json.encodeToString(GeminiRequest.serializer(), requestBody))

        if (!response.status.isSuccess()) {
            val body = response.bodyAsText()
            Log.w(TAG, "Gemini request failed: ${response.status} - $body")
            throw IOException("Gemini request failed with status ${response.status}")
        }

        val responseBody = response.bodyAsText()

        val parsed = try {
            json.decodeFromString(GeminiResponse.serializer(), responseBody)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode Gemini response: $responseBody", e)
            throw e
        }

        return parsed.candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.firstNotNullOfOrNull { it.text }
            ?: throw IOException("Gemini response had no text content").also {
                Log.w(TAG, "Gemini response had no candidates/text: $responseBody")
            }
    }
}

@Serializable
internal data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig
)

@Serializable
internal data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
internal data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

@Serializable
internal data class GeminiInlineData(
    val mimeType: String,
    val data: String
)

@Serializable
internal data class GeminiGenerationConfig(
    val responseMimeType: String = "application/json",
    val responseSchema: JsonObject
)

@Serializable
internal data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

@Serializable
internal data class GeminiCandidate(
    val content: GeminiContent? = null
)
