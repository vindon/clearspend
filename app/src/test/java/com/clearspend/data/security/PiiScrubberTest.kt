package com.clearspend.data.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PiiScrubberTest {

    @Test
    fun testScrubAccountNumbers() {
        val input = "debited from a/c 123456789012 on 22-09."
        val scrubbed = PiiScrubber.scrub(input)
        assertFalse(scrubbed.contains("123456789012"))
        assertTrue(scrubbed.contains("XX9012"))
    }

    @Test
    fun testScrubCardPan() {
        val input = "Transaction on card 4111222233334444 approved."
        val scrubbed = PiiScrubber.scrub(input)
        assertFalse(scrubbed.contains("4111222233334444"))
        assertTrue(scrubbed.contains("XXXX-XXXX-XXXX-4444"))
    }

    @Test
    fun testScrubOtp() {
        val input = "Your OTP: 849201 is valid for 10 minutes."
        val scrubbed = PiiScrubber.scrub(input)
        assertFalse(scrubbed.contains("849201"))
        assertTrue(scrubbed.contains("[REDACTED]"))
    }

    @Test
    fun testCleanMerchant() {
        val raw = "SWIGGY PRIVATE LIMITED*"
        val cleaned = PiiScrubber.cleanMerchant(raw)
        assertFalse(cleaned.contains("PRIVATE", ignoreCase = true))
        assertFalse(cleaned.contains("LIMITED", ignoreCase = true))
        assertFalse(cleaned.contains("*"))
        assertTrue(cleaned.startsWith("Swiggy"))
    }
}
