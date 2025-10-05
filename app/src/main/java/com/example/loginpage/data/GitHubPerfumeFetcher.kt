package com.example.loginpage.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

object GitHubPerfumeFetcher {
    private const val TAG = "GitHubPerfumeFetcher"

    // Correct RAW URL format (no "refs/heads")
    private const val JSON_URL =
        "https://raw.githubusercontent.com/Chirantharatnayake/ArumAroma_Json/main/perfumes.json"

    @Volatile private var cache: List<LocalPerfume>? = null

    suspend fun fetch(context: Context): List<LocalPerfume> = cache ?: withContext(Dispatchers.IO) {
        try {
            val response = URL(JSON_URL).readText()
            Log.d(TAG, "Raw response (first 200): ${response.take(200)}")

            // Your file is a top-level array like: [ { "id":0, "nameRes":"...", "imageRes":"...", ... }, ... ]
            val arr = JSONArray(response)

            val res = context.resources
            val pkg = context.packageName

            val list = buildList {
                for (i in 0 until arr.length()) {
                    val o: JSONObject = arr.getJSONObject(i)

                    // ---- NAME: prefer plain name/title; else resolve nameRes -> string resource ----
                    var name = o.optString("name").ifBlank { o.optString("title") }
                    if (name.isBlank()) {
                        val nameResKey = o.optString("nameRes")
                        if (nameResKey.isNotBlank()) {
                            val nameId = res.getIdentifier(nameResKey, "string", pkg)
                            name = if (nameId != 0) context.getString(nameId) else nameResKey
                        }
                    }
                    if (name.isBlank()) {
                        Log.w(TAG, "Skipping index=$i: missing name/title/nameRes")
                        continue
                    }

                    // ---- IMAGE: accept imageRes (drawable name) or URLs ----
                    val imageAny = firstNonBlank(o, "image", "imageUrl", "imageRes", "posterName", "poster", "img")
                    if (imageAny.isBlank()) {
                        Log.w(TAG, "Skipping '$name': missing image key (image/imageUrl/imageRes/...)")
                        continue
                    }
                    val isUrl = imageAny.startsWith("http", ignoreCase = true)
                    val imageResId = if (isUrl) 0 else {
                        val resName = imageAny.substringBeforeLast('.') // handle "mperfume1.png"
                        res.getIdentifier(resName, "drawable", pkg)
                    }

                    // ---- ID ----
                    val id = if (o.has("id")) o.optInt("id", i) else i

                    // ---- PRICE: number or string ----
                    val price = if (o.has("price"))
                        runCatching { o.getDouble("price") }.getOrElse {
                            o.optString("price").toDoubleOrNull() ?: 0.0
                        }
                    else 0.0

                    // ---- DESCRIPTION: description/desc or resolve descriptionRes ----
                    var description = o.optString("description").ifBlank { o.optString("desc") }
                    if (description.isBlank()) {
                        val descResKey = o.optString("descriptionRes")
                        if (descResKey.isNotBlank()) {
                            val descId = res.getIdentifier(descResKey, "string", pkg)
                            description = if (descId != 0) context.getString(descId) else descResKey
                        }
                    }

                    // ---- GENDER normalization ----
                    val genderRaw = firstNonBlank(o, "gender", "category", "type")
                    val gender = when (genderRaw.lowercase()) {
                        "men", "male", "m" -> "men"
                        "women", "female", "f" -> "women"
                        else -> null
                    }

                    add(
                        LocalPerfume(
                            id = id,
                            name = name,
                            imageResId = imageResId,
                            price = price,
                            description = description,
                            gender = gender,
                            imageUrl = if (isUrl) imageAny else null
                        )
                    )
                }
            }

            Log.d(TAG, "Loaded ${list.size} items from JSON.")
            list.take(3).forEach {
                Log.d(TAG, "Sample -> id=${it.id}, name=${it.name}, price=${it.price}, url=${it.imageUrl}, res=${it.imageResId}")
            }

            cache = list
            list
        } catch (e: Exception) {
            Log.e(TAG, "Fetch failed: ${e.message}", e)
            emptyList()
        }
    }

    private fun firstNonBlank(o: JSONObject, vararg keys: String): String {
        for (k in keys) {
            val v = o.optString(k, "")
            if (v.isNotBlank()) return v
        }
        return ""
    }
}
