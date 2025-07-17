package com.example.sangeet.repository

import com.example.sangeet.model.MusicModel

interface MusicRepository {

    // Add a new music
    fun addMusic(
        music: MusicModel,
        callback: (Boolean, String) -> Unit
    )

    // Update music by id with new data
    fun updateMusic(
        musicId: String,
        updatedData: Map<String, Any?>,
        callback: (Boolean, String) -> Unit
    )

    // Delete music by id
    fun deleteMusic(
        musicId: String,
        callback: (Boolean, String) -> Unit
    )

    // Get all musics (asynchronously)
    fun getAllMusics(callback: (Boolean, String, List<MusicModel>?) -> Unit)

    // Get music by ID
    fun getMusicById(
        musicId: String,
        callback: (Boolean, String, MusicModel?) -> Unit
    )
}
