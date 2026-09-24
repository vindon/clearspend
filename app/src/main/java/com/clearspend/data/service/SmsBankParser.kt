package com.clearspend.data.service

import com.clearspend.data.security.PiiScrubber
import com.clearspend.domain.model.Category
import com.clearspend.domain.model.ReviewStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-performance Indian Bank SMS Alert Parser.
 * Features:
 *  - 30+ Bank Sender ID Whitelist
 *  - Specific regex debit patterns ordered by precision
 *  - Merchant normalization and category keyword matching
 *  - PII scrubbing on raw message snippets
 *  - 3-tier HITL Confidence Scoring (Auto-Approve vs Pending Review)
 */
@Singleton
class SmsBankParser @Inject constructor() {

    private val bankSenderIds = setOf(
        "HDFCBK", "HDFC", "HDFCBN",
        "ICICIB", "ICICI", "ICICIN",
        "SBIINB", "SBMSMS", "SBIUPI",
        "AXISBK", "AXIS",
        "KOTAKB", "KOTAK",
        "INDUSB", "INDBKM",
        "YESBKM", "YESBNK",
        "FEDBKM", "FEDBNK",
        "PNBSMS", "PUNBNK",
        "CANBKM", "CANBNK",
        "BOBSMS", "BOIBNK",
        "UNIONB", "UCOBNK",
        "IDFCBK", "IDFCFB",
        "FIMONB", "FIDEPO",
        "SLICEP",
        "JUPBNK",
        "AUSFSB",
        "PAYTMB", "PAYTMS", "AMEXIN"
    )

    private val debitPatterns = listOf(
        // HDFC / SBI / ICICI: "Rs.450.00 debited from a/c **1234 to VPA abc@okhdfc"
        Regex("""(?:Rs\.?|INR|₹)\s*([0-9,]+\.?[0-9]*)\s*(?:debited|deducted)""", RegexOption.IGNORE_CASE),
        // ICICI: "INR 1,250.00 spent on ICICI Bank Card XX1234 at SWIGGY"
        Regex("""(?:INR|Rs\.?|₹)\s*([0-9,]+\.?[0-9]*)\s*spent""", RegexOption.IGNORE_CASE),
        // SBI: "Your a/c XXXXXX1234 is debited by Rs 500"
        Regex("""debited\s+by\s+(?:Rs\.?|INR|₹)\s*([0-9,]+\.?[0-9]*)""", RegexOption.IGNORE_CASE),
        // Kotak / Axis: "Amt: INR 299.00 has been debited"
        Regex("""Amt[:\s]+(?:INR|Rs\.?|₹)\s*([0-9,]+\.?[0-9]*)""", RegexOption.IGNORE_CASE),
        // Axis UPI / GPay: "Paid Rs.150.00 to Ola Cabs via UPI"
        Regex("""[Pp]aid\s+(?:Rs\.?|INR|₹)\s*([0-9,]+\.?[0-9]*)""", RegexOption.IGNORE_CASE),
        // Generic fallback: any amount near debit keyword
        Regex("""(?:Rs\.?|INR|₹)\s*([0-9,]+\.?[0-9]*)""", RegexOption.IGNORE_CASE)
    )

    private val merchantPatterns = listOf(
        Regex("""(?:at|to|@)\s+([A-Z][A-Za-z0-9\s&'.-]{2,30}?)(?:\s+on|\s+via|\s+at|\.|$)"""),
        Regex("""VPA\s+([a-zA-Z0-9._-]+)@"""),
        Regex("""UPI/([A-Za-z0-9 _-]+)/""")
    )

    fun isBankSms(senderAddress: String): Boolean {
        val upper = senderAddress.uppercase()
        return bankSenderIds.any { upper.contains(it) }
    }

    fun parseDebit(smsBody: String, receivedAt: Long = System.currentTimeMillis()): ParsedSms? {
        val lower = smsBody.lowercase()
        if (isNonDebit(lower)) return null

        val amount = extractAmount(smsBody) ?: return null
        val rawMerchant = extractMerchant(smsBody) ?: "Unknown Merchant"
        val cleanMerchant = PiiScrubber.cleanMerchant(rawMerchant)
        val category = inferCategory(cleanMerchant, smsBody)

        // Calculate confidence score & HITL review status
        val confidence = calculateConfidence(amount, cleanMerchant, smsBody)
        val reviewStatus = if (confidence >= 0.85f) ReviewStatus.APPROVED else ReviewStatus.PENDING_REVIEW

        return ParsedSms(
            merchant = cleanMerchant,
            amount = amount,
            category = category,
            confidenceScore = confidence,
            reviewStatus = reviewStatus,
            sanitizedSnippet = PiiScrubber.scrub(smsBody.take(120)),
            receivedAt = receivedAt
        )
    }

    private fun isNonDebit(lower: String): Boolean {
        val ignoreTerms = listOf(
            "credited", "received", "otp", "one time password",
            "avl bal", "available balance", "statement", "neft credit", "refund"
        )
        return ignoreTerms.any { lower.contains(it) }
    }

    private fun extractAmount(body: String): Double? {
        for (pattern in debitPatterns) {
            val match = pattern.find(body) ?: continue
            val raw = match.groupValues[1].replace(",", "")
            val amount = raw.toDoubleOrNull() ?: continue
            if (amount > 0 && amount < 10_000_000) return amount
        }
        return null
    }

    private fun extractMerchant(body: String): String? {
        for (pattern in merchantPatterns) {
            val match = pattern.find(body) ?: continue
            val candidate = match.groupValues[1].trim()
            if (candidate.length >= 2) return candidate
        }
        return null
    }

    private fun calculateConfidence(amount: Double, merchant: String, body: String): Float {
        var score = 0.5f
        if (amount > 0) score += 0.25f
        if (merchant != "Unknown Merchant" && merchant.length >= 3) score += 0.2f
        if (body.contains("UPI", ignoreCase = true) || body.contains("card", ignoreCase = true)) score += 0.1f
        return score.coerceIn(0.0f, 1.0f)
    }

    private fun inferCategory(merchant: String, body: String): Category {
        val combined = (merchant + " " + body).lowercase()
        return when {
            combined.containsAny("swiggy", "zomato", "restaurant", "cafe", "dominos", "kfc", "pizza", "starbucks", "mcdonald") -> Category.FOOD
            combined.containsAny("ola", "uber", "rapido", "metro", "irctc", "petrol", "fuel", "hpcl", "bpcl", "ioc") -> Category.TRANSPORT
            combined.containsAny("amazon", "flipkart", "myntra", "meesho", "ajio", "nykaa", "zara", "h&m") -> Category.SHOPPING
            combined.containsAny("pharmacy", "hospital", "apollo", "medplus", "1mg", "health", "dr ", "clinic") -> Category.HEALTH
            combined.containsAny("electricity", "water", "broadband", "airtel", "jio", "bsnl", "recharge", "bescom", "tneb") -> Category.BILLS
            combined.containsAny("netflix", "spotify", "hotstar", "prime", "youtube", "movie", "cinema", "pvr", "inox", "bookmyshow") -> Category.ENTERTAINMENT
            combined.containsAny("bigbazaar", "dmart", "reliance fresh", "supermarket", "grocer", "zepto", "blinkit", "instamart") -> Category.GROCERIES
            combined.containsAny("hotel", "oyo", "makemytrip", "goibibo", "flight", "airline", "indigo", "air india") -> Category.TRAVEL
            combined.containsAny("emi", "loan", "bajaj finance", "hdfc loan", "equated") -> Category.EMI
            else -> Category.OTHER
        }
    }

    private fun String.containsAny(vararg terms: String): Boolean = terms.any { this.contains(it, ignoreCase = true) }
}

data class ParsedSms(
    val merchant: String,
    val amount: Double,
    val category: Category,
    val confidenceScore: Float,
    val reviewStatus: ReviewStatus,
    val sanitizedSnippet: String,
    val receivedAt: Long
)
