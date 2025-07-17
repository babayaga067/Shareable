package com.example.sangeet.model

data class MusicModel(
    var musicId: String = "",
    var musicName: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var audioUrl: String = "",
    var artistId: String = "",
    var artistName: String = "",
    var duration: Long = 0,
    var genre: String = "",
    var uploadedBy: String = "",
    var uploadedAt: Long = System.currentTimeMillis(),
    var playCount: Int = 0
)