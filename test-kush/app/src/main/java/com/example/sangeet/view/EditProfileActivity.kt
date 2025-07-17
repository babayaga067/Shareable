package com.example.sangeet.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.sangeet.repository.UserRepositoryImpl
import com.example.sangeet.viewmodel.UserViewModel

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userId = intent.getStringExtra("userId") ?: ""
        setContent {
            val navController = rememberNavController()
            EditProfileScreen(navController = navController, userId = userId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController, userId: String) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF4A004A), Color(0xFF1C0038)))
    val context = LocalContext.current

    val userViewModel = remember { UserViewModel(UserRepositoryImpl()) }
    val userState by userViewModel.user.observeAsState()

    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        userViewModel.getUserById(userId)
    }

    LaunchedEffect(userState) {
        userState?.let {
            name = it.fullName.orEmpty()
            bio = it.bio.orEmpty()
            imageUrl = it.profileImageUrl.orEmpty()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Profile Preview",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(40.dp))
                            .align(Alignment.CenterHorizontally)
                    )
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
                        Text("Profile Info", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors()
                        )

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors()
                        )

                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("Profile Image URL") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors()
                        )
                    }
                }

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "Please enter your display name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSaving = true
                        val updatedData = mapOf(
                            "fullName" to name,
                            "bio" to bio,
                            "profileImageUrl" to imageUrl
                        )

                        userViewModel.updateProfile(userId, updatedData) { success, message ->
                            isSaving = false
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            if (success) navController.popBackStack()
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFFE91E63),
    unfocusedBorderColor = Color.Gray,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.Gray,
    cursorColor = Color.White,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)