// ─── data/service/GeminiReceiptParser.kt ────────────────────────────────────
package com.clearspend.data.service

import com.clearspend.BuildConfig
import com.clearspend.domain.model.Category
import com.clearspend.domain.model.ScanResult
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Two-step receipt parsing pipeline:
 *   Step 1 — ML Kit OCR (on-device, offline) extracts raw text from image
 *   Step 2 — Gemini Flash parses structured data from raw text
 *
 * Gemini is ONLY called for parsing text, never for image analysis.
 * This keeps the free tier viable: ~₹0.001 per scan.
 */
@Singleton
class GeminiReceiptParser @Inject constructor() {

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.1f     // Low temp = deterministic structured output
            maxOutputTokens = 256  // Receipt data is small
        }
    )

    /**
     * Parse OCR-extracted text into structured transaction data.
     * Returns ScanResult.Success, Partial, or Failure.
     */
    suspend fun parseReceiptText(rawOcrText: String): ScanResult {
        if (rawOcrText.isBlank()) return ScanResult.Failure("Empty OCR text")

        val prompt = buildPrompt(rawOcrText)

        return try {
            val response = model.generateContent(prompt)
            val text = response.text?.trim() ?: return ScanResult.Failure("Empty Gemini response")
            parseGeminiResponse(text, rawOcrText)
        } catch (e: Exception) {
            ScanResult.Failure("AI parsing failed: ${e.message}")
        }
    }

    /**
     * AI Weekly Coach: analyze week's transactions and return plain-English insight.
     * Called only for Pro users, once per week via WorkManager.
     */
    suspend fun generateWeeklyInsight(
        weekSummary: String,        // Pre-formatted by CoachRepository
        lastWeekSummary: String
    ): String? {
        val prompt = """
You are a friendly, concise personal finance coach. 
Analyze the user's spending data and give ONE clear, actionable insight.

THIS WEEK:
$weekSummary

LAST WEEK:
$lastWeekSummary

Rules:
- Write exactly 2-3 sentences. No more.
- Start with the most important observation.  
- End with ONE specific action the user can take.
- Tone: warm, direct, non-judgmental. Like a smart friend.
- DO NOT use bullet points, headers, or markdown.
- DO NOT mention specific amounts unless it adds real value.

Example output:
"Your food spending was your highest category this week, driven mostly by weekday lunches. 
Compared to last week, total spending increased by about 20%. 
Packing lunch just two days a week could save you roughly ₹1,500 this month."
""".trimIndent()

        return try {
            model.generateContent(prompt).text?.trim()
        } catch (e: Exception) {
            null
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private fun buildPrompt(rawText: String): String = """
Extract transaction details from this receipt text. 
Respond ONLY with a JSON object and nothing else — no markdown, no explanation.

Receipt text:
\"\"\"
$rawText
\"\"\"

Required JSON format:
{
  "merchant": "string — business name, clean and short (e.g. 'Swiggy', 'BigBazaar', 'Zara')",
  "amount": number — total amount paid as a decimal (e.g. 450.00),
  "currency": "string — 3-letter currency code detected (default 'INR')",
  "category": "one of: FOOD, TRANSPORT, SHOPPING, HEALTH, BILLS, ENTERTAINMENT, GROCERIES, TRAVEL, EMI, OTHER",
  "confidence": "HIGH | MEDIUM | LOW"
}

Rules:
- amount must be the FINAL total paid, not subtotals or tax lines.
- If you cannot determine the amount with HIGH or MEDIUM confidence, set confidence to LOW.
- For category, use context clues: restaurant/cafe = FOOD, pharmacy = HEALTH, etc.
- Merchant should be clean, not include address or branch info.
""".trimIndent()

    private fun parseGeminiResponse(json: String, rawOcrText: String): ScanResult {
        return try {
            // Strip any accidental markdown fences
            val clean = json
                .removePrefix("```json").removePrefix("```")
                .removeSuffix("```").trim()

            // Simple key extraction (avoids adding a JSON library dependency)
            val merchant = extractJsonString(clean, "merchant") ?: "Unknown Merchant"
            val amount = extractJsonNumber(clean, "amount")
            val category = extractJsonString(clean, "category")
                ?.let { Category.fromGeminiResponse(it) } ?: Category.OTHER
            val confidence = extractJsonString(clean, "confidence") ?: "LOW"

            when {
                amount != null && confidence != "LOW" -> {
                    ScanResult.Success(
                        merchant = merchant,
                        amount = amount,
                        category = category,
                        rawText = rawOcrText
                    )
                }
                amount != null -> {
                    // Low confidence — show to user for confirmation
                    ScanResult.Partial(rawText = rawOcrText, possibleAmount = amount)
                }
                else -> ScanResult.Failure("Could not determine amount from receipt")
            }
        } catch (e: Exception) {
            ScanResult.Failure("Parse error: ${e.message}")
        }
    }

    private fun extractJsonString(json: String, key: String): String? {
        val pattern = Regex("\"$key\"\\s*:\\s*\"([^\"]+)\"")
        return pattern.find(json)?.groupValues?.get(1)
    }

    private fun extractJsonNumber(json: String, key: String): Double? {
        val pattern = Regex("\"$key\"\\s*:\\s*([0-9]+\\.?[0-9]*)")
        return pattern.find(json)?.groupValues?.get(1)?.toDoubleOrNull()
    }
}
