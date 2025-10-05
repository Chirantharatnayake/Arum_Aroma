package com.example.loginpage.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Model used by UI when loading from JSON directly (plain strings instead of string resource IDs)
data class LocalPerfume(
    val id: Int,
    val name: String,
    val imageResId: Int,
    val price: Double,
    val description: String,
    val gender: String? = null,
    val imageUrl: String? = null
)

object PerfumeJsonLoader {
    private const val ASSET_FILE = "perfumes.json"
    private const val TAG = "PerfumeLoader"

    fun load(context: Context): List<LocalPerfume> {
        return try {
            val json = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<JsonPerfume>>() {}.type
            val rawList: List<JsonPerfume> = Gson().fromJson(json, type)

            val mapped = rawList.map { jp ->
                val resId = context.resources.getIdentifier(jp.image, "drawable", context.packageName)
                LocalPerfume(
                    id = jp.id,
                    name = jp.name,
                    imageResId = resId,
                    price = jp.price,
                    description = jp.description,
                    gender = jp.gender
                )
            }
            Log.d(TAG, "Loaded ${mapped.size} perfumes from JSON")
            mapped.forEach { Log.d(TAG, "Perfume: ${it.id} - ${it.name} (${it.gender})") }
            mapped
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load perfumes: ${e.message}", e)
            emptyList()
        }
    }
}

