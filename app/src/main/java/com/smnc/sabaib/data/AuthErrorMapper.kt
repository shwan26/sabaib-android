package com.smnc.sabaib.data

import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import java.io.IOException

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
    is RestException -> "Server is not available right now. Please try again later."
    else -> "Something went wrong. Please try again."
}
