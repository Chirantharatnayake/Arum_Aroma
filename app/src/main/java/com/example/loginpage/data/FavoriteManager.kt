package com.example.loginpage.data

import androidx.compose.runtime.mutableStateListOf

object FavoriteManager {
    val favoritePerfumes = mutableStateListOf<Int>()

    fun toggleFavorite(id: Int) {
        if (favoritePerfumes.contains(id)) {
            favoritePerfumes.remove(id)
        } else {
            favoritePerfumes.add(id)
        }
    }

    fun isFavorite(id: Int): Boolean {
        return favoritePerfumes.contains(id)
    }
}
