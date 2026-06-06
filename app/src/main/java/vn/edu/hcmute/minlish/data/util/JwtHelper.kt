package vn.edu.hcmute.minlish.data.util

import android.util.Base64
import org.json.JSONObject
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object JwtHelper {
    private val SECRET_KEY = vn.edu.hcmute.minlish.BuildConfig.JWT_SECRET

    fun generateToken(email: String, userId: Int): String {
        val headerJson = JSONObject()
        headerJson.put("alg", "HS256")
        headerJson.put("typ", "JWT")
        val header = Base64.encodeToString(headerJson.toString().toByteArray(), Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE)

        val payloadJson = JSONObject()
        payloadJson.put("email", email)
        payloadJson.put("userId", userId)
        val exp = System.currentTimeMillis() + 7L * 24L * 60L * 60L * 1000L // 7 ngày
        payloadJson.put("exp", exp)
        val payload = Base64.encodeToString(payloadJson.toString().toByteArray(), Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE)

        val dataToSign = "$header.$payload"
        val signature = signHmac256(dataToSign, SECRET_KEY)

        val token = "$header.$payload.$signature"
        return token
    }

    fun validateAndParseToken(token: String): JSONObject? {
        val parts = token.split(".")
        if (parts.size != 3) {
            return null
        }
        val header = parts[0]
        val payload = parts[1]
        val signature = parts[2]

        val dataToSign = "$header.$payload"
        val expectedSignature = signHmac256(dataToSign, SECRET_KEY)
        if (signature != expectedSignature) {
            return null
        }

        val decodedPayloadBytes = Base64.decode(payload, Base64.DEFAULT)
        val payloadJsonString = String(decodedPayloadBytes)
        val payloadJson = JSONObject(payloadJsonString)
        val exp = payloadJson.optLong("exp", 0L)
        if (System.currentTimeMillis() > exp) {
            return null
        }

        return payloadJson
    }

    private fun signHmac256(data: String, secret: String): String {
        val sha256HMAC = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        sha256HMAC.init(secretKey)
        val signedBytes = sha256HMAC.doFinal(data.toByteArray())
        val base64Signed = Base64.encodeToString(signedBytes, Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE)
        return base64Signed
    }
}
