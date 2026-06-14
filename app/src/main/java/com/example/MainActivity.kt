package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.model.FontEntity
import com.example.data.model.WallpaperEntity
import com.example.data.repository.WallpaperFontRepository
import com.example.ui.components.FontStyleMapper
import com.example.ui.components.WallpaperBitmapGenerator
import com.example.ui.components.WallpaperRenderer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WallpaperFontViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core data structures
        val database = AppDatabase.getDatabase(this)
        val repository = WallpaperFontRepository(database.wallpaperFontDao())

        setContent {
            val viewModel: WallpaperFontViewModel = viewModel(
                factory = WallpaperFontViewModel.Factory(repository)
            )

            val appliedFont by viewModel.appliedFont.collectAsState()

            MyApplicationTheme(appliedFont = appliedFont) {
                // Main Container handling Window Insets correctly
                Scaffold(
                    modifier = Modifier.fillMaxSize().testTag("main_scaffold"),
                    bottomBar = {
                        val currentTab by viewModel.selectedMainTab.collectAsState()
                        NavigationBar(
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { viewModel.selectMainTab(0) },
                                icon = { Icon(Icons.Default.List, contentDescription = "Wallpapers") },
                                label = { Text("Wallpapers") },
                                modifier = Modifier.testTag("nav_tab_wallpapers")
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { viewModel.selectMainTab(1) },
                                icon = { Icon(Icons.Default.Create, contentDescription = "Fonts") },
                                label = { Text("Fonts") },
                                modifier = Modifier.testTag("nav_tab_fonts")
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { viewModel.selectMainTab(2) },
                                icon = { Icon(Icons.Default.Star, contentDescription = "My Studio") },
                                label = { Text("My Studio") },
                                modifier = Modifier.testTag("nav_tab_studio")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val currentTab by viewModel.selectedMainTab.collectAsState()

                        // Main tab displays inside animated transition blocks
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "TabContent"
                        ) { tabIndex ->
                            when (tabIndex) {
                                0 -> WallpaperTabScreen(viewModel)
                                1 -> FontTabScreen(viewModel)
                                else -> AppliedStudioTabScreen(viewModel)
                            }
                        }

                        // Fullscreen Detail Screens placed on top when active
                        val activeWallpaperDetail by viewModel.selectedWallpaperForDetail.collectAsState()
                        if (activeWallpaperDetail != null) {
                            WallpaperDetailOverlay(
                                wallpaper = activeWallpaperDetail!!,
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        val activeFontDetail by viewModel.selectedFontForDetail.collectAsState()
                        if (activeFontDetail != null) {
                            FontDetailOverlay(
                                font = activeFontDetail!!,
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// REUSABLE PALETTE PICKER COLORS
// ---------------------------------------------------------
val DESIGNER_COLORS = listOf(
    0xFF111111L to "Midnight",
    0xFF0D1B2AL to "Abyss",
    0xFFD2143AL to "Crimson",
    0xFFFF1493L to "Cyber Pink",
    0xFF00FFFFL to "Electric Cyan",
    0xFF39FF14L to "Lime Glow",
    0xFFFF8C00L to "Lava Glow",
    0xFF8A2BE2L to "Violet",
    0xFF4A5D6EL to "Slate Blue",
    0xFFFFB6C1L to "Pastel Pink",
    0xFF9FE2BFL to "Seafoam Green",
    0xFFFFD700L to "Royal Gold"
)

// ---------------------------------------------------------
// TAB SCREEN: WALLPAPERS (1000 ITEMS)
// ---------------------------------------------------------
@Composable
fun WallpaperTabScreen(viewModel: WallpaperFontViewModel) {
    val wallpapers by viewModel.filteredWallpapers.collectAsState()
    val searchQuery by viewModel.wallpaperSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedWallpaperCategory.collectAsState()

    val categories = listOf("All", "Abstract", "Gradient", "Minimalist", "Geometric", "Vaporwave", "Scenery", "Liquid Art")

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Wallpaper Studio",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "Explore 1000 high-fidelity customizable wallpapers",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateWallpaperSearch(it) },
                placeholder = { Text("Search 1000 patterns, names or #IDs...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateWallpaperSearch("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("wallpaper_search_input"),
                singleLine = true
            )
        }

        // Category Ribbon
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectWallpaperCategory(category) },
                    label = { Text(category) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Elegant Dark Statistics Overlay
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFD0BCFF),
                contentColor = Color(0xFF381E72)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("elegant_dark_stats_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFEADDFF), shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Library Icon",
                            tint = Color(0xFF381E72),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "LIBRARY STATUS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color(0xFF381E72).copy(alpha = 0.8f)
                        )
                        Text(
                            text = "1,024 Assets Ready",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(Color(0xFF381E72).copy(alpha = 0.2f))
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "FREE SPACE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF381E72).copy(alpha = 0.8f)
                    )
                    Text(
                        text = "12.4 GB",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }

        // 1000 Item Virtualized Grid
        if (wallpapers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "No items Found",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No Wallpapers Found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Try custom seed IDs like #240 or palette names",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("wallpaper_grid")
            ) {
                items(wallpapers, key = { it.id }) { wp ->
                    WallpaperCard(
                        wallpaper = wp,
                        onCardClick = { viewModel.selectWallpaperForDetail(wp) },
                        onFavoriteToggle = { viewModel.toggleWallpaperFavorite(wp) }
                    )
                }
            }
        }
    }
}

@Composable
fun WallpaperCard(
    wallpaper: WallpaperEntity,
    onCardClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clickable { onCardClick() }
            .testTag("wallpaper_card_${wallpaper.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Live procedurally rendered Vector preview
            WallpaperRenderer(
                wallpaper = wallpaper,
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic card details layer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                            startY = 0f
                        )
                    )
                    .padding(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "#${wallpaper.id.toString().padStart(3, '0')}",
                            color = Color(wallpaper.tertiaryColor).copy(alpha = 0.95f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = wallpaper.name.substringBefore(" #"),
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Tappable Favorite target size optimized
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { onFavoriteToggle() }
                            .testTag("fav_btn_${wallpaper.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (wallpaper.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite Toggle",
                            tint = if (wallpaper.isFavorite) Color.Red else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Category badge
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = wallpaper.category,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------
// TAB SCREEN: FONTS (1000 ITEMS)
// ---------------------------------------------------------
@Composable
fun FontTabScreen(viewModel: WallpaperFontViewModel) {
    val fonts by viewModel.filteredFonts.collectAsState()
    val searchQuery by viewModel.fontSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedFontCategory.collectAsState()

    val categories = listOf("All", "Sans-Serif", "Serif", "Monospace", "Handwriting", "Retro Display", "Brutalist")

    Column(modifier = Modifier.fillMaxSize()) {
        // Font Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Mobile Fonts Center",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "Apply 1000 premium distinct typographic combinations",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateFontSearch(it) },
                placeholder = { Text("Search 1000 fonts, names or #IDs...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateFontSearch("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("font_search_input"),
                singleLine = true
            )
        }

        // Category ribbon
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectFontCategory(category) },
                    label = { Text(category) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Font Virtualized List
        if (fonts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "No items Found",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No Fonts Found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Try integer IDs like #520 or Font prefix combos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("font_list")
            ) {
                items(fonts, key = { it.id }) { font ->
                    FontItemCard(
                        font = font,
                        onCardClick = { viewModel.selectFontForDetail(font) },
                        onFavoriteToggle = { viewModel.toggleFontFavorite(font) },
                        onApplyClick = { viewModel.applyFont(font) }
                    )
                }
            }
        }
    }
}

@Composable
fun FontItemCard(
    font: FontEntity,
    onCardClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onApplyClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("font_card_${font.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "#${font.id.toString().padStart(3, '0')}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = font.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (font.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite Font",
                            tint = if (font.isFavorite) Color.Red else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Real visual live typography sample of this font's styling parameters
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)) {
                    Text(
                        text = "Pack my box with five dozen liquor jugs.",
                        style = FontStyleMapper.getComposeStyle(
                            font = font,
                            fontSize = 18.sp,
                            customColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${font.category} / Weight: ${font.fontWeight}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onApplyClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = if (font.isApplied) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer)
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("apply_font_btn_${font.id}")
                ) {
                    if (font.isApplied) {
                        Icon(Icons.Default.Check, contentDescription = "Applied", modifier = Modifier.size(12.dp).padding(end = 4.dp))
                        Text("Active", style = MaterialTheme.typography.labelSmall)
                    } else {
                        Text("Apply", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// TAB SCREEN: MY APPLIED STUDIO (FAVORITES & ACTIVE PREVIEWS)
// ---------------------------------------------------------
@Composable
fun AppliedStudioTabScreen(viewModel: WallpaperFontViewModel) {
    val appliedWp by viewModel.appliedWallpaper.collectAsState()
    val appliedFn by viewModel.appliedFont.collectAsState()

    val favWps by viewModel.favoriteWallpapers.collectAsState()
    val favFns by viewModel.favoriteFonts.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize().testTag("studio_tab_column")
    ) {
        // Welcoming
        item {
            Text(
                text = "My Design Studio",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Active styles and personal quick selections",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Row showing Applied Status Preview
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Active Layout Integration Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Applied Wallpaper Mockup Device Panel
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    val wp = appliedWp
                                    if (wp != null) {
                                        viewModel.selectWallpaperForDetail(wp)
                                    }
                                }
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                val wp = appliedWp
                                if (wp != null) {
                                    WallpaperRenderer(wallpaper = wp, modifier = Modifier.fillMaxSize())
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            wp.name.substringBefore(" #"),
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(Color.Gray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No Wallpaper Applied", color = Color.White, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }

                        // Applied Font Mockup Device Panel
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    if (appliedFn != null) {
                                        viewModel.selectFontForDetail(appliedFn)
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "System Font",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (appliedFn != null) {
                                        Text(
                                            text = "Abc",
                                            style = FontStyleMapper.getComposeStyle(
                                                font = appliedFn!!,
                                                fontSize = 44.sp,
                                                customColor = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    } else {
                                        Text("None")
                                    }
                                }

                                Text(
                                    text = appliedFn?.name ?: "Default System",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Favorites: Wallpapers (Horizontal scroll)
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Wallpaper Favorites (${favWps.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (favWps.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Tap the heart icons while browsing to save style choices.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(favWps, key = { it.id }) { wp ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .width(130.dp)
                                .height(180.dp)
                                .clickable { viewModel.selectWallpaperForDetail(wp) }
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                WallpaperRenderer(wallpaper = wp, modifier = Modifier.fillMaxSize())
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .background(Color.Black.copy(alpha = 0.61f))
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        wp.name.substringBefore(" #"),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Favorites: Fonts
        item {
            Text(
                "Font Favorites (${favFns.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (favFns.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Pin typography choices to favorites to access them instantly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                for (fn in favFns) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable { viewModel.selectFontForDetail(fn) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(fn.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(fn.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = "Open Detail",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// DETAILED OVERLAY: WALLPAPER CUSTOMIZER & DIRECT MANAGER
// ---------------------------------------------------------
@Composable
fun WallpaperDetailOverlay(
    wallpaper: WallpaperEntity,
    viewModel: WallpaperFontViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val customPrimaryColor by viewModel.customPrimaryColor.collectAsState()
    val customSecondaryColor by viewModel.customSecondaryColor.collectAsState()
    val customTertiaryColor by viewModel.customTertiaryColor.collectAsState()

    // Determine currently customized wallpaper colors
    val finalPrimary = if (customPrimaryColor != null) Color(customPrimaryColor!!) else Color(wallpaper.primaryColor)
    val finalSecondary = if (customSecondaryColor != null) Color(customSecondaryColor!!) else Color(wallpaper.secondaryColor)
    val finalTertiary = if (customTertiaryColor != null) Color(customTertiaryColor!!) else Color(wallpaper.tertiaryColor)

    // Handle back intercepts gracefully
    BackHandler {
        viewModel.selectWallpaperForDetail(null)
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("wallpaper_detail_panel")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Action ribbon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectWallpaperForDetail(null) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Text(
                    text = "Wallpaper Studio Editor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { viewModel.toggleWallpaperFavorite(wallpaper) }) {
                    Icon(
                        imageVector = if (wallpaper.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (wallpaper.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Preview viewport with phone-aspect ratio simulation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .testTag("desktop_simulate_view")
            ) {
                // Procedural live drawing featuring tailored sliders colors!
                WallpaperRenderer(
                    wallpaper = wallpaper,
                    modifier = Modifier.fillMaxSize(),
                    customPrimaryColor = finalPrimary,
                    customSecondaryColor = finalSecondary,
                    customTertiaryColor = finalTertiary
                )

                // Simulative interface bars overlaid on canvas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("12:42", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                        Text("Saturday, June 13", color = Color.White.copy(alpha = 0.82f), fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Phone Mock", tint = Color.White, modifier = Modifier.size(24.dp))
                        Icon(Icons.Default.Email, contentDescription = "Message Mock", tint = Color.White, modifier = Modifier.size(24.dp))
                        Icon(Icons.Default.Settings, contentDescription = "Camera Mock", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }

            // Editor Controls Pane
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Column {
                        Text(
                            text = wallpaper.name.substringBefore(" #"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = wallpaper.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Pattern: ${wallpaper.id.toString().padStart(3, '0')}/1000",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Color customization sections
                item {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Text("Color Presets Tuning", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { viewModel.resetCustomColors() }) {
                                Text("Reset Palette")
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Primary selector
                            ColorPickerRow(
                                label = "Base Shade",
                                currentColor = finalPrimary,
                                onColorSelected = { viewModel.customizePrimaryColor(it) }
                            )

                            // Secondary selector
                            ColorPickerRow(
                                label = "Accent Tone 1",
                                currentColor = finalSecondary,
                                onColorSelected = { viewModel.customizeSecondaryColor(it) }
                            )

                            // Tertiary selector
                            ColorPickerRow(
                                label = "Accent Tone 2",
                                currentColor = finalTertiary,
                                onColorSelected = { viewModel.customizeTertiaryColor(it) }
                            )
                        }
                    }
                }

                // Control action buttons
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Install & Set Layout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Button(
                            onClick = {
                                // Set procedured bitmap directly as lock screen
                                val finalWp = wallpaper.copy(
                                    primaryColor = customPrimaryColor ?: wallpaper.primaryColor,
                                    secondaryColor = customSecondaryColor ?: wallpaper.secondaryColor,
                                    tertiaryColor = customTertiaryColor ?: wallpaper.tertiaryColor
                                )
                                viewModel.applyWallpaper(finalWp)
                                WallpaperBitmapGenerator.applyToDevice(context, finalWp, 0) // HomeScreen
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("wp_set_home_btn")
                        ) {
                            Icon(Icons.Default.List, contentDescription = "Home Device")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Set on System Home Screen")
                        }

                        Button(
                            onClick = {
                                val finalWp = wallpaper.copy(
                                    primaryColor = customPrimaryColor ?: wallpaper.primaryColor,
                                    secondaryColor = customSecondaryColor ?: wallpaper.secondaryColor,
                                    tertiaryColor = customTertiaryColor ?: wallpaper.tertiaryColor
                                )
                                viewModel.applyWallpaper(finalWp)
                                WallpaperBitmapGenerator.applyToDevice(context, finalWp, 1) // LockScreen
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("wp_set_lock_btn")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock Device")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Set on Lock Screen")
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    val finalWp = wallpaper.copy(
                                        primaryColor = customPrimaryColor ?: wallpaper.primaryColor,
                                        secondaryColor = customSecondaryColor ?: wallpaper.secondaryColor,
                                        tertiaryColor = customTertiaryColor ?: wallpaper.tertiaryColor
                                    )
                                    viewModel.applyWallpaper(finalWp)
                                    WallpaperBitmapGenerator.applyToDevice(context, finalWp, 2) // Both
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                modifier = Modifier.weight(1.3f).height(48.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Both Devices")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Apply to Both", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Vector Design exported to Gallery!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Mock Download")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPickerRow(
    label: String,
    currentColor: Color,
    onColorSelected: (Long) -> Unit
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(DESIGNER_COLORS) { (colorVal, colorName) ->
                val col = Color(colorVal)
                val isSelected = (col.red == currentColor.red && col.green == currentColor.green && col.blue == currentColor.blue)

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(col)
                        .clickable { onColorSelected(colorVal) }
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (col.red + col.green + col.blue > 1.8f) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// DETAILED OVERLAY: TYPOGRAPHIC PLAYGROUND SANDBOX
// ---------------------------------------------------------
@Composable
fun FontDetailOverlay(
    font: FontEntity,
    viewModel: WallpaperFontViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sandboxText by viewModel.fontSandboxText.collectAsState()
    val sandboxSize by viewModel.fontSandboxSize.collectAsState()

    BackHandler {
        viewModel.selectFontForDetail(null)
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("font_detail_panel")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Action ribbon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectFontForDetail(null) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Text(
                    text = "Typography Sandbox",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { viewModel.toggleFontFavorite(font) }) {
                    Icon(
                        imageVector = if (font.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (font.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Main typography specs block
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                font.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Style Catalog Index: #${font.id} / 1000 Available Options",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                KeyValueItem("Category", font.category)
                                KeyValueItem("Font Weight", font.fontWeight.toString())
                                KeyValueItem("Italics Support", if (font.isItalic) "Yes" else "No")
                            }
                        }
                    }
                }

                // Sandbox Playground Panel
                item {
                    Column {
                        Text(
                            "Live Interactive Sandbox",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                OutlinedTextField(
                                    value = sandboxText,
                                    onValueChange = { viewModel.updateFontSandboxText(it) },
                                    label = { Text("Sandbox Input Tester") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("sandbox_text_box")
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Sizing Slider
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = "Font Size", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Size: ${sandboxSize.toInt()}sp", style = MaterialTheme.typography.labelMedium)
                                    Slider(
                                        value = sandboxSize,
                                        onValueChange = { viewModel.updateFontSandboxSize(it) },
                                        valueRange = 12f..72f,
                                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp).testTag("sandbox_slider")
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // The dynamic interactive sandboxed preview block
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .padding(14.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sandboxText,
                                        style = FontStyleMapper.getComposeStyle(
                                            font = font,
                                            fontSize = sandboxSize.sp,
                                            customColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier.testTag("sandbox_preview_text")
                                    )
                                }
                            }
                        }
                    }
                }

                // CSS Inspector Panel
                item {
                    Column {
                        Text(
                            "CSS & XML Attributes Inspector",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "Web Style Codes",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = {
                                            Toast.makeText(context, "Copied Design attributes to clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Copy code", modifier = Modifier.size(14.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = FontStyleMapper.getCssCode(font),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Apply button
                item {
                    Button(
                        onClick = {
                            viewModel.applyFont(font)
                            Toast.makeText(context, "Applied dynamic Font theme to this app!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("wp_set_font_btn")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Apply globally")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (font.isApplied) "Active System App Theme Font" else "Apply Dynamic Font Theme Globally"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KeyValueItem(key: String, value: String) {
    Column {
        Text(text = key, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}
