package com.example.sangeet.view

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    var isRepeating by remember { mutableStateOf(false) }

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
                try {
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
                        if (isRepeating) {
                            seekTo(0)
                            start()
                            isPlaying = true
                        } else {
                            isPlaying = false
                            progress = 0f
                        }
                    }
                } catch (e: Exception) {
                    isBuffering = false
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

    // Full screen without bottom navigation
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with back button
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    "Now Playing",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                // Placeholder for symmetry
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Album art - made bigger
            AsyncImage(
                model = music.imageUrl.ifEmpty { "https://via.placeholder.com/300" },
                contentDescription = "Album cover of ${music.musicName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(300.dp) // Increased from 220dp
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Song info
            Text(
                text = music.musicName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = music.artistName,
                fontSize = 16.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Progress slider
            Slider(
                value = progress,
                onValueChange = { newProgress ->
                    progress = newProgress
                    mediaPlayer?.let { mp ->
                        val newPosition = (newProgress * mp.duration).toInt()
                        mp.seekTo(newPosition)
                    }
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Time display
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(currentTimeText, fontSize = 14.sp, color = Color.White)
                Text(durationText, fontSize = 14.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Control buttons
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Repeat button
                IconButton(
                    onClick = { isRepeating = !isRepeating },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            if (isRepeating) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (isRepeating) Color.White else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // -10s button
                IconButton(
                    onClick = {
                        mediaPlayer?.let { mp ->
                            val newPosition = (mp.currentPosition - 10000).coerceAtLeast(0)
                            mp.seekTo(newPosition)
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Text(
                        "-10",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Play/Pause button
                IconButton(
                    onClick = {
                        mediaPlayer?.let { mp ->
                            if (mp.isPlaying) {
                                mp.pause()
                                isPlaying = false
                            } else {
                                mp.start()
                                isPlaying = true
                            }
                        }
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // +10s button
                IconButton(
                    onClick = {
                        mediaPlayer?.let { mp ->
                            val newPosition = (mp.currentPosition + 10000).coerceAtMost(mp.duration)
                            mp.seekTo(newPosition)
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Text(
                        "+10",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Shuffle button (placeholder for symmetry)
                IconButton(
                    onClick = { /* TODO: Implement shuffle */ },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Loading indicator
            if (isBuffering && !isPlaying) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

private fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
