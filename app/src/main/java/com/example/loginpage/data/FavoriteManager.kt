// file: com/example/loginpage/data/FavoriteManager.kt
package com.example.loginpage.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf

object FavoriteManager {
    private const val TAG = "FavoriteManager"

    // In-memory favorites for resource-based items
    val favoritePerfumes = mutableStateListOf<Int>()

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        // Load persisted favorites
        val stored = LocalStorage.loadFavoriteIds(appContext!!)
        favoritePerfumes.clear()
        favoritePerfumes.addAll(stored)
        Log.d(TAG, "Loaded ${stored.size} favorite resource perfumes from storage")
    }

    private fun persist() {
        appContext?.let { LocalStorage.saveFavoriteIds(it, favoritePerfumes) }
    }

    fun toggleFavorite(id: Int) {
        if (favoritePerfumes.contains(id)) {
            favoritePerfumes.remove(id)
        } else {
            favoritePerfumes.add(id)
        }
        persist()
    }

    fun isFavorite(id: Int): Boolean = favoritePerfumes.contains(id)

    // ---- Remote (JSON) favorites using centralized LocalStorage ----

    fun isFavoriteKey(context: Context, key: String): Boolean {
        return LocalStorage.loadRemoteFavoriteKeys(context.applicationContext).contains(key)
    }

    fun toggleFavoriteKey(context: Context, key: String) {
        val ctx = context.applicationContext
        val set = LocalStorage.loadRemoteFavoriteKeys(ctx).toMutableSet()
        if (!set.add(key)) set.remove(key)
        LocalStorage.saveRemoteFavoriteKeys(ctx, set)
        Log.d(TAG, "Remote favorites count=${set.size}")
    }

    fun getAllFavoriteKeys(context: Context): Set<String> =
        LocalStorage.loadRemoteFavoriteKeys(context.applicationContext)

    fun reloadForActiveUser() {
        val ctx = appContext ?: return
        val stored = LocalStorage.loadFavoriteIds(ctx)
        favoritePerfumes.clear()
        favoritePerfumes.addAll(stored)
        Log.d(TAG, "Reloaded favorites for active user; count=${stored.size}")
    }
}
