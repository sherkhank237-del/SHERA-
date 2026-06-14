package com.example.ui.components

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.Log
import android.widget.Toast
import com.example.data.model.WallpaperEntity
import kotlin.math.cos
import kotlin.math.sin

object WallpaperBitmapGenerator {

    fun generateWallpaperBitmap(wallpaper: WallpaperEntity, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val pColor = (wallpaper.primaryColor).toInt()
        val sColor = (wallpaper.secondaryColor).toInt()
        val tColor = (wallpaper.tertiaryColor).toInt()

        when (wallpaper.patternType) {
            0 -> drawSmoothGradient(canvas, paint, width, height, pColor, sColor, tColor)
            1 -> drawAuroraWaves(canvas, paint, width, height, pColor, sColor, tColor)
            2 -> drawNeonGrid(canvas, paint, width, height, pColor, sColor, tColor)
            3 -> drawStarryGalaxy(canvas, paint, width, height, pColor, sColor, tColor)
            4 -> drawGeometricMatrix(canvas, paint, width, height, pColor, sColor, tColor)
            5 -> drawOrganicLiquid(canvas, paint, width, height, pColor, sColor, tColor)
            else -> drawRetroSynthWave(canvas, paint, width, height, pColor, sColor, tColor)
        }

        return bitmap
    }

    private fun drawSmoothGradient(canvas: Canvas, paint: Paint, w: Int, h: Int, p: Int, s: Int, t: Int) {
        val shader = LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), intArrayOf(p, s, t), null, Shader.TileMode.CLAMP)
        paint.shader = shader
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null
    }

    private fun drawAuroraWaves(canvas: Canvas, paint: Paint, w: Int, h: Int, p: Int, s: Int, t: Int) {
        canvas.drawColor(p)

        // Wave 1
        val path1 = Path().apply {
            moveTo(0f, h * 0.4f)
            cubicTo(w * 0.3f, h * 0.25f, w * 0.7f, h * 0.55f, w.toFloat(), h * 0.35f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        val shader1 = LinearGradient(0f, h * 0.25f, 0f, h.toFloat(), s, 0x00FFFFFF, Shader.TileMode.CLAMP)
        paint.shader = shader1
        canvas.drawPath(path1, paint)

        // Wave 2
        val path2 = Path().apply {
            moveTo(0f, h * 0.55f)
            cubicTo(w * 0.4f, h * 0.65f, w * 0.6f, h * 0.45f, w.toFloat(), h * 0.58f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        val shader2 = LinearGradient(0f, h * 0.45f, 0f, h.toFloat(), t, 0x00FFFFFF, Shader.TileMode.CLAMP)
        paint.shader = shader2
        canvas.drawPath(path2, paint)
        paint.shader = null
    }

    private fun drawNeonGrid(canvas: Canvas, paint: Paint, w: Int, h: Int, p: Int, s: Int, t: Int) {
        canvas.drawColor(p)

        val gridSpacing = w / 12f

        // Draw horizontal grid lines
        var y = 0f
        while (y < h) {
            // Glow line
            paint.color = s
            paint.alpha = 90
            paint.strokeWidth = 3f
            canvas.drawLine(0f, y, w.toFloat(), y, paint)

            // Center thin line
            paint.alpha = 200
            paint.strokeWidth = 1f
            canvas.drawLine(0f, y, w.toFloat(), y, paint)
            y += gridSpacing
        }

        // Draw vertical grid lines
        var x = 0f
        while (x < w) {
            paint.color = t
            paint.alpha = 90
            paint.strokeWidth = 3f
            canvas.drawLine(x, 0f, x, h.toFloat(), paint)

            paint.alpha = 200
            paint.strokeWidth = 1f
            canvas.drawLine(x, 0f, x, h.toFloat(), paint)
            x += gridSpacing
        }
    }

    private fun drawStarryGalaxy(canvas: Canvas, paint: Paint, w: Int, h: Int, p: Int, s: Int, t: Int) {
        val bgShader = RadialGradient(w * 0.5f, h * 0.5f, w * 0.9f, s, p, Shader.TileMode.CLAMP)
        paint.shader = bgShader
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        // Nebula
        val nebShader = RadialGradient(w * 0.3f, h * 0.3f, w * 0.5f, (t and 0x00FFFFFF) or 0x4D000000, 0x00FFFFFF, Shader.TileMode.CLAMP)
        paint.shader = nebShader
        canvas.drawCircle(w * 0.3f, h * 0.3f, w * 0.5f, paint)
        paint.shader = null

        // Stars
        paint.color = android.graphics.Color.WHITE
        for (i in 0..30) {
            val starX = (sin(i.toDouble() * 123.45) * 0.5 + 0.5) * w
            val starY = (cos(i.toDouble() * 987.65) * 0.5 + 0.5) * h
            val starRadius = (sin(i.toDouble()) * 1.5 + 2.0).toFloat()
            paint.alpha = ((cos(i.toDouble() * 2.0) * 0.3 + 0.7) * 255).toInt()

            canvas.drawCircle(starX.toFloat(), starY.toFloat(), starRadius, paint)
        }
        paint.alpha = 255
    }

    private fun drawGeometricMatrix(canvas: Canvas, paint: Paint, w: Int, h: Int, p: Int, s: Int, t: Int) {
        canvas.drawColor(p)

        // Circle 1
        paint.color = s
        paint.alpha = 64
        canvas.drawCircle(w * 0.15f, h * 0.2f, w * 0.4f, paint)

        // Circle 2
        paint.color = t
        paint.alpha = 51
        canvas.drawCircle(w * 0.9f, h * 0.85f, w * 0.5f, paint)

        // Diamond
        paint.color = s
        paint.alpha = 38
        val path = Path().apply {
            val squareSize = w * 0.3f
            moveTo(w * 0.7f, h * 0.25f)
            lineTo(w * 0.7f + squareSize, h * 0.25f + 50f)
            lineTo(w * 0.7f + squareSize - 50f, h * 0.25f + squareSize + 50f)
            lineTo(w * 0.7f - 50f, h * 0.25f + squareSize)
            close()
        }
        canvas.drawPath(path, paint)

        // Diagonal Line
        paint.color = t
        paint.alpha = 76
        paint.strokeWidth = 24f
        canvas.drawLine(0f, h * 0.75f, w.toFloat(), h * 0.55f, paint)
        paint.strokeWidth = 1f
        paint.alpha = 255
    }

    private fun drawOrganicLiquid(canvas: Canvas, paint: Paint, w: Int, h: Int, p: Int, s: Int, t: Int) {
        canvas.drawColor(p)

        // Blob 1
        paint.color = s
        paint.alpha = 90
        val blob1 = Path().apply {
            moveTo(0f, h * 0.2f)
            cubicTo(w * 0.4f, h * 0.1f, w * 0.5f, h * 0.4f, w * 0.3f, h * 0.5f)
            cubicTo(w * 0.1f, h * 0.6f, w * 0.2f, h * 0.8f, 0f, h * 0.75f)
            close()
        }
        canvas.drawPath(blob1, paint)

        // Blob 2
        paint.color = t
        paint.alpha = 76
        val blob2 = Path().apply {
            moveTo(w.toFloat(), h * 0.8f)
            cubicTo(w * 0.6f, h * 0.9f, w * 0.5f, h * 0.6f, w * 0.7f, h * 0.5f)
            cubicTo(w * 0.9f, h * 0.4f, w * 0.8f, h * 0.2f, w.toFloat(), h * 0.3f)
            close()
        }
        canvas.drawPath(blob2, paint)
        paint.alpha = 255
    }

    private fun drawRetroSynthWave(canvas: Canvas, paint: Paint, w: Int, h: Int, p: Int, s: Int, t: Int) {
        canvas.drawColor(p)

        val sunCenterY = h * 0.45f
        val sunRadius = w * 0.35f
        val sunCenterX = w * 0.5f

        // Sun Glow
        val glowShader = RadialGradient(sunCenterX, sunCenterY, sunRadius * 1.5f, (s and 0x00FFFFFF) or 0x66000000, 0x00FFFFFF, Shader.TileMode.CLAMP)
        paint.shader = glowShader
        canvas.drawCircle(sunCenterX, sunCenterY, sunRadius * 1.5f, paint)

        // Sun
        val sunShader = LinearGradient(0f, sunCenterY - sunRadius, 0f, sunCenterY + sunRadius, s, t, Shader.TileMode.CLAMP)
        paint.shader = sunShader
        canvas.drawCircle(sunCenterX, sunCenterY, sunRadius, paint)
        paint.shader = null

        // Grid Lines
        val perspectiveStartY = h * 0.55f
        val perspectiveEndY = h
        val linesCount = 10

        paint.color = t
        for (i in 0..linesCount) {
            val progress = i.toFloat() / linesCount
            val currentY = perspectiveStartY + (progress * progress) * (perspectiveEndY - perspectiveStartY)
            paint.alpha = (progress * 153).toInt()
            paint.strokeWidth = 2f
            canvas.drawLine(0f, currentY, w.toFloat(), currentY, paint)
        }

        // Radiating Lines
        val vanishingPointX = w * 0.5f
        val vanishingPointY = perspectiveStartY - 100f
        val columnsCount = 12

        paint.alpha = 115
        for (c in 0..columnsCount) {
            val bottomPortion = c.toFloat() / columnsCount
            val edgeX = bottomPortion * w
            canvas.drawLine(vanishingPointX, vanishingPointY, edgeX, perspectiveEndY.toFloat(), paint)
        }
        paint.strokeWidth = 1f
        paint.alpha = 255
    }

    // Set high-fidelity wallpaper to lock screen or home screen
    fun applyToDevice(context: Context, wallpaper: WallpaperEntity, screenType: Int) {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            // Retrieve actual screen dimensions safely, defaulting to standard proportions if unavailable
            val width = if (wallpaperManager.desiredMinimumWidth > 0) wallpaperManager.desiredMinimumWidth else 1080
            val height = if (wallpaperManager.desiredMinimumHeight > 0) wallpaperManager.desiredMinimumHeight else 2400

            val bitmap = generateWallpaperBitmap(wallpaper, width, height)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                val flag = when (screenType) {
                    0 -> WallpaperManager.FLAG_SYSTEM
                    1 -> WallpaperManager.FLAG_LOCK
                    else -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                }
                wallpaperManager.setBitmap(bitmap, null, true, flag)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }

            Toast.makeText(context, "Applied Wallpaper Successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("WallpaperBitmapGenerator", "Error setting device wallpaper", e)
            Toast.makeText(context, "Error setting wallpaper: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
