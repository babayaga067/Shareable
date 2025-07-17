package com.example.sangeet.view

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
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
    val recentlyPlayed = safeAllMusics.takeLast(4)
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
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

                    // ✅ FIXED: QuickAccessSection using same navigation as sidebar
                    item {
                        QuickAccessSection(
                            navController = navController,
                            userId = userId
                        )
                    }

                    if (recentlyPlayed.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recently Played",
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
                                items(recentlyPlayed) { music ->
                                    MusicCard(
                                        music = music,
                                        isFavorite = favoriteMusics.any { it.musicId == music.musicId },
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

                    if (recommended.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recommended For You",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(recommended.take(5)) { music ->
                            MusicListItem(
                                music = music,
                                isFavorite = favoriteMusics.any { it.musicId == music.musicId },
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

// ✅ FIXED: QuickAccessSection using same navigation route as sidebar
@Composable
fun QuickAccessSection(
    navController: NavController,
    userId: String
) {
    val context = LocalContext.current

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
                // ✅ FIXED: Using same navigation route as sidebar
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
fun MusicCard(
    music: MusicModel,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit,
    onAddToPlaylist: (MusicModel) -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = "Music",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = music.musicName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = music.artistName,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { onToggleFavorite(music.musicId) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { onAddToPlaylist(music) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.PlaylistAdd,
                        contentDescription = "Add to Playlist",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MusicListItem(
    music: MusicModel,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit,
    onAddToPlaylist: (MusicModel) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = "Music",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = music.musicName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = music.artistName,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

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
        }
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
