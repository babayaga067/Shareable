package com.example.sangeet.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sangeet.model.ArtistModel
import com.example.sangeet.repository.ArtistRepositoryImpl
import com.example.sangeet.viewmodel.ArtistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistListScreen(navController: NavController) {
    val gradient = Brush.verticalGradient(colors = listOf(Color(0xFF4A004A), Color(0xFF1C0038)))
    val artistViewModel = remember { ArtistViewModel(ArtistRepositoryImpl()) }

    val allArtists by artistViewModel.allArtists.observeAsState(emptyList())
    val searchResults by artistViewModel.searchResults.observeAsState(emptyList())
    val isLoading by artistViewModel.isLoading.observeAsState(false)

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        artistViewModel.getAllArtists()
    }

    LaunchedEffect(searchQuery) {
        isSearching = searchQuery.isNotBlank()
        if (isSearching) artistViewModel.searchArtists(searchQuery)
    }

    Column(modifier = Modifier.fillMaxSize().background(gradient)) {
        TopAppBar(
            title = { Text("Artists", color = Color.White, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(25.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search artists...", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE91E63))
                }
            }

            isSearching && searchResults.isEmpty() -> {
                EmptyState(
                    title = "No artists found",
                    subtitle = "Try searching with different keywords",
                    icon = Icons.Default.Search
                )
            }

            !isSearching && allArtists.isEmpty() -> {
                EmptyState(
                    title = "No artists available",
                    subtitle = "Artists will appear here once added",
                    icon = Icons.Default.Person
                )
            }

            else -> {
                val artistsToShow = if (isSearching) searchResults else allArtists

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(artistsToShow) { artist ->
                        ArtistItem(artist = artist) {
                            // navController.navigate(Screen.ArtistDetail(artist.artistId).route)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(title: String, subtitle: String, icon: ImageVector) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = "Empty icon", tint = Color.Gray, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, color = Color.Gray, fontSize = 18.sp)
            Text(subtitle, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun ArtistItem(artist: ArtistModel, onClick: () -> Unit) {
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (artist.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = artist.imageUrl,
                    contentDescription = "${artist.artistName} Image",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Default Artist Icon",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(artist.artistName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (artist.genre.isNotBlank()) {
                    Text(artist.genre, color = Color.Gray, fontSize = 14.sp)
                }
                Text("${artist.followersCount} followers • ${artist.musicIds.size} songs", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}