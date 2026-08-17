package com.smnc.sabaib.util

fun generateGroupCode(
    length: Int = 6
): String {

    val characters =
        "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    return (1..length)
        .map {
            characters.random()
        }
        .joinToString("")
}