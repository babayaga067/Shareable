package com.example.sangeet.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
        ModalNavigationDrawer(drawerState = drawerState, drawerContent = {}) {
            Scaffold { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(gradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Please log in to continue", color = Color.White)
                }
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

    val refreshDashboard = {
        musicViewModel.getAllMusics()
        userViewModel.getUserById(userId)
        favoriteViewModel.getUserFavoriteMusics(userId)
        playlistViewModel.getUserPlaylists(userId)
        Toast.makeText(context, "Refreshing...", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) { refreshDashboard() }

    val recentlyPlayed = allMusics.takeLast(4).filterNotNull()
    val recommended = allMusics.take(10).filterNotNull()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidebarDrawer(
                navController = navController,
                onClose = { scope.launch { drawerState.close() } },
                currentUser = currentUser
            )
        }
    ) {
        Scaffold(
            bottomBar = { BottomNavigationBar() },
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
                            onProfileClick = { navController.navigate(Screen.Profile(userId).route) },
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                    }

                    item { QuickAccessSection(navController = navController, userId = userId) }

                    item {
                        RecentlyPlayedSection(
                            navController = navController,
                            musics = recentlyPlayed,
                            isLoading = isLoading,
                            userId = userId,
                            favoriteMusics = favoriteMusics,
                            onToggleFavorite = { musicId -> toggleFavorite(userId, musicId, favoriteViewModel, context) },
                            onAddToPlaylist = { music ->
                                selectedMusic = music
                                showDialog = true
                            }
                        )
                    }

                    item {
                        RecommendationSection(
                            musics = recommended,
                            isLoading = isLoading,
                            userId = userId,
                            favoriteMusics = favoriteMusics,
                            navController = navController,
                            onToggleFavorite = { musicId -> toggleFavorite(userId, musicId, favoriteViewModel, context) },
                            onAddToPlaylist = { music ->
                                selectedMusic = music
                                showDialog = true
                            }
                        )
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
                            playlistViewModel.addMusicToPlaylist(
                                playlistId = playlistId,
                                musicId = selectedMusic!!.musicId
                            ) { success, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                            showDialog = false
                            selectedMusic = null
                        }
                    )
                }
            }
        }
    }
}