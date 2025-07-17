package com.example.sangeet.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sangeet.model.MusicModel
import com.example.sangeet.navigation.Screen

@Composable
fun RecommendationSection(
    musics: List<MusicModel>,
    isLoading: Boolean,
    userId: String,
    favoriteMusics: List<MusicModel>,
    navController: NavController,
    onToggleFavorite: (String) -> Unit,
    onAddToPlaylist: (MusicModel) -> Unit
) {
    val favoriteIds = remember(favoriteMusics) { favoriteMusics.map { it.musicId }.toSet() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Recommendations",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFE91E63)
                    )
                }
            }

            musics.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recommended music found",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(musics) { music ->
                        RecommendationItem(
                            music = music,
                            isFavorite = favoriteIds.contains(music.musicId),
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