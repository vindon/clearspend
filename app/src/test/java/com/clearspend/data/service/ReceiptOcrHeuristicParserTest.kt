package com.clearspend.data.service

import com.clearspend.domain.model.Category
import com.clearspend.domain.model.ScanResult
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ReceiptOcrHeuristicParserTest {

    private lateinit var parser: ReceiptOcrHeuristicParser

    @Before
    fun setUp() {
        parser = ReceiptOcrHeuristicParser()
    }

    @Test
    fun testParseThermalReceipt() {
        val ocr = """
            McDonald's Family Restaurant
            Store #104, MG Road, Bangalore
            Invoice #49281
            1x McSpicy Chicken     ₹249.00
            1x French Fries M       ₹99.00
            1x Coke Zero            ₹60.00
            Subtotal:              ₹408.00
            CGST 2.5%:              ₹10.20
            SGST 2.5%:              ₹10.20
            TOTAL AMOUNT:          ₹428.40
            Thank you!
        """.trimIndent()

        val result = parser.parse(ocr)
        assertTrue(result is ScanResult.Success)
        val success = result as ScanResult.Success

        assertEquals(428.40, success.amount, 0.05)
        assertEquals(Category.FOOD, success.category)
        assertTrue(success.merchant.contains("McDonald", ignoreCase = true))
    }

    @Test
    fun testParseSupermarketReceipt() {
        val ocr = """
            D-MART RETAIL
            Whitefield, Bangalore
            Item 1 Aashirvaad Atta 5kg   320.00
            Item 2 Tata Salt 1kg          28.00
            Item 3 Amul Butter 500g      275.00
            TOTAL:                       623.00
            Card Payment Verified
        """.trimIndent()

        val result = parser.parse(ocr)
        assertTrue(result is ScanResult.Success)
        val success = result as ScanResult.Success

        assertEquals(623.0, success.amount, 0.1)
        assertEquals(Category.GROCERIES, success.category)
    }

    @Test
    fun testEmptyOcrFailsGracefully() {
        val result = parser.parse("")
        assertTrue(result is ScanResult.Failure)
    }
}
