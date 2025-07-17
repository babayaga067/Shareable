package com.example.sangeet.repository

import com.example.sangeet.model.FavoriteModel
import com.example.sangeet.model.MusicModel

interface FavoriteRepository {
    
    fun addToFavorites(
        userId: String,
        musicId: String,
        callback: (Boolean, String) -> Unit
    )
    
    fun removeFromFavorites(
        userId: String,
        musicId: String,
        callback: (Boolean, String) -> Unit
    )
    
    fun getUserFavorites(
        userId: String,
        callback: (Boolean, String, List<FavoriteModel>?) -> Unit
    )
    
    fun getUserFavoriteMusics(
        userId: String,
        callback: (Boolean, String, List<MusicModel>?) -> Unit
    )
    
    fun isMusicFavorite(
        userId: String,
        musicId: String,
        callback: (Boolean) -> Unit
    )
}