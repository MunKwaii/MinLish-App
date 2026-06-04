package vn.edu.hcmute.minlish.data.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property để khởi tạo DataStore (dùng chung 1 instance)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "minlish_settings")

class SettingsManager(private val context: Context) {

    companion object {
        private val KEY_NEW_WORDS_LIMIT = intPreferencesKey("new_words_limit")
        // Dark mode: true = dark, false = light, key absent = theo hệ thống
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_DARK_MODE_SET = booleanPreferencesKey("dark_mode_set")
    }

    // ── New words limit ───────────────────────────────────────────────────────

    val newWordsLimitFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_NEW_WORDS_LIMIT] ?: 10
    }

    suspend fun saveNewWordsLimit(limit: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NEW_WORDS_LIMIT] = limit
        }
    }

    // ── Dark mode ─────────────────────────────────────────────────────────────
    // null  → theo hệ thống (System default)
    // true  → Dark mode
    // false → Light mode

    /**
     * Flow phát ra:
     *   null  – chưa đặt, dùng theme hệ thống
     *   true  – dark
     *   false – light
     */
    val darkModeFlow: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        if (prefs[KEY_DARK_MODE_SET] == true) prefs[KEY_DARK_MODE] else null
    }

    suspend fun setDarkMode(isDark: Boolean?) {
        context.dataStore.edit { prefs ->
            if (isDark == null) {
                prefs.remove(KEY_DARK_MODE)
                prefs.remove(KEY_DARK_MODE_SET)
            } else {
                prefs[KEY_DARK_MODE] = isDark
                prefs[KEY_DARK_MODE_SET] = true
            }
        }
    }

    /**
     * Xoay vòng: null → true → false → null → ...
     */
    suspend fun toggleDarkMode(current: Boolean?) {
        val next = when (current) {
            null -> true
            true -> false
            false -> null
        }
        setDarkMode(next)
    }
}
