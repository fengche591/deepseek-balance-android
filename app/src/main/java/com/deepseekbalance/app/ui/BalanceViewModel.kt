package com.deepseekbalance.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deepseekbalance.app.data.BalanceRefreshResult
import com.deepseekbalance.app.data.BalanceService
import com.deepseekbalance.app.data.BalanceServiceFactory
import com.deepseekbalance.app.data.BalanceSnapshot
import com.deepseekbalance.app.widget.BalanceWidgetRenderer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BalanceUiState(
    val apiKeyInput: String = "",
    val hasSavedApiKey: Boolean = false,
    val isRefreshing: Boolean = false,
    val snapshot: BalanceSnapshot? = null,
    val errorMessage: String? = null,
)

class BalanceViewModel(application: Application) : AndroidViewModel(application) {
    private val service: BalanceService = BalanceServiceFactory.create(application)
    private val currentCacheState = service.cachedState()

    private val _uiState = MutableStateFlow(
        BalanceUiState(
            hasSavedApiKey = service.hasApiKey(),
            snapshot = currentCacheState.snapshot,
            errorMessage = currentCacheState.errorMessage,
        ),
    )
    val uiState: StateFlow<BalanceUiState> = _uiState.asStateFlow()

    init {
        if (_uiState.value.hasSavedApiKey) {
            refresh()
        }
    }

    fun onApiKeyChanged(value: String) {
        _uiState.update {
            it.copy(
                apiKeyInput = value,
                errorMessage = null,
            )
        }
    }

    fun saveAndRefresh() {
        val apiKey = _uiState.value.apiKeyInput.trim()
        if (apiKey.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "请输入 DeepSeek API Key") }
            return
        }

        service.saveApiKey(apiKey)
        _uiState.update {
            it.copy(
                apiKeyInput = "",
                hasSavedApiKey = true,
                errorMessage = null,
            )
        }
        refresh()
    }

    fun refresh() {
        if (_uiState.value.isRefreshing) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            when (val result = service.refresh()) {
                is BalanceRefreshResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            snapshot = result.snapshot,
                            errorMessage = null,
                        )
                    }
                }

                is BalanceRefreshResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            snapshot = result.cachedSnapshot,
                            errorMessage = result.message,
                        )
                    }
                }
            }
            BalanceWidgetRenderer.updateAll(getApplication())
        }
    }
}
