package com.deepseekbalance.app.data

data class BalanceSnapshot(
    val totalBalance: String,
    val isAvailable: Boolean,
    val fetchedAtEpochMillis: Long,
)
