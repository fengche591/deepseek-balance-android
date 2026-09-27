package com.deepseekbalance.app.data

sealed interface BalanceRefreshResult {
    data class Success(val snapshot: BalanceSnapshot) : BalanceRefreshResult

    data class Failure(
        val message: String,
        val cachedSnapshot: BalanceSnapshot?,
    ) : BalanceRefreshResult
}

class BalanceService(
    private val apiKeyStore: ApiKeyStore,
    private val fetcher: BalanceFetcher,
    private val cache: BalanceCache,
) {
    fun hasApiKey(): Boolean = !apiKeyStore.load().isNullOrBlank()

    fun cachedState(): BalanceCacheState = cache.read()

    fun saveApiKey(apiKey: String) {
        apiKeyStore.save(apiKey)
    }

    suspend fun refresh(): BalanceRefreshResult {
        val apiKey = apiKeyStore.load()
        if (apiKey.isNullOrBlank()) {
            val message = "请先保存 DeepSeek API Key"
            cache.saveError(message)
            return BalanceRefreshResult.Failure(message, cache.read().snapshot)
        }

        return try {
            val snapshot = fetcher.fetch(apiKey)
            cache.saveSnapshot(snapshot)
            BalanceRefreshResult.Success(snapshot)
        } catch (exception: BalanceFetchException) {
            val message = exception.message ?: "余额查询失败"
            cache.saveError(message)
            BalanceRefreshResult.Failure(message, cache.read().snapshot)
        }
    }
}

object BalanceServiceFactory {
    fun create(context: android.content.Context): BalanceService {
        val applicationContext = context.applicationContext
        return BalanceService(
            apiKeyStore = EncryptedApiKeyStore(applicationContext),
            fetcher = DeepSeekBalanceClient(),
            cache = SharedPreferencesBalanceCache(applicationContext),
        )
    }
}
