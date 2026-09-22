package com.smnc.sabaib

import com.smnc.sabaib.util.AgeGate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgeGateTest {
    private val now = 2026

    @Test
    fun bornFourteenYearsAgoIsEligible() {
        assertTrue(AgeGate.isEligible(2012, now))
        assertTrue(AgeGate.isEligible(1990, now))
    }

    @Test
    fun youngerThanFourteenIsRejected() {
        assertFalse(AgeGate.isEligible(2013, now))
        assertFalse(AgeGate.isEligible(now, now))
    }

    @Test
    fun implausibleYearsAreInvalid() {
        assertFalse(AgeGate.isValidYear(1899, now))
        assertFalse(AgeGate.isValidYear(2027, now))
        assertFalse(AgeGate.isEligible(1899, now))
        assertTrue(AgeGate.isValidYear(1900, now))
    }
}
