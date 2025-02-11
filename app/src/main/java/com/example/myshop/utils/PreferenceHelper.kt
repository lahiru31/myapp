package com.example.myshop.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.example.myshop.models.User

class PreferenceHelper private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREF_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        @Volatile
        private var instance: PreferenceHelper? = null

        fun getInstance(context: Context): PreferenceHelper {
            return instance ?: synchronized(this) {
                instance ?: PreferenceHelper(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    // User Authentication State
    var isUserLoggedIn: Boolean
        get() = prefs.getBoolean(Constants.PREF_USER_LOGGED_IN, false)
        set(value) = prefs.edit { putBoolean(Constants.PREF_USER_LOGGED_IN, value) }

    // User ID
    var userId: String?
        get() = prefs.getString(Constants.PREF_USER_ID, null)
        set(value) = prefs.edit { putString(Constants.PREF_USER_ID, value) }

    // User Type (CUSTOMER/ADMIN)
    var userType: String?
        get() = prefs.getString(Constants.PREF_USER_TYPE, Constants.USER_TYPE_CUSTOMER)
        set(value) = prefs.edit { putString(Constants.PREF_USER_TYPE, value) }

    // FCM Token
    var fcmToken: String?
        get() = prefs.getString(Constants.PREF_FCM_TOKEN, null)
        set(value) = prefs.edit { putString(Constants.PREF_FCM_TOKEN, value) }

    // Cached User Data
    var cachedUser: User?
        get() {
            val json = prefs.getString("cached_user", null)
            return if (json != null) {
                try {
                    gson.fromJson(json, User::class.java)
                } catch (e: Exception) {
                    null
                }
            } else null
        }
        set(value) = prefs.edit {
            if (value != null) {
                putString("cached_user", gson.toJson(value))
            } else {
                remove("cached_user")
            }
        }

    // Last Sync Time
    var lastSyncTime: Long
        get() = prefs.getLong("last_sync_time", 0)
        set(value) = prefs.edit { putLong("last_sync_time", value) }

    // App Theme
    var isDarkMode: Boolean
        get() = prefs.getBoolean("dark_mode", false)
        set(value) = prefs.edit { putBoolean("dark_mode", value) }

    // Notification Settings
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean("notifications_enabled", true)
        set(value) = prefs.edit { putBoolean("notifications_enabled", value) }

    var orderNotificationsEnabled: Boolean
        get() = prefs.getBoolean("order_notifications", true)
        set(value) = prefs.edit { putBoolean("order_notifications", value) }

    var promotionalNotificationsEnabled: Boolean
        get() = prefs.getBoolean("promo_notifications", true)
        set(value) = prefs.edit { putBoolean("promo_notifications", value) }

    // Cart Count
    var cartItemCount: Int
        get() = prefs.getInt("cart_count", 0)
        set(value) = prefs.edit { putInt("cart_count", value) }

    // Search History
    var searchHistory: Set<String>
        get() = prefs.getStringSet("search_history", setOf()) ?: setOf()
        set(value) = prefs.edit { putStringSet("search_history", value) }

    // Recently Viewed Products
    var recentlyViewedProducts: Set<String>
        get() = prefs.getStringSet("recently_viewed", setOf()) ?: setOf()
        set(value) = prefs.edit { putStringSet("recently_viewed", value) }

    // App Language
    var appLanguage: String
        get() = prefs.getString("app_language", Constants.DEFAULT_LANGUAGE) ?: Constants.DEFAULT_LANGUAGE
        set(value) = prefs.edit { putString("app_language", value) }

    // Clear all preferences
    fun clearAll() {
        prefs.edit { clear() }
    }

    // Clear user-specific data
    fun clearUserData() {
        prefs.edit {
            remove(Constants.PREF_USER_LOGGED_IN)
            remove(Constants.PREF_USER_ID)
            remove(Constants.PREF_USER_TYPE)
            remove("cached_user")
            remove("cart_count")
            remove("search_history")
            remove("recently_viewed")
        }
    }

    // Add to search history
    fun addToSearchHistory(query: String) {
        val history = searchHistory.toMutableSet()
        history.add(query)
        if (history.size > 10) { // Keep only last 10 searches
            history.remove(history.first())
        }
        searchHistory = history
    }

    // Add to recently viewed products
    fun addToRecentlyViewed(productId: String) {
        val recent = recentlyViewedProducts.toMutableSet()
        recent.add(productId)
        if (recent.size > 20) { // Keep only last 20 products
            recent.remove(recent.first())
        }
        recentlyViewedProducts = recent
    }

    // Check if specific feature is enabled
    fun isFeatureEnabled(featureKey: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean("feature_$featureKey", defaultValue)
    }

    // Set feature flag
    fun setFeatureEnabled(featureKey: String, enabled: Boolean) {
        prefs.edit { putBoolean("feature_$featureKey", enabled) }
    }
}
