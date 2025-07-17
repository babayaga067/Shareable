package com.example.sangeet.model

data class FavoriteModel(
    val favoriteId: String = "",            // Keep only if you plan to manage IDs manually
    val userId: String = "",
    val musicId: String = "",
    val addedAt: Long = System.currentTimeMillis(),

    // Optional user metadata
    val playCount: Int = 0,
    val lastPlayedAt: Long? = null,         // Nullable if not always tracked
    val rating: Float? = null,              // Optional rating (0.0 - 5.0)
    val notes: String? = null               // Optional personal notes
)

data class UserFollowArtistModel(
    val followId: String = "",
    val userId: String = "",
    val artistId: String = "",
    val followedAt: Long = System.currentTimeMillis(),
    val notificationsEnabled: Boolean = true
)
