package vn.edu.hcmute.minlish.data.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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

        private val KEY_DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        private val KEY_DAILY_REMINDER_TIME = stringPreferencesKey("daily_reminder_time")
        private val KEY_DUE_WORDS_REMINDER_ENABLED = booleanPreferencesKey("due_words_reminder_enabled")
        private val KEY_EMAIL_NOTIFICATION_ENABLED = booleanPreferencesKey("email_notification_enabled")
        private val KEY_PUSH_NOTIFICATION_ENABLED = booleanPreferencesKey("push_notification_enabled")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    // ── New words limit ───────────────────────────────────────────────────────

    val newWordsLimitFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        val limit: Int? = prefs[KEY_NEW_WORDS_LIMIT]
        if (limit != null) {
            limit
        } else {
            10
        }
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
        if (prefs[KEY_DARK_MODE_SET] == true) {
            prefs[KEY_DARK_MODE]
        } else {
            null
        }
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
        val next: Boolean?
        if (current == null) {
            next = true
        } else if (current == true) {
            next = false
        } else {
            next = null
        }
        setDarkMode(next)
    }

    // ── Notification Settings ──────────────────────────────────────────────────

    val dailyReminderEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        val enabled: Boolean? = prefs[KEY_DAILY_REMINDER_ENABLED]
        if (enabled != null) {
            enabled
        } else {
            true
        }
    }

    suspend fun saveDailyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DAILY_REMINDER_ENABLED] = enabled
        }
    }

    val dailyReminderTimeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        val time: String? = prefs[KEY_DAILY_REMINDER_TIME]
        if (time != null) {
            time
        } else {
            "20:00"
        }
    }

    suspend fun saveDailyReminderTime(time: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DAILY_REMINDER_TIME] = time
        }
    }

    val dueWordsReminderEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        val enabled: Boolean? = prefs[KEY_DUE_WORDS_REMINDER_ENABLED]
        if (enabled != null) {
            enabled
        } else {
            true
        }
    }

    suspend fun saveDueWordsReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DUE_WORDS_REMINDER_ENABLED] = enabled
        }
    }

    val emailNotificationEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        val enabled: Boolean? = prefs[KEY_EMAIL_NOTIFICATION_ENABLED]
        if (enabled != null) {
            enabled
        } else {
            false
        }
    }

    suspend fun saveEmailNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EMAIL_NOTIFICATION_ENABLED] = enabled
        }
    }

    val pushNotificationEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        val enabled: Boolean? = prefs[KEY_PUSH_NOTIFICATION_ENABLED]
        if (enabled != null) {
            enabled
        } else {
            true
        }
    }

    suspend fun savePushNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PUSH_NOTIFICATION_ENABLED] = enabled
        }
    }

    val biometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        val enabled: Boolean? = prefs[KEY_BIOMETRIC_ENABLED]
        if (enabled != null) {
            enabled
        } else {
            false
        }
    }

    suspend fun saveBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }
}
