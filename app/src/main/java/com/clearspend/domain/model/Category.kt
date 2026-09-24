package com.clearspend.domain.model

/**
 * 10 Core spending categories with associated display tokens.
 */
enum class Category(
    val displayName: String,
    val emoji: String,
    val colorHex: String
) {
    FOOD("Food & Dining", "🍽️", "#F97316"),
    TRANSPORT("Transport", "🚗", "#3B82F6"),
    SHOPPING("Shopping", "🛍️", "#EC4899"),
    HEALTH("Health", "💊", "#22C55E"),
    BILLS("Bills & Utilities", "⚡", "#EAB308"),
    ENTERTAINMENT("Entertainment", "🎬", "#A855F7"),
    GROCERIES("Groceries", "🛒", "#14B8A6"),
    TRAVEL("Travel", "✈️", "#06B6D4"),
    EMI("EMI & Loans", "🏦", "#EF4444"),
    OTHER("Other", "📦", "#6B7280");

    companion object {
        fun fromDisplayName(name: String): Category =
            entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: OTHER

        fun fromGeminiResponse(raw: String): Category =
            entries.firstOrNull {
                raw.contains(it.name, ignoreCase = true) ||
                raw.contains(it.displayName, ignoreCase = true)
            } ?: OTHER
    }
}
