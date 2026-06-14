package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpapers")
data class WallpaperEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val category: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val tertiaryColor: Long,
    val patternType: Int, // 0: Gradient, 1: Aurora Wave, 2: Neon Grid, 3: Starry Night, 4: Geometric Matrix, 5: Organic Liquid, 6: Retro Grid
    val isFavorite: Boolean = false,
    val isApplied: Boolean = false,
    val downloadCount: Int = 0
)
