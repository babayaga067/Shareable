package com.example.sangeet.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sangeet.model.UserModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    currentUser: UserModel?,
    onDismiss: () -> Unit,
    onSave: (UserModel) -> Unit
) {
    var fullName by remember(currentUser?.fullName) {
        mutableStateOf(currentUser?.fullName.orEmpty())
    }

    val email by remember(currentUser?.email) {
        mutableStateOf(currentUser?.email.orEmpty())
    }

    val isValid by remember(fullName) {
        derivedStateOf { fullName.trim().isNotEmpty() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFFE91E63),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = {},
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.White.copy(alpha = 0.5f),
                        disabledLabelColor = Color.White.copy(alpha = 0.5f)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    currentUser?.let {
                        onSave(it.copy(fullName = fullName.trim()))
                    }
                },
                enabled = isValid
            ) {
                Text("Save", color = if (isValid) Color(0xFFE91E63) else Color.Gray)
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