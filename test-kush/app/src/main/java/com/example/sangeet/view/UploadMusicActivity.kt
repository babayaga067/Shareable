package com.example.sangeet.view

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sangeet.model.MusicModel
import com.example.sangeet.repository.MusicRepositoryImpl
import com.example.sangeet.repository.UserRepositoryImpl
import com.example.sangeet.utils.FileUploadUtil
import com.example.sangeet.viewmodel.MusicViewModel
import com.example.sangeet.viewmodel.UserViewModel
import java.util.UUID
import com.example.sangeet.component.MusicMetadataSection


class UploadMusicActivity : ComponentActivity() {

    private val userViewModel by lazy { UserViewModel(UserRepositoryImpl()) }
    private val musicViewModel by lazy { MusicViewModel(MusicRepositoryImpl()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userId = intent.getStringExtra("userId").orEmpty()

        setContent {
            val navController = rememberNavController()

            UploadMusicScreen(
                navController = navController,
                userId = userId,
                musicViewModel = musicViewModel,
                userViewModel = userViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadMusicScreen(
    navController: NavController,
    userId: String,
    musicViewModel: MusicViewModel,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val gradient = Brush.verticalGradient(listOf(Color(0xFF4A004A), Color(0xFF1C0038)))

    var musicName by remember { mutableStateOf("") }
    var artistName by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> audioUri = uri }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> imageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Music", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                MusicMetadataSection(
                    musicName = musicName,
                    artistName = artistName,
                    genre = genre,
                    description = description,
                    duration = duration,
                    onMusicNameChange = { musicName = it },
                    onArtistNameChange = { artistName = it },
                    onGenreChange = { genre = it },
                    onDescriptionChange = { description = it },
                    onDurationChange = { duration = it }
                )
            }

            item {
                Text("Select Files", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        Button(onClick = { audioPicker.launch("audio/*") }) {
                            Text("Pick Audio")
                        }
                    }
                    item {
                        Button(onClick = { imagePicker.launch("image/*") }) {
                            Text("Pick Image")
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (musicName.isBlank() || artistName.isBlank() || audioUri == null) {
                            Toast.makeText(context, "Please fill required fields and select audio", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isUploading = true
                        val musicId = UUID.randomUUID().toString()

                        FileUploadUtil.uploadAudio(context, audioUri!!) { audioSuccess, audioUrl ->
                            if (audioSuccess && audioUrl != null) {
                                val finalize = { imageUrl: String ->
                                    val music = MusicModel(
                                        musicId = musicId,
                                        musicName = musicName,
                                        artistName = artistName,
                                        genre = genre,
                                        description = description,
                                        duration = duration.toLongOrNull() ?: 0L,
                                        audioUrl = audioUrl,
                                        imageUrl = imageUrl,
                                        uploadedBy = userId,
                                        uploadedAt = System.currentTimeMillis()
                                    )
                                    musicViewModel.addMusic(music) { success, message ->
                                        isUploading = false
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                        if (success) navController.popBackStack()
                                    }
                                }

                                if (imageUri != null) {
                                    FileUploadUtil.uploadImage(context, imageUri!!) { imageSuccess, imageUrl ->
                                        finalize(if (imageSuccess && imageUrl != null) imageUrl else "")
                                    }
                                } else {
                                    finalize("")
                                }
                            } else {
                                isUploading = false
                                Toast.makeText(context, "Failed to upload audio", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isUploading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Music", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}