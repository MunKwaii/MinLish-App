package vn.edu.hcmute.minlish.data.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vn.edu.hcmute.minlish.BuildConfig
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {

    suspend fun sendOtpEmail(toEmail: String, otpCode: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val smtpEmail = BuildConfig.SMTP_EMAIL
            val smtpPassword = BuildConfig.SMTP_PASSWORD

            if (smtpEmail.isBlank() || smtpPassword.isBlank()) {
                val errorException = Exception("Chưa cấu hình tài khoản SMTP gửi mail trong local.properties!")
                return@withContext Result.failure(errorException)
            }

            val props = Properties()
            props.put("mail.smtp.host", "smtp.gmail.com")
            props.put("mail.smtp.socketFactory.port", "465")
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            props.put("mail.smtp.auth", "true")
            props.put("mail.smtp.port", "465")

            try {
                val authenticator = object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        val auth = PasswordAuthentication(smtpEmail, smtpPassword)
                        return auth
                    }
                }
                val session = Session.getInstance(props, authenticator)

                val message = MimeMessage(session)
                message.setFrom(InternetAddress(smtpEmail, "MinLish App"))
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                message.setSubject("Mã xác thực đăng ký tài khoản MinLish")
                
                val emailContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 12px; background-color: #f9f9f9;\">" +
                        "<h2 style=\"color: #2e7d32; text-align: center;\">MinLish - Ứng dụng Học Tiếng Anh</h2>" +
                        "<hr style=\"border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                        "<p style=\"font-size: 16px; color: #333;\">Xin chào,</p>" +
                        "<p style=\"font-size: 16px; color: #333;\">Bạn đang thực hiện đăng ký tài khoản trên ứng dụng MinLish. Vui lòng sử dụng mã OTP dưới đây để hoàn tất quá trình xác thực email:</p>" +
                        "<div style=\"text-align: center; margin: 30px 0;\">" +
                        "<span style=\"font-size: 32px; font-weight: bold; color: #2e7d32; letter-spacing: 5px; padding: 10px 24px; border: 2px dashed #2e7d32; border-radius: 8px; background-color: #e8f5e9; display: inline-block;\">" + otpCode + "</span>" +
                        "</div>" +
                        "<p style=\"font-size: 14px; color: #ef5350; font-weight: bold; text-align: center;\">Mã xác thực có hiệu lực trong vòng 2 phút.</p>" +
                        "<hr style=\"border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                        "<p style=\"font-size: 12px; color: #777; text-align: center;\">Đây là email được gửi tự động, vui lòng không phản hồi lại email này.</p>" +
                        "</div>"
                
                message.setContent(emailContent, "text/html; charset=utf-8")

                Transport.send(message)
                return@withContext Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                val failMessage = "Lỗi gửi Email SMTP: " + (e.message ?: "Không rõ nguyên nhân")
                val failException = Exception(failMessage)
                return@withContext Result.failure(failException)
            }
        }
    }

    suspend fun sendReminderEmail(toEmail: String, subject: String, content: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val smtpEmail: String = BuildConfig.SMTP_EMAIL
            val smtpPassword: String = BuildConfig.SMTP_PASSWORD

            if (smtpEmail.isBlank() || smtpPassword.isBlank()) {
                val errorException: Exception = Exception("Chưa cấu hình tài khoản SMTP gửi mail trong local.properties!")
                return@withContext Result.failure(errorException)
            }

            val props: Properties = Properties()
            props.put("mail.smtp.host", "smtp.gmail.com")
            props.put("mail.smtp.socketFactory.port", "465")
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            props.put("mail.smtp.auth", "true")
            props.put("mail.smtp.port", "465")

            try {
                val authenticator: Authenticator = object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        val auth: PasswordAuthentication = PasswordAuthentication(smtpEmail, smtpPassword)
                        return auth
                    }
                }
                val session: Session = Session.getInstance(props, authenticator)

                val message: MimeMessage = MimeMessage(session)
                message.setFrom(InternetAddress(smtpEmail, "MinLish App"))
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                message.setSubject(subject)
                message.setContent(content, "text/html; charset=utf-8")

                Transport.send(message)
                return@withContext Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                val failMessage: String = "Lỗi gửi Email SMTP: " + (e.message ?: "Không rõ nguyên nhân")
                val failException: Exception = Exception(failMessage)
                return@withContext Result.failure(failException)
            }
        }
    }

    suspend fun sendForgotPasswordOtpEmail(toEmail: String, otpCode: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val smtpEmail: String = BuildConfig.SMTP_EMAIL
            val smtpPassword: String = BuildConfig.SMTP_PASSWORD

            if (smtpEmail.isBlank() || smtpPassword.isBlank()) {
                val errorException: Exception = Exception("Chưa cấu hình tài khoản SMTP gửi mail trong local.properties!")
                return@withContext Result.failure(errorException)
            }

            val props: Properties = Properties()
            props.put("mail.smtp.host", "smtp.gmail.com")
            props.put("mail.smtp.socketFactory.port", "465")
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            props.put("mail.smtp.auth", "true")
            props.put("mail.smtp.port", "465")

            try {
                val authenticator: Authenticator = object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        val auth: PasswordAuthentication = PasswordAuthentication(smtpEmail, smtpPassword)
                        return auth
                    }
                }
                val session: Session = Session.getInstance(props, authenticator)

                val message: MimeMessage = MimeMessage(session)
                message.setFrom(InternetAddress(smtpEmail, "MinLish App"))
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                message.setSubject("Mã xác minh khôi phục mật khẩu MinLish")

                val emailContent: String = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 12px; background-color: #f9f9f9;\">" +
                        "<h2 style=\"color: #1976d2; text-align: center;\">MinLish - Khôi Phục Mật Khẩu</h2>" +
                        "<hr style=\"border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                        "<p style=\"font-size: 16px; color: #333;\">Xin chào,</p>" +
                        "<p style=\"font-size: 16px; color: #333;\">Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản MinLish. Vui lòng sử dụng mã OTP dưới đây để hoàn tất việc xác minh:</p>" +
                        "<div style=\"text-align: center; margin: 30px 0;\">" +
                        "<span style=\"font-size: 32px; font-weight: bold; color: #1976d2; letter-spacing: 5px; padding: 10px 24px; border: 2px dashed #1976d2; border-radius: 8px; background-color: #e3f2fd; display: inline-block;\">" + otpCode + "</span>" +
                        "</div>" +
                        "<p style=\"font-size: 14px; color: #ef5350; font-weight: bold; text-align: center;\">Mã xác thực có hiệu lực trong vòng 2 phút. Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.</p>" +
                        "<hr style=\"border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                        "<p style=\"font-size: 12px; color: #777; text-align: center;\">Đây là email được gửi tự động, vui lòng không phản hồi lại email này.</p>" +
                        "</div>"

                message.setContent(emailContent, "text/html; charset=utf-8")

                Transport.send(message)
                return@withContext Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                val errMsgStr: String
                if (e.message != null) {
                    errMsgStr = e.message!!
                } else {
                    errMsgStr = "Không rõ nguyên nhân"
                }
                val failMessage: String = "Lỗi gửi Email SMTP: " + errMsgStr
                val failException: Exception = Exception(failMessage)
                return@withContext Result.failure(failException)
            }
        }
    }
}
