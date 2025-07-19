package com.example.sangeet.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sangeet.navigation.Screen
import com.example.sangeet.repository.FavoriteRepositoryImpl
import com.example.sangeet.viewmodel.FavoriteViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(navController: NavController, userId: String) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF4A004A), Color(0xFF1C0038)))
    val context = LocalContext.current
    val favoriteViewModel = remember { FavoriteViewModel(FavoriteRepositoryImpl()) }

    val favoriteMusics by favoriteViewModel.favoriteMusics.observeAsState(emptyList())
    val isLoading by favoriteViewModel.isLoading.observeAsState(false)

    LaunchedEffect(userId) {
        favoriteViewModel.getUserFavoriteMusics(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Favorites", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFE91E63))
                    }
                }

                favoriteMusics.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.FavoriteBorder,
                                contentDescription = "No Favorites",
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No favorite songs yet", color = Color.Gray, fontSize = 18.sp)
                            Text("Start adding songs to your favorites!", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(favoriteMusics) { music ->
                            FavoriteCard(
                                music = music,
                                userId = userId,
                                onPlay = {
                                    // Navigate to PlayingNow screen with the music ID
                                    navController.navigate(Screen.PlayingNow(music.musicId).route)
                                },
                                onRemove = { musicId ->
                                    favoriteViewModel.removeFromFavorites(userId, musicId) { success, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        if (success) favoriteViewModel.getUserFavoriteMusics(userId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteCard(
    music: com.example.sangeet.model.MusicModel,
    userId: String,
    onPlay: () -> Unit,
    onRemove: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art with fallback
            AsyncImage(
                model = when {
                    music.imageUrl.isNotEmpty() && music.imageUrl.startsWith("/") -> File(music.imageUrl)
                    music.imageUrl.isNotEmpty() -> music.imageUrl
                    else -> null
                },
                contentDescription = "${music.musicName} album art",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = music.musicName.takeIf { it.isNotEmpty() } ?: "Unknown Song",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Text(
                    text = music.artistName.takeIf { it.isNotEmpty() } ?: "Unknown Artist",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                if (music.genre.isNotEmpty()) {
                    Text(
                        text = music.genre,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            // Play button
            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE91E63), CircleShape)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Remove from favorites button
            IconButton(onClick = { onRemove(music.musicId) }) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Remove from Favorites",
                    tint = Color(0xFFE91E63)
                )
            }
        }
    }
}
