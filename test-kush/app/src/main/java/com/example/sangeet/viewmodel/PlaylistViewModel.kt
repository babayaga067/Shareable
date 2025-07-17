package com.example.sangeet.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sangeet.model.PlaylistModel
import com.example.sangeet.model.MusicModel
import com.example.sangeet.repository.PlaylistRepository

class PlaylistViewModel(private val repo: PlaylistRepository) : ViewModel() {
    
    private val _userPlaylists = MutableLiveData<List<PlaylistModel>>()
    val userPlaylists: LiveData<List<PlaylistModel>> get() = _userPlaylists
    
    private val _currentPlaylist = MutableLiveData<PlaylistModel?>()
    val currentPlaylist: LiveData<PlaylistModel?> get() = _currentPlaylist
    
    private val _playlistMusics = MutableLiveData<List<MusicModel>>()
    val playlistMusics: LiveData<List<MusicModel>> get() = _playlistMusics
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    
    fun createPlaylist(
        playlist: PlaylistModel,
        callback: (Boolean, String) -> Unit
    ) {
        _isLoading.value = true
        repo.createPlaylist(playlist) { success, message ->
            _isLoading.value = false
            if (success) {
                getUserPlaylists(playlist.userId)
            }
            callback(success, message)
        }
    }
    
    fun updatePlaylist(
        playlistId: String,
        updatedData: Map<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        _isLoading.value = true
        repo.updatePlaylist(playlistId, updatedData) { success, message ->
            _isLoading.value = false
            if (success) {
                getPlaylistById(playlistId)
            }
            callback(success, message)
        }
    }
    
    fun deletePlaylist(
        playlistId: String,
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _isLoading.value = true
        repo.deletePlaylist(playlistId) { success, message ->
            _isLoading.value = false
            if (success) {
                getUserPlaylists(userId)
                _currentPlaylist.value = null
            }
            callback(success, message)
        }
    }
    
    fun getUserPlaylists(userId: String) {
        _isLoading.value = true
        repo.getUserPlaylists(userId) { success, message, playlists ->
            _isLoading.value = false
            if (success) {
                _userPlaylists.postValue(playlists ?: emptyList())
            }
        }
    }
    
    fun getPlaylistById(playlistId: String) {
        _isLoading.value = true
        repo.getPlaylistById(playlistId) { success, message, playlist ->
            _isLoading.value = false
            if (success) {
                _currentPlaylist.postValue(playlist)
            }
        }
    }
    
    fun addMusicToPlaylist(
        playlistId: String,
        musicId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _isLoading.value = true
        repo.addMusicToPlaylist(playlistId, musicId) { success, message ->
            _isLoading.value = false
            if (success) {
                getPlaylistById(playlistId)
                getPlaylistMusics(playlistId)
            }
            callback(success, message)
        }
    }
    
    fun removeMusicFromPlaylist(
        playlistId: String,
        musicId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _isLoading.value = true
        repo.removeMusicFromPlaylist(playlistId, musicId) { success, message ->
            _isLoading.value = false
            if (success) {
                getPlaylistById(playlistId)
                getPlaylistMusics(playlistId)
            }
            callback(success, message)
        }
    }
    
    fun getPlaylistMusics(playlistId: String) {
        _isLoading.value = true
        repo.getPlaylistMusics(playlistId) { success, message, musics ->
            _isLoading.value = false
            if (success) {
                _playlistMusics.postValue(musics ?: emptyList())
            }
        }
    }
    
    fun clearCurrentPlaylist() {
        _currentPlaylist.value = null
        _playlistMusics.value = emptyList()
    }
}