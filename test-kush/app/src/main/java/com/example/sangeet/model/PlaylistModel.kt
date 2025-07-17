package com.example.sangeet.model

data class PlaylistModel(
    val playlistId: String = "",
    val userId: String = "",
    val playlistName: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val musicIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isPublic: Boolean = false
)
