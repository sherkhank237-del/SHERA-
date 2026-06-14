package com.example.data.dao

import androidx.room.*
import com.example.data.model.FontEntity
import com.example.data.model.WallpaperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperFontDao {

    // --- Wallpaper Operations ---
    @Query("SELECT * FROM wallpapers ORDER BY id ASC")
    fun getAllWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE id = :id")
    suspend fun getWallpaperById(id: Int): WallpaperEntity?

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1 ORDER BY id ASC")
    fun getFavoriteWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isApplied = 1 LIMIT 1")
    fun getAppliedWallpaper(): Flow<WallpaperEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpapers(wallpapers: List<WallpaperEntity>)

    @Update
    suspend fun updateWallpaper(wallpaper: WallpaperEntity)

    @Query("UPDATE wallpapers SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateWallpaperFavorite(id: Int, isFavorite: Boolean)

    @Query("UPDATE wallpapers SET downloadCount = downloadCount + 1 WHERE id = :id")
    suspend fun incrementWallpaperDownloads(id: Int)

    @Transaction
    suspend fun applyWallpaper(id: Int) {
        clearAppliedWallpaper()
        setWallpaperApplied(id)
    }

    @Query("UPDATE wallpapers SET isApplied = 0")
    suspend fun clearAppliedWallpaper()

    @Query("UPDATE wallpapers SET isApplied = 1 WHERE id = :id")
    suspend fun setWallpaperApplied(id: Int)


    // --- Font Operations ---
    @Query("SELECT * FROM fonts ORDER BY id ASC")
    fun getAllFonts(): Flow<List<FontEntity>>

    @Query("SELECT * FROM fonts WHERE id = :id")
    suspend fun getFontById(id: Int): FontEntity?

    @Query("SELECT * FROM fonts WHERE isFavorite = 1 ORDER BY id ASC")
    fun getFavoriteFonts(): Flow<List<FontEntity>>

    @Query("SELECT * FROM fonts WHERE isApplied = 1 LIMIT 1")
    fun getAppliedFont(): Flow<FontEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFonts(fonts: List<FontEntity>)

    @Update
    suspend fun updateFont(font: FontEntity)

    @Query("UPDATE fonts SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFontFavorite(id: Int, isFavorite: Boolean)

    @Transaction
    suspend fun applyFont(id: Int) {
        clearAppliedFont()
        setFontApplied(id)
    }

    @Query("UPDATE fonts SET isApplied = 0")
    suspend fun clearAppliedFont()

    @Query("UPDATE fonts SET isApplied = 1 WHERE id = :id")
    suspend fun setFontApplied(id: Int)
}
