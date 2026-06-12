package com.flypigs.typechomanager.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Material 3 Expressive — Blogga 配色方案
// 主色 #5B6EFF、背景 #F6F7FB、卡片 #FFFFFF
private val LightColors = lightColorScheme(
    primary = Color(0xFF5B6EFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E2FF),
    onPrimaryContainer = Color(0xFF00105C),
    secondary = Color(0xFF5CC8FF),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6EDFF),
    onSecondaryContainer = Color(0xFF001E33),
    tertiary = Color(0xFF7C5CCC),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEDE0FF),
    onTertiaryContainer = Color(0xFF20005C),
    error = Color(0xFFFF5252),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF6F7FB),
    onBackground = Color(0xFF1A1C20),
    surface = Color.White,
    onSurface = Color(0xFF1A1C20),
    surfaceVariant = Color(0xFFE7E8F0),
    onSurfaceVariant = Color(0xFF444650),
    surfaceContainerHigh = Color(0xFFF0F1F5),
    surfaceContainer = Color(0xFFF6F7FB),
    surfaceContainerLow = Color(0xFFFBFBFE),
    outline = Color(0xFF747680),
    outlineVariant = Color(0xFFC4C6D0),
    inverseSurface = Color(0xFF2F3038),
    inverseOnSurface = Color(0xFFEFF0F8),
    inversePrimary = Color(0xFFB8C3FF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB8C3FF),
    onPrimary = Color(0xFF002579),
    primaryContainer = Color(0xFF0039A8),
    onPrimaryContainer = Color(0xFFE0E2FF),
    secondary = Color(0xFF8ED4FF),
    onSecondary = Color(0xFF003550),
    secondaryContainer = Color(0xFF004C73),
    onSecondaryContainer = Color(0xFFD6EDFF),
    tertiary = Color(0xFFD4BBFF),
    onTertiary = Color(0xFF370090),
    tertiaryContainer = Color(0xFF5200B8),
    onTertiaryContainer = Color(0xFFEDE0FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF121318),
    onBackground = Color(0xFFE2E2EC),
    surface = Color(0xFF1A1C20),
    onSurface = Color(0xFFE2E2EC),
    surfaceVariant = Color(0xFF444650),
    onSurfaceVariant = Color(0xFFC4C6D0),
    surfaceContainerHigh = Color(0xFF2A2C32),
    surfaceContainer = Color(0xFF1F2125),
    surfaceContainerLow = Color(0xFF1A1C20),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF444650),
    inverseSurface = Color(0xFFE2E2EC),
    inverseOnSurface = Color(0xFF2F3038),
    inversePrimary = Color(0xFF0050E0),
)

@Composable
fun TypechoManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,  // 默认关闭动态取色，保持品牌色
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
