package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fonts")
data class FontEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val category: String, // "Sans-Serif", "Serif", "Monospace", "Handwriting", "Retro Display", "Brutalist"
    val baseStyle: String, // "Sans", "Serif", "Monospace", "Cursive", "Decorative"
    val fontWeight: Int,   // 100 to 900 (Thin to Black)
    val letterSpacing: Float, // -0.05f to 0.4f em
    val isItalic: Boolean,
    val shadowRadius: Float = 0f, // 0 to 8f
    val shadowColor: Long = 0x40000000L, // default 25% black
    val isFavorite: Boolean = false,
    val isApplied: Boolean = false
)
