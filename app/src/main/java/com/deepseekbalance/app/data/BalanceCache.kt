package com.deepseekbalance.app.data

import android.content.Context

data class BalanceCacheState(
    val snapshot: BalanceSnapshot?,
    val errorMessage: String?,
)

interface BalanceCache {
    fun read(): BalanceCacheState

    fun saveSnapshot(snapshot: BalanceSnapshot)

    fun saveError(message: String)
}

class SharedPreferencesBalanceCache(context: Context) : BalanceCache {
    private val preferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun read(): BalanceCacheState {
        val totalBalance = preferences.getString(KEY_TOTAL_BALANCE, null)
        val fetchedAt = preferences.getLong(KEY_FETCHED_AT, 0L)
        val snapshot = if (totalBalance != null && fetchedAt > 0L) {
            BalanceSnapshot(
                totalBalance = totalBalance,
                isAvailable = preferences.getBoolean(KEY_IS_AVAILABLE, false),
                fetchedAtEpochMillis = fetchedAt,
            )
        } else {
            null
        }

        return BalanceCacheState(
            snapshot = snapshot,
            errorMessage = preferences.getString(KEY_ERROR_MESSAGE, null),
        )
    }

    override fun saveSnapshot(snapshot: BalanceSnapshot) {
        preferences.edit()
            .putString(KEY_TOTAL_BALANCE, snapshot.totalBalance)
            .putBoolean(KEY_IS_AVAILABLE, snapshot.isAvailable)
            .putLong(KEY_FETCHED_AT, snapshot.fetchedAtEpochMillis)
            .remove(KEY_ERROR_MESSAGE)
            .commit()
    }

    override fun saveError(message: String) {
        preferences.edit()
            .putString(KEY_ERROR_MESSAGE, message)
            .commit()
    }

    private companion object {
        const val KEY_ERROR_MESSAGE = "error_message"
        const val KEY_FETCHED_AT = "fetched_at"
        const val KEY_IS_AVAILABLE = "is_available"
        const val KEY_TOTAL_BALANCE = "total_balance"
        const val PREFERENCES_NAME = "balance_cache"
    }
}
