package vn.edu.hcmute.minlish.data.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(private val context: Context) {
    private val prefName = "minlish_session"
    private val keyJwtToken = "jwt_token"
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString(keyJwtToken, token)
        editor.apply()
    }

    fun getToken(): String? {
        val token = sharedPreferences.getString(keyJwtToken, null)
        return token
    }

    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.remove(keyJwtToken)
        editor.apply()
    }
}
