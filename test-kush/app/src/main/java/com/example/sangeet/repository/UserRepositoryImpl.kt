package com.example.sangeet.repository

import com.example.sangeet.model.UserModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserRepositoryImpl : UserRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

    // 🔐 Authentication
    override fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            callback(task.isSuccessful, task.exception?.message ?: "Login successful")
        }
    }

    override fun register(email: String, password: String, callback: (Boolean, String, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            val uid = auth.currentUser?.uid.orEmpty()
            if (task.isSuccessful) {
                callback(true, "Registration successful", uid)
            } else {
                callback(false, task.exception?.message ?: "Registration failed", "")
            }
        }
    }

    override fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            callback(task.isSuccessful, task.exception?.message ?: "Reset link sent")
        }
    }

    override fun updatePassword(userId: String, oldPassword: String, newPassword: String, callback: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        val email = user?.email
        if (user == null || email.isNullOrEmpty()) {
            callback(false, "User not logged in")
            return
        }

        val credential = EmailAuthProvider.getCredential(email, oldPassword)
        user.reauthenticate(credential).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    callback(updateTask.isSuccessful, updateTask.exception?.message ?: "Password updated")
                }
            } else {
                callback(false, authTask.exception?.message ?: "Re-authentication failed")
            }
        }
    }

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override fun logout(callback: () -> Unit) {
        auth.signOut()
        callback()
    }

    // 🗂️ Profile Management
    override fun addUserToDatabase(userId: String, userModel: UserModel, callback: (Boolean, String) -> Unit) {
        ref.child(userId).setValue(userModel).addOnCompleteListener { task ->
            callback(task.isSuccessful, task.exception?.message ?: "User added")
        }
    }

    override fun updateProfile(userId: String, updates: Map<String, Any?>, callback: (Boolean, String) -> Unit) {
        ref.child(userId).updateChildren(updates).addOnCompleteListener { task ->
            callback(task.isSuccessful, task.exception?.message ?: "Profile updated")
        }
    }

    override fun getUserById(userId: String, callback: (Boolean, String, UserModel?) -> Unit) {
        ref.child(userId).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(UserModel::class.java)
                callback(true, "User retrieved successfully", user)
            }
            .addOnFailureListener {
                callback(false, it.message ?: "Failed to retrieve user", null)
            }
    }

    // 🎧 Favorites, Playlists, Following
    private fun updateStringListField(userId: String, field: String, value: String, add: Boolean, callback: (Boolean, String) -> Unit) {
        ref.child(userId).child(field).get().addOnSuccessListener { snapshot ->
            val currentList = (snapshot.value as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val updatedList = currentList.toMutableList().apply {
                if (add) add(value) else remove(value)
            }
            ref.child(userId).child(field).setValue(updatedList).addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message ?: "$field updated")
            }
        }.addOnFailureListener {
            callback(false, it.message ?: "Failed to update $field")
        }
    }

    override fun addFavoriteToUser(userId: String, favoriteId: String, callback: (Boolean, String) -> Unit) {
        updateStringListField(userId, "favoriteIds", favoriteId, true, callback)
    }

    override fun removeFavoriteFromUser(userId: String, favoriteId: String, callback: (Boolean, String) -> Unit) {
        updateStringListField(userId, "favoriteIds", favoriteId, false, callback)
    }

    override fun addPlaylistToUser(userId: String, playlistId: String, callback: (Boolean, String) -> Unit) {
        updateStringListField(userId, "playlistIds", playlistId, true, callback)
    }

    override fun removePlaylistFromUser(userId: String, playlistId: String, callback: (Boolean, String) -> Unit) {
        updateStringListField(userId, "playlistIds", playlistId, false, callback)
    }

    override fun addFollowedArtistToUser(userId: String, artistId: String, callback: (Boolean, String) -> Unit) {
        updateStringListField(userId, "followedArtistIds", artistId, true, callback)
    }

    override fun removeFollowedArtistFromUser(userId: String, artistId: String, callback: (Boolean, String) -> Unit) {
        updateStringListField(userId, "followedArtistIds", artistId, false, callback)
    }

    override fun addUploadedMusicToUser(userId: String, musicId: String, callback: (Boolean, String) -> Unit) {
        ref.child(userId).child("uploadedMusicIds").get().addOnSuccessListener { snapshot ->
            val current = (snapshot.value as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val updated = current.toMutableList().apply { add(musicId) }
            val updates = mapOf("uploadedMusicIds" to updated, "totalUploads" to updated.size)
            ref.child(userId).updateChildren(updates).addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message ?: "Music upload recorded")
            }
        }.addOnFailureListener {
            callback(false, it.message ?: "Failed to record music upload")
        }
    }

    // 📊 Stats
    override fun incrementUserPlayCount(userId: String, callback: (Boolean, String) -> Unit) {
        ref.child(userId).child("totalPlaysCount").get().addOnSuccessListener { snapshot ->
            val current = snapshot.getValue(Int::class.java) ?: 0
            ref.child(userId).child("totalPlaysCount").setValue(current + 1).addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message ?: "Play count updated")
            }
        }.addOnFailureListener {
            callback(false, it.message ?: "Failed to update play count")
        }
    }

    override fun incrementUserUploadCount(userId: String, callback: (Boolean, String) -> Unit) {
        ref.child(userId).child("totalUploads").get().addOnSuccessListener { snapshot ->
            val current = snapshot.getValue(Int::class.java) ?: 0
            ref.child(userId).child("totalUploads").setValue(current + 1).addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message ?: "Upload count updated")
            }
        }.addOnFailureListener {
            callback(false, it.message ?: "Failed to update upload count")
        }
    }

    override fun updateLastLoginTime(userId: String, callback: (Boolean, String) -> Unit) {
        ref.child(userId).child("lastLoginAt").setValue(System.currentTimeMillis()).addOnCompleteListener { task ->
            callback(task.isSuccessful, task.exception?.message ?: "Last login updated")
        }
    }

    // 🔎 Discovery
    override fun searchUsers(query: String, callback: (List<UserModel>) -> Unit) {
        ref.orderByChild("fullName").startAt(query).endAt(query + "\uf8ff").get().addOnSuccessListener { snapshot ->
            val results = snapshot.children.mapNotNull { it.getValue(UserModel::class.java) }
            callback(results)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }

    override fun getUsersByIds(userIds: List<String>, callback: (List<UserModel>) -> Unit) {
        if (userIds.isEmpty()) {
            callback(emptyList())
            return
        }

        val results = mutableListOf<UserModel>()
        var completed = 0

        userIds.forEach { id ->
            ref.child(id).get().addOnSuccessListener { snapshot ->
                snapshot.getValue(UserModel::class.java)?.let { results.add(it) }
                completed++
                if (completed == userIds.size) callback(results)
            }.addOnFailureListener {
                completed++
                if (completed == userIds.size) callback(results)
            }
        }
    }
}