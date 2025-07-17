package com.example.sangeet.repository

import com.example.sangeet.model.FavoriteModel
import com.example.sangeet.model.MusicModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavoriteRepositoryImpl : FavoriteRepository {
    
    private val favoritesRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Favorites")
    private val musicsRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Musics")
    
    override fun addToFavorites(
        userId: String,
        musicId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val favoriteId = favoritesRef.push().key ?: return callback(false, "Failed to generate ID")
        val favorite = FavoriteModel(
            favoriteId = favoriteId,
            userId = userId,
            musicId = musicId
        )
        
        favoritesRef.child(favoriteId).setValue(favorite)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message ?: "Added to favorites")
            }
    }
    
    override fun removeFromFavorites(
        userId: String,
        musicId: String,
        callback: (Boolean, String) -> Unit
    ) {
        favoritesRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var found = false
                    for (child in snapshot.children) {
                        val favorite = child.getValue(FavoriteModel::class.java)
                        if (favorite?.musicId == musicId) {
                            child.ref.removeValue()
                            found = true
                            break
                        }
                    }
                    callback(found, if (found) "Removed from favorites" else "Not found in favorites")
                }
                
                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            })
    }
    
    override fun getUserFavorites(
        userId: String,
        callback: (Boolean, String, List<FavoriteModel>?) -> Unit
    ) {
        favoritesRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favorites = mutableListOf<FavoriteModel>()
                    for (child in snapshot.children) {
                        child.getValue(FavoriteModel::class.java)?.let { favorites.add(it) }
                    }
                    callback(true, "Favorites retrieved", favorites)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }
    
    override fun getUserFavoriteMusics(
        userId: String,
        callback: (Boolean, String, List<MusicModel>?) -> Unit
    ) {
        getUserFavorites(userId) { success, message, favorites ->
            if (!success || favorites.isNullOrEmpty()) {
                callback(success, message, emptyList())
                return@getUserFavorites
            }
            
            val musicIds = favorites.map { it.musicId }
            val musics = mutableListOf<MusicModel>()
            var completed = 0
            
            for (musicId in musicIds) {
                musicsRef.child(musicId).get().addOnSuccessListener { snapshot ->
                    snapshot.getValue(MusicModel::class.java)?.let { musics.add(it) }
                    completed++
                    if (completed == musicIds.size) {
                        callback(true, "Favorite musics retrieved", musics)
                    }
                }.addOnFailureListener {
                    completed++
                    if (completed == musicIds.size) {
                        callback(true, "Favorite musics retrieved", musics)
                    }
                }
            }
        }
    }
    
    override fun isMusicFavorite(
        userId: String,
        musicId: String,
        callback: (Boolean) -> Unit
    ) {
        favoritesRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var isFavorite = false
                    for (child in snapshot.children) {
                        val favorite = child.getValue(FavoriteModel::class.java)
                        if (favorite?.musicId == musicId) {
                            isFavorite = true
                            break
                        }
                    }
                    callback(isFavorite)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }
}