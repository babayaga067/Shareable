package com.example.sangeet.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sangeet.model.UserModel
import java.util.*

@Composable
fun DashboardTopBar(
    currentUser: UserModel?,
    onRefresh: () -> Unit,
    onProfileClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val greeting by remember {
        mutableStateOf(
            when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                in 5..11 -> "Good Morning"
                in 12..16 -> "Good Afternoon"
                in 17..22 -> "Good Evening"
                else -> "Hello"
            }
        )
    }

    val avatarLetter = currentUser?.fullName?.trim()?.firstOrNull()?.uppercase() ?: "U"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Open menu",
                tint = Color.White
            )
        }

        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "Hi ${currentUser?.fullName ?: "User"},",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = greeting,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh content",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarLetter,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}