package com.example.sangeet.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sangeet.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(navController: NavController) {
    val aboutDescription = remember {
        "Sangeet is a vibrant music streaming platform built to amplify independent voices and connect global audiences. Powered by Kotlin and Jetpack Compose, we deliver lightning-fast playback, curated recommendations, and a seamless user experience."
    }

    val contactEmail = "support@sangeet.io"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Us") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1B0032))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Sangeet logo",
                    modifier = Modifier
                        .size(90.dp)
                        .padding(bottom = 12.dp)
                )

                Text(
                    text = "Welcome to Sangeet",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = aboutDescription,
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Contact Us",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = contactEmail,
                        modifier = Modifier.padding(12.dp),
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "© ${java.time.Year.now()} Sangeet Inc.",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}