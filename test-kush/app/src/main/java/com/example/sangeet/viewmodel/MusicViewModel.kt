package com.example.sangeet.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sangeet.model.MusicModel
import com.example.sangeet.repository.MusicRepository
import kotlinx.coroutines.launch
import android.util.Log

class MusicViewModel(private val repo: MusicRepository) : ViewModel() {

    private val _allMusics = MutableLiveData<List<MusicModel>>()
    val allMusics: LiveData<List<MusicModel>> get() = _allMusics

    private val _music = MutableLiveData<MusicModel?>()
    val music: LiveData<MusicModel?> get() = _music

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun addMusic(music: MusicModel, callback: (Boolean, String) -> Unit) {
        if (music.musicId.isEmpty()) {
            callback(false, "Invalid music data")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                repo.addMusic(music) { success, message ->
                    _isLoading.value = false
                    if (success) {
                        getAllMusics()
                    } else {
                        _errorMessage.value = message
                    }
                    callback(success, message)
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMsg = "Error adding music: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e("MusicViewModel", errorMsg, e)
                callback(false, errorMsg)
            }
        }
    }

    fun updateMusic(
        musicId: String,
        updatedData: Map<String, Any>,
        callback: (Boolean, String) -> Unit
    ) {
        if (musicId.isEmpty()) {
            callback(false, "Invalid music ID")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                repo.updateMusic(musicId, updatedData) { success, message ->
                    _isLoading.value = false
                    if (success) {
                        getAllMusics()
                        getMusicById(musicId)
                    } else {
                        _errorMessage.value = message
                    }
                    callback(success, message)
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMsg = "Error updating music: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e("MusicViewModel", errorMsg, e)
                callback(false, errorMsg)
            }
        }
    }

    fun deleteMusic(musicId: String, callback: (Boolean, String) -> Unit) {
        if (musicId.isEmpty()) {
            callback(false, "Invalid music ID")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                repo.deleteMusic(musicId) { success, message ->
                    _isLoading.value = false
                    if (success) {
                        getAllMusics()
                        _music.value = null
                    } else {
                        _errorMessage.value = message
                    }
                    callback(success, message)
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMsg = "Error deleting music: ${e.message}"
                _errorMessage.value = errorMsg
                Log.e("MusicViewModel", errorMsg, e)
                callback(false, errorMsg)
            }
        }
    }

    fun getAllMusics(callback: ((Boolean, String, List<MusicModel>?) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

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
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMsg = "Error loading musics: ${e.message}"
                _errorMessage.value = errorMsg
                _allMusics.postValue(emptyList())
                Log.e("MusicViewModel", errorMsg, e)
                callback?.invoke(false, errorMsg, null)
            }
        }
    }

    fun getMusicById(
        musicId: String,
        callback: ((Boolean, String, MusicModel?) -> Unit)? = null
    ) {
        if (musicId.isEmpty()) {
            callback?.invoke(false, "Invalid music ID", null)
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

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
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMsg = "Error loading music: ${e.message}"
                _errorMessage.value = errorMsg
                _music.postValue(null)
                Log.e("MusicViewModel", errorMsg, e)
                callback?.invoke(false, errorMsg, null)
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearMusicData() {
        _music.value = null
    }

    fun clearAllMusicsData() {
        _allMusics.value = emptyList()
    }

    fun refreshData() {
        getAllMusics()
    }

    fun isMusicsEmpty(): Boolean {
        return _allMusics.value?.isEmpty() ?: true
    }

    fun getMusicsCount(): Int {
        return _allMusics.value?.size ?: 0
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("MusicViewModel", "ViewModel cleared")
    }
}
