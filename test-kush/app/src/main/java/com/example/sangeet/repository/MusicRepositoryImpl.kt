package com.example.sangeet.repository

import com.example.sangeet.model.MusicModel
import com.google.firebase.database.*

class MusicRepositoryImpl : MusicRepository {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.reference.child("Musics")

    override fun addMusic(music: MusicModel, callback: (Boolean, String) -> Unit) {
        val musicId = if (music.musicId.isEmpty()) ref.push().key ?: "" else music.musicId
        if (musicId.isEmpty()) {
            callback(false, "Failed to generate music ID")
            return
        }
        
        val newMusic = music.copy(musicId = musicId)
        ref.child(musicId).setValue(newMusic)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message ?: "Music added successfully!")
            }
    }


    override fun updateMusic(
        musicId: String,
        updatedData: Map<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(musicId).updateChildren(updatedData).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Music updated successfully!")
            } else {
                callback(false, it.exception?.message ?: "Failed to update music.")
            }
        }
    }

    override fun deleteMusic(
        musicId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(musicId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Music deleted successfully!")
            } else {
                callback(false, it.exception?.message ?: "Failed to delete music.")
            }
        }
    }

    override fun getAllMusics(callback: (Boolean, String, List<MusicModel>?) -> Unit) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allMusics = mutableListOf<MusicModel>()
                for (eachMusic in snapshot.children) {
                    val music = eachMusic.getValue(MusicModel::class.java)
                    if (music != null) {
                        allMusics.add(music)
                    }
                }
                callback(true, "Musics fetched successfully", allMusics)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getMusicById(
        musicId: String,
        callback: (Boolean, String, MusicModel?) -> Unit
    ) {
        ref.child(musicId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val music = snapshot.getValue(MusicModel::class.java)
                if (music != null) {
                    callback(true, "Music fetched successfully!", music)
                } else {
                    callback(false, "Music not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }
    fun getRecentlyPlayed(userId: String, callback: (Boolean, String, List<MusicModel>?) -> Unit) {
        ref.orderByChild("playCount")
            .limitToLast(10)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val recent = mutableListOf<MusicModel>()
                    for (child in snapshot.children) {
                        val music = child.getValue(MusicModel::class.java)
                        if (music != null) recent.add(music)
                    }
                    callback(true, "Fetched recently played", recent.reversed()) // Most recent first
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }
}
