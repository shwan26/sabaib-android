package com.smnc.sabaib.util

import kotlin.math.roundToLong

fun bahtToSatang(
    amount: Double
): Long {

    return (amount * 100)
        .roundToLong()
}

fun satangToBaht(
    satang: Long
): Double {

    return satang / 100.0
}