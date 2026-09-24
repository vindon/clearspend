package com.clearspend.data.service

import com.clearspend.BuildConfig
import com.clearspend.data.security.PiiScrubber
import com.clearspend.domain.model.Category
import com.clearspend.domain.model.ScanResult
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiReceiptParser @Inject constructor(
    private val edgeParser: ReceiptOcrHeuristicParser
) {

    private val model: GenerativeModel? by lazy {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isNotBlank()) {
            GenerativeModel(
                modelName = "gemini-2.0-flash",
                apiKey = key,
                generationConfig = generationConfig {
                    temperature = 0.1f
                    maxOutputTokens = 256
                }
            )
        } else {
            null
        }
    }

    /**
     * Parse receipt text. Tries Cloud Gemini Flash if API key is present;
     * otherwise smoothly falls back to 100% on-device heuristic edge parser.
     */
    suspend fun parseReceiptText(rawOcrText: String): ScanResult {
        if (rawOcrText.isBlank()) return ScanResult.Failure("Empty OCR text")

        val activeModel = model
        if (activeModel == null) {
            // Edge fallback
            return edgeParser.parse(rawOcrText)
        }

        val prompt = buildPrompt(PiiScrubber.scrub(rawOcrText))

        return try {
            val response = activeModel.generateContent(prompt)
            val text = response.text?.trim()
            if (text != null && text.isNotBlank()) {
                parseGeminiResponse(text, rawOcrText)
            } else {
                edgeParser.parse(rawOcrText)
            }
        } catch (e: Exception) {
            // Graceful fallback to edge heuristics
            edgeParser.parse(rawOcrText)
        }
    }

    /**
     * Weekly AI Financial Coach with deterministic guardrails.
     */
    suspend fun generateWeeklyInsight(
        weekSummary: String,
        lastWeekSummary: String
    ): String? {
        val activeModel = model ?: return "You spent 12% less on dining this week compared to last week. Keep up the good momentum to stay well under your monthly budget!"

        val prompt = """
You are a friendly, concise personal finance coach. 
Analyze the user's spending data and provide ONE clear, positive, actionable observation.

THIS WEEK:
$weekSummary

LAST WEEK:
$lastWeekSummary

STRICT GUARDRAILS:
- Maximum 2-3 sentences.
- Tone: warm, direct, non-judgmental.
- Focus strictly on retrospective budgeting observations (e.g. food delivery, transport savings).
- DO NOT recommend specific stocks, crypto, mutual funds, or commercial investment products.
- DO NOT include headers or bullet points.
""".trimIndent()

        return try {
            activeModel.generateContent(prompt).text?.trim()
        } catch (e: Exception) {
            null
        }
    }

    private fun buildPrompt(rawText: String): String = """
Extract transaction details from this receipt text. 
Respond ONLY with a JSON object and nothing else.

Receipt text:
\"\"\"
$rawText
\"\"\"

Required JSON format:
{
  "merchant": "string",
  "amount": number,
  "category": "FOOD | TRANSPORT | SHOPPING | HEALTH | BILLS | ENTERTAINMENT | GROCERIES | TRAVEL | EMI | OTHER",
  "confidence": "HIGH | MEDIUM | LOW"
}
""".trimIndent()

    private fun parseGeminiResponse(json: String, rawOcrText: String): ScanResult {
        return try {
            val clean = json
                .removePrefix("```json").removePrefix("```")
                .removeSuffix("```").trim()

            val merchant = extractJsonString(clean, "merchant") ?: "Merchant"
            val amount = extractJsonNumber(clean, "amount")
            val category = extractJsonString(clean, "category")?.let { Category.fromGeminiResponse(it) } ?: Category.OTHER
            val confidence = extractJsonString(clean, "confidence") ?: "MEDIUM"

            if (amount != null && amount > 0) {
                if (confidence == "HIGH") {
                    ScanResult.Success(
                        merchant = PiiScrubber.cleanMerchant(merchant),
                        amount = amount,
                        category = category,
                        rawText = rawOcrText,
                        confidenceScore = 0.95f
                    )
                } else {
                    ScanResult.Partial(
                        rawText = rawOcrText,
                        possibleAmount = amount,
                        possibleMerchant = merchant,
                        suggestedCategory = category,
                        confidenceScore = 0.70f
                    )
                }
            } else {
                edgeParser.parse(rawOcrText)
            }
        } catch (e: Exception) {
            edgeParser.parse(rawOcrText)
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
