package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BennuGold,
    onPrimary = Color(0xFF241500),
    primaryContainer = Color(0xFF422800),
    onPrimaryContainer = BennuGoldLight,
    secondary = BennuAmber,
    onSecondary = Color(0xFF241500),
    secondaryContainer = Color(0xFF332000),
    onSecondaryContainer = BennuGoldLight,
    tertiary = StudyCyan,
    onTertiary = Color(0xFF001F2A),
    background = MidnightBackground,
    onBackground = TextPrimary,
    surface = MidnightSurface,
    onSurface = TextPrimary,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = MidnightCardBorder,
    outlineVariant = MidnightCardBorderSubtle
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
