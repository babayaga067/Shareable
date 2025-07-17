package com.example.sangeet.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sangeet.model.FavoriteModel
import com.example.sangeet.model.MusicModel
import com.example.sangeet.repository.FavoriteRepository
import kotlinx.coroutines.launch
import android.util.Log

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
        if (userId.isEmpty() || musicId.isEmpty()) {
            callback(false, "Invalid user or music ID")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                repo.addToFavorites(userId, musicId) { success, message ->
                    _isLoading.value = false
                    if (success) {
                        getUserFavoriteMusics(userId)
                    }
                    callback(success, message)
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMsg = "Error adding to favorites: ${e.message}"
                Log.e("FavoriteViewModel", errorMsg, e)
                callback(false, errorMsg)
            }
        }
    }

    fun removeFromFavorites(
        userId: String,
        musicId: String,
        callback: (Boolean, String) -> Unit
    ) {
        if (userId.isEmpty() || musicId.isEmpty()) {
            callback(false, "Invalid user or music ID")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                repo.removeFromFavorites(userId, musicId) { success, message ->
                    _isLoading.value = false
                    if (success) {
                        getUserFavoriteMusics(userId)
                    }
                    callback(success, message)
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMsg = "Error removing from favorites: ${e.message}"
                Log.e("FavoriteViewModel", errorMsg, e)
                callback(false, errorMsg)
            }
        }
    }

    fun getUserFavorites(userId: String) {
        if (userId.isEmpty()) {
            _favorites.postValue(emptyList())
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                repo.getUserFavorites(userId) { success, message, favorites ->
                    _isLoading.value = false
                    if (success) {
                        _favorites.postValue(favorites ?: emptyList())
                    } else {
                        _favorites.postValue(emptyList())
                        Log.e("FavoriteViewModel", "Error getting favorites: $message")
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _favorites.postValue(emptyList())
                Log.e("FavoriteViewModel", "Error getting user favorites: ${e.message}", e)
            }
        }
    }

    fun getUserFavoriteMusics(userId: String) {
        if (userId.isEmpty()) {
            _favoriteMusics.postValue(emptyList())
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                repo.getUserFavoriteMusics(userId) { success, message, musics ->
                    _isLoading.value = false
                    if (success) {
                        _favoriteMusics.postValue(musics ?: emptyList())
                    } else {
                        _favoriteMusics.postValue(emptyList())
                        Log.e("FavoriteViewModel", "Error getting favorite musics: $message")
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _favoriteMusics.postValue(emptyList())
                Log.e("FavoriteViewModel", "Error getting user favorite musics: ${e.message}", e)
            }
        }
    }

    fun checkIfMusicIsFavorite(userId: String, musicId: String) {
        if (userId.isEmpty() || musicId.isEmpty()) {
            _isFavorite.postValue(false)
            return
        }

        viewModelScope.launch {
            try {
                repo.isMusicFavorite(userId, musicId) { isFav ->
                    _isFavorite.postValue(isFav)
                }
            } catch (e: Exception) {
                _isFavorite.postValue(false)
                Log.e("FavoriteViewModel", "Error checking favorite status: ${e.message}", e)
            }
        }
    }

    fun toggleFavorite(
        userId: String,
        musicId: String,
        callback: (Boolean, String) -> Unit
    ) {
        if (userId.isEmpty() || musicId.isEmpty()) {
            callback(false, "Invalid user or music ID")
            return
        }

        viewModelScope.launch {
            try {
                repo.isMusicFavorite(userId, musicId) { isFav ->
                    if (isFav) {
                        removeFromFavorites(userId, musicId, callback)
                    } else {
                        addToFavorites(userId, musicId, callback)
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Error toggling favorite: ${e.message}"
                Log.e("FavoriteViewModel", errorMsg, e)
                callback(false, errorMsg)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("FavoriteViewModel", "ViewModel cleared")
    }
}
