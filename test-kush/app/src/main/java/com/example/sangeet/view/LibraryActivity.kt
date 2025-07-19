package com.example.sangeet.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sangeet.component.AppBottomNavigationBar
import com.example.sangeet.component.MusicListItem
import com.example.sangeet.model.MusicModel
import com.example.sangeet.navigation.Screen
import com.example.sangeet.repository.FavoriteRepositoryImpl
import com.example.sangeet.repository.MusicRepositoryImpl
import com.example.sangeet.repository.PlaylistRepositoryImpl
import com.example.sangeet.repository.UserRepositoryImpl
import com.example.sangeet.viewmodel.FavoriteViewModel
import com.example.sangeet.viewmodel.MusicViewModel
import com.example.sangeet.viewmodel.PlaylistViewModel
import com.example.sangeet.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class LibraryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LibraryScreen()
        }
    }
}

@Composable
fun LibraryScreen(navController: NavController? = null) {
    val userRepository = UserRepositoryImpl()
    val musicRepository = MusicRepositoryImpl()
    val favoriteRepository = FavoriteRepositoryImpl()
    val playlistRepository = PlaylistRepositoryImpl()

    val userViewModel = remember { UserViewModel(userRepository) }
    val musicViewModel = remember { MusicViewModel(musicRepository) }
    val favoriteViewModel = remember { FavoriteViewModel(favoriteRepository) }
    val playlistViewModel = remember { PlaylistViewModel(playlistRepository) }

    val currentUser by userViewModel.user.observeAsState()
    val recentlyPlayed by musicViewModel.allMusics.observeAsState(emptyList())
    val favoriteMusics by favoriteViewModel.favoriteMusics.observeAsState(emptyList())
    val context = LocalContext.current

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "user123"

    LaunchedEffect(userId) {
        if (userId != "user123") {
            userViewModel.getUserById(userId)
            favoriteViewModel.getUserFavoriteMusics(userId)
        }
        musicViewModel.getAllMusics()
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF5B0E9C), Color(0xFF27005D))
    )

    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                navController = navController,
                currentRoute = "library"
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Greeting Section
                GreetingSection(currentUser = currentUser)

                Spacer(modifier = Modifier.height(24.dp))

                // Library Cards Section
                Text("Your Library", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LibraryCard(
                        icon = Icons.Default.Favorite,
                        title = "Favourite",
                        subtitle = "Your loved songs collection",
                        modifier = Modifier.weight(1f)
                    ) {
                        navController?.navigate(Screen.Favorites(userId).route)
                    }

                    LibraryCard(
                        icon = Icons.Default.PlaylistPlay,
                        title = "Playlists",
                        subtitle = "Custom music collections",
                        modifier = Modifier.weight(1f)
                    ) {
                        navController?.navigate(Screen.Playlists(userId).route)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recently Played Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recently Played", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Icon(Icons.Default.ExpandMore, contentDescription = null, tint = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recently Played Content - This should fill remaining space
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f) // This makes it expand to fill remaining space
                ) {
                    if (recentlyPlayed.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Recently played songs will appear here",
                                color = Color.White.copy(0.7f),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        // Remove height constraint and let it fill available space
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(recentlyPlayed.filterNotNull().takeLast(20).reversed()) { music ->
                                val isFavorite = favoriteMusics.any { it.musicId == music.musicId }

                                MusicListItem(
                                    music = music,
                                    isFavorite = isFavorite,
                                    onToggleFavorite = {
                                        // Toggle favorite functionality
                                        favoriteViewModel.toggleFavorite(
                                            userId,
                                            music.musicId
                                        ) { success, message ->
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                            if (success) {
                                                favoriteViewModel.getUserFavoriteMusics(userId)
                                            }
                                        }
                                    },
                                    onAddToPlaylist = {
                                        // Navigate to playlist selection or show playlist selection dialog
                                        navController?.navigate(Screen.Playlists(userId).route)
                                        Toast.makeText(
                                            context,
                                            "Adding ${music.musicName} to playlist",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onNavigate = {
                                        navController?.navigate(Screen.PlayingNow(music.musicId).route)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GreetingSection(currentUser: com.example.sangeet.model.UserModel?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Hi ${currentUser?.fullName ?: "User"},", color = Color.White, fontWeight = FontWeight.SemiBold)
            Text("Good Afternoon", color = Color.White, fontSize = 14.sp)
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0D3F5)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentUser?.fullName?.firstOrNull()?.uppercase() ?: "U",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun LibraryCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp) // Increased height for better subtitle visibility
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x40FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Increased padding for better spacing
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon at the top
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "$title icon",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp) // Slightly larger icon
                )
            }

            // Title and subtitle at the bottom
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2 // Allow for longer subtitles
                )
            }
        }
    }
}
