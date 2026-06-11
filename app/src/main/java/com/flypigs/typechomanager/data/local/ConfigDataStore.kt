package com.flypigs.typechomanager.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.flypigs.typechomanager.data.model.BlogConfig
import com.flypigs.typechomanager.data.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * DataStore wrapper for persisting blog configuration.
 *
 * Stores [BlogConfig] fields as individual preference keys and
 * maintains a boolean `hasConfig` flag to indicate whether the
 * user has completed the login / configuration step.
 */
class ConfigDataStore(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val ENDPOINT = stringPreferencesKey("endpoint")
        val USERNAME = stringPreferencesKey("username")
        val PASSWORD = stringPreferencesKey("password")
        val BLOG_NAME = stringPreferencesKey("blog_name")
        val BLOG_URL = stringPreferencesKey("blog_url")
        val HAS_CONFIG = booleanPreferencesKey("has_config")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PULL_TO_REFRESH = booleanPreferencesKey("pull_to_refresh")
        val IMAGE_QUALITY = stringPreferencesKey("image_quality")
    }

    /**
     * Persist all fields of [config] and set `hasConfig = true`.
     */
    suspend fun saveConfig(config: BlogConfig) {
        dataStore.edit { prefs ->
            prefs[Keys.ENDPOINT] = config.endpoint
            prefs[Keys.USERNAME] = config.username
            prefs[Keys.PASSWORD] = config.password
            prefs[Keys.BLOG_NAME] = config.blogName ?: ""
            prefs[Keys.BLOG_URL] = config.blogUrl
            prefs[Keys.HAS_CONFIG] = true
        }
    }

    /**
     * Read the stored [BlogConfig]. Returns defaults when nothing has been saved yet.
     */
    suspend fun getConfig(): BlogConfig {
        return dataStore.data.map { prefs ->
            BlogConfig(
                endpoint = prefs[Keys.ENDPOINT] ?: "",
                username = prefs[Keys.USERNAME] ?: "",
                password = prefs[Keys.PASSWORD] ?: "",
                blogName = prefs[Keys.BLOG_NAME] ?: "",
                blogUrl = prefs[Keys.BLOG_URL] ?: ""
            )
        }.first()
    }

    /**
     * Remove all stored preferences (logs the user out).
     */
    suspend fun clearConfig() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    /**
     * Save the preferred theme mode as a string: "system", "light", or "dark".
     */
    suspend fun saveThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    /**
     * Read the stored theme mode. Defaults to [ThemeMode.SYSTEM].
     */
    suspend fun getThemeMode(): ThemeMode {
        return getThemeModeFlow().first()
    }

    fun getThemeModeFlow(): kotlinx.coroutines.flow.Flow<ThemeMode> {
        return dataStore.data.map { prefs ->
            val raw = prefs[Keys.THEME_MODE] ?: "SYSTEM"
            try {
                ThemeMode.valueOf(raw)
            } catch (_: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }
    }

    /**
     * Returns `true` when a blog configuration has been saved at least once.
     */
    suspend fun isLoggedIn(): Boolean {
        return dataStore.data.map { prefs ->
            prefs[Keys.HAS_CONFIG] ?: false
        }.first()
    }

    suspend fun getPullToRefreshEnabled(): Boolean {
        return dataStore.data.map { prefs ->
            prefs[Keys.PULL_TO_REFRESH] ?: true
        }.first()
    }

    suspend fun setPullToRefreshEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.PULL_TO_REFRESH] = enabled
        }
    }

    suspend fun getImageQuality(): String {
        return dataStore.data.map { prefs ->
            prefs[Keys.IMAGE_QUALITY] ?: "高质量"
        }.first()
    }

    suspend fun setImageQuality(quality: String) {
        dataStore.edit { prefs ->
            prefs[Keys.IMAGE_QUALITY] = quality
        }
    }
}
