package com.example.sangeet.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sangeet.R
import com.example.sangeet.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(2000) // Optional: Replace with actual logic completion trigger if needed
        val user = FirebaseAuth.getInstance().currentUser
        val destination = if (user != null) Screen.Dashboard.route else Screen.Login.route

        navController.navigate(destination) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            Image(
                painter = painterResource(R.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "App Logo"
                )
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}
