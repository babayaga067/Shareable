package com.example.sangeet.model

data class UserProfileWithRelationships(
    val user: UserModel,
    val favoriteMusic: List<MusicModel>,
    val playlists: List<PlaylistModel>,
    val uploadedMusic: List<MusicModel>,
    val followedArtists: List<ArtistModel>
)