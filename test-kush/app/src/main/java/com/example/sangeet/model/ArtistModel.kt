package com.example.sangeet.model

data class ArtistModel(
    val artistId: String = "",
    val artistName: String = "",
    val bio: String = "",
    val imageUrl: String = "",
    val genre: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    
    // Relationships
    val musicIds: List<String> = emptyList(),
    val followerIds: List<String> = emptyList(),
    val albumIds: List<String> = emptyList(),
    
    // Artist stats and info
    val followersCount: Int = 0,
    val totalPlays: Int = 0,
    val monthlyListeners: Int = 0,
    val isVerified: Boolean = false,
    val socialLinks: Map<String, String> = emptyMap(),
    val country: String = "",
    val debutYear: Int = 0
)