// ─── data/service/SmsBankParser.kt ──────────────────────────────────────────
package com.clearspend.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.clearspend.domain.model.Category
import com.clearspend.domain.model.Transaction
import com.clearspend.domain.model.TransactionSource
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses bank transaction SMS alerts from 30+ Indian banks.
 *
 * Strategy:
 *   1. Check if sender ID is in the Indian bank whitelist
 *   2. Apply regex patterns to extract debit amount + merchant
 *   3. If regex confidence is low, flag for Gemini fallback (optional)
 *
 * Dedup: Same merchant + amount within 5 min = single transaction.
 * NO bank login. NO UPI credentials. READ_SMS permission only.
 */
@Singleton
class SmsBankParser @Inject constructor() {

    // ── Indian Bank Sender ID Whitelist ───────────────────────────────────────
    // Covers HDFC, ICICI, SBI, Axis, Kotak, IndusInd, Yes, Federal, PNB,
    // Canara, BOB, Union, IDFC, Fi, Slice, Jupiter, AU Small Finance
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
        "FIMONB", "FIDEPO",   // Fi (Federal + EpiFi)
        "SLICEP",             // Slice
        "JUPBNK",             // Jupiter
        "AUSFSB",             // AU Small Finance
        "PAYTMB", "PAYTMS"   // Paytm Payments Bank
    )

    // ── Regex patterns ordered by specificity ────────────────────────────────
    // Covers most common Indian bank SMS formats
    private val debitPatterns = listOf(
        // HDFC: "Rs.450.00 debited from a/c **1234 to VPA abc@okhdfc"
        Regex("""(?:Rs\.?|INR|₹)\s*([0-9,]+\.?[0-9]*)\s*(?:debited|deducted)""", RegexOption.IGNORE_CASE),
        // ICICI: "INR 1,250.00 spent on ICICI Bank Card XX1234 at SWIGGY"
        Regex("""(?:INR|Rs\.?|₹)\s*([0-9,]+\.?[0-9]*)\s*spent""", RegexOption.IGNORE_CASE),
        // SBI: "Your a/c XXXXXX1234 is debited by Rs 500"
        Regex("""debited\s+by\s+(?:Rs\.?|INR|₹)\s*([0-9,]+\.?[0-9]*)""", RegexOption.IGNORE_CASE),
        // Kotak: "Amt: INR 299.00 has been debited"
        Regex("""Amt[:\s]+(?:INR|Rs\.?|₹)\s*([0-9,]+\.?[0-9]*)""", RegexOption.IGNORE_CASE),
        // Axis UPI: "Paid Rs.150.00 to Ola Cabs via UPI"
        Regex("""[Pp]aid\s+(?:Rs\.?|INR|₹)\s*([0-9,]+\.?[0-9]*)""", RegexOption.IGNORE_CASE),
        // Generic fallback: any amount near debit keywords
        Regex("""(?:Rs\.?|INR|₹)\s*([0-9,]+\.?[0-9]*)""", RegexOption.IGNORE_CASE),
    )

    private val merchantPatterns = listOf(
        // "at MERCHANT NAME on" or "at MERCHANT NAME."
        Regex("""(?:at|to|@)\s+([A-Z][A-Za-z0-9\s&'.-]{2,30}?)(?:\s+on|\s+via|\s+at|\.|$)"""),
        // "VPA merchant@bank" → extract merchant part
        Regex("""VPA\s+([a-zA-Z0-9._-]+)@"""),
        // "UPI/merchant-name/"
        Regex("""UPI/([A-Za-z0-9 _-]+)/"""),
    )

    // ── Public API ────────────────────────────────────────────────────────────

    fun isBankSms(senderAddress: String): Boolean {
        val upper = senderAddress.uppercase()
        return bankSenderIds.any { upper.contains(it) }
    }

    /**
     * Parse a bank SMS body into a Transaction.
     * Returns null if not a debit transaction (credits, OTP, balance alerts, etc.)
     */
    fun parseDebit(smsBody: String, receivedAt: Long = System.currentTimeMillis()): ParsedSms? {
        // Skip non-debit messages early
        val lowerBody = smsBody.lowercase()
        if (isNonDebitSms(lowerBody)) return null

        val amount = extractAmount(smsBody) ?: return null
        val merchant = extractMerchant(smsBody) ?: "Unknown"
        val category = inferCategory(merchant, smsBody)

        return ParsedSms(
            merchant = merchant.trim().take(50),
            amount = amount,
            category = category,
            rawBody = smsBody,
            receivedAt = receivedAt
        )
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun isNonDebitSms(lowerBody: String): Boolean {
        val creditKeywords = listOf(
            "credited", "received", "otp", "one time password",
            "avl bal", "available balance", "statement", "neft credit"
        )
        return creditKeywords.any { lowerBody.contains(it) }
    }

    private fun extractAmount(body: String): Double? {
        for (pattern in debitPatterns) {
            val match = pattern.find(body) ?: continue
            val raw = match.groupValues[1].replace(",", "")
            val amount = raw.toDoubleOrNull() ?: continue
            if (amount > 0 && amount < 10_000_000) return amount  // Sanity cap: 1 crore
        }
        return null
    }

    private fun extractMerchant(body: String): String? {
        for (pattern in merchantPatterns) {
            val match = pattern.find(body)
            val candidate = match?.groupValues?.get(1)?.trim()
            if (!candidate.isNullOrBlank() && candidate.length >= 2) {
                return cleanMerchantName(candidate)
            }
        }
        return null
    }

    private fun cleanMerchantName(raw: String): String {
        // Remove common suffixes that aren't part of the brand name
        return raw
            .replace(Regex("(?i)(pvt|ltd|llp|inc|corp|private|limited)\\.?\\s*$"), "")
            .replace(Regex("[*#_]+"), "")
            .trim()
            .split(" ")
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
    }

    private fun inferCategory(merchant: String, body: String): Category {
        val combined = (merchant + " " + body).lowercase()
        return when {
            combined.containsAny("swiggy", "zomato", "restaurant", "cafe", "dominos", "kfc", "pizza") ->
                Category.FOOD
            combined.containsAny("ola", "uber", "rapido", "metro", "irctc", "petrol", "fuel") ->
                Category.TRANSPORT
            combined.containsAny("amazon", "flipkart", "myntra", "meesho", "ajio", "nykaa") ->
                Category.SHOPPING
            combined.containsAny("pharmacy", "hospital", "apollo", "medplus", "1mg", "health") ->
                Category.HEALTH
            combined.containsAny("electricity", "water", "broadband", "airtel", "jio", "bsnl", "recharge") ->
                Category.BILLS
            combined.containsAny("netflix", "spotify", "hotstar", "prime", "youtube", "movie", "cinema") ->
                Category.ENTERTAINMENT
            combined.containsAny("bigbazaar", "dmart", "reliance fresh", "supermarket", "grocer") ->
                Category.GROCERIES
            combined.containsAny("hotel", "oyo", "makemytrip", "goibibo", "flight", "airline") ->
                Category.TRAVEL
            combined.containsAny("emi", "loan", "bajaj finance", "hdfc loan", "equated") ->
                Category.EMI
            else -> Category.OTHER
        }
    }

    private fun String.containsAny(vararg terms: String): Boolean =
        terms.any { this.contains(it, ignoreCase = true) }
}

// ─── Result type from SMS parsing ────────────────────────────────────────────
data class ParsedSms(
    val merchant: String,
    val amount: Double,
    val category: Category,
    val rawBody: String,
    val receivedAt: Long
)

// ─── BroadcastReceiver registered in AndroidManifest ─────────────────────────
class SmsReceiver : BroadcastReceiver() {
    // Injected via Hilt EntryPoint pattern (see di/SmsReceiverEntryPoint.kt)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        messages?.forEach { smsMessage ->
            val sender = smsMessage.originatingAddress ?: return@forEach
            val body = smsMessage.messageBody ?: return@forEach
            // Forward to SmsProcessingService (runs in background via WorkManager)
            SmsProcessingService.enqueue(context, sender, body, smsMessage.timestampMillis)
        }
    }
}
