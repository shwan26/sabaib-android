package com.smnc.sabaib.data

import android.util.Log
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.NotFoundRestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import java.io.IOException

private const val TAG = "AuthErrorMapper"

fun Throwable.toUserMessage(): String = when (this) {
    is AuthRestException -> when (errorCode) {
        AuthErrorCode.InvalidCredentials -> "Incorrect email or password."
        AuthErrorCode.UserNotFound -> "No account found with this email."
        AuthErrorCode.EmailExists, AuthErrorCode.UserAlreadyExists ->
            "An account with this email already exists."
        AuthErrorCode.EmailNotConfirmed -> "Please verify your email before logging in."
        AuthErrorCode.WeakPassword -> "Password is too weak. Please use a stronger one."
        AuthErrorCode.EmailAddressInvalid -> "Please enter a valid email address."
        AuthErrorCode.OverRequestRateLimit, AuthErrorCode.OverEmailSendRateLimit ->
            "Too many attempts. Please try again later."
        else -> "Something went wrong. Please try again."
    }
    is HttpRequestException, is IOException ->
        "No internet connection. Please check your network and try again."
    is RestException -> {
        // statusCode/error/description are swallowed if we don't log them here -
        // this is the only place that ever sees the server's actual response.
        Log.e(TAG, "RestException [$statusCode] $error: $description", this)
        when (this) {
            is UnauthorizedRestException -> "Your session has expired. Please log in again."
            is NotFoundRestException -> "This feature isn't available right now. Please try again later."
            is BadRequestRestException -> "That request couldn't be processed. Please try again."
            else -> when (statusCode) {
                in 500..599 -> "Server is not available right now. Please try again later."
                else -> "Something went wrong (code $statusCode). Please try again."
            }
        }
    }
    else -> {
        Log.e(TAG, "Unmapped error: ${this::class.simpleName}", this)
        "Something went wrong. Please try again."
    }
}
