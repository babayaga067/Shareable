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
        // Auth
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController, userViewModel)
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }

        // Dashboard & Menu
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController, userViewModel, musicViewModel, favoriteViewModel, playlistViewModel)
        }

        composable(Screen.Menu.route) {
            MenuScreen(navController)
        }

        // Discovery
        composable(Screen.Search.route) {
            SearchScreen(navController)
        }

        // FIXED: Pass navController to LibraryScreen
        composable(Screen.Library.route) {
            LibraryScreen(navController)
        }

        // Profile
        composable(
            route = Screen.Profile.base,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            ProfileScreen(navController, userId, userViewModel)
        }

        composable(
            route = Screen.EditProfile.base,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            EditProfileScreen(navController, userId)
        }

        // Music PlayingNow
        composable(
            route = Screen.PlayingNow.base,
            arguments = listOf(navArgument("musicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val musicId = backStackEntry.arguments?.getString("musicId").orEmpty()
            PlayingNowScreen(musicId, musicViewModel, navController)
        }

        composable(
            route = Screen.UploadMusic.base,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            UploadMusicScreen(navController, userId, musicViewModel, userViewModel)
        }

        // Favorites
        composable(
            route = Screen.Favorites.base,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            FavoritesScreen(navController, userId)
        }

        // Playlists
        composable(
            route = Screen.Playlists.base,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            PlaylistScreen(navController, userId)
        }

        composable(
            route = Screen.CreatePlaylist.base,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            CreatePlaylistScreen(navController, userId)
        }

        // Playlist Detail Screen
        composable(
            route = Screen.PlaylistDetail.base,
            arguments = listOf(
                navArgument("playlistId") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId").orEmpty()
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            PlaylistDetailScreen(navController, playlistId, userId, playlistViewModel, musicViewModel)
        }

        // Artists
        composable(Screen.Artists.route) {
            ArtistListScreen(navController)
        }

        // Info
        composable(Screen.AboutUs.route) {
            AboutUsScreen(navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
    }
}
