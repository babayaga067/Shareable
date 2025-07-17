package com.example.sangeet.repository

import com.example.sangeet.model.ArtistModel
import com.example.sangeet.model.MusicModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ArtistRepositoryImpl : ArtistRepository {
    
    private val artistsRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Artists")
    private val musicsRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Musics")
    
    override fun addArtist(
        artist: ArtistModel,
        callback: (Boolean, String) -> Unit
    ) {
        val artistId = artistsRef.push().key ?: return callback(false, "Failed to generate ID")
        val newArtist = artist.copy(artistId = artistId)
        
        artistsRef.child(artistId).setValue(newArtist)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message ?: "Artist added")
            }
    }
    
    override fun updateArtist(
        artistId: String,
        updatedData: Map<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        artistsRef.child(artistId).updateChildren(updatedData)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message ?: "Artist updated")
            }
    }
    
    override fun deleteArtist(
        artistId: String,
        callback: (Boolean, String) -> Unit
    ) {
        artistsRef.child(artistId).removeValue()
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message ?: "Artist deleted")
            }
    }
    
    override fun getAllArtists(
        callback: (Boolean, String, List<ArtistModel>?) -> Unit
    ) {
        artistsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val artists = mutableListOf<ArtistModel>()
                for (child in snapshot.children) {
                    child.getValue(ArtistModel::class.java)?.let { artists.add(it) }
                }
                callback(true, "Artists retrieved", artists)
            }
            
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }
    
    override fun getArtistById(
        artistId: String,
        callback: (Boolean, String, ArtistModel?) -> Unit
    ) {
        artistsRef.child(artistId).get()
            .addOnSuccessListener { snapshot ->
                val artist = snapshot.getValue(ArtistModel::class.java)
                callback(true, "Artist retrieved", artist)
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message ?: "Failed to get artist", null)
            }
    }
    
    override fun getArtistMusics(
        artistId: String,
        callback: (Boolean, String, List<MusicModel>?) -> Unit
    ) {
        musicsRef.orderByChild("artistId").equalTo(artistId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val musics = mutableListOf<MusicModel>()
                    for (child in snapshot.children) {
                        child.getValue(MusicModel::class.java)?.let { musics.add(it) }
                    }
                    callback(true, "Artist musics retrieved", musics)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }
    
    override fun searchArtists(
        query: String,
        callback: (Boolean, String, List<ArtistModel>?) -> Unit
    ) {
        artistsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val artists = mutableListOf<ArtistModel>()
                for (child in snapshot.children) {
                    val artist = child.getValue(ArtistModel::class.java)
                    if (artist != null && artist.artistName.contains(query, ignoreCase = true)) {
                        artists.add(artist)
                    }
                }
                callback(true, "Search results retrieved", artists)
            }
            
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }
    
    override fun followArtist(
        artistId: String,
        callback: (Boolean, String) -> Unit
    ) {
        getArtistById(artistId) { success, message, artist ->
            if (!success || artist == null) {
                callback(false, "Artist not found")
                return@getArtistById
            }
            
            val updatedFollowers = artist.followersCount + 1
            updateArtist(artistId, mapOf("followersCount" to updatedFollowers)) { updateSuccess, updateMessage ->
                callback(updateSuccess, if (updateSuccess) "Artist followed" else updateMessage)
            }
        }
    }
}