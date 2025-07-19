package com.example.sangeet.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.sangeet.navigation.Screen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sangeet.model.MusicModel

@Composable
fun RecentlyPlayedSection(
    navController: NavController,
    musics: List<MusicModel>,
    isLoading: Boolean,
    userId: String,
    favoriteMusics: List<MusicModel>,
    onToggleFavorite: (String) -> Unit,
    onAddToPlaylist: (MusicModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize() // ✅ Changed from fillMaxWidth to fillMaxSize
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Recently Played",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(), // ✅ Changed to fill remaining space
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFE91E63),
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            musics.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(), // ✅ Changed to fill remaining space
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No music uploaded yet",
                        color = Color.White.copy(0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            else -> {
                // ✅ Changed from LazyRow to LazyColumn for vertical scrolling
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize() // ✅ Fill all available space
                        .padding(bottom = 80.dp), // ✅ Add bottom padding for navigation bar
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(musics) { music ->
                        val isFavorite = favoriteMusics.any { it.musicId == music.musicId }

                        // ✅ Use MusicListItem instead of MusicCard for better vertical layout
                        MusicListItem(
                            music = music,
                            isFavorite = isFavorite,
                            onToggleFavorite = { onToggleFavorite(music.musicId) },
                            onAddToPlaylist = { onAddToPlaylist(music) },
                            onNavigate = {
                                navController.navigate(Screen.PlayingNow(music.musicId).route)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ✅ Alternative: Keep horizontal scrolling but make it full width
@Composable
fun RecentlyPlayedSectionHorizontal(
    navController: NavController,
    musics: List<MusicModel>,
    isLoading: Boolean,
    userId: String,
    favoriteMusics: List<MusicModel>,
    onToggleFavorite: (String) -> Unit,
    onAddToPlaylist: (MusicModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize() // ✅ Fill entire available space
            .padding(start = 16.dp, end = 16.dp, bottom = 80.dp) // ✅ Bottom padding for nav bar
    ) {
        Text(
            text = "Recently Played",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFE91E63),
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            musics.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No music uploaded yet",
                        color = Color.White.copy(0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            else -> {
                // ✅ Grid layout for better space utilization
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Display in rows of 2 cards each
                    items(musics.chunked(2)) { rowMusics ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowMusics.forEach { music ->
                                val isFavorite = favoriteMusics.any { it.musicId == music.musicId }

                                Box(modifier = Modifier.weight(1f)) {
                                    MusicCard(
                                        music = music,
                                        isFavorite = isFavorite,
                                        onToggleFavorite = { onToggleFavorite(music.musicId) },
                                        onAddToPlaylist = { onAddToPlaylist(music) },
                                        onNavigate = {
                                            navController.navigate(Screen.PlayingNow(music.musicId).route)
                                        }
                                    )
                                }
                            }

                            // Fill remaining space if odd number of items
                            if (rowMusics.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
