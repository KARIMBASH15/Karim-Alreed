package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val GameColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = DarkNavyBackground,
    primaryContainer = GoldDark,
    secondary = LudoBlue,
    onSecondary = TextWhite,
    background = DarkNavyBackground,
    surface = DarkNavySurface,
    surfaceVariant = DarkNavyCard,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun LudoDominoTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = GameColorScheme,
            typography = Typography,
            content = content
        )
    }
}
