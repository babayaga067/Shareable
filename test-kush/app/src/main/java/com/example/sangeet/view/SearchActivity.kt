package com.example.sangeet.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import com.example.sangeet.model.MusicModel
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

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        musicViewModel.getAllMusics()
        if (userId.isNotEmpty()) favoriteViewModel.getUserFavoriteMusics(userId)
    }

    val filteredMusics = allMusics
        .filterNotNull() // 🔐 removes null items
        .filter {
            it.musicName.contains(searchQuery, true) ||
                    it.artistName.contains(searchQuery, true) ||
                    it.genre.contains(searchQuery, true)
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .padding(16.dp)
    ) {
        // 🧭 Top Bar
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = { navController?.navigateUp() ?: (context as? Activity)?.finish() }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Spacer(modifier = Modifier.weight(1f))
            Text("Search", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
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

        // 🔍 Search Input
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search here", color = Color.LightGray) },
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
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🎶 Section Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Songs For You", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /* Future filter/sort */ }) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Sort",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🎧 Results Display
        if (filteredMusics.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No music found", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMusics) { music ->
                    val isFavorite = favoriteMusics.any { it.musicId == music.musicId }
                    MusicItem(
                        music = music,
                        isFavorite = isFavorite,
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

@Composable
fun MusicItem(
    music: MusicModel,
    isFavorite: Boolean,
    onHeartClick: () -> Unit
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = if (music.imageUrl.startsWith("/")) File(music.imageUrl) else music.imageUrl.ifEmpty { "https://via.placeholder.com/50" },
                    contentDescription = music.musicName,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        music.musicName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        music.artistName.ifEmpty { "Unknown Artist" },
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    if (music.genre.isNotEmpty()) {
                        Text(music.genre, fontSize = 10.sp, color = Color.Gray)
                    }
                }

                IconButton(onClick = onHeartClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFE91E63) else Color.LightGray
                    )
                }
            }
        }

