package com.example.data.database

import com.example.data.model.FontEntity
import com.example.data.model.WallpaperEntity
import kotlin.random.Random

object DatabasePrepopulator {

    // 20 High-fidelity Color Schemes for Wallpapers
    private val PALETTES = listOf(
        Palette("Synthwave", 0xFF2B0938, 0xFFFF2A74, 0xFF05D9E8),
        Palette("Oceanic Calm", 0xFF0D1B2A, 0xFF1B263B, 0xFF415A77),
        Palette("Pastel Dream", 0xFFFFE5EC, 0xFFFFC2D1, 0xFFD8B4F8),
        Palette("Desert Sand", 0xFFE29578, 0xFFF5CAC3, 0xFF83C5BE),
        Palette("Cyberpunk Edge", 0xFF0E0B16, 0xFFE7326B, 0xFF4F5D75),
        Palette("Deep Forest", 0xFF143642, 0xFF0F8B8D, 0xFFEC9A29),
        Palette("Crimson Flame", 0xFF1A090D, 0xFFD62246, 0xFFF4A261),
        Palette("Midnight Slate", 0xFF111111, 0xFF333333, 0xFF666666),
        Palette("Rose Gold", 0xFF2B1B17, 0xFFB38B6D, 0xFFE8C39E),
        Palette("Acid Lime", 0xFF1B261C, 0xFF2ECC71, 0xFFF1C40F),
        Palette("Holographic", 0xFF9B5DE5, 0xFFF15BB5, 0xFF00BBF9),
        Palette("Chalkboard", 0xFF1C1D1F, 0xFF233D4D, 0xFFFE7F2D),
        Palette("Sakura Mist", 0xFF4A1E29, 0xFFD81B60, 0xFFFBC02D),
        Palette("Abyssal Blue", 0xFF03001E, 0xFF7303C0, 0xFFEC38BC),
        Palette("Breeze Mint", 0xFF003B46, 0xFF07575B, 0xFF66A5AD),
        Palette("Aurora Green", 0xFF051923, 0xFF005F73, 0xFF0A9396),
        Palette("Nordic Blue", 0xFF2E3440, 0xFF434C5E, 0xFF88C0D0),
        Palette("Lava Flow", 0xFF3D0C02, 0xFF800000, 0xFFFF4500),
        Palette("Neon Violet", 0xFF1F003C, 0xFF4D007F, 0xFFE100FF),
        Palette("Sunset Glow", 0xFFF77F00, 0xFFFCBF49, 0xFFEAE2B7)
    )

    private val WALLPAPER_CATEGORIES = listOf(
        "Abstract", "Gradient", "Minimalist", "Geometric", "Vaporwave", "Scenery", "Liquid Art"
    )

    private val FONT_PREFIXES = listOf(
        "Aether", "Alpha", "Apex", "Aura", "Avant", "Bodoni", "Canyon", "Cosmic", "Drift", "Flux",
        "Futura", "Garamond", "Gothic", "Horizon", "Insignia", "Lora", "Monolith", "Neo", "Neue",
        "Nova", "Oasis", "Orbit", "Pixel", "Quantum", "Radiant", "Roboto", "Spectra", "Vektor", "Zenith"
    )

    private val FONT_SUFFIXES = listOf(
        "Sans", "Serif", "Mono", "Script", "Display", "Slab", "Code", "Grotesque", "Deco",
        "Brush", "Writer", "Space", "Wave", "Glow", "Type", "Text", "Chalk", "Retro", "Brutal"
    )

    private val FONT_CATEGORIES = listOf(
        "Sans-Serif", "Serif", "Monospace", "Handwriting", "Retro Display", "Brutalist"
    )

    data class Palette(val name: String, val primary: Long, val secondary: Long, val tertiary: Long)

    // Generate 1000 wallpapers deterministically
    fun generateWallpapers(): List<WallpaperEntity> {
        val wallpapers = ArrayList<WallpaperEntity>(1000)
        // We use a fixed seed Random to guarantee deterministic values
        val random = Random(42)

        for (i in 1..1000) {
            val palette = PALETTES[random.nextInt(PALETTES.size)]
            val patternType = random.nextInt(7) // 0 to 6
            val category = WALLPAPER_CATEGORIES[random.nextInt(WALLPAPER_CATEGORIES.size)]

            val patternName = when (patternType) {
                0 -> "Smooth Gradient"
                1 -> "Aurora Waves"
                2 -> "Cyber Neon Grid"
                3 -> "Starry Galaxy"
                4 -> "Geometric Matrix"
                5 -> "Organic Liquid"
                else -> "Retro Synth Wave"
            }

            val name = "${palette.name} $patternName #$i"

            // Slightly perturb colors based on ID to make every single one unique
            val seedShift = i * 153L
            val pColor = (palette.primary and 0xFF000000L) or ((palette.primary + seedShift) and 0x00FFFFFFL) or 0xFF000000L
            val sColor = (palette.secondary and 0xFF000000L) or ((palette.secondary - seedShift) and 0x00FFFFFFL) or 0xFF000000L
            val tColor = (palette.tertiary and 0xFF000000L) or ((palette.tertiary + seedShift * 2) and 0x00FFFFFFL) or 0xFF000000L

            wallpapers.add(
                WallpaperEntity(
                    id = i,
                    name = name,
                    category = category,
                    primaryColor = pColor,
                    secondaryColor = sColor,
                    tertiaryColor = tColor,
                    patternType = patternType,
                    isFavorite = false,
                    isApplied = (i == 1), // App starts with #1 applied
                    downloadCount = random.nextInt(20, 1500)
                )
            )
        }
        return wallpapers
    }

    // Generate 1000 font configs deterministically
    fun generateFonts(): List<FontEntity> {
        val fonts = ArrayList<FontEntity>(1000)
        val random = Random(99)

        // Predefined shadow options
        val shadowColors = listOf(
            0x00000000L, // No shadow
            0x60000000L, // Soft dark shadow
            0x80FF0000L, // Neon Red outer glow
            0x8000FFFFL, // Cyber Cyan shadow
            0x90FF00FFL, // Pink glow
            0x7000FF00L, // Terminal Green glow
            0x50FFFFFFL  // Soft white overlay
        )

        for (i in 1..1000) {
            val prefix = FONT_PREFIXES[random.nextInt(FONT_PREFIXES.size)]
            val suffix = FONT_SUFFIXES[random.nextInt(FONT_SUFFIXES.size)]
            val category = FONT_CATEGORIES[random.nextInt(FONT_CATEGORIES.size)]

            val name = "$prefix $suffix #$i"

            val baseStyle = when (category) {
                "Sans-Serif" -> "Sans"
                "Serif" -> "Serif"
                "Monospace" -> "Monospace"
                "Handwriting" -> "Cursive"
                "Retro Display" -> "Decorative"
                else -> "Sans" // Brutalist uses bold sans combinations
            }

            val fontWeight = when (random.nextInt(5)) {
                0 -> 300 // Light
                1 -> 400 // Regular
                2 -> 600 // SemiBold
                3 -> 700 // Bold
                else -> 900 // Black (extremely heavy, good for brutalist)
            }

            val letterSpacing = when (random.nextInt(5)) {
                0 -> -0.02f
                1 -> 0.0f
                2 -> 0.05f
                3 -> 0.12f
                else -> 0.25f
            }

            val isItalic = (random.nextFloat() < 0.15f) // 15% italic chance

            val shadowChoice = random.nextInt(shadowColors.size)
            val shadowColor = shadowColors[shadowChoice]
            val shadowRadius = if (shadowChoice > 0) random.nextFloat() * 6f + 2f else 0f

            fonts.add(
                FontEntity(
                    id = i,
                    name = name,
                    category = category,
                    baseStyle = baseStyle,
                    fontWeight = fontWeight,
                    letterSpacing = letterSpacing,
                    isItalic = isItalic,
                    shadowRadius = shadowRadius,
                    shadowColor = shadowColor,
                    isFavorite = false,
                    isApplied = (i == 1) // App starts with font #1 applied
                )
            )
        }
        return fonts
    }
}
