package com.deepseekbalance.app.data

import kotlin.test.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DeepSeekBalanceParserTest {
    @Test
    fun `returns CNY total and ignores other currencies`() {
        val response = """
            {
              "is_available": true,
              "balance_infos": [
                {
                  "currency": "USD",
                  "total_balance": "12.34",
                  "granted_balance": "0.00",
                  "topped_up_balance": "12.34"
                },
                {
                  "currency": "CNY",
                  "total_balance": "88.66",
                  "granted_balance": "8.00",
                  "topped_up_balance": "80.66"
                }
              ]
            }
        """.trimIndent()

        val result = DeepSeekBalanceParser.parse(response, fetchedAtEpochMillis = 123L)

        assertEquals("88.66", result.totalBalance)
        assertEquals(true, result.isAvailable)
        assertEquals(123L, result.fetchedAtEpochMillis)
    }

    @Test
    fun `preserves unavailable account status`() {
        val response = """
            {
              "is_available": false,
              "balance_infos": [
                {
                  "currency": "CNY",
                  "total_balance": "0.00"
                }
              ]
            }
        """.trimIndent()

        val result = DeepSeekBalanceParser.parse(response, fetchedAtEpochMillis = 456L)

        assertEquals(false, result.isAvailable)
    }

    @Test
    fun `rejects response without CNY balance`() {
        val response = """
            {
              "is_available": true,
              "balance_infos": [
                {
                  "currency": "USD",
                  "total_balance": "12.34"
                }
              ]
            }
        """.trimIndent()

        assertThrows(BalanceFetchException::class.java) {
            DeepSeekBalanceParser.parse(response, fetchedAtEpochMillis = 1L)
        }
    }
}
