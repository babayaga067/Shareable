package com.example.sangeet.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sangeet.component.ChangePasswordDialog
import com.example.sangeet.component.EditProfileDialog
import com.example.sangeet.component.ProfileOptionCard
import com.example.sangeet.repository.UserRepositoryImpl
import com.example.sangeet.viewmodel.UserViewModel
import com.example.sangeet.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : ComponentActivity() {
    private val userViewModel by lazy {
        UserViewModel(UserRepositoryImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userId = intent.getStringExtra("userId").orEmpty()

        setContent {
            val navController = rememberNavController()
            ProfileScreen(
                navController = navController,
                userId = userId,
                userViewModel = userViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController? = null,
    userId: String,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
    )

    val currentUser by userViewModel.user.observeAsState()
    var isLoading by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) userViewModel.getUserById(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController?.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE91E63))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarLetter = currentUser?.fullName?.takeIf { it.isNotBlank() }?.firstOrNull()?.uppercase() ?: "U"
                        Text(
                            text = avatarLetter,
                            fontSize = 48.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(currentUser?.fullName ?: "User", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(currentUser?.email ?: "user@example.com", fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f))

                    Spacer(modifier = Modifier.height(32.dp))

                    ProfileOptionCard(
                        icon = Icons.Default.Edit,
                        title = "Edit Profile",
                        subtitle = "Update your personal info",
                        onClick = { showEditDialog = true }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileOptionCard(
                        icon = Icons.Default.Lock,
                        title = "Change Password",
                        subtitle = "Update your account password",
                        onClick = { showPasswordDialog = true }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileOptionCard(
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        subtitle = "App preferences and configurations",
                        onClick = { /* Future settings */ }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            navController?.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                            Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
                    }
                }
            }

            if (showEditDialog) {
                EditProfileDialog(
                    currentUser = currentUser,
                    onDismiss = { showEditDialog = false },
                    onSave = { updatedUser ->
                        isLoading = true
                        val updates = mapOf("fullName" to updatedUser.fullName)
                        userViewModel.updateProfile(userId, updates) { success, _ ->
                            isLoading = false
                            if (success) userViewModel.getUserById(userId)
                        }
                        showEditDialog = false
                    }
                )
            }

            if (showPasswordDialog) {
                ChangePasswordDialog(
                    onDismiss = { showPasswordDialog = false },
                    onSave = { _, newPassword ->
                        val user = FirebaseAuth.getInstance().currentUser
                        user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Password updated successfully", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Error updating password", Toast.LENGTH_LONG).show()
                            }
                        }
                        showPasswordDialog = false
                    }
                )
            }
        }
    }
}