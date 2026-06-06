package vn.edu.hcmute.minlish.data.util

import android.util.Base64
import org.json.JSONObject

/**
 * Utility để decode Google ID Token (JWT) và trích xuất thông tin user.
 *
 * Google ID Token là một JWT có 3 phần: header.payload.signature
 * Payload chứa các claims: email, name, sub (Google user ID), iss, aud, exp, ...
 *
 * Trong app local (không có backend server), ta decode payload để lấy thông tin.
 * Trong app có backend, token này phải được gửi lên server để verify signature
 * với Google public keys.
 */
object GoogleTokenVerifier {

    data class GoogleUserInfo(
        val email: String,
        val name: String,
        val googleId: String,   // "sub" claim - unique Google user ID
        val pictureUrl: String? // URL ảnh đại diện Google
    )

    /**
     * Decode payload của Google ID Token để lấy thông tin user.
     * @return GoogleUserInfo nếu token hợp lệ, null nếu không.
     */
    fun decodeIdToken(idToken: String): GoogleUserInfo? {
        return try {
            val parts = idToken.split(".")
            if (parts.size != 3) return null

            // Decode phần payload (phần thứ 2)
            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP)
            val payload = JSONObject(String(payloadBytes))

            // Kiểm tra token chưa hết hạn
            val exp = payload.optLong("exp", 0) * 1000 // convert to milliseconds
            if (System.currentTimeMillis() > exp) return null

            // Kiểm tra issuer là Google
            val iss = payload.optString("iss", "")
            if (iss != "accounts.google.com" && iss != "https://accounts.google.com") {
                return null
            }

            GoogleUserInfo(
                email = payload.optString("email", ""),
                name = payload.optString("name", ""),
                googleId = payload.optString("sub", ""),
                pictureUrl = payload.optString("picture", null)
            )
        } catch (e: Exception) {
            null
        }
    }
}
