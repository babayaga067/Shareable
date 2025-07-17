package com.example.sangeet.repository

import com.example.sangeet.model.PlaylistModel
import com.example.sangeet.model.MusicModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PlaylistRepositoryImpl : PlaylistRepository {
    
    private val playlistsRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Playlists")
    private val musicsRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Musics")
    
    override fun createPlaylist(
        playlist: PlaylistModel,
        callback: (Boolean, String) -> Unit
    ) {
        val playlistId = playlistsRef.push().key ?: return callback(false, "Failed to generate ID")
        val newPlaylist = playlist.copy(playlistId = playlistId)
        
        playlistsRef.child(playlistId).setValue(newPlaylist)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message ?: "Playlist created")
            }
    }
    
    override fun updatePlaylist(
        playlistId: String,
        updatedData: Map<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        playlistsRef.child(playlistId).updateChildren(updatedData)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message ?: "Playlist updated")
            }
    }
    
    override fun deletePlaylist(
        playlistId: String,
        callback: (Boolean, String) -> Unit
    ) {
        playlistsRef.child(playlistId).removeValue()
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message ?: "Playlist deleted")
            }
    }
    
    override fun getUserPlaylists(
        userId: String,
        callback: (Boolean, String, List<PlaylistModel>?) -> Unit
    ) {
        playlistsRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val playlists = mutableListOf<PlaylistModel>()
                    for (child in snapshot.children) {
                        child.getValue(PlaylistModel::class.java)?.let { playlists.add(it) }
                    }
                    callback(true, "Playlists retrieved", playlists)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }
    
    override fun getPlaylistById(
        playlistId: String,
        callback: (Boolean, String, PlaylistModel?) -> Unit
    ) {
        playlistsRef.child(playlistId).get()
            .addOnSuccessListener { snapshot ->
                val playlist = snapshot.getValue(PlaylistModel::class.java)
                callback(true, "Playlist retrieved", playlist)
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message ?: "Failed to get playlist", null)
            }
    }
    
    override fun addMusicToPlaylist(
        playlistId: String,
        musicId: String,
        callback: (Boolean, String) -> Unit
    ) {
        getPlaylistById(playlistId) { success, message, playlist ->
            if (!success || playlist == null) {
                callback(false, "Playlist not found")
                return@getPlaylistById
            }
            
            val updatedMusicIds = playlist.musicIds.toMutableList()
            if (!updatedMusicIds.contains(musicId)) {
                updatedMusicIds.add(musicId)
                updatePlaylist(playlistId, mapOf("musicIds" to updatedMusicIds), callback)
            } else {
                callback(false, "Music already in playlist")
            }
        }
    }
    
    override fun removeMusicFromPlaylist(
        playlistId: String,
        musicId: String,
        callback: (Boolean, String) -> Unit
    ) {
        getPlaylistById(playlistId) { success, message, playlist ->
            if (!success || playlist == null) {
                callback(false, "Playlist not found")
                return@getPlaylistById
            }
            
            val updatedMusicIds = playlist.musicIds.toMutableList()
            if (updatedMusicIds.remove(musicId)) {
                updatePlaylist(playlistId, mapOf("musicIds" to updatedMusicIds), callback)
            } else {
                callback(false, "Music not found in playlist")
            }
        }
    }
    
    override fun getPlaylistMusics(
        playlistId: String,
        callback: (Boolean, String, List<MusicModel>?) -> Unit
    ) {
        getPlaylistById(playlistId) { success, message, playlist ->
            if (!success || playlist == null) {
                callback(false, "Playlist not found", null)
                return@getPlaylistById
            }
            
            if (playlist.musicIds.isEmpty()) {
                callback(true, "No musics in playlist", emptyList())
                return@getPlaylistById
            }
            
            val musics = mutableListOf<MusicModel>()
            var completed = 0
            
            for (musicId in playlist.musicIds) {
                musicsRef.child(musicId).get().addOnSuccessListener { snapshot ->
                    snapshot.getValue(MusicModel::class.java)?.let { musics.add(it) }
                    completed++
                    if (completed == playlist.musicIds.size) {
                        callback(true, "Playlist musics retrieved", musics)
                    }
                }.addOnFailureListener {
                    completed++
                    if (completed == playlist.musicIds.size) {
                        callback(true, "Playlist musics retrieved", musics)
                    }
                }
            }
        }
    }
}