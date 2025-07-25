package com.example.sangeet.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.sangeet.component.*
import com.example.sangeet.model.MusicModel
import com.example.sangeet.navigation.Screen
import com.example.sangeet.utils.toggleFavorite
import com.example.sangeet.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    musicViewModel: MusicViewModel,
    favoriteViewModel: FavoriteViewModel,
    playlistViewModel: PlaylistViewModel
) {
    val context = LocalContext.current
    val gradient = Brush.verticalGradient(listOf(Color(0xFF4A004A), Color(0xFF1C0038)))
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId == null) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
        return
    }

    val allMusics by musicViewModel.allMusics.observeAsState(emptyList())
    val isLoading by musicViewModel.isLoading.observeAsState(false)
    val currentUser by userViewModel.user.observeAsState()
    val favoriteMusics by favoriteViewModel.favoriteMusics.observeAsState(emptyList())
    val userPlaylists by playlistViewModel.userPlaylists.observeAsState(emptyList())

    var showDialog by remember { mutableStateOf(false) }
    var selectedMusic by remember { mutableStateOf<MusicModel?>(null) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val refreshDashboard = {
        scope.launch {
            try {
                musicViewModel.getAllMusics()
                userViewModel.getUserById(userId)
                favoriteViewModel.getUserFavoriteMusics(userId)
                playlistViewModel.getUserPlaylists(userId)
                hasError = false
                Toast.makeText(context, "Refreshing...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                hasError = true
                errorMessage = e.message ?: "Unknown error occurred"
                Toast.makeText(context, "Error refreshing: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        Unit
    }

    LaunchedEffect(userId) {
        scope.launch {
            try {
                musicViewModel.getAllMusics()
                userViewModel.getUserById(userId)
                favoriteViewModel.getUserFavoriteMusics(userId)
                playlistViewModel.getUserPlaylists(userId)
            } catch (e: Exception) {
                hasError = true
                errorMessage = e.message ?: "Unknown error occurred"
            }
        }
    }

    val safeAllMusics = allMusics.filterNotNull()
    val recentlyPlayed = safeAllMusics.takeLast(15)
    val recommended = safeAllMusics.take(10)

    if (hasError) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Something went wrong",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = errorMessage,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = {
                        hasError = false
                        refreshDashboard()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Text("Retry", color = Color.White)
                }
            }
        }
        return
    }

    if (isLoading && safeAllMusics.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = Color.White)
                Text(
                    text = "Loading your music...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (currentUser != null) {
                SidebarDrawer(
                    navController = navController,
                    onClose = { scope.launch { drawerState.close() } },
                    currentUser = currentUser
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController = navController)
            },
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradient)
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top Bar
                    item {
                        DashboardTopBar(
                            currentUser = currentUser,
                            onRefresh = refreshDashboard,
                            onProfileClick = {
                                scope.launch {
                                    try {
                                        navController.navigate(Screen.Profile(userId).route)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onMenuClick = {
                                scope.launch {
                                    try {
                                        drawerState.open()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Menu error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }

                    // Quick Actions
                    item {
                        QuickAccessSection(
                            navController = navController,
                            userId = userId
                        )
                    }

                    // Recently Played Section
                    if (recentlyPlayed.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recently Played",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // ✅ FIXED: Using route navigation instead of Intent
                        items(recentlyPlayed) { music ->
                            ExpandedMusicListItem(
                                music = music,
                                isFavorite = favoriteMusics.any { it.musicId == music.musicId },
                                onMusicClick = { clickedMusic ->
                                    // ✅ FIXED: Use route navigation instead of Intent
                                    navController.navigate(Screen.PlayingNow(clickedMusic.musicId).route)
                                },
                                onToggleFavorite = { musicId ->
                                    scope.launch {
                                        try {
                                            toggleFavorite(userId, musicId, favoriteViewModel, context)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error toggling favorite: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onAddToPlaylist = { music ->
                                    selectedMusic = music
                                    showDialog = true
                                }
                            )
                        }
                    }

                    // Recommended Section
                    if (recommended.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recommended For You",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(recommended.take(8)) { music ->
                                    CompactMusicCard(
                                        music = music,
                                        isFavorite = favoriteMusics.any { it.musicId == music.musicId },
                                        onMusicClick = { clickedMusic ->
                                            // ✅ FIXED: Use route navigation instead of Intent
                                            navController.navigate(Screen.PlayingNow(clickedMusic.musicId).route)
                                        },
                                        onToggleFavorite = { musicId ->
                                            scope.launch {
                                                try {
                                                    toggleFavorite(userId, musicId, favoriteViewModel, context)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Error toggling favorite: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        onAddToPlaylist = { music ->
                                            selectedMusic = music
                                            showDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Empty State
                    if (safeAllMusics.isEmpty() && !isLoading) {
                        item {
                            EmptyStateMessage(
                                onUploadClick = {
                                    navController.navigate(Screen.UploadMusic(userId).route)
                                }
                            )
                        }
                    }
                }

                // Dialog
                if (showDialog && selectedMusic != null) {
                    AddToPlaylistDialog(
                        navController = navController,
                        userId = userId,
                        music = selectedMusic!!,
                        playlists = userPlaylists,
                        onDismiss = {
                            showDialog = false
                            selectedMusic = null
                        },
                        onAddToPlaylist = { playlistId ->
                            scope.launch {
                                try {
                                    playlistViewModel.addMusicToPlaylist(
                                        playlistId = playlistId,
                                        musicId = selectedMusic!!.musicId
                                    ) { success, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                    showDialog = false
                                    selectedMusic = null
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error adding to playlist: ${e.message}", Toast.LENGTH_SHORT).show()
                                    showDialog = false
                                    selectedMusic = null
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// ✅ Expanded Music List Item for Recently Played (Full Width)
@Composable
fun ExpandedMusicListItem(
    music: MusicModel,
    isFavorite: Boolean,
    onMusicClick: (MusicModel) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onAddToPlaylist: (MusicModel) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMusicClick(music) },
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (!music.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = music.imageUrl,
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Music Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = music.musicName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = music.artistName,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!music.genre.isNullOrEmpty()) {
                    Text(
                        text = music.genre,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action Buttons
            IconButton(onClick = { onToggleFavorite(music.musicId) }) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else Color.White
                )
            }

            IconButton(onClick = { onAddToPlaylist(music) }) {
                Icon(
                    Icons.Default.PlaylistAdd,
                    contentDescription = "Add to Playlist",
                    tint = Color.White
                )
            }

            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ✅ Compact Music Card for Recommended Section
@Composable
fun CompactMusicCard(
    music: MusicModel,
    isFavorite: Boolean,
    onMusicClick: (MusicModel) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onAddToPlaylist: (MusicModel) -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(180.dp)
            .clickable { onMusicClick(music) },
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Album Art
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (!music.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = music.imageUrl,
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Text(
                text = music.musicName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = music.artistName,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { onToggleFavorite(music.musicId) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }

                IconButton(
                    onClick = { onAddToPlaylist(music) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.PlaylistAdd,
                        contentDescription = "Add to Playlist",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickAccessSection(
    navController: NavController,
    userId: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Quick Actions",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        navController.navigate(Screen.UploadMusic(userId).route)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Upload Music",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Music", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        navController.navigate(Screen.Favorites(userId).route)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Favorites",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Favorites", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.Black.copy(alpha = 0.9f),
        contentColor = Color.White
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (currentRoute == "dashboard") Color.White else Color.Gray
                )
            },
            label = {
                Text(
                    "Home",
                    color = if (currentRoute == "dashboard") Color.White else Color.Gray
                )
            },
            selected = currentRoute == "dashboard",
            onClick = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.White.copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = if (currentRoute == "search") Color.White else Color.Gray
                )
            },
            label = {
                Text(
                    "Search",
                    color = if (currentRoute == "search") Color.White else Color.Gray
                )
            },
            selected = currentRoute == "search",
            onClick = {
                navController.navigate(Screen.Search.route)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.White.copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.LibraryMusic,
                    contentDescription = "Your Library",
                    tint = if (currentRoute == "library") Color.White else Color.Gray
                )
            },
            label = {
                Text(
                    "Your Library",
                    color = if (currentRoute == "library") Color.White else Color.Gray
                )
            },
            selected = currentRoute == "library",
            onClick = {
                navController.navigate(Screen.Library.route)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
fun EmptyStateMessage(onUploadClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Default.LibraryMusic,
            contentDescription = "No Music",
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = "No music found",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Upload your first song to get started!",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = onUploadClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE91E63)
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload Music", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
