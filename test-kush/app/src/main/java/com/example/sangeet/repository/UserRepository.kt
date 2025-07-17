package com.example.sangeet.repository

import com.example.sangeet.model.UserModel
import com.google.firebase.auth.FirebaseUser

interface UserRepository {

    // Authentication
    fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit)

    fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    )
    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit)
    fun updatePassword(
        userId: String,
        oldPassword: String,
        newPassword: String,
        callback: (Boolean, String) -> Unit
    )
    fun logout(callback: () -> Unit)
    fun getCurrentUser(): FirebaseUser?

    // Database - Basic CRUD
    fun addUserToDatabase(userId: String, userModel: UserModel, callback: (Boolean, String) -> Unit)
    fun getUserById(userId: String, callback: (Boolean, String, UserModel?) -> Unit)
    fun updateProfile(userId: String, updates: Map<String, Any?>, callback: (Boolean, String) -> Unit)
    
    // Relationship Management
    fun addFavoriteToUser(userId: String, favoriteId: String, callback: (Boolean, String) -> Unit)
    fun removeFavoriteFromUser(userId: String, favoriteId: String, callback: (Boolean, String) -> Unit)
    fun addPlaylistToUser(userId: String, playlistId: String, callback: (Boolean, String) -> Unit)
    fun removePlaylistFromUser(userId: String, playlistId: String, callback: (Boolean, String) -> Unit)
    fun addFollowedArtistToUser(userId: String, artistId: String, callback: (Boolean, String) -> Unit)
    fun removeFollowedArtistFromUser(userId: String, artistId: String, callback: (Boolean, String) -> Unit)
    fun addUploadedMusicToUser(userId: String, musicId: String, callback: (Boolean, String) -> Unit)
    
    // User Statistics
    fun incrementUserPlayCount(userId: String, callback: (Boolean, String) -> Unit)
    fun incrementUserUploadCount(userId: String, callback: (Boolean, String) -> Unit)
    fun updateLastLoginTime(userId: String, callback: (Boolean, String) -> Unit)
    
    // User Search and Discovery
    fun searchUsers(query: String, callback: (List<UserModel>) -> Unit)
    fun getUsersByIds(userIds: List<String>, callback: (List<UserModel>) -> Unit)
}
