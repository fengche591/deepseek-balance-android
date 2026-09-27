package com.deepseekbalance.app.data

import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BalanceServiceTest {
    @Test
    fun `successful refresh stores and returns CNY snapshot`() = runTest {
        val snapshot = BalanceSnapshot("10.25", true, 100L)
        val cache = FakeBalanceCache()
        val service = BalanceService(
            apiKeyStore = FakeApiKeyStore("secret"),
            fetcher = FakeBalanceFetcher(snapshot),
            cache = cache,
        )

        val result = service.refresh()

        assertIs<BalanceRefreshResult.Success>(result)
        assertEquals(snapshot, cache.state.snapshot)
        assertEquals(null, cache.state.errorMessage)
    }

    @Test
    fun `missing API key returns failure without calling fetcher`() = runTest {
        var fetchCalled = false
        val fetcher = FakeBalanceFetcher {
            fetchCalled = true
            error("fetch should not be called")
        }
        val service = BalanceService(
            apiKeyStore = FakeApiKeyStore(null),
            fetcher = fetcher,
            cache = FakeBalanceCache(),
        )

        val result = service.refresh()

        assertIs<BalanceRefreshResult.Failure>(result)
        assertEquals("请先保存 DeepSeek API Key", result.message)
        assertEquals(false, fetchCalled)
    }

    @Test
    fun `failed refresh keeps previous balance`() = runTest {
        val previous = BalanceSnapshot("9.99", true, 50L)
        val cache = FakeBalanceCache(BalanceCacheState(previous, null))
        val service = BalanceService(
            apiKeyStore = FakeApiKeyStore("secret"),
            fetcher = FakeBalanceFetcher { throw BalanceFetchException("请求超时，请稍后重试") },
            cache = cache,
        )

        val result = service.refresh()

        assertIs<BalanceRefreshResult.Failure>(result)
        assertEquals(previous, result.cachedSnapshot)
        assertEquals(previous, cache.state.snapshot)
        assertEquals("请求超时，请稍后重试", cache.state.errorMessage)
    }

    private class FakeApiKeyStore(private var apiKey: String?) : ApiKeyStore {
        override fun load(): String? = apiKey

        override fun save(apiKey: String) {
            this.apiKey = apiKey
        }
    }

    private class FakeBalanceFetcher(
        private val response: suspend (String) -> BalanceSnapshot,
    ) : BalanceFetcher {
        constructor(snapshot: BalanceSnapshot) : this({ snapshot })

        override suspend fun fetch(apiKey: String): BalanceSnapshot = response(apiKey)
    }

    private class FakeBalanceCache(
        var state: BalanceCacheState = BalanceCacheState(null, null),
    ) : BalanceCache {
        override fun read(): BalanceCacheState = state

        override fun saveSnapshot(snapshot: BalanceSnapshot) {
            state = BalanceCacheState(snapshot, null)
        }

        override fun saveError(message: String) {
            state = state.copy(errorMessage = message)
        }
    }
}
