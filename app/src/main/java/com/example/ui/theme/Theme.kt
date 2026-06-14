package com.example.ui.theme

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

private val ElegantDarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF49454F),
    onPrimaryContainer = Color(0xFFD0BCFF),
    secondary = Color(0xFFEADDFF),
    onSecondary = Color(0xFF21005D),
    secondaryContainer = Color(0xFF2B2930),
    onSecondaryContainer = Color(0xFFE6E1E5),
    tertiary = Color(0xFFD0BCFF),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2B2930), // Match card backgrounds exactly
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF49454F),
    outlineVariant = Color(0xFF313033)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Lock to dark for Elegant Dark
  dynamicColor: Boolean = false, // Disable system dynamics so Elegant Dark shines
  appliedFont: com.example.data.model.FontEntity? = null,
  content: @Composable () -> Unit,
) {
  val colorScheme = ElegantDarkColorScheme
  val computedTypography = generateCustomTypography(appliedFont)

  MaterialTheme(colorScheme = colorScheme, typography = computedTypography, content = content)
}
