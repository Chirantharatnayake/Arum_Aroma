package com.example.loginpage.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

private const val ZEN_TAG = "ZenQuotesFetcher"

/** UI-ready model for quotes. */
data class UiQuote(
    val text: String,
    val author: String
)

object ZenQuotesFetcher {
    private val client = OkHttpClient()

    // Keywords the user requested for filtering
    private val keywords = listOf("perfume", "fragrance", "scent", "aroma")

    // Fallback curated quotes (perfume related)
    private val fallback = listOf(
        UiQuote("A perfume is like a piece of clothing, a message, a way of presenting oneself — a costume that according to the person who wears it.", "Paloma Picasso"),
        UiQuote("Perfume is the art that makes memory speak.", "Francis Kurkdjian"),
        UiQuote("A woman’s perfume tells more about her than her handwriting.", "Christian Dior"),
        UiQuote("Fragrance is the voice of inanimate things.", "Mary Webb"),
        UiQuote("A good fragrance is really a powerful cocktail of memories and emotion.", "Jeffrey Stepakoff")
    )

    /**
     * Downloads quotes from ZenQuotes and returns only perfume-related ones.
     * If the call fails or none match, returns curated fallback quotes.
     */
    suspend fun fetchPerfumeQuotes(limit: Int = 8): List<UiQuote> = withContext(Dispatchers.IO) {
        val url = "https://zenquotes.io/api/quotes"
        val req = Request.Builder().url(url).get().build()
        try {
            client.newCall(req).execute().use { res ->
                if (!res.isSuccessful) {
                    Log.w(ZEN_TAG, "ZenQuotes HTTP ${res.code}")
                    return@withContext fallback.take(limit)
                }
                val body = res.body?.string().orEmpty()
                val arr = JSONArray(body)
                val all = mutableListOf<UiQuote>()
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val q = o.optString("q").trim()
                    val a = o.optString("a").trim()
                    if (q.isNotEmpty() && a.isNotEmpty()) {
                        all += UiQuote(q, a)
                    }
                }
                val filtered = all.filter { q ->
                    val t = q.text.lowercase()
                    keywords.any { kw -> t.contains(kw) }
                }
                val result = (if (filtered.isNotEmpty()) filtered else fallback)
                Log.i(ZEN_TAG, "Quotes loaded: api=${all.size}, filtered=${filtered.size}, returning=${result.size.coerceAtMost(limit)}")
                return@withContext result.take(limit)
            }
        } catch (e: Exception) {
            Log.e(ZEN_TAG, "ZenQuotes exception: ${e.message}", e)
            return@withContext fallback.take(limit)
        }
    }
}

