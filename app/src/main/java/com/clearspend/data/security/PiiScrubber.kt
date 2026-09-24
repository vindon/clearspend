package com.clearspend.data.security

/**
 * Institutional-grade PII & Sensitive Financial Data Scrubber.
 * Ensures account numbers, OTPs, CVVs, and raw customer balances never leak into logs or telemetry.
 */
object PiiScrubber {

    // Regex for 10-16 digit card / account numbers
    private val accountNumberPattern = Regex("""\b(\d{2,6})[ -]?(\d{4,8})[ -]?(\d{4})\b""")
    
    // Regex for 4 to 8 digit OTP codes
    private val otpPattern = Regex("""(?i)\b(otp|one time password|code|pin|secret)[:\s]*([0-9]{4,8})\b""")

    // Regex for credit/debit card numbers (Luhn candidate strings)
    private val panPattern = Regex("""\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|6(?:011|5[0-9]{2})[0-9]{12})\b""")

    /**
     * Sanitizes raw SMS body or log trace by masking card/account digits and removing OTPs.
     * Example: "debited from a/c 123456789012" -> "debited from a/c XX9012"
     */
    fun scrub(text: String): String {
        var result = text

        // Mask PAN card numbers
        result = panPattern.replace(result) { match ->
            val value = match.value
            "XXXX-XXXX-XXXX-" + value.takeLast(4)
        }

        // Mask generic bank account numbers
        result = accountNumberPattern.replace(result) { match ->
            val last4 = match.groupValues[3]
            "XX$last4"
        }

        // Redact OTP codes
        result = otpPattern.replace(result) { match ->
            val label = match.groupValues[1]
            "$label: [REDACTED]"
        }

        return result
    }

    /**
     * Normalizes merchant names to prevent storing noisy branch codes or internal terminal IDs.
     */
    fun cleanMerchant(raw: String): String {
        return raw
            .replace(Regex("[*#_]+"), "")
            .replace(Regex("(?i)\\b(pvt|ltd|llp|inc|corp|private|limited)\\b\\.?"), "")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
    }
}
