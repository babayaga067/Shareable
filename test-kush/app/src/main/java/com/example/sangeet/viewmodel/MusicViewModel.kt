package com.example.sangeet.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sangeet.model.MusicModel
import com.example.sangeet.repository.MusicRepository

class MusicViewModel(private val repo: MusicRepository) : ViewModel() {

    // LiveData for all musics
    private val _allMusics = MutableLiveData<List<MusicModel?>>()
    val allMusics: LiveData<List<MusicModel?>> get() = _allMusics

    // LiveData for single music
    private val _music = MutableLiveData<MusicModel?>()
    val music: LiveData<MusicModel?> get() = _music

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Add a new music
    fun addMusic(music: MusicModel, callback: (Boolean, String) -> Unit) {
        _isLoading.value = true
        repo.addMusic(music) { success, message ->
            _isLoading.value = false
            if (success) {
                // Refresh the musics list after successful addition
                getAllMusics()
            } else {
                _errorMessage.value = message
            }
            callback(success, message)
        }
    }

    // Update music by ID with new data
    fun updateMusic(
        musicId: String,
        updatedData: Map<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        _isLoading.value = true
        repo.updateMusic(musicId, updatedData) { success, message ->
            _isLoading.value = false
            if (success) {
                // Refresh the musics list after successful update
                getAllMusics()
                // Also refresh the single music if it's the same ID
                getMusicById(musicId)
            } else {
                _errorMessage.value = message
            }
            callback(success, message)
        }
    }

    // Delete music by ID
    fun deleteMusic(musicId: String, callback: (Boolean, String) -> Unit) {
        _isLoading.value = true
        repo.deleteMusic(musicId) { success, message ->
            _isLoading.value = false
            if (success) {
                // Refresh the musics list after successful deletion
                getAllMusics()
                // Clear the single music if it was the deleted one
                _music.value = null
            } else {
                _errorMessage.value = message
            }
            callback(success, message)
        }
    }

    // Get all musics
    fun getAllMusics(callback: ((Boolean, String, List<MusicModel>?) -> Unit)? = null) {
        _isLoading.value = true
        repo.getAllMusics { success, message, musics ->
            _isLoading.value = false
            if (success) {
                _allMusics.postValue(musics ?: emptyList())
            } else {
                _allMusics.postValue(emptyList())
                _errorMessage.value = message
            }
            callback?.invoke(success, message, musics)
        }
    }

    // Get music by ID
    fun getMusicById(
        musicId: String,
        callback: ((Boolean, String, MusicModel?) -> Unit)? = null
    ) {
        _isLoading.value = true
        repo.getMusicById(musicId) { success, message, music ->
            _isLoading.value = false
            if (success) {
                _music.postValue(music)
            } else {
                _music.postValue(null)
                _errorMessage.value = message
            }
            callback?.invoke(success, message, music)
        }
    }
//
//    // Search musics by name or other criteria
//    fun searchMusics(
//        query: String,
//        callback: ((Boolean, String, List<MusicModel>?) -> Unit)? = null
//    ) {
//        _isLoading.value = true
//        repo.searchMusics(query) { success, message, musics ->
//            _isLoading.value = false
//            if (success) {
//                _allMusics.postValue(musics ?: emptyList())
//            } else {
//                _allMusics.postValue(emptyList())
//                _errorMessage.value = message
//            }
//            callback?.invoke(success, message, musics)
//        }
//    }
//
//    // Get musics by category
//    fun getMusicsByCategory(
//        category: String,
//        callback: ((Boolean, String, List<MusicModel>?) -> Unit)? = null
//    ) {
//        _isLoading.value = true
//        repo.getMusicsByCategory(category) { success, message, musics ->
//            _isLoading.value = false
//            if (success) {
//                _allMusics.postValue(musics ?: emptyList())
//            } else {
//                _allMusics.postValue(emptyList())
//                _errorMessage.value = message
//            }
//            callback?.invoke(success, message, musics)
//        }
//    }

//    // Clear error message
//    fun clearErrorMessage() {
//        _errorMessage.value = null
//    }

    // Clear music data
    fun clearMusicData() {
        _music.value = null
    }

    // Clear all musics data
    fun clearAllMusicsData() {
        _allMusics.value = emptyList()
    }

    // Refresh data
    fun refreshData() {
        getAllMusics()
    }

    // Check if musics list is empty
    fun isMusicsEmpty(): Boolean {
        return _allMusics.value?.isEmpty() ?: true
    }

    // Get musics count
    fun getMusicsCount(): Int {
        return _allMusics.value?.size ?: 0
    }
}