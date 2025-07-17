package com.example.sangeet.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sangeet.component.SplashScreen
import com.example.sangeet.repository.*
import com.example.sangeet.view.*
import com.example.sangeet.viewmodel.*

@Composable
fun AppNavGraph(navController: NavHostController) {
    val userViewModel = remember { UserViewModel(UserRepositoryImpl()) }
    val musicViewModel = remember { MusicViewModel(MusicRepositoryImpl()) }
    val favoriteViewModel = remember { FavoriteViewModel(FavoriteRepositoryImpl()) }
    val playlistViewModel = remember { PlaylistViewModel(PlaylistRepositoryImpl()) }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        //  Auth
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController, userViewModel) }
        composable(Screen.Register.route) { RegisterScreen(navController) }

        //  Dashboard & Menu
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController, userViewModel, musicViewModel, favoriteViewModel, playlistViewModel)
        }

        composable(Screen.Menu.route) {
            MenuScreen(navController)
        }

        //  Discovery
        composable(Screen.Search.route) { SearchScreen(navController) }
        composable(Screen.Library.route) { LibraryScreen() }

        //  Profile
        composable(Screen.Profile.base, listOf(navArgument("userId") { type = NavType.StringType })) {
            val userId = it.arguments?.getString("userId").orEmpty()
            ProfileScreen(navController, userId, userViewModel)
        }
        composable(Screen.EditProfile.base, listOf(navArgument("userId") { type = NavType.StringType })) {
            val userId = it.arguments?.getString("userId").orEmpty()
            EditProfileScreen(navController, userId)
        }

        //  Music
        composable(Screen.PlayingNow.base, listOf(navArgument("musicId") { type = NavType.StringType })) {
            val musicId = it.arguments?.getString("musicId").orEmpty()
            PlayingNowScreen(musicId, musicViewModel, navController)
        }
        composable(Screen.UploadMusic.base, listOf(navArgument("userId") { type = NavType.StringType })) {
            val userId = it.arguments?.getString("userId").orEmpty()
            UploadMusicScreen(navController, userId, musicViewModel, userViewModel)
        }

        //  Favorites
        composable(Screen.Favorites.base, listOf(navArgument("userId") { type = NavType.StringType })) {
            val userId = it.arguments?.getString("userId").orEmpty()
            FavoritesScreen(navController, userId)
        }

        //  Playlists
        composable(Screen.Playlists.base, listOf(navArgument("userId") { type = NavType.StringType })) {
            val userId = it.arguments?.getString("userId").orEmpty()
            PlaylistScreen(navController, userId)
        }
        composable(Screen.CreatePlaylist.base, listOf(navArgument("userId") { type = NavType.StringType })) { //optional changes
            val userId = it.arguments?.getString("userId").orEmpty()
            CreatePlaylistScreen(navController, userId)
        }

        //  Artists
        composable(Screen.Artists.route) { ArtistListScreen(navController) }

        //  Info
        composable(Screen.AboutUs.route) { AboutUsScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
    }
}