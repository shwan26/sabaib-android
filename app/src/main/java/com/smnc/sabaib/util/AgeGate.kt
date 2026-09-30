package com.smnc.sabaib.util

import java.time.Year

object AgeGate {
    const val MIN_AGE = 14
    private const val EARLIEST_YEAR = 1900

    fun isValidYear(year: Int, currentYear: Int = Year.now().value): Boolean =
        year in EARLIEST_YEAR..currentYear

    /** Year-only check: born in [currentYear] - [MIN_AGE] or earlier. */
    fun isEligible(birthYear: Int, currentYear: Int = Year.now().value): Boolean =
        isValidYear(birthYear, currentYear) && currentYear - birthYear >= MIN_AGE
}
