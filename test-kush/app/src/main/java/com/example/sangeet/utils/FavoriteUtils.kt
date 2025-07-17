package com.example.sangeet.utils

import android.content.Context
import android.widget.Toast
import com.example.sangeet.viewmodel.FavoriteViewModel

fun toggleFavorite(
    userId: String,
    musicId: String,
    favoriteViewModel: FavoriteViewModel,
    context: Context
) {
    // Optional: define this in a shared constants file
    val guestUserId = "user123"

    if (userId == guestUserId) {
        Toast.makeText(context, "Please log in to use favorites", Toast.LENGTH_SHORT).show()
        return
    }

    val isFavorite = favoriteViewModel.favoriteMusics.value?.any { it.musicId == musicId } == true

    val onComplete = { success: Boolean, message: String? ->
        Toast.makeText(
            context,
            message ?: if (isFavorite) "Removed from favorites" else "Added to favorites",
            Toast.LENGTH_SHORT
        ).show()

        if (success) {
            favoriteViewModel.getUserFavoriteMusics(userId)
        }
    }

    if (isFavorite) {
        favoriteViewModel.removeFromFavorites(userId, musicId, onComplete)
    } else {
        favoriteViewModel.addToFavorites(userId, musicId, onComplete)
    }
}