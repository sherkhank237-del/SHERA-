package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.data.model.FontEntity

object FontStyleMapper {

    fun getComposeStyle(font: FontEntity, fontSize: TextUnit = 20.sp, customColor: Color? = null): TextStyle {
        val fontFamily = when (font.baseStyle.lowercase()) {
            "serif" -> FontFamily.Serif
            "monospace" -> FontFamily.Monospace
            "cursive" -> FontFamily.Cursive
            "sans", "sans-serif" -> FontFamily.SansSerif
            "decorative" -> FontFamily.Default
            else -> FontFamily.Default
        }

        val weight = FontWeight(font.fontWeight)
        val style = if (font.isItalic) FontStyle.Italic else FontStyle.Normal
        val spacing = font.letterSpacing.em

        val shadow = if (font.shadowRadius > 0f) {
            Shadow(
                color = Color(font.shadowColor),
                offset = Offset(3f, 3f),
                blurRadius = font.shadowRadius
            )
        } else {
            null
        }

        return TextStyle(
            fontFamily = fontFamily,
            fontWeight = weight,
            fontStyle = style,
            letterSpacing = spacing,
            shadow = shadow,
            fontSize = fontSize,
            color = customColor ?: Color.Unspecified
        )
    }

    // Help generate short CSS representation for developers to inspect the font styling!
    fun getCssCode(font: FontEntity): String {
        val familyName = when (font.baseStyle.lowercase()) {
            "serif" -> "serif, Times New Roman"
            "monospace" -> "monospace, Courier New"
            "cursive" -> "cursive, Brush Script MT"
            "decorative" -> "fantasy, Impact"
            else -> "sans-serif, Arial"
        }
        val weight = font.fontWeight
        val italic = if (font.isItalic) "italic" else "normal"
        val spacing = "${font.letterSpacing}em"
        val shadowStr = if (font.shadowRadius > 0f) {
            val colorHex = "#" + (font.shadowColor and 0xFFFFFFL).toString(16).padStart(6, '0')
            "text-shadow: 2px 2px ${font.shadowRadius.toInt()}px $colorHex;"
        } else {
            "text-shadow: none;"
        }

        return """
            /* CSS Styling for font ${font.name} */
            .font-sample {
              font-family: ${familyName};
              font-weight: ${weight};
              font-style: ${italic};
              letter-spacing: ${spacing};
              ${shadowStr}
            }
        """.trimIndent()
    }
}
