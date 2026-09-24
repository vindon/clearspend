package com.clearspend.data.service

import com.clearspend.data.security.PiiScrubber
import com.clearspend.domain.model.Category
import com.clearspend.domain.model.ScanResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 100% On-Device Edge Receipt Parser.
 * Operates offline directly on ML Kit TextRecognition output with ZERO cloud API calls and zero latency.
 */
@Singleton
class ReceiptOcrHeuristicParser @Inject constructor() {

    private val totalKeywords = listOf(
        "total", "grand total", "net amount", "amount paid",
        "balance due", "subtotal", "amt paid", "invoice total", "bill total"
    )

    private val currencySymbols = Regex("""(?:Rs\.?|INR|₹|\$|€|£)\s*([0-9,]+\.?[0-9]*)""", RegexOption.IGNORE_CASE)
    private val standaloneAmountPattern = Regex("""\b([0-9]{1,5}\.[0-9]{2})\b""")

    fun parse(rawOcrText: String): ScanResult {
        if (rawOcrText.isBlank()) {
            return ScanResult.Failure("No text detected in receipt image.")
        }

        val lines = rawOcrText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            return ScanResult.Failure("Empty receipt content.")
        }

        val merchant = extractMerchant(lines)
        val amount = extractAmount(lines)
        val category = inferCategory(merchant, rawOcrText)

        return when {
            amount != null && amount > 0 && merchant.isNotBlank() -> {
                ScanResult.Success(
                    merchant = PiiScrubber.cleanMerchant(merchant),
                    amount = amount,
                    category = category,
                    rawText = rawOcrText,
                    confidenceScore = 0.88f
                )
            }
            amount != null && amount > 0 -> {
                ScanResult.Partial(
                    rawText = rawOcrText,
                    possibleAmount = amount,
                    possibleMerchant = merchant.ifBlank { "Store / Counter" },
                    suggestedCategory = category,
                    confidenceScore = 0.65f
                )
            }
            else -> {
                ScanResult.Partial(
                    rawText = rawOcrText,
                    possibleAmount = null,
                    possibleMerchant = merchant.ifBlank { null },
                    suggestedCategory = category,
                    confidenceScore = 0.40f
                )
            }
        }
    }

    private fun extractMerchant(lines: List<String>): String {
        // Typically the first 1-3 lines contain the merchant/store name
        for (line in lines.take(4)) {
            val lower = line.lowercase()
            if (totalKeywords.any { lower.contains(it) }) continue
            if (line.any { it.isDigit() } && line.length < 5) continue
            if (lower.contains("tax") || lower.contains("gst") || lower.contains("date") || lower.contains("bill")) continue
            if (line.length in 3..35) {
                return line
            }
        }
        return lines.firstOrNull()?.take(30) ?: "Merchant"
    }

    private fun extractAmount(lines: List<String>): Double? {
        // Priority 1: Line containing total keyword + currency symbol or number
        for (line in lines.reversed()) {
            val lower = line.lowercase()
            if (totalKeywords.any { lower.contains(it) }) {
                val symbolMatch = currencySymbols.find(line)
                if (symbolMatch != null) {
                    symbolMatch.groupValues[1].replace(",", "").toDoubleOrNull()?.let { return it }
                }
                val standAloneMatch = standaloneAmountPattern.find(line)
                if (standAloneMatch != null) {
                    standAloneMatch.groupValues[1].replace(",", "").toDoubleOrNull()?.let { return it }
                }
            }
        }

        // Priority 2: Any line with currency symbol
        for (line in lines.reversed()) {
            val symbolMatch = currencySymbols.find(line)
            if (symbolMatch != null) {
                val amt = symbolMatch.groupValues[1].replace(",", "").toDoubleOrNull()
                if (amt != null && amt > 0) return amt
            }
        }

        // Priority 3: Highest standalone decimal amount in the lower half of receipt
        val candidateAmounts = mutableListOf<Double>()
        for (line in lines) {
            standaloneAmountPattern.findAll(line).forEach { match ->
                match.groupValues[1].toDoubleOrNull()?.let { candidateAmounts.add(it) }
            }
        }

        return candidateAmounts.filter { it < 100_000 }.maxOrNull()
    }

    private fun inferCategory(merchant: String, rawText: String): Category {
        val combined = (merchant + " " + rawText).lowercase()
        return when {
            combined.containsAny("food", "restaurant", "cafe", "dine", "kitchen", "burger", "pizza", "coffee", "bistro", "bakery", "hotel") -> Category.FOOD
            combined.containsAny("pharmacy", "medical", "chemist", "hospital", "pharma", "clinic", "health", "care") -> Category.HEALTH
            combined.containsAny("supermarket", "mart", "bazaar", "grocer", "provisions", "fruit", "vegetable") -> Category.GROCERIES
            combined.containsAny("fuel", "petrol", "transport", "cab", "parking", "toll") -> Category.TRANSPORT
            combined.containsAny("fashion", "wear", "cloth", "apparel", "shoes", "retail", "store", "lifestyle") -> Category.SHOPPING
            combined.containsAny("cinema", "theatre", "movie", "ticket", "amusement", "game") -> Category.ENTERTAINMENT
            combined.containsAny("electricity", "water", "bill", "broadband", "power", "utility") -> Category.BILLS
            else -> Category.OTHER
        }
    }

    private fun String.containsAny(vararg terms: String): Boolean = terms.any { this.contains(it, ignoreCase = true) }
}
