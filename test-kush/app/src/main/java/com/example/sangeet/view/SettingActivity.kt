package com.example.sangeet.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "Preferences",
                    fontSize = 20.sp,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                SettingToggleCard(
                    title = "Notifications",
                    subtitle = "Enable push notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }

            item {
                SettingToggleCard(
                    title = "Dark Mode",
                    subtitle = "Enable dark theme",
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            }
        }
    }
}

@Composable
fun SettingToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, color = Color.White)
                Text(text = subtitle, fontSize = 12.sp, color = Color.LightGray)
            }

            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}