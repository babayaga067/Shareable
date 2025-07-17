package com.example.sangeet.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sangeet.viewmodel.UserViewModel
import com.example.sangeet.navigation.Screen

@Composable
fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.White,
    unfocusedBorderColor = Color.LightGray,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.LightGray,
    cursorColor = Color.White,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
@Composable
fun LoginScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF4A004A), Color(0xFF1C0038))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = "App Logo",
                        tint = Color.Cyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sangeet",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.trimStart() },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = loginFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        Icon(
                            imageVector = icon,
                            contentDescription = if (passwordVisible) "Hide Password" else "Show Password",
                            tint = Color.White,
                            modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = loginFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    text = "Forgot Password?",
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable {
                            val trimmedEmail = email.trim()
                            when {
                                trimmedEmail.isEmpty() ->
                                    Toast.makeText(context, "Please enter your email first", Toast.LENGTH_SHORT).show()

                                !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() ->
                                    Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()

                                else ->
                                    userViewModel.forgetPassword(trimmedEmail) { success, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                )
            }

            item {
                Button(
                    onClick = {
                        val trimmedEmail = email.trim()
                        val trimmedPassword = password.trim()

                        when {
                            trimmedEmail.isEmpty() || trimmedPassword.isEmpty() ->
                                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()

                            !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() ->
                                Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()

                            else ->
                                userViewModel.login(trimmedEmail, trimmedPassword) { success, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    if (success) {
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    }
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("LOGIN", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            item {
                Row {
                    Text("Don’t have an account? ", color = Color.White)
                    TextButton(onClick = {
                        navController.navigate(Screen.Register.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }) {
                        Text("Sign Up", color = Color.Cyan)
                    }
                }
            }
        }
    }
}