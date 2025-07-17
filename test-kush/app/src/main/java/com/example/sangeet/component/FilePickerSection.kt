package com.example.sangeet.component

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FilePickerSection(
    audioUri: Uri?,
    imageUri: Uri?,
    onAudioPick: () -> Unit,
    onImagePick: () -> Unit
) {
    val audioLabel by remember(audioUri) {
        mutableStateOf(audioUri?.lastPathSegment ?: "Select Audio File")
    }

    val imageLabel by remember(imageUri) {
        mutableStateOf(imageUri?.lastPathSegment ?: "Select Cover Image")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("File Selection", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAudioPick() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = "Audio File", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = audioLabel, color = Color.White)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onImagePick() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Image, contentDescription = "Cover Image", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = imageLabel, color = Color.White)
            }

            Text(
                text = "Note: Files will be uploaded to backend using Firebase.",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}