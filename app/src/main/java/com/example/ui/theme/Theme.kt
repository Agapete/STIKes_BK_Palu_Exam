package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SkyLight,
    onPrimary = Navy900,
    primaryContainer = NavyPrimaryDark,
    onPrimaryContainer = Slate100,
    secondary = CyanAccent,
    onSecondary = Navy900,
    background = Navy900,
    surface = Navy800,
    onBackground = Slate100,
    onSurface = Slate100,
    surfaceVariant = Navy700,
    onSurfaceVariant = Slate200
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NavyPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Slate100,
    onPrimaryContainer = Navy900,
    secondary = BlueAccent,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = Slate50,
    surface = androidx.compose.ui.graphics.Color.White,
    onBackground = Slate900,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
