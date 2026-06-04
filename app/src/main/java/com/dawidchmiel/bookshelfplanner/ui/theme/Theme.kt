package com.dawidchmiel.bookshelfplanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colors extracted and inspired by the app launcher logo
private val DeepNavy = Color(0xFF011D44)
private val LightCream = Color(0xFFFAF7F0)
private val DarkCream = Color(0xFFEFECE3)
private val MutedGreen = Color(0xFF458356)
private val SoftCream = Color(0xFFFCFBF7)
private val White = Color(0xFFFFFFFF)

// Dark theme color definitions
private val DarkNavy = Color(0xFF0A1220)
private val NavyBlue = Color(0xFF1E2E4A)
private val LightNavy = Color(0xFF2C3E5B)
private val CreamText = Color(0xFFFAF7F0)
private val LightGreen = Color(0xFF67B07D)

private val LightColors = lightColorScheme(
    primary = DeepNavy,
    onPrimary = White,
    secondary = MutedGreen,
    onSecondary = White,
    background = LightCream,
    onBackground = DeepNavy,
    surface = SoftCream,
    onSurface = DeepNavy,
    surfaceVariant = DarkCream,
    onSurfaceVariant = DeepNavy,
    secondaryContainer = DarkCream,
    onSecondaryContainer = DeepNavy,
    surfaceTint = Color.Transparent
)

private val DarkColors = darkColorScheme(
    primary = NavyBlue,
    onPrimary = CreamText,
    secondary = LightGreen,
    onSecondary = DarkNavy,
    background = DarkNavy,
    onBackground = CreamText,
    surface = NavyBlue,
    onSurface = CreamText,
    surfaceVariant = LightNavy,
    onSurfaceVariant = CreamText,
    secondaryContainer = LightNavy,
    onSecondaryContainer = CreamText,
    surfaceTint = Color.Transparent
)

@Composable
fun BookShelfTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
