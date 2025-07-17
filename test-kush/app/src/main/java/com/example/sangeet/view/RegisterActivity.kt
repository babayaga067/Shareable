package com.example.sangeet.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.util.PatternsCompat
import androidx.navigation.NavController
import com.example.sangeet.model.UserModel
import com.example.sangeet.repository.UserRepositoryImpl
import com.example.sangeet.viewmodel.UserViewModel
import com.example.sangeet.navigation.Screen

@Composable
fun RegisterScreen(
    navController: NavController,
    onSignupSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    val repo = remember { UserRepositoryImpl() }
    val userViewModel = remember { UserViewModel(repo) }

    val gradient = Brush.verticalGradient(listOf(Color(0xFF4A004A), Color(0xFF1C0038)))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(horizontal = 24.dp)
            .imePadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text("Sign Up", fontSize = 30.sp, color = Color.White)
                Spacer(modifier = Modifier.height(20.dp))
            }

            item { RegisterTextField("Full Name", name) { name = it } }
            item { RegisterTextField("Email", email, KeyboardType.Email) { email = it } }
            item { RegisterTextField("Password", password, KeyboardType.Password, isPassword = true) { password = it } }
            item { RegisterTextField("Confirm Password", confirmPassword, KeyboardType.Password, isPassword = true) { confirmPassword = it } }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color.White)
                    )
                    Text("I accept the Terms & Conditions", color = Color.White, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val trimmedName = name.trim()
                        val trimmedEmail = email.trim()

                        when {
                            trimmedName.isEmpty() || trimmedEmail.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ->
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()

                            !PatternsCompat.EMAIL_ADDRESS.matcher(trimmedEmail).matches() ->
                                Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()

                            password.length < 6 ->
                                Toast.makeText(context, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show()

                            password != confirmPassword ->
                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()

                            !termsAccepted ->
                                Toast.makeText(context, "Please accept terms", Toast.LENGTH_SHORT).show()

                            else -> {
                                userViewModel.register(trimmedEmail, password) { success, message, userId ->
                                    if (success) {
                                        val userModel = UserModel(userId, trimmedName, trimmedEmail, password)
                                        userViewModel.addUserToDatabase(userId, userModel) { dbSuccess, dbMessage ->
                                            if (dbSuccess) {
                                                Toast.makeText(context, "Signup successful!", Toast.LENGTH_LONG).show()
                                                navController.navigate(Screen.Login.route) {
                                                    popUpTo(Screen.Register.route) { inclusive = true }
                                                }
                                                onSignupSuccess()
                                            } else {
                                                Toast.makeText(context, "Database error: $dbMessage", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Registration failed: $message", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA68C8)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("SIGN UP", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row {
                    Text("Already have an account? ", color = Color.White)
                    TextButton(onClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }) {
                        Text("Login", color = Color.Cyan)
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, color = Color.White, fontSize = 14.sp) },
        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        trailingIcon = {
            if (isPassword) {
                val icon = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = if (passwordVisible) "Hide Password" else "Show Password", tint = Color.White)
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.LightGray,
            cursorColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    )
}