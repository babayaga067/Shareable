package com.example.sangeet.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showCurrent) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showCurrent = !showCurrent }) {
                            Icon(
                                imageVector = if (showCurrent) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFFE91E63),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNew = !showNew }) {
                            Icon(
                                imageVector = if (showNew) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFFE91E63),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirm = !showConfirm }) {
                            Icon(
                                imageVector = if (showConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFFE91E63),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newPassword == confirmPassword && newPassword.isNotEmpty()) {
                        onSave(currentPassword, newPassword)
                    }
                },
                enabled = newPassword == confirmPassword && newPassword.isNotEmpty() && currentPassword.isNotEmpty()
            ) {
                Text("Change", color = Color(0xFFE91E63))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = Color(0xFF2A2A3E)
    )
}