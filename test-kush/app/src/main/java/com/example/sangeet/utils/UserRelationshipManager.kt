package com.example.sangeet.utils

import com.example.sangeet.model.*
import com.example.sangeet.repository.*

sealed class RelationshipResult {
    data class Success(val message: String, val data: Any? = null) : RelationshipResult()
    data class Failure(val message: String) : RelationshipResult()
}

class UserRelationshipManager(
    private val userRepository: UserRepository,
    private val favoriteRepository: FavoriteRepository,
    private val playlistRepository: PlaylistRepository,
    private val artistRepository: ArtistRepository,
    private val musicRepository: MusicRepository
) {

    fun addMusicToFavorites(
        userId: String,
        musicId: String,
        onResult: (RelationshipResult) -> Unit
    ) {
        val favoriteId = generateFavoriteId()

        favoriteRepository.addToFavorites(userId, musicId) { success, msg ->
            if (!success) {
                onResult(RelationshipResult.Failure("Failed to add to favorites: $msg"))
                return@addToFavorites
            }

            userRepository.addFavoriteToUser(userId, favoriteId) { userSuccess, userMsg ->
                if (userSuccess) {
                    onResult(RelationshipResult.Success("Music added to favorites"))
                } else {
                    rollbackFavorite(userId, musicId)
                    onResult(RelationshipResult.Failure("User update failed: $userMsg"))
                }
            }
        }
    }

    fun followArtist(
        userId: String,
        artistId: String,
        onResult: (RelationshipResult) -> Unit
    ) {
        userRepository.addFollowedArtistToUser(userId, artistId) { userSuccess, userMsg ->
            if (!userSuccess) {
                onResult(RelationshipResult.Failure("Failed to follow artist: $userMsg"))
                return@addFollowedArtistToUser
            }

            artistRepository.followArtist(artistId) { artistSuccess, artistMsg ->
                if (artistSuccess) {
                    onResult(RelationshipResult.Success("Artist followed successfully"))
                } else {
                    rollbackFollowedArtist(userId, artistId)
                    onResult(RelationshipResult.Failure("Artist update failed: $artistMsg"))
                }
            }
        }
    }

    fun createPlaylistWithMusic(
        userId: String,
        playlistName: String,
        description: String,
        musicIds: List<String>,
        onResult: (RelationshipResult) -> Unit
    ) {
        val playlistId = generatePlaylistId()
        val playlistModel = PlaylistModel(
            playlistId = playlistId,
            userId = userId,
            playlistName = playlistName,
            description = description,
            musicIds = musicIds,
            createdAt = System.currentTimeMillis()
        )

        playlistRepository.createPlaylist(playlistModel) { success, msg ->
            if (!success) {
                onResult(RelationshipResult.Failure("Failed to create playlist: $msg"))
                return@createPlaylist
            }

            userRepository.addPlaylistToUser(userId, playlistId) { userSuccess, userMsg ->
                if (userSuccess) {
                    onResult(RelationshipResult.Success("Playlist created successfully", playlistId))
                } else {
                    rollbackPlaylist(playlistId)
                    onResult(RelationshipResult.Failure("User playlist update failed: $userMsg"))
                }
            }
        }
    }

    fun uploadMusicAsUser(
        userId: String,
        musicModel: MusicModel,
        onResult: (RelationshipResult) -> Unit
    ) {
        val updatedModel = musicModel.copy(
            uploadedBy = userId,
            uploadedAt = System.currentTimeMillis()
        )

        musicRepository.addMusic(updatedModel) { success, msg ->
            if (!success) {
                onResult(RelationshipResult.Failure("Music upload failed: $msg"))
                return@addMusic
            }

            userRepository.addUploadedMusicToUser(userId, updatedModel.musicId) { userSuccess, userMsg ->
                if (userSuccess) {
                    userRepository.incrementUserUploadCount(userId) { _, _ -> }
                    onResult(RelationshipResult.Success("Music uploaded", updatedModel.musicId))
                } else {
                    rollbackMusicUpload(updatedModel.musicId)
                    onResult(RelationshipResult.Failure("User upload update failed: $userMsg"))
                }
            }
        }
    }

    fun getUserWithRelationships(
        userId: String,
        onResult: (UserProfileWithRelationships?) -> Unit
    ) {
        userRepository.getUserById(userId) { success, _, user ->
            if (!success || user == null) {
                onResult(null)
                return@getUserById
            }

            playlistRepository.getUserPlaylists(userId) { playlistSuccess, _, playlists ->
                val profile = UserProfileWithRelationships(
                    user = user,
                    favoriteMusic = emptyList(), // Extend when favorite loader added
                    playlists = playlists ?: emptyList(),
                    uploadedMusic = emptyList(),
                    followedArtists = emptyList()
                )
                onResult(profile)
            }
        }
    }

    // 🔁 Rollback helpers
    private fun rollbackFavorite(userId: String, musicId: String) {
        favoriteRepository.removeFromFavorites(userId, musicId) { _, _ -> }
    }

    private fun rollbackFollowedArtist(userId: String, artistId: String) {
        userRepository.removeFollowedArtistFromUser(userId, artistId) { _, _ -> }
    }

    private fun rollbackPlaylist(playlistId: String) {
        playlistRepository.deletePlaylist(playlistId) { _, _ -> }
    }

    private fun rollbackMusicUpload(musicId: String) {
        musicRepository.deleteMusic(musicId) { _, _ -> }
    }

    // 🆔 ID generators
    private fun generateFavoriteId(): String =
        "fav_${System.currentTimeMillis()}_${(1000..9999).random()}"

    private fun generatePlaylistId(): String =
        "playlist_${System.currentTimeMillis()}_${(1000..9999).random()}"
}