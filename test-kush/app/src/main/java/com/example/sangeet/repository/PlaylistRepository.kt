package com.example.sangeet.repository

import com.example.sangeet.model.PlaylistModel
import com.example.sangeet.model.MusicModel

interface PlaylistRepository {
    
    fun createPlaylist(
        playlist: PlaylistModel,
        callback: (Boolean, String) -> Unit
    )
    
    fun updatePlaylist(
        playlistId: String,
        updatedData: Map<String, Any?>,
        callback: (Boolean, String) -> Unit
    )
    
    fun deletePlaylist(
        playlistId: String,
        callback: (Boolean, String) -> Unit
    )
    
    fun getUserPlaylists(
        userId: String,
        callback: (Boolean, String, List<PlaylistModel>?) -> Unit
    )
    
    fun getPlaylistById(
        playlistId: String,
        callback: (Boolean, String, PlaylistModel?) -> Unit
    )
    
    fun addMusicToPlaylist(
        playlistId: String,
        musicId: String,
        callback: (Boolean, String) -> Unit
    )
    
    fun removeMusicFromPlaylist(
        playlistId: String,
        musicId: String,
        callback: (Boolean, String) -> Unit
    )
    
    fun getPlaylistMusics(
        playlistId: String,
        callback: (Boolean, String, List<MusicModel>?) -> Unit
    )
}