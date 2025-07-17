package com.example.sangeet.repository

import com.example.sangeet.model.ArtistModel
import com.example.sangeet.model.MusicModel

interface ArtistRepository {
    
    fun addArtist(
        artist: ArtistModel,
        callback: (Boolean, String) -> Unit
    )
    
    fun updateArtist(
        artistId: String,
        updatedData: Map<String, Any?>,
        callback: (Boolean, String) -> Unit
    )
    
    fun deleteArtist(
        artistId: String,
        callback: (Boolean, String) -> Unit
    )
    
    fun getAllArtists(
        callback: (Boolean, String, List<ArtistModel>?) -> Unit
    )
    
    fun getArtistById(
        artistId: String,
        callback: (Boolean, String, ArtistModel?) -> Unit
    )
    
    fun getArtistMusics(
        artistId: String,
        callback: (Boolean, String, List<MusicModel>?) -> Unit
    )
    
    fun searchArtists(
        query: String,
        callback: (Boolean, String, List<ArtistModel>?) -> Unit
    )
    
    fun followArtist(
        artistId: String,
        callback: (Boolean, String) -> Unit
    )
}