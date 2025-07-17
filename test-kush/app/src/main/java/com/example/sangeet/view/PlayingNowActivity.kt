package com.example.sangeet.view

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.sangeet.viewmodel.MusicViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayingNowScreen(
    musicId: String,
    musicViewModel: MusicViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val musicLive by musicViewModel.music.observeAsState()
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }
    var durationText by remember { mutableStateOf("0:00") }
    var currentTimeText by remember { mutableStateOf("0:00") }

    LaunchedEffect(musicId) {
        if (musicId.isNotEmpty()) {
            musicViewModel.getMusicById(musicId)
        }
    }

    LaunchedEffect(musicLive) {
        mediaPlayer?.release()
        mediaPlayer = null

        musicLive?.audioUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepareAsync()
                isBuffering = true
                setOnPreparedListener {
                    isBuffering = false
                    start()
                    isPlaying = true
                    durationText = formatTime(duration.takeIf { it > 0 } ?: 0)
                }
                setOnCompletionListener {
                    isPlaying = false
                    progress = 0f
                }
            }
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let {
                val current = it.currentPosition
                val total = it.duration
                if (total > 0) {
                    progress = current / total.toFloat()
                    currentTimeText = formatTime(current)
                }
            }
            delay(500)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    val gradient = Brush.verticalGradient(listOf(Color(0xFF4C005F), Color(0xFF9D00B7)))

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF4C005F), contentColor = Color.White) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Dashboard.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, null) },
                    label = { Text("Search") },
                    selected = false,
                    onClick = { /* future: Screen.Search */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LibraryMusic, null) },
                    label = { Text("Library") },
                    selected = true,
                    onClick = { /* future: Screen.Library */ }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
        ) {
            if (musicLive == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
                return@Box
            }

            val music = musicLive!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Playing now", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))

                AsyncImage(
                    model = music.imageUrl.ifEmpty { "https://via.placeholder.com/220" },
                    contentDescription = "Album cover of ${music.musicName}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text(music.musicName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(music.artistName, fontSize = 14.sp, color = Color.LightGray)

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = progress,
                    onValueChange = {
                        progress = it
                        mediaPlayer?.seekTo((it * (mediaPlayer?.duration ?: 0)).toInt())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.LightGray,
                        inactiveTrackColor = Color.Gray
                    )
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(currentTimeText, fontSize = 12.sp, color = Color.White)
                    Text(durationText, fontSize = 12.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Repeat, contentDescription = "Repeat", tint = Color.White)
                    Text("-10s", color = Color.White)
                    IconButton(onClick = {
                        mediaPlayer?.let {
                            if (it.isPlaying) {
                                it.pause()
                                isPlaying = false
                            } else {
                                it.start()
                                isPlaying = true
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                    Text("+10s", color = Color.White)
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }

                if (isBuffering && !isPlaying) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}