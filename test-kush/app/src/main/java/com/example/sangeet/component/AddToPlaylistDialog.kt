package com.example.sangeet.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sangeet.model.MusicModel
import com.example.sangeet.model.PlaylistModel
import com.example.sangeet.navigation.Screen

@Composable
fun AddToPlaylistDialog(
    navController: NavController,
    userId: String,
    music: MusicModel,
    playlists: List<PlaylistModel>,
    onDismiss: () -> Unit,
    onAddToPlaylist: (String) -> Unit
) {
    val scrollState = rememberLazyListState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add to Playlist",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Select a playlist to add \"${music.musicName}\":",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (playlists.isEmpty()) {
                    Text(
                        text = "No playlists found. Create a playlist first.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                } else {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(playlists) { playlist ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAddToPlaylist(playlist.playlistId)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Black.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlaylistPlay,
                                        contentDescription = "Playlist Icon",
                                        tint = Color(0xFFE91E63),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = playlist.playlistName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                        if (playlist.description.isNotBlank()) {
                                            Text(
                                                text = playlist.description,
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 12.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                navController.navigate(Screen.CreatePlaylist(userId).route)
                onDismiss()
            }) {
                Text("Create New", color = Color(0xFFE91E63))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = Color(0xFF1A1A2E),
        textContentColor = Color.White
    )
}