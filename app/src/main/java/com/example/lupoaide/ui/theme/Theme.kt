package com.example.lupoaide.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary, // #000c3d
    onPrimary = LightPrimary, // #FFFFFF
    primaryContainer = DarkNavyContainer,
    onPrimaryContainer = TertiaryBoth, // #99c5ff
    secondary = DarkSecondary, // #4f5058
    onSecondary = LightPrimary,
    secondaryContainer = Color(0xFF282C35),
    onSecondaryContainer = LightSecondary, // #b5bbc3
    tertiary = TertiaryBoth, // #99c5ff
    onTertiary = DarkPrimary,
    tertiaryContainer = Color(0xFF0F2B6B),
    onTertiaryContainer = TertiaryBoth,
    background = DarkNavyBackground, // #000c3d
    onBackground = LightPrimary,
    surface = DarkNavySurface,
    onSurface = LightPrimary,
    surfaceVariant = DarkNavySurfaceVariant,
    onSurfaceVariant = LightSecondary,
    outline = DarkSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary, // #FFFFFF
    onPrimary = DarkPrimary, // #000c3d
    primaryContainer = LightContainer,
    onPrimaryContainer = DarkPrimary,
    secondary = LightSecondary, // #b5bbc3
    onSecondary = DarkPrimary,
    secondaryContainer = Color(0xFFE2E6EC),
    onSecondaryContainer = DarkPrimary,
    tertiary = TertiaryBoth, // #99c5ff
    onTertiary = DarkPrimary,
    tertiaryContainer = Color(0xFFD6E7FF),
    onTertiaryContainer = DarkPrimary,
    background = LightPureBackground, // #FFFFFF
    onBackground = DarkPrimary,
    surface = LightPureSurface, // #FFFFFF
    onSurface = DarkPrimary,
    surfaceVariant = LightPureSurfaceVariant,
    onSurfaceVariant = DarkSecondary,
    outline = LightSecondary
)

@Composable
fun LupoAideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
