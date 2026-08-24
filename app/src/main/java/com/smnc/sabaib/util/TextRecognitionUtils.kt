package com.smnc.sabaib.util

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Runs Google ML Kit's on-device text recognizer against [bitmap] and
 * returns the raw recognized text, one receipt line roughly per text line.
 *
 * Note: ML Kit's on-device recognizer only supports Latin, Chinese,
 * Japanese, Korean, and Devanagari script - it does NOT recognize Thai.
 * On a Thai receipt this reliably picks up prices and any English/Latin
 * text, but Thai item names will come back blank. That's expected; the
 * Review screen lets the user fill those in by hand.
 */
suspend fun recognizeTextFrom(bitmap: Bitmap): String {
    val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    )
    val image = InputImage.fromBitmap(bitmap, 0)

    return suspendCancellableCoroutine { continuation ->
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                continuation.resume(visionText.text)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }

        continuation.invokeOnCancellation {
            recognizer.close()
        }
    }
}