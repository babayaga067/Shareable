package com.example.sangeet.navigation

sealed class Screen(val route: String) {

    //  Authentication & Entry
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    //  Core App Views
    object Dashboard : Screen("dashboard")
    object Menu : Screen("menu")
    object AboutUs : Screen("about_us")

    //  Discovery & Settings
    object Library : Screen("library")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object Artists : Screen("artists")

    //  Profile & Management
    data class Profile(val userId: String) : Screen("profile/$userId") {
        companion object { const val base = "profile/{userId}" }
    }
    data class EditProfile(val userId: String) : Screen("edit_profile/$userId") {
        companion object { const val base = "edit_profile/{userId}" }
    }

    //  Music Playback & Upload
    data class PlayingNow(val musicId: String) : Screen("playing_now/$musicId") {
        companion object { const val base = "playing_now/{musicId}" }
    }
    data class UploadMusic(val userId: String) : Screen("upload_music/$userId") {
        companion object { const val base = "upload_music/{userId}" }
    }

    //  Playlists
    data class CreatePlaylist(val userId: String) : Screen("create_playlist/$userId") {
        companion object { const val base = "create_playlist/{userId}" }
    }
    data class Playlists(val userId: String) : Screen("playlists/$userId") {
        companion object { const val base = "playlists/{userId}" }
    }

    //  Favorites
    data class Favorites(val userId: String) : Screen("favorites/$userId") {
        companion object { const val base = "favorites/{userId}" }
    }
}