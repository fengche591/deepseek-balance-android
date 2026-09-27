package com.deepseekbalance.app.data

import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface BalanceFetcher {
    suspend fun fetch(apiKey: String): BalanceSnapshot
}

class BalanceFetchException(message: String) : Exception(message)

class DeepSeekBalanceClient(
    private val httpClient: OkHttpClient = defaultHttpClient(),
    private val endpoint: HttpUrl = DEFAULT_ENDPOINT.toHttpUrl(),
    private val clock: () -> Long = System::currentTimeMillis,
) : BalanceFetcher {
    override suspend fun fetch(apiKey: String): BalanceSnapshot = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "application/json")
            .get()
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw BalanceFetchException(httpErrorMessage(response.code))
                }
                DeepSeekBalanceParser.parse(body, clock())
            }
        } catch (exception: BalanceFetchException) {
            throw exception
        } catch (_: SocketTimeoutException) {
            throw BalanceFetchException("请求超时，请稍后重试")
        } catch (_: IOException) {
            throw BalanceFetchException("网络连接失败，请检查网络后重试")
        } catch (_: SerializationException) {
            throw BalanceFetchException("余额响应格式异常")
        }
    }

    private fun httpErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            401 -> "API Key 无效或已失效"
            403 -> "API Key 没有查询余额的权限"
            429 -> "请求过于频繁，请稍后重试"
            else -> "余额查询失败（HTTP $statusCode）"
        }
    }

    private companion object {
        const val DEFAULT_ENDPOINT = "https://api.deepseek.com/user/balance"

        fun defaultHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .callTimeout(20, TimeUnit.SECONDS)
                .build()
        }
    }
}

object DeepSeekBalanceParser {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun parse(responseBody: String, fetchedAtEpochMillis: Long): BalanceSnapshot {
        val response = json.decodeFromString<BalanceResponse>(responseBody)
        val cnyBalance = response.balanceInfos.firstOrNull {
            it.currency.equals("CNY", ignoreCase = true)
        } ?: throw BalanceFetchException("未找到人民币余额")

        return BalanceSnapshot(
            totalBalance = cnyBalance.totalBalance,
            isAvailable = response.isAvailable,
            fetchedAtEpochMillis = fetchedAtEpochMillis,
        )
    }
}

@Serializable
private data class BalanceResponse(
    @SerialName("is_available")
    val isAvailable: Boolean = false,
    @SerialName("balance_infos")
    val balanceInfos: List<BalanceInfo> = emptyList(),
)

@Serializable
private data class BalanceInfo(
    val currency: String,
    @SerialName("total_balance")
    val totalBalance: String,
)
