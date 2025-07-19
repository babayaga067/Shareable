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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sangeet.component.AppBottomNavigationBar
import com.example.sangeet.component.MusicListItem
import com.example.sangeet.model.MusicModel
import com.example.sangeet.model.PlaylistModel
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
    val userPlaylists by playlistViewModel.userPlaylists.observeAsState(emptyList())
    val context = LocalContext.current

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "user123"

    // State for playlist selection dialog
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var selectedMusic by remember { mutableStateOf<MusicModel?>(null) }

    LaunchedEffect(userId) {
        if (userId != "user123") {
            userViewModel.getUserById(userId)
            favoriteViewModel.getUserFavoriteMusics(userId)
            playlistViewModel.getUserPlaylists(userId)
        }
        musicViewModel.getAllMusics()
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF5B0E9C), Color(0xFF27005D))
    )

    // Playlist selection dialog
    if (showPlaylistDialog && selectedMusic != null) {
        PlaylistSelectionDialog(
            music = selectedMusic!!,
            playlists = userPlaylists,
            onDismiss = {
                showPlaylistDialog = false
                selectedMusic = null
            },
            onPlaylistSelected = { playlist ->
                playlistViewModel.addMusicToPlaylist(
                    playlist.playlistId,
                    selectedMusic!!.musicId
                ) { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                showPlaylistDialog = false
                selectedMusic = null
            },
            onCreateNewPlaylist = {
                navController?.navigate(Screen.CreatePlaylist(userId).route)
                showPlaylistDialog = false
                selectedMusic = null
            }
        )
    }

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
                                        // Show playlist selection dialog instead of navigating
                                        selectedMusic = music
                                        showPlaylistDialog = true
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
fun PlaylistSelectionDialog(
    music: MusicModel,
    playlists: List<PlaylistModel>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (PlaylistModel) -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 400.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B4E))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Add to Playlist",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Song info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = music.imageUrl.ifEmpty { "https://via.placeholder.com/40" },
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            music.musicName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Text(
                            music.artistName.ifEmpty { "Unknown Artist" },
                            color = Color.Gray,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Create new playlist option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCreateNewPlaylist() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4A2C7A)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Create New Playlist",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Playlists list
                if (playlists.isNotEmpty()) {
                    Text(
                        "Your Playlists",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(playlists) { playlist ->
                            PlaylistItem(
                                playlist = playlist,
                                onClick = { onPlaylistSelected(playlist) }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No playlists yet. Create your first playlist!",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistItem(
    playlist: PlaylistModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3A1F5C)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = if (playlist.imageUrl.isNotEmpty()) playlist.imageUrl else "https://via.placeholder.com/40",
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    playlist.playlistName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    "${playlist.musicIds?.size ?: 0} songs",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Icon(
                Icons.Default.PlaylistAdd,
                contentDescription = null,
                tint = Color(0xFFE91E63),
                modifier = Modifier.size(20.dp)
            )
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
