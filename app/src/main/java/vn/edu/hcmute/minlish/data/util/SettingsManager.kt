package vn.edu.hcmute.minlish.data.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property để khởi tạo DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "minlish_settings")

class SettingsManager(private val context: Context) {

    companion object {
        private val KEY_NEW_WORDS_LIMIT = intPreferencesKey("new_words_limit")
    }

    // Đọc luồng dữ liệu limit bất đồng bộ dưới dạng Flow
    val newWordsLimitFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_NEW_WORDS_LIMIT] ?: 10 // Mặc định là 10 từ mới mỗi ngày
    }

    // Lưu limit bất đồng bộ
    suspend fun saveNewWordsLimit(limit: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NEW_WORDS_LIMIT] = limit
        }
    }
}
