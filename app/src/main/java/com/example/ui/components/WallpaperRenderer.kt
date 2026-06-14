package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.data.model.WallpaperEntity
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WallpaperRenderer(
    wallpaper: WallpaperEntity,
    modifier: Modifier = Modifier,
    customPrimaryColor: Color? = null,
    customSecondaryColor: Color? = null,
    customTertiaryColor: Color? = null
) {
    val pColor = customPrimaryColor ?: Color(wallpaper.primaryColor)
    val sColor = customSecondaryColor ?: Color(wallpaper.secondaryColor)
    val tColor = customTertiaryColor ?: Color(wallpaper.tertiaryColor)
    val patternType = wallpaper.patternType

    Canvas(modifier = modifier.fillMaxSize()) {
        try {
            when (patternType) {
                0 -> drawSmoothGradient(pColor, sColor, tColor)
                1 -> drawAuroraWaves(pColor, sColor, tColor)
                2 -> drawNeonGrid(pColor, sColor, tColor)
                3 -> drawStarryGalaxy(pColor, sColor, tColor)
                4 -> drawGeometricMatrix(pColor, sColor, tColor)
                5 -> drawOrganicLiquid(pColor, sColor, tColor)
                else -> drawRetroSynthWave(pColor, sColor, tColor)
            }
        } catch (e: Exception) {
            // Safe fallback to simple solid background
            drawRect(color = pColor)
        }
    }
}

private fun DrawScope.drawSmoothGradient(primary: Color, secondary: Color, tertiary: Color) {
    // Elegant Multi-stop linear gradient
    val brush = Brush.linearGradient(
        colors = listOf(primary, secondary, tertiary),
        start = Offset(0f, 0f),
        end = Offset(size.width, size.height)
    )
    drawRect(brush = brush)
}

private fun DrawScope.drawAuroraWaves(primary: Color, secondary: Color, tertiary: Color) {
    // Solid deep primary color background
    drawRect(color = primary)

    // Draw flowing wave 1 (secondary color)
    val path1 = Path().apply {
        moveTo(0f, size.height * 0.4f)
        cubicTo(
            size.width * 0.3f, size.height * 0.25f,
            size.width * 0.7f, size.height * 0.55f,
            size.width, size.height * 0.35f
        )
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    val brush1 = Brush.verticalGradient(
        colors = listOf(secondary.copy(alpha = 0.7f), Color.Transparent),
        startY = size.height * 0.25f,
        endY = size.height
    )
    drawPath(path = path1, brush = brush1)

    // Draw flowing wave 2 (tertiary color)
    val path2 = Path().apply {
        moveTo(0f, size.height * 0.55f)
        cubicTo(
            size.width * 0.4f, size.height * 0.65f,
            size.width * 0.6f, size.height * 0.45f,
            size.width, size.height * 0.58f
        )
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    val brush2 = Brush.verticalGradient(
        colors = listOf(tertiary.copy(alpha = 0.6f), Color.Transparent),
        startY = size.height * 0.45f,
        endY = size.height
    )
    drawPath(path = path2, brush = brush2)
}

private fun DrawScope.drawNeonGrid(primary: Color, secondary: Color, tertiary: Color) {
    // Dark background using primary color
    drawRect(color = primary)

    val gridSpacing = size.width / 12f

    // Draw horizontal grid lines
    var y = 0f
    while (y < size.height) {
        // Draw glow line
        drawLine(
            color = secondary.copy(alpha = 0.35f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 3f
        )
        // Center thin line
        drawLine(
            color = secondary.copy(alpha = 0.8f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += gridSpacing
    }

    // Draw vertical grid lines
    var x = 0f
    while (x < size.width) {
        drawLine(
            color = tertiary.copy(alpha = 0.35f),
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 3f
        )
        drawLine(
            color = tertiary.copy(alpha = 0.8f),
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
        x += gridSpacing
    }
}

private fun DrawScope.drawStarryGalaxy(primary: Color, secondary: Color, tertiary: Color) {
    // Deep starry night gradient background
    val bgBrush = Brush.radialGradient(
        colors = listOf(secondary, primary),
        center = Offset(size.width * 0.5f, size.height * 0.5f),
        radius = size.width * 0.9f
    )
    drawRect(brush = bgBrush)

    // Ambient Nebula Glow
    val nebulaBrush = Brush.radialGradient(
        colors = listOf(tertiary.copy(alpha = 0.3f), Color.Transparent),
        center = Offset(size.width * 0.3f, size.height * 0.3f),
        radius = size.width * 0.5f
    )
    drawCircle(brush = nebulaBrush, radius = size.width * 0.5f, center = Offset(size.width * 0.3f, size.height * 0.3f))

    // Let's draw 30 deterministic shiny stars based on canvas coordinates
    for (i in 0..30) {
        val starX = (sin(i.toDouble() * 123.45) * 0.5 + 0.5) * size.width
        val starY = (cos(i.toDouble() * 987.65) * 0.5 + 0.5) * size.height
        val starRadius = (sin(i.toDouble()) * 1.5 + 2.0).toFloat()
        val opacity = (cos(i.toDouble() * 2.0) * 0.3 + 0.7).toFloat()

        drawCircle(
            color = Color.White.copy(alpha = opacity),
            radius = starRadius,
            center = Offset(starX.toFloat(), starY.toFloat())
        )
    }
}

private fun DrawScope.drawGeometricMatrix(primary: Color, secondary: Color, tertiary: Color) {
    // Clean solid color background
    drawRect(color = primary)

    // Overlapping modern geometrical forms
    // Top-left design circle
    drawCircle(
        color = secondary.copy(alpha = 0.25f),
        radius = size.width * 0.4f,
        center = Offset(size.width * 0.15f, size.height * 0.2f)
    )

    // Bottom-right giant ring
    drawCircle(
        color = tertiary.copy(alpha = 0.2f),
        radius = size.width * 0.5f,
        center = Offset(size.width * 0.9f, size.height * 0.85f)
    )

    // Tilted Brutalist Squares
    val squareSize = size.width * 0.3f
    val path = Path().apply {
        moveTo(size.width * 0.7f, size.height * 0.25f)
        lineTo(size.width * 0.7f + squareSize, size.height * 0.25f + 50f)
        lineTo(size.width * 0.7f + squareSize - 50f, size.height * 0.25f + squareSize + 50f)
        lineTo(size.width * 0.7f - 50f, size.height * 0.25f + squareSize)
        close()
    }
    drawPath(path = path, color = secondary.copy(alpha = 0.15f))

    // Dynamic diagonal accent bar
    drawLine(
        color = tertiary.copy(alpha = 0.3f),
        start = Offset(0f, size.height * 0.75f),
        end = Offset(size.width, size.height * 0.55f),
        strokeWidth = 24f
    )
}

private fun DrawScope.drawOrganicLiquid(primary: Color, secondary: Color, tertiary: Color) {
    // Solid canvas
    drawRect(color = primary)

    // Blob 1
    val blob1 = Path().apply {
        moveTo(0f, size.height * 0.2f)
        cubicTo(
            size.width * 0.4f, size.height * 0.1f,
            size.width * 0.5f, size.height * 0.4f,
            size.width * 0.3f, size.height * 0.5f
        )
        cubicTo(
            size.width * 0.1f, size.height * 0.6f,
            size.width * 0.2f, size.height * 0.8f,
            0f, size.height * 0.75f
        )
        close()
    }
    drawPath(path = blob1, color = secondary.copy(alpha = 0.35f))

    // Blob 2
    val blob2 = Path().apply {
        moveTo(size.width, size.height * 0.8f)
        cubicTo(
            size.width * 0.6f, size.height * 0.9f,
            size.width * 0.5f, size.height * 0.6f,
            size.width * 0.7f, size.height * 0.5f
        )
        cubicTo(
            size.width * 0.9f, size.height * 0.4f,
            size.width * 0.8f, size.height * 0.2f,
            size.width, size.height * 0.3f
        )
        close()
    }
    drawPath(path = blob2, color = tertiary.copy(alpha = 0.3f))
}

private fun DrawScope.drawRetroSynthWave(primary: Color, secondary: Color, tertiary: Color) {
    // Midnight background using primary color
    drawRect(color = primary)

    // Dynamic Retro Sunset graphic (semi-circle with segment bars)
    val sunCenterY = size.height * 0.45f
    val sunRadius = size.width * 0.35f
    val sunCenterX = size.width * 0.5f

    // Under-glow of sun
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(secondary.copy(alpha = 0.4f), Color.Transparent),
            center = Offset(sunCenterX, sunCenterY),
            radius = sunRadius * 1.5f
        ),
        radius = sunRadius * 1.5f,
        center = Offset(sunCenterX, sunCenterY)
    )

    // Draw Sunset stripes
    var clipY = sunCenterY - sunRadius
    val sunGg = Brush.verticalGradient(
        colors = listOf(secondary, tertiary),
        startY = sunCenterY - sunRadius,
        endY = sunCenterY + sunRadius
    )

    // Segment draw using custom arc calculations
    drawCircle(
        brush = sunGg,
        radius = sunRadius,
        center = Offset(sunCenterX, sunCenterY)
    )

    // Grid wireframe horizontal perspective lines at bottom
    val perspectiveStartY = size.height * 0.55f
    val perspectiveEndY = size.height
    val linesCount = 10

    for (i in 0..linesCount) {
        // Perspective horizontal spacing
        val progress = i.toFloat() / linesCount
        // Exponential-like curving spacing to simulate deep 3D perspective
        val currentY = perspectiveStartY + (progress * progress) * (perspectiveEndY - perspectiveStartY)

        drawLine(
            color = tertiary.copy(alpha = progress * 0.61f),
            start = Offset(0f, currentY),
            end = Offset(size.width, currentY),
            strokeWidth = 2f
        )
    }

    // Perspective vertical radiating lines
    val vanishingPointX = size.width * 0.5f
    val vanishingPointY = perspectiveStartY - 100f
    val columnsCount = 12

    for (c in 0..columnsCount) {
        val bottomPortion = c.toFloat() / columnsCount
        val edgeX = bottomPortion * size.width

        drawLine(
            color = tertiary.copy(alpha = 0.45f),
            start = Offset(vanishingPointX, vanishingPointY),
            end = Offset(edgeX, perspectiveEndY),
            strokeWidth = 1.5f
        )
    }
}
