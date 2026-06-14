package com.example.data.repository

import android.util.Log
import com.example.data.dao.WallpaperFontDao
import com.example.data.database.DatabasePrepopulator
import com.example.data.model.FontEntity
import com.example.data.model.WallpaperEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class WallpaperFontRepository(private val dao: WallpaperFontDao) {

    val allWallpapers: Flow<List<WallpaperEntity>> = dao.getAllWallpapers()
    val favoriteWallpapers: Flow<List<WallpaperEntity>> = dao.getFavoriteWallpapers()
    val appliedWallpaper: Flow<WallpaperEntity?> = dao.getAppliedWallpaper()

    val allFonts: Flow<List<FontEntity>> = dao.getAllFonts()
    val favoriteFonts: Flow<List<FontEntity>> = dao.getFavoriteFonts()
    val appliedFont: Flow<FontEntity?> = dao.getAppliedFont()

    // Ensuring the database is prepopulated with 1000 wallpapers & 1000 fonts on first launch
    suspend fun checkAndPrepopulate() {
        withContext(Dispatchers.IO) {
            try {
                // Read from wallpapers stream (using firstOrNull to check if empty non-blocking)
                val existingWallpapers = dao.getAllWallpapers().firstOrNull()
                if (existingWallpapers.isNullOrEmpty()) {
                    Log.i("WallpaperFontRepository", "Database empty. Generating 1000 wallpapers...")
                    val generatedWallpapers = DatabasePrepopulator.generateWallpapers()
                    dao.insertWallpapers(generatedWallpapers)
                    Log.i("WallpaperFontRepository", "Successfully inserted 1000 wallpapers.")
                }

                val existingFonts = dao.getAllFonts().firstOrNull()
                if (existingFonts.isNullOrEmpty()) {
                    Log.i("WallpaperFontRepository", "Database empty. Generating 1000 fonts...")
                    val generatedFonts = DatabasePrepopulator.generateFonts()
                    dao.insertFonts(generatedFonts)
                    Log.i("WallpaperFontRepository", "Successfully inserted 1000 fonts.")
                }
            } catch (e: Exception) {
                Log.e("WallpaperFontRepository", "Error during base database prepopulation", e)
            }
        }
    }

    suspend fun getWallpaperById(id: Int): WallpaperEntity? {
        return withContext(Dispatchers.IO) {
            dao.getWallpaperById(id)
        }
    }

    suspend fun toggleWallpaperFavorite(id: Int, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            dao.updateWallpaperFavorite(id, isFavorite)
        }
    }

    suspend fun incrementDownloads(id: Int) {
        withContext(Dispatchers.IO) {
            dao.incrementWallpaperDownloads(id)
        }
    }

    suspend fun applyWallpaper(id: Int) {
        withContext(Dispatchers.IO) {
            dao.applyWallpaper(id)
        }
    }

    suspend fun updateWallpaper(wallpaper: WallpaperEntity) {
        withContext(Dispatchers.IO) {
            dao.updateWallpaper(wallpaper)
        }
    }

    suspend fun getFontById(id: Int): FontEntity? {
        return withContext(Dispatchers.IO) {
            dao.getFontById(id)
        }
    }

    suspend fun toggleFontFavorite(id: Int, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            dao.updateFontFavorite(id, isFavorite)
        }
    }

    suspend fun applyFont(id: Int) {
        withContext(Dispatchers.IO) {
            dao.applyFont(id)
        }
    }
}
