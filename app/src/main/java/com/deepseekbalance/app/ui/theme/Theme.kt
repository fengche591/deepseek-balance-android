package com.deepseekbalance.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B70F3),
    onPrimary = Color.White,
    secondary = Color(0xFF006A67),
    onSecondary = Color.White,
    tertiary = Color(0xFF8A5100),
    onTertiary = Color.White,
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF1B1F23),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1F23),
    surfaceVariant = Color(0xFFE7EAEE),
    onSurfaceVariant = Color(0xFF454A4F),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8C8FF),
    onPrimary = Color(0xFF003062),
    secondary = Color(0xFF55DAD5),
    onSecondary = Color(0xFF003735),
    tertiary = Color(0xFFFFB95C),
    onTertiary = Color(0xFF492900),
    background = Color(0xFF111418),
    onBackground = Color(0xFFE1E2E5),
    surface = Color(0xFF181C20),
    onSurface = Color(0xFFE1E2E5),
    surfaceVariant = Color(0xFF42474D),
    onSurfaceVariant = Color(0xFFC2C7CE),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun DeepSeekBalanceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
