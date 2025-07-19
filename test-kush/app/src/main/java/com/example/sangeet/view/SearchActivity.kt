package com.example.sangeet.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sangeet.component.AppBottomNavigationBar
import com.example.sangeet.model.MusicModel
import com.example.sangeet.navigation.Screen
import com.example.sangeet.repository.FavoriteRepositoryImpl
import com.example.sangeet.repository.MusicRepositoryImpl
import com.example.sangeet.viewmodel.FavoriteViewModel
import com.example.sangeet.viewmodel.MusicViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import androidx.compose.foundation.lazy.items

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SearchScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController? = null) {
    val gradientColors = listOf(Color(0xFF6100FF), Color(0xFF9A00FF))
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid.orEmpty()

    val musicViewModel = remember { MusicViewModel(MusicRepositoryImpl()) }
    val favoriteViewModel = remember { FavoriteViewModel(FavoriteRepositoryImpl()) }

    val allMusics by musicViewModel.allMusics.observeAsState(emptyList())
    val favoriteMusics by favoriteViewModel.favoriteMusics.observeAsState(emptyList())
    val isLoading by musicViewModel.isLoading.observeAsState(false)

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        musicViewModel.getAllMusics()
        if (userId.isNotEmpty()) favoriteViewModel.getUserFavoriteMusics(userId)
    }

    val filteredMusics = if (searchQuery.isEmpty()) {
        allMusics.filterNotNull()
    } else {
        allMusics
            .filterNotNull()
            .filter {
                it.musicName.contains(searchQuery, true) ||
                        it.artistName.contains(searchQuery, true) ||
                        it.genre.contains(searchQuery, true)
            }
    }

    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                navController = navController,
                currentRoute = "search"
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        navController?.navigateUp() ?: (context as? Activity)?.finish()
                    }
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Search",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { /* Future profile action */ }) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Input
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search songs, artists, genres...", color = Color.LightGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(10.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (searchQuery.isEmpty()) "All Songs" else "Search Results",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                if (filteredMusics.isNotEmpty()) {
                    Text(
                        "${filteredMusics.size} songs",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Results Display
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    filteredMusics.isEmpty() && searchQuery.isNotEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = "No results",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No music found for \"$searchQuery\"",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Try searching with different keywords",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    filteredMusics.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No music available",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(filteredMusics) { music ->
                                val isFavorite = favoriteMusics.any { it.musicId == music.musicId }
                                SearchMusicItem(
                                    music = music,
                                    isFavorite = isFavorite,
                                    onMusicClick = {
                                        // Navigate to PlayingNow screen
                                        navController?.navigate(Screen.PlayingNow(music.musicId).route)
                                    },
                                    onHeartClick = {
                                        favoriteViewModel.toggleFavorite(
                                            userId,
                                            music.musicId
                                        ) { success, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
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
}

@Composable
fun SearchMusicItem(
    music: MusicModel,
    isFavorite: Boolean,
    onMusicClick: () -> Unit,
    onHeartClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMusicClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art
            AsyncImage(
                model = when {
                    music.imageUrl.startsWith("/") -> File(music.imageUrl)
                    music.imageUrl.isNotEmpty() -> music.imageUrl
                    else -> "https://via.placeholder.com/60"
                },
                contentDescription = music.musicName,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Music Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = music.musicName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = music.artistName.ifEmpty { "Unknown Artist" },
                    fontSize = 14.sp,
                    color = Color.LightGray,
                    maxLines = 1
                )
                if (music.genre.isNotEmpty()) {
                    Text(
                        text = music.genre,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }

            // Play button
            IconButton(
                onClick = onMusicClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE91E63).copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Favorite button
            IconButton(onClick = onHeartClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFE91E63) else Color.LightGray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
