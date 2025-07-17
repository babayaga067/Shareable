package com.example.sangeet.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sangeet.model.ArtistModel
import com.example.sangeet.model.MusicModel
import com.example.sangeet.repository.ArtistRepository

class ArtistViewModel(private val repo: ArtistRepository) : ViewModel() {

    private val _allArtists = MutableLiveData<List<ArtistModel>>()
    val allArtists: LiveData<List<ArtistModel>> get() = _allArtists

    private val _currentArtist = MutableLiveData<ArtistModel?>()
    val currentArtist: LiveData<ArtistModel?> get() = _currentArtist

    private val _artistMusics = MutableLiveData<List<MusicModel>>()
    val artistMusics: LiveData<List<MusicModel>> get() = _artistMusics

    private val _searchResults = MutableLiveData<List<ArtistModel>>()
    val searchResults: LiveData<List<ArtistModel>> get() = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun addArtist(
        artist: ArtistModel,
        callback: (Boolean, String) -> Unit
    ) {
        _isLoading.value = true
        repo.addArtist(artist) { success, message ->
            _isLoading.value = false
            if (success) {
                getAllArtists()
            }
            callback(success, message)
        }
    }

    fun updateArtist(
        artistId: String,
        updatedData: Map<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        _isLoading.value = true
        repo.updateArtist(artistId, updatedData) { success, message ->
            _isLoading.value = false
            if (success) {
                getArtistById(artistId)
                getAllArtists()
            }
            callback(success, message)
        }
    }

    fun deleteArtist(
        artistId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _isLoading.value = true
        repo.deleteArtist(artistId) { success, message ->
            _isLoading.value = false
            if (success) {
                getAllArtists()
                _currentArtist.value = null
            }
            callback(success, message)
        }
    }

    fun getAllArtists() {
        _isLoading.value = true
        repo.getAllArtists { success, message, artists ->
            _isLoading.value = false
            if (success) {
                _allArtists.postValue(artists ?: emptyList())
            }
        }
    }

    fun getArtistById(artistId: String) {
        _isLoading.value = true
        repo.getArtistById(artistId) { success, message, artist ->
            _isLoading.value = false
            if (success) {
                _currentArtist.postValue(artist)
            }
        }
    }

    fun getArtistMusics(artistId: String) {
        _isLoading.value = true
        repo.getArtistMusics(artistId) { success, message, musics ->
            _isLoading.value = false
            if (success) {
                _artistMusics.postValue(musics ?: emptyList())
            }
        }
    }

    fun searchArtists(query: String) {
        _isLoading.value = true
        repo.searchArtists(query) { success, message, artists ->
            _isLoading.value = false
            if (success) {
                _searchResults.postValue(artists ?: emptyList())
            }
        }
    }

    fun followArtist(
        artistId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _isLoading.value = true
        repo.followArtist(artistId) { success, message ->
            _isLoading.value = false
            if (success) {
                getArtistById(artistId)
                getAllArtists()
            }
            callback(success, message)
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    fun clearCurrentArtist() {
        _currentArtist.value = null
        _artistMusics.value = emptyList()
    }
}