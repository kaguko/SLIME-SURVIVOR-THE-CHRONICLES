package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GameColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = ForestDarkSurface,
    onPrimaryContainer = NeonCyan,
    secondary = NeonFireOrange,
    onSecondary = Color.Black,
    secondaryContainer = CardWoodBg,
    onSecondaryContainer = PixelGold,
    tertiary = SporePurple,
    onTertiary = Color.White,
    background = ForestNightDark,
    onBackground = Color(0xFFE2E8F0),
    surface = ForestDarkSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = NeonCyan
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GameColorScheme,
        typography = Typography,
        content = content
    )
}
