package com.clearspend.data.service

import com.clearspend.domain.model.Category
import com.clearspend.domain.model.ReviewStatus
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SmsBankParserTest {

    private lateinit var parser: SmsBankParser

    @Before
    fun setUp() {
        parser = SmsBankParser()
    }

    @Test
    fun testBankSenderIdWhitelist() {
        assertTrue(parser.isBankSms("VM-HDFCBK"))
        assertTrue(parser.isBankSms("AD-ICICIB"))
        assertTrue(parser.isBankSms("VK-SBIINB"))
        assertTrue(parser.isBankSms("AXISBK"))
        assertTrue(parser.isBankSms("KOTAKB"))
        assertTrue(parser.isBankSms("FIMONB"))
        assertTrue(parser.isBankSms("JUPBNK"))
        assertFalse(parser.isBankSms("PROMO-SALE"))
        assertFalse(parser.isBankSms("DOMINOS"))
    }

    @Test
    fun testHdfcUpiDebit() {
        val sms = "Rs.450.00 debited from a/c **1234 on 22-09-24 to VPA swiggy@okhdfc on UPI ref 426189."
        val result = parser.parseDebit(sms)

        assertNotNull(result)
        assertEquals(450.0, result!!.amount, 0.01)
        assertEquals(Category.FOOD, result.category)
        assertTrue(result.merchant.contains("Swiggy", ignoreCase = true))
        assertEquals(ReviewStatus.APPROVED, result.reviewStatus)
    }

    @Test
    fun testIciciCardSpend() {
        val sms = "INR 1,250.00 spent on ICICI Bank Card XX1940 at SWIGGY on 22-SEP-24. Avl Lmt: INR 45,000.00."
        val result = parser.parseDebit(sms)

        assertNotNull(result)
        assertEquals(1250.0, result!!.amount, 0.01)
        assertEquals(Category.FOOD, result.category)
        assertTrue(result.merchant.contains("Swiggy", ignoreCase = true))
    }

    @Test
    fun testSbiDebit() {
        val sms = "Your a/c XXXXXX5678 is debited by Rs 500 on 22Sep24 at UBER INDIA via UPI."
        val result = parser.parseDebit(sms)

        assertNotNull(result)
        assertEquals(500.0, result!!.amount, 0.01)
        assertEquals(Category.TRANSPORT, result.category)
        assertTrue(result.merchant.contains("Uber", ignoreCase = true))
    }

    @Test
    fun testKotakAmtDebited() {
        val sms = "Amt: INR 299.00 has been debited from Account XX3456 to Netflix on 22-Sep-24."
        val result = parser.parseDebit(sms)

        assertNotNull(result)
        assertEquals(299.0, result!!.amount, 0.01)
        assertEquals(Category.ENTERTAINMENT, result.category)
    }

    @Test
    fun testAxisUpiPaid() {
        val sms = "Paid Rs.150.00 to Apollo Pharmacy via UPI ref 429182. Bal: INR 12,400."
        val result = parser.parseDebit(sms)

        assertNotNull(result)
        assertEquals(150.0, result!!.amount, 0.01)
        assertEquals(Category.HEALTH, result.category)
    }

    @Test
    fun testNonDebitSmsFilteredOut() {
        val otpSms = "Your OTP for HDFC Bank NetBanking transaction is 482910. Do not share with anyone."
        assertNull(parser.parseDebit(otpSms))

        val creditSms = "INR 50,000.00 credited to your account XX1234 on 22-Sep-24 by SALARY."
        assertNull(parser.parseDebit(creditSms))

        val balanceSms = "Dear Customer, Avl Bal in A/c XX4321 is Rs. 14,230.50 as on 22-SEP."
        assertNull(parser.parseDebit(balanceSms))
    }

    @Test
    fun testSanitizeSnippetsInOutput() {
        val sms = "Rs.750.00 debited from a/c 123456789012 at Blinkit."
        val result = parser.parseDebit(sms)

        assertNotNull(result)
        assertFalse(result!!.sanitizedSnippet.contains("123456789012"))
        assertTrue(result.sanitizedSnippet.contains("XX9012"))
    }
}
