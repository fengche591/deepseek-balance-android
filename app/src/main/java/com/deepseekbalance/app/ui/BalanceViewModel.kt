package com.deepseekbalance.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deepseekbalance.app.data.BalanceRefreshResult
import com.deepseekbalance.app.data.BalanceService
import com.deepseekbalance.app.data.BalanceServiceFactory
import com.deepseekbalance.app.data.BalanceSnapshot
import com.deepseekbalance.app.widget.BalanceWidgetRenderer
import com.deepseekbalance.app.widget.WidgetImageStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BalanceUiState(
    val apiKeyInput: String = "",
    val hasSavedApiKey: Boolean = false,
    val isRefreshing: Boolean = false,
    val snapshot: BalanceSnapshot? = null,
    val errorMessage: String? = null,
    val hasCustomWidgetImage: Boolean = false,
    val isSavingWidgetImage: Boolean = false,
    val widgetImageErrorMessage: String? = null,
)

class BalanceViewModel(application: Application) : AndroidViewModel(application) {
    private val service: BalanceService = BalanceServiceFactory.create(application)
    private val widgetImageStore = WidgetImageStore(application)
    private val currentCacheState = service.cachedState()

    private val _uiState = MutableStateFlow(
        BalanceUiState(
            hasSavedApiKey = service.hasApiKey(),
            snapshot = currentCacheState.snapshot,
            errorMessage = currentCacheState.errorMessage,
            hasCustomWidgetImage = widgetImageStore.hasImage(),
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

    fun setWidgetImage(uri: Uri) {
        if (_uiState.value.isSavingWidgetImage) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSavingWidgetImage = true,
                    widgetImageErrorMessage = null,
                )
            }
            val saved = withContext(Dispatchers.IO) {
                widgetImageStore.saveFromUri(uri)
            }
            _uiState.update {
                it.copy(
                    isSavingWidgetImage = false,
                    hasCustomWidgetImage = saved,
                    widgetImageErrorMessage = if (saved) null else "背景图片设置失败",
                )
            }
            if (saved) {
                BalanceWidgetRenderer.updateAll(getApplication())
            }
        }
    }

    fun clearWidgetImage() {
        if (_uiState.value.isSavingWidgetImage) {
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                widgetImageStore.clear()
            }
            _uiState.update {
                it.copy(
                    hasCustomWidgetImage = false,
                    widgetImageErrorMessage = null,
                )
            }
            BalanceWidgetRenderer.updateAll(getApplication())
        }
    }
}
