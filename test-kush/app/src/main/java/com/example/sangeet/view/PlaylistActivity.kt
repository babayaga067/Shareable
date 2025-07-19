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
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.sangeet.model.PlaylistModel
import com.example.sangeet.navigation.Screen
import com.example.sangeet.repository.PlaylistRepositoryImpl
import com.example.sangeet.viewmodel.PlaylistViewModel
import java.io.File

class PlaylistActivity : ComponentActivity() {
    private val playlistViewModel by lazy {
        PlaylistViewModel(PlaylistRepositoryImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userId = intent.getStringExtra("userId").orEmpty()

        setContent {
            val navController = rememberNavController()
            PlaylistScreen(
                navController = navController,
                userId = userId
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(navController: NavController, userId: String) {
    val playlistViewModel = remember { PlaylistViewModel(PlaylistRepositoryImpl()) }
    val userPlaylists by playlistViewModel.userPlaylists.observeAsState(emptyList())
    val isLoading by playlistViewModel.isLoading.observeAsState(false)
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var playlistToDelete by remember { mutableStateOf<PlaylistModel?>(null) }

    val gradient = Brush.verticalGradient(listOf(Color(0xFF4A004A), Color(0xFF1C0038)))

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            playlistViewModel.getUserPlaylists(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Playlists", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.CreatePlaylist(userId).route)
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Playlist", tint = Color.White)
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

                userPlaylists.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PlaylistPlay, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No playlists yet", color = Color.Gray, fontSize = 18.sp)
                            Text("Create your first playlist!", color = Color.Gray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                navController.navigate(Screen.CreatePlaylist(userId).route)
                            }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Playlist")
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(userPlaylists) { playlist ->
                            PlaylistCard(
                                playlist = playlist,
                                onClick = {
                                    // Navigate using proper Compose Navigation
                                    navController.navigate(
                                        Screen.PlaylistDetail(playlist.playlistId, userId).route
                                    )
                                },
                                onDeleteClick = {
                                    playlistToDelete = playlist
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // Delete confirmation dialog
            if (showDeleteDialog && playlistToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        playlistToDelete = null
                    },
                    title = { Text("Delete Playlist", color = Color.White) },
                    text = { Text("Are you sure you want to delete \"${playlistToDelete?.playlistName}\"?", color = Color.Gray) },
                    confirmButton = {
                        TextButton(onClick = {
                            playlistToDelete?.let { playlist ->
                                playlistViewModel.deletePlaylist(playlist.playlistId, userId) { success, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    if (success) {
                                        playlistViewModel.getUserPlaylists(userId)
                                    }
                                }
                            }
                            playlistToDelete = null
                            showDeleteDialog = false
                        }) {
                            Text("Delete", color = Color(0xFFE91E63))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            playlistToDelete = null
                            showDeleteDialog = false
                        }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    },
                    containerColor = Color.Black.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun PlaylistCard(
    playlist: PlaylistModel,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = if (playlist.imageUrl.isNotEmpty() && playlist.imageUrl.startsWith("/")) {
                    File(playlist.imageUrl)
                } else if (playlist.imageUrl.isNotEmpty()) {
                    playlist.imageUrl
                } else {
                    null
                },
                contentDescription = "Playlist cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.playlistName.takeIf { it.isNotEmpty() } ?: "Untitled Playlist",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "${playlist.musicIds?.size ?: 0} songs",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                if (playlist.description.isNotBlank()) {
                    Text(
                        text = playlist.description,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}
