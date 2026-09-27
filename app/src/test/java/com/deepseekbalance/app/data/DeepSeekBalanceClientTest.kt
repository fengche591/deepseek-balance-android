package com.deepseekbalance.app.data

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class DeepSeekBalanceClientTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `sends bearer token and parses account balance`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                      "is_available": true,
                      "balance_infos": [
                        {
                          "currency": "CNY",
                          "total_balance": "25.50"
                        }
                      ]
                    }
                    """.trimIndent(),
                ),
        )
        val client = DeepSeekBalanceClient(
            httpClient = OkHttpClient(),
            endpoint = server.url("/user/balance"),
            clock = { 789L },
        )

        val result = client.fetch("secret-key")
        val request = server.takeRequest()

        assertEquals("Bearer secret-key", request.getHeader("Authorization"))
        assertEquals("/user/balance", request.path)
        assertEquals("25.50", result.totalBalance)
        assertEquals(789L, result.fetchedAtEpochMillis)
    }

    @Test
    fun `maps unauthorized response to useful message`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))
        val client = DeepSeekBalanceClient(
            httpClient = OkHttpClient(),
            endpoint = server.url("/user/balance"),
        )

        val exception = assertFailsWith<BalanceFetchException> {
            client.fetch("bad-key")
        }

        assertEquals("API Key 无效或已失效", exception.message)
    }
}
