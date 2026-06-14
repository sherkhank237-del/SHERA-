package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.FontEntity
import com.example.data.model.WallpaperEntity
import com.example.data.repository.WallpaperFontRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WallpaperFontViewModel(private val repository: WallpaperFontRepository) : ViewModel() {

    // Main layout tabs: 0 = Wallpapers, 1 = Fonts, 2 = Favorites/Applied
    private val _selectedMainTab = MutableStateFlow(0)
    val selectedMainTab: StateFlow<Int> = _selectedMainTab.asStateFlow()

    fun selectMainTab(tab: Int) {
        _selectedMainTab.value = tab
    }

    // --- Wallpaper Exploration States ---
    private val _wallpaperSearchQuery = MutableStateFlow("")
    val wallpaperSearchQuery: StateFlow<String> = _wallpaperSearchQuery.asStateFlow()

    private val _selectedWallpaperCategory = MutableStateFlow("All")
    val selectedWallpaperCategory: StateFlow<String> = _selectedWallpaperCategory.asStateFlow()

    fun updateWallpaperSearch(query: String) {
        _wallpaperSearchQuery.value = query
    }

    fun selectWallpaperCategory(category: String) {
        _selectedWallpaperCategory.value = category
    }

    // Combined Flow containing 1000 searchable and filterable wallpapers
    val filteredWallpapers: StateFlow<List<WallpaperEntity>> = combine(
        repository.allWallpapers,
        _wallpaperSearchQuery,
        _selectedWallpaperCategory
    ) { wallpapers, query, category ->
        var list = wallpapers

        // Match category
        if (category != "All") {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Match search query (ID or Name)
        if (query.isNotBlank()) {
            val q = query.trim()
            val queryAsId = q.removePrefix("#").toIntOrNull()
            list = list.filter {
                it.name.contains(q, ignoreCase = true) || 
                it.id == queryAsId ||
                it.category.contains(q, ignoreCase = true)
            }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- Font Exploration States ---
    private val _fontSearchQuery = MutableStateFlow("")
    val fontSearchQuery: StateFlow<String> = _fontSearchQuery.asStateFlow()

    private val _selectedFontCategory = MutableStateFlow("All")
    val selectedFontCategory: StateFlow<String> = _selectedFontCategory.asStateFlow()

    fun updateFontSearch(query: String) {
        _fontSearchQuery.value = query
    }

    fun selectFontCategory(category: String) {
        _selectedFontCategory.value = category
    }

    // Combined Flow containing 1000 searchable and filterable fonts
    val filteredFonts: StateFlow<List<FontEntity>> = combine(
        repository.allFonts,
        _fontSearchQuery,
        _selectedFontCategory
    ) { fonts, query, category ->
        var list = fonts

        // Match category
        if (category != "All") {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Match search query (ID or Name)
        if (query.isNotBlank()) {
            val q = query.trim()
            val queryAsId = q.removePrefix("#").toIntOrNull()
            list = list.filter {
                it.name.contains(q, ignoreCase = true) || 
                it.id == queryAsId ||
                it.category.contains(q, ignoreCase = true)
            }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- Favorites Streams ---
    val favoriteWallpapers: StateFlow<List<WallpaperEntity>> = repository.favoriteWallpapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteFonts: StateFlow<List<FontEntity>> = repository.favoriteFonts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- Currently Applied / Selected States ---
    val appliedWallpaper: StateFlow<WallpaperEntity?> = repository.appliedWallpaper
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appliedFont: StateFlow<FontEntity?> = repository.appliedFont
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    // --- Detail & Customization State ---
    private val _selectedWallpaperForDetail = MutableStateFlow<WallpaperEntity?>(null)
    val selectedWallpaperForDetail: StateFlow<WallpaperEntity?> = _selectedWallpaperForDetail.asStateFlow()

    private val _selectedFontForDetail = MutableStateFlow<FontEntity?>(null)
    val selectedFontForDetail: StateFlow<FontEntity?> = _selectedFontForDetail.asStateFlow()

    // Interactive custom color properties to live customize any wallpaper
    private val _customPrimaryColor = MutableStateFlow<Long?>(null)
    val customPrimaryColor: StateFlow<Long?> = _customPrimaryColor.asStateFlow()

    private val _customSecondaryColor = MutableStateFlow<Long?>(null)
    val customSecondaryColor: StateFlow<Long?> = _customSecondaryColor.asStateFlow()

    private val _customTertiaryColor = MutableStateFlow<Long?>(null)
    val customTertiaryColor: StateFlow<Long?> = _customTertiaryColor.asStateFlow()

    // Interactive Font Sandbox State
    private val _fontSandboxText = MutableStateFlow("Change lives with beautifully crafted typography designs.")
    val fontSandboxText: StateFlow<String> = _fontSandboxText.asStateFlow()

    private val _fontSandboxSize = MutableStateFlow(24f)
    val fontSandboxSize: StateFlow<Float> = _fontSandboxSize.asStateFlow()

    init {
        // Enforce DB Prepopulation immediately
        viewModelScope.launch {
            repository.checkAndPrepopulate()
        }
    }

    fun selectWallpaperForDetail(wallpaper: WallpaperEntity?) {
        _selectedWallpaperForDetail.value = wallpaper
        if (wallpaper != null) {
            _customPrimaryColor.value = wallpaper.primaryColor
            _customSecondaryColor.value = wallpaper.secondaryColor
            _customTertiaryColor.value = wallpaper.tertiaryColor
        } else {
            _customPrimaryColor.value = null
            _customSecondaryColor.value = null
            _customTertiaryColor.value = null
        }
    }

    fun selectFontForDetail(font: FontEntity?) {
        _selectedFontForDetail.value = font
    }

    // Color customization actions
    fun customizePrimaryColor(colorLong: Long) {
        _customPrimaryColor.value = colorLong
    }

    fun customizeSecondaryColor(colorLong: Long) {
        _customSecondaryColor.value = colorLong
    }

    fun customizeTertiaryColor(colorLong: Long) {
        _customTertiaryColor.value = colorLong
    }

    fun resetCustomColors() {
        val wp = _selectedWallpaperForDetail.value
        if (wp != null) {
            _customPrimaryColor.value = wp.primaryColor
            _customSecondaryColor.value = wp.secondaryColor
            _customTertiaryColor.value = wp.tertiaryColor
        }
    }

    // Font sandbox controls
    fun updateFontSandboxText(text: String) {
        _fontSandboxText.value = text
    }

    fun updateFontSandboxSize(size: Float) {
        _fontSandboxSize.value = size
    }


    // --- Interactive Operations (Direct Database DB Updates via Flow Repository) ---
    fun toggleWallpaperFavorite(wallpaper: WallpaperEntity) {
        viewModelScope.launch {
            repository.toggleWallpaperFavorite(wallpaper.id, !wallpaper.isFavorite)
        }
    }

    fun toggleFontFavorite(font: FontEntity) {
        viewModelScope.launch {
            repository.toggleFontFavorite(font.id, !font.isFavorite)
        }
    }

    fun applyWallpaper(wallpaper: WallpaperEntity) {
        viewModelScope.launch {
            // Check if there are customized colors, write them into a customized wallpaper state update!
            val updatedWp = wallpaper.copy(
                primaryColor = _customPrimaryColor.value ?: wallpaper.primaryColor,
                secondaryColor = _customSecondaryColor.value ?: wallpaper.secondaryColor,
                tertiaryColor = _customTertiaryColor.value ?: wallpaper.tertiaryColor
            )
            repository.updateWallpaper(updatedWp)
            repository.applyWallpaper(updatedWp.id)
            repository.incrementDownloads(updatedWp.id)
            // Keep the detail selection synced
            _selectedWallpaperForDetail.value = updatedWp
        }
    }

    fun applyFont(font: FontEntity) {
        viewModelScope.launch {
            repository.applyFont(font.id)
            _selectedFontForDetail.value = font
        }
    }


    // --- Simple Factory Class ---
    class Factory(private val repository: WallpaperFontRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WallpaperFontViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WallpaperFontViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
