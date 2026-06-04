package vn.edu.hcmute.minlish.ui.theme

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    // null means system default, true means dark, false means light
    private val _isDarkMode = MutableStateFlow<Boolean?>(
        if (prefs.contains("is_dark_mode")) prefs.getBoolean("is_dark_mode", false) else null
    )
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    fun toggleTheme() {
        val current = _isDarkMode.value
        val next = if (current == null) true else if (current == true) false else null
        _isDarkMode.value = next

        if (next == null) {
            prefs.edit().remove("is_dark_mode").apply()
        } else {
            prefs.edit().putBoolean("is_dark_mode", next).apply()
        }
    }

    fun setDarkMode(isDark: Boolean?) {
        _isDarkMode.value = isDark
        if (isDark == null) {
            prefs.edit().remove("is_dark_mode").apply()
        } else {
            prefs.edit().putBoolean("is_dark_mode", isDark).apply()
        }
    }
}
