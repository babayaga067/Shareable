package com.example.sangeet.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sangeet.model.MusicModel
import com.example.sangeet.repository.MusicRepositoryImpl
import com.example.sangeet.repository.UserRepositoryImpl
import com.example.sangeet.viewmodel.MusicViewModel
import com.example.sangeet.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class LibraryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LibraryScreen()
        }
    }
}

@Composable
fun LibraryScreen(navController: NavController? = null) {
    val userRepository = UserRepositoryImpl()
    val musicRepository = MusicRepositoryImpl()
    val userViewModel = remember { UserViewModel(userRepository) }
    val musicViewModel = remember { MusicViewModel(musicRepository) }

    val currentUser by userViewModel.user.observeAsState()
    val recentlyPlayed by musicViewModel.allMusics.observeAsState(emptyList())

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "user123"

    LaunchedEffect(userId) {
        if (userId != "user123") {
            userViewModel.getUserById(userId)
        }
        musicViewModel.getAllMusics()
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF5B0E9C), Color(0xFF27005D))
    )

    Scaffold(
        bottomBar = { BottomNavigationBar() },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                GreetingSection(currentUser = currentUser)

                Spacer(modifier = Modifier.height(24.dp))

                Text("Your Library", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LibraryCard(
                        icon = Icons.Default.Favorite,
                        title = "Favourite",
                        subtitle = "View favorites"
                    ) {
                        navController?.navigate("favorites/$userId")
                    }
                    LibraryCard(
                        icon = Icons.Default.PlaylistPlay,
                        title = "Playlists",
                        subtitle = "View playlists"
                    ) {
                        navController?.navigate("playlists/$userId")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recently Played", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Icon(Icons.Default.ExpandMore, contentDescription = null, tint = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (recentlyPlayed.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Recently played songs will appear here",
                            color = Color.White.copy(0.7f),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(recentlyPlayed.filterNotNull().takeLast(10).reversed()) { music ->
                            SongItem(music)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GreetingSection(currentUser: com.example.sangeet.model.UserModel?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Hi ${currentUser?.fullName ?: "User"},", color = Color.White, fontWeight = FontWeight.SemiBold)
            Text("Good Afternoon", color = Color.White, fontSize = 14.sp)
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0D3F5)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentUser?.fullName?.firstOrNull()?.uppercase() ?: "U",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun LibraryCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x40FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = "$title icon", tint = Color.White)
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}

@Composable
fun SongItem(music: MusicModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(music.imageUrl.ifEmpty { "https://via.placeholder.com/50" })
                .crossfade(true)
                .build(),
            contentDescription = music.musicName,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(music.musicName, color = Color.White, fontWeight = FontWeight.Medium)
            Text(music.artistName.ifEmpty { "Unknown Artist" }, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
        Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.White)
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar(
        containerColor = Color(0xFF3D0E5C),
        contentColor = Color.White
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = false,
            onClick = {}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = null) },
            label = { Text("Search") },
            selected = false,
            onClick = {}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
            label = { Text("Your Library") },
            selected = true,
            onClick = {}
        )
    }
}