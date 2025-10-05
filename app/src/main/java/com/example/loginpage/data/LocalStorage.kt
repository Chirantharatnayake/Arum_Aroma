// app/src/main/java/com/example/loginpage/data/LocalStorage.kt
package com.example.loginpage.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/** Centralized SharedPreferences helper for favorites, cart, ambient light, theme, profile photo, and basic user profile. */
object LocalStorage {
    private const val PREFS_NAME = "app_local_storage"

    private const val KEY_FAVORITES_IDS = "favorites_ids"          // Comma separated Int ids
    private const val KEY_FAVORITES_REMOTE = "favorites_remote"    // String Set
    private const val KEY_CART_IDS = "cart_ids"                    // Comma separated Int ids
    private const val KEY_AMBIENT_ENABLED = "ambient_enabled"      // Boolean
    private const val KEY_AMBIENT_RANGE = "ambient_range"          // String?
    private const val KEY_DARK_MODE_ENABLED = "dark_mode_enabled"  // Boolean

    // Battery alerts
    private const val KEY_BATTERY_ALERT_ENABLED = "battery_alert_enabled" // Boolean
    private const val KEY_BATTERY_LAST_BUCKET = "battery_last_bucket"     // Int

    // Profile picture
    private const val KEY_PROFILE_IMAGE_PATH = "profile_image_path"

    // Basic user profile
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"

    // Saved card preference & data
    private const val KEY_SAVE_CARD_ENABLED = "save_card_enabled"  // Boolean
    private const val KEY_CARD_NAME = "card_name"                  // String
    private const val KEY_CARD_NUMBER = "card_number"              // String (formatted or masked)
    private const val KEY_CARD_EXPIRY = "card_expiry"              // String MM/YY
    private const val KEY_CARD_BRAND = "card_brand"                // String (VISA/MASTERCARD/...)

    private var initialized = false

    internal fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun init(ctx: Context) {
        if (!initialized) {
            initialized = true
        }
    }

    // -------- Favorites (resource id based) --------
    fun saveFavoriteIds(ctx: Context, ids: Collection<Int>) {
        val csv = ids.joinToString(",")
        prefs(ctx).edit().putString(KEY_FAVORITES_IDS, csv).apply()
    }

    fun loadFavoriteIds(ctx: Context): Set<Int> {
        val csv = prefs(ctx).getString(KEY_FAVORITES_IDS, null) ?: return emptySet()
        return csv.split(',').filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() }.toSet()
    }

    // -------- Favorites (remote keys) --------
    fun saveRemoteFavoriteKeys(ctx: Context, keys: Set<String>) {
        prefs(ctx).edit().putStringSet(KEY_FAVORITES_REMOTE, keys).apply()
    }

    fun loadRemoteFavoriteKeys(ctx: Context): Set<String> =
        prefs(ctx).getStringSet(KEY_FAVORITES_REMOTE, emptySet()) ?: emptySet()

    // -------- Cart --------
    fun saveCartIds(ctx: Context, ids: Collection<Int>) {
        val csv = ids.joinToString(",")
        prefs(ctx).edit().putString(KEY_CART_IDS, csv).apply()
    }

    fun loadCartIds(ctx: Context): List<Int> {
        val csv = prefs(ctx).getString(KEY_CART_IDS, null) ?: return emptyList()
        return csv.split(',').filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() }
    }

    // -------- Ambient Light --------
    fun saveAmbientEnabled(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_AMBIENT_ENABLED, enabled).apply()
    }

    fun loadAmbientEnabled(ctx: Context): Boolean =
        prefs(ctx).getBoolean(KEY_AMBIENT_ENABLED, false)

    fun saveAmbientRange(ctx: Context, id: String?) {
        prefs(ctx).edit().putString(KEY_AMBIENT_RANGE, id).apply()
    }

    fun loadAmbientRange(ctx: Context): String? =
        prefs(ctx).getString(KEY_AMBIENT_RANGE, null)

    // -------- Dark Mode Preference --------
    fun saveDarkModeEnabled(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_DARK_MODE_ENABLED, enabled).apply()
    }

    fun loadDarkModeEnabled(ctx: Context): Boolean =
        prefs(ctx).getBoolean(KEY_DARK_MODE_ENABLED, false)

    // -------- Battery Alerts Preference --------
    fun saveBatteryAlertEnabled(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_BATTERY_ALERT_ENABLED, enabled).apply()
    }

    fun loadBatteryAlertEnabled(ctx: Context): Boolean =
        prefs(ctx).getBoolean(KEY_BATTERY_ALERT_ENABLED, false)

    fun saveBatteryLastBucket(ctx: Context, bucket: Int) {
        prefs(ctx).edit().putInt(KEY_BATTERY_LAST_BUCKET, bucket).apply()
    }

    fun loadBatteryLastBucket(ctx: Context): Int =
        prefs(ctx).getInt(KEY_BATTERY_LAST_BUCKET, -1)

    // -------- Basic User Profile --------
    fun saveUserProfile(ctx: Context, username: String?, email: String?) {
        prefs(ctx).edit()
            .apply {
                if (username != null) putString(KEY_USER_NAME, username)
                if (email != null) putString(KEY_USER_EMAIL, email)
            }
            .apply()
    }

    fun saveUserEmail(ctx: Context, email: String) {
        prefs(ctx).edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun saveUserName(ctx: Context, username: String) {
        prefs(ctx).edit().putString(KEY_USER_NAME, username).apply()
    }

    fun loadUserName(ctx: Context): String? =
        prefs(ctx).getString(KEY_USER_NAME, null)

    fun loadUserEmail(ctx: Context): String? =
        prefs(ctx).getString(KEY_USER_EMAIL, null)

    fun clearUserProfile(ctx: Context) {
        prefs(ctx).edit()
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .apply()
    }

    // -------- Profile Picture (Bitmap stored in internal storage) --------
    /** Save a profile Bitmap into internal storage and persist its absolute path in prefs. */
    fun saveProfileBitmap(ctx: Context, bitmap: Bitmap): Uri {
        val file = File(ctx.filesDir, "profile_image.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
        }
        prefs(ctx).edit().putString(KEY_PROFILE_IMAGE_PATH, file.absolutePath).apply()
        return Uri.fromFile(file)
    }

    /** Load the saved profile Bitmap (or null if none/invalid). */
    fun loadProfileBitmap(ctx: Context): Bitmap? {
        val path = prefs(ctx).getString(KEY_PROFILE_IMAGE_PATH, null) ?: return null
        val file = File(path)
        if (!file.exists()) return null
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    /** Clear the saved profile image (optional helper). */
    fun clearProfileBitmap(ctx: Context) {
        val path = prefs(ctx).getString(KEY_PROFILE_IMAGE_PATH, null) ?: return
        val file = File(path)
        if (file.exists()) file.delete()
        prefs(ctx).edit().remove(KEY_PROFILE_IMAGE_PATH).apply()
    }

    // -------- Saved Card Preference & Data --------
    data class SavedCard(
        val name: String,
        val number: String, // store formatted (no CVV); may be masked by UI if needed
        val expiry: String,
        val brand: String
    )

    fun saveSaveCardEnabled(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_SAVE_CARD_ENABLED, enabled).apply()
    }

    fun loadSaveCardEnabled(ctx: Context): Boolean =
        prefs(ctx).getBoolean(KEY_SAVE_CARD_ENABLED, false)

    fun saveCardDetails(ctx: Context, name: String, numberFormatted: String, expiry: String, brand: String) {
        // Never store CVV
        prefs(ctx).edit()
            .putString(KEY_CARD_NAME, name)
            .putString(KEY_CARD_NUMBER, numberFormatted)
            .putString(KEY_CARD_EXPIRY, expiry)
            .putString(KEY_CARD_BRAND, brand)
            .apply()
    }

    fun loadCardDetails(ctx: Context): SavedCard? {
        val name = prefs(ctx).getString(KEY_CARD_NAME, null) ?: return null
        val number = prefs(ctx).getString(KEY_CARD_NUMBER, null) ?: return null
        val expiry = prefs(ctx).getString(KEY_CARD_EXPIRY, null) ?: return null
        val brand = prefs(ctx).getString(KEY_CARD_BRAND, null) ?: "CARD"
        return SavedCard(name = name, number = number, expiry = expiry, brand = brand)
    }

    fun clearSavedCard(ctx: Context) {
        prefs(ctx).edit()
            .remove(KEY_CARD_NAME)
            .remove(KEY_CARD_NUMBER)
            .remove(KEY_CARD_EXPIRY)
            .remove(KEY_CARD_BRAND)
            .apply()
    }
}
