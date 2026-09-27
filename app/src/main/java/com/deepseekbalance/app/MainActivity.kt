package com.deepseekbalance.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.deepseekbalance.app.ui.BalanceScreen
import com.deepseekbalance.app.ui.BalanceViewModel
import com.deepseekbalance.app.ui.theme.DeepSeekBalanceTheme

class MainActivity : ComponentActivity() {
    private val viewModel: BalanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeepSeekBalanceTheme {
                BalanceScreen(viewModel)
            }
        }
    }
}
