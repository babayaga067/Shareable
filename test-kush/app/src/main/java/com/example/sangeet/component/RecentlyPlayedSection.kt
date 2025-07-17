package com.example.sangeet.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
            .fillMaxWidth()
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
                        .fillMaxWidth()
                        .height(120.dp),
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
                        .fillMaxWidth()
                        .height(120.dp),
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
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 4.dp, end = 16.dp)
                ) {
                    items(musics) { music ->
                        val isFavorite = favoriteMusics.any { it.musicId == music.musicId }

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
            }
        }
    }
}