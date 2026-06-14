package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.model.FontEntity
import com.example.ui.components.FontStyleMapper

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    )
)

fun generateCustomTypography(font: FontEntity?): Typography {
    if (font == null) return Typography

    val baseStyle = FontStyleMapper.getComposeStyle(font, fontSize = 16.sp)
    val displayFontFamily = baseStyle.fontFamily
    val fontWeight = baseStyle.fontWeight
    val fontStyle = baseStyle.fontStyle
    val letterSpacing = baseStyle.letterSpacing
    val shadow = baseStyle.shadow

    fun buildStyle(baseSize: Float, baseLineHeight: Float): TextStyle {
        return TextStyle(
            fontFamily = displayFontFamily,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            letterSpacing = letterSpacing,
            shadow = shadow,
            fontSize = baseSize.sp,
            lineHeight = baseLineHeight.sp
        )
    }

    return Typography(
        displayLarge = buildStyle(57f, 64f),
        displayMedium = buildStyle(45f, 52f),
        displaySmall = buildStyle(36f, 44f),
        headlineLarge = buildStyle(32f, 40f),
        headlineMedium = buildStyle(28f, 36f),
        headlineSmall = buildStyle(24f, 32f),
        titleLarge = buildStyle(22f, 28f),
        titleMedium = buildStyle(16f, 24f),
        titleSmall = buildStyle(14f, 20f),
        bodyLarge = buildStyle(16f, 24f),
        bodyMedium = buildStyle(14f, 20f),
        bodySmall = buildStyle(12f, 16f),
        labelLarge = buildStyle(14f, 20f),
        labelMedium = buildStyle(12f, 16f),
        labelSmall = buildStyle(11f, 16f)
    )
}

