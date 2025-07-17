package com.example.sangeet.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sangeet.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MenuScreen()
        }
    }
}

@Composable
fun MenuScreen(navController: NavController? = null) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val gradient = Brush.verticalGradient(listOf(Color(0xFF4B0082), Color(0xFF8A2BE2)))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { TopBar() }

            item {
                Text(
                    "Welcome back!",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                MenuButton("Dashboard", Icons.Default.Home) {
                    navController?.navigate(Screen.Dashboard.route)
                }
            }

            if (userId != null) {
                item {
                    MenuButton("Profile", Icons.Default.Person) {
                        navController?.navigate(Screen.Profile(userId).route)
                    }
                }

                item {
                    MenuButton("Upload Music", Icons.Default.CloudUpload) {
                        navController?.navigate(Screen.UploadMusic(userId).route)
                    }
                }

                item {
                    MenuButton("Playlists", Icons.Default.List) {
                        navController?.navigate(Screen.Playlists(userId).route)
                    }
                }

                item {
                    MenuButton("Favorites", Icons.Default.Favorite) {
                        navController?.navigate(Screen.Favorites(userId).route)
                    }
                }
            }

            item {
                MenuButton("Help and Support", Icons.Default.Info) {
                    navController?.navigate(Screen.Settings.route)
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp) ) } // leaves space above bottom nav
        }

        BottomNavBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            navController = navController
        )
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(Icons.Default.Menu, contentDescription = "Menu icon", tint = Color.White)
        Text("Menu", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(24.dp))
    }
}

@Composable
fun MenuButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Icon(icon, contentDescription = "$title icon", tint = Color.White)
        }
    }
}

@Composable
fun BottomNavBar(modifier: Modifier = Modifier, navController: NavController? = null) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color(0x55000000)
    ) {
        NavigationBarItem(
            selected = false,
            onClick = { navController?.navigate(Screen.Dashboard.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Dashboard") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController?.navigate(Screen.Search.route) },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Search") }
        )
        NavigationBarItem(
            selected = true,
            onClick = { navController?.navigate(Screen.Library.route) },
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
            label = { Text("Library") }
        )
    }
}
