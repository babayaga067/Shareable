package com.example.sangeet.model

data class UserModel(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val profileImageUrl: String = "",
    val bio: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),

    // Relationships - Lists of IDs referencing other entities
    val uploadedMusicIds: List<String> = emptyList(),
    val favoriteIds: List<String> = emptyList(),
    val playlistIds: List<String> = emptyList(),
    val followedArtistIds: List<String> = emptyList(),
    val followerIds: List<String> = emptyList(),

    // User preferences and stats
    val totalPlaysCount: Int = 0,
    val totalUploads: Int = 0,
    val isArtist: Boolean = false,
    val artistId: String = "",
    val isVerified: Boolean = false
)
