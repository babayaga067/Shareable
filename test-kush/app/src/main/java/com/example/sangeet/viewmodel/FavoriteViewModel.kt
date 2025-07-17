package com.example.sangeet.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sangeet.model.FavoriteModel
import com.example.sangeet.model.MusicModel
import com.example.sangeet.repository.FavoriteRepository

class FavoriteViewModel(private val repo: FavoriteRepository) : ViewModel() {
    
    private val _favoriteMusics = MutableLiveData<List<MusicModel>>()
    val favoriteMusics: LiveData<List<MusicModel>> get() = _favoriteMusics
    
    private val _favorites = MutableLiveData<List<FavoriteModel>>()
    val favorites: LiveData<List<FavoriteModel>> get() = _favorites
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    
    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> get() = _isFavorite
    
    fun addToFavorites(
        userId: String,
        musicId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _isLoading.value = true
        repo.addToFavorites(userId, musicId) { success, message ->
            _isLoading.value = false
            if (success) {
                getUserFavoriteMusics(userId)
            }
            callback(success, message)
        }
    }
    
    fun removeFromFavorites(
        userId: String,
        musicId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _isLoading.value = true
        repo.removeFromFavorites(userId, musicId) { success, message ->
            _isLoading.value = false
            if (success) {
                getUserFavoriteMusics(userId)
            }
            callback(success, message)
        }
    }
    
    fun getUserFavorites(userId: String) {
        _isLoading.value = true
        repo.getUserFavorites(userId) { success, message, favorites ->
            _isLoading.value = false
            if (success) {
                _favorites.postValue(favorites ?: emptyList())
            }
        }
    }
    
    fun getUserFavoriteMusics(userId: String) {
        _isLoading.value = true
        repo.getUserFavoriteMusics(userId) { success, message, musics ->
            _isLoading.value = false
            if (success) {
                _favoriteMusics.postValue(musics ?: emptyList())
            }
        }
    }
    
    fun checkIfMusicIsFavorite(userId: String, musicId: String) {
        repo.isMusicFavorite(userId, musicId) { isFav ->
            _isFavorite.postValue(isFav)
        }
    }
    
    fun toggleFavorite(
        userId: String,
        musicId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.isMusicFavorite(userId, musicId) { isFav ->
            if (isFav) {
                removeFromFavorites(userId, musicId, callback)
            } else {
                addToFavorites(userId, musicId, callback)
            }
        }
    }
}