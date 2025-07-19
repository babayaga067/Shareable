package com.example.sangeet.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.sangeet.navigation.Screen

@Composable
fun AppBottomNavigationBar(
    navController: NavController? = null,
    currentRoute: String = ""
) {
    NavigationBar(
        containerColor = Color(0xFF3D0E5C),
        contentColor = Color.White
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = currentRoute == "dashboard",
            onClick = {
                navController?.navigate(Screen.Dashboard.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = null) },
            label = { Text("Search") },
            selected = currentRoute == "search",
            onClick = {
                navController?.navigate(Screen.Search.route) {
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
            label = { Text("Your Library") },
            selected = currentRoute == "library",
            onClick = {
                navController?.navigate(Screen.Library.route) {
                    launchSingleTop = true
                }
            }
        )
    }
}
