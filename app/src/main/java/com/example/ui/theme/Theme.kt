package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LupoDarkColorScheme = darkColorScheme(
  primary = LupoPrimaryGold,
  onPrimary = Color.Black,
  secondary = LupoCyanNode,
  onSecondary = Color.Black,
  tertiary = LupoEmeraldGreen,
  background = LupoCanvasDark,
  onBackground = LupoTextPrimary,
  surface = LupoSurfaceDark,
  onSurface = LupoTextPrimary,
  surfaceVariant = LupoSurfaceVariant,
  onSurfaceVariant = LupoTextSecondary,
  outline = LupoCardBorder
)

@Composable
fun LupoAideTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = LupoDarkColorScheme,
    typography = Typography,
    content = content
  )
}

