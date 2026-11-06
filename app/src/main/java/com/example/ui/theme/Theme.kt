package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LofiDarkColorScheme = darkColorScheme(
    primary = LofiPeach,
    onPrimary = Color(0xFF2B1810),
    primaryContainer = LofiSurfaceVariant,
    onPrimaryContainer = LofiCream,
    secondary = LofiMatcha,
    onSecondary = Color(0xFF102818),
    secondaryContainer = Color(0xFF2C3E30),
    onSecondaryContainer = LofiCream,
    tertiary = LofiLavender,
    onTertiary = Color(0xFF251330),
    background = LofiDuskBg,
    onBackground = LofiCream,
    surface = LofiSurface,
    onSurface = LofiCream,
    surfaceVariant = LofiSurfaceVariant,
    onSurfaceVariant = LofiMutedText,
    outline = Color(0xFF4A3E5C)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LofiDarkColorScheme,
        typography = LofiTypography,
        content = content
    )
}
