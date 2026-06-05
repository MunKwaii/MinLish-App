package vn.edu.hcmute.minlish.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import vn.edu.hcmute.minlish.MinLishApplication
import vn.edu.hcmute.minlish.data.local.entity.StudyProgress
import vn.edu.hcmute.minlish.data.local.entity.Word
import vn.edu.hcmute.minlish.data.util.EmailSender
import vn.edu.hcmute.minlish.data.util.JwtHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult: BroadcastReceiver.PendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app: MinLishApplication = context.applicationContext as MinLishApplication
                val sessionToken: String? = app.sessionManager.getToken()
                
                if (sessionToken == null) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Báo thức MinLish: Không tìm thấy phiên đăng nhập!", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                val payload: JSONObject? = JwtHelper.validateAndParseToken(sessionToken)
                if (payload == null) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Báo thức MinLish: Hết hạn phiên đăng nhập!", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                val email: String = payload.optString("email")
                val userId: Int = payload.optInt("userId", -1)
                if (email.isEmpty() || userId == -1) {
                    return@launch
                }

                val dailyEnabled: Boolean = app.settingsManager.dailyReminderEnabledFlow.first()
                val dueEnabled: Boolean = app.settingsManager.dueWordsReminderEnabledFlow.first()
                val emailAlertEnabled: Boolean = app.settingsManager.emailNotificationEnabledFlow.first()
                val pushEnabled: Boolean = app.settingsManager.pushNotificationEnabledFlow.first()

                val currentTime: Long = System.currentTimeMillis()
                val isTestAlarm: Boolean = (intent.action == "vn.edu.hcmute.minlish.TEST_ALARM")

                val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayDateStr: String = dateFormat.format(Date(currentTime))
                val progress: StudyProgress? = app.progressRepository.getProgressByDate(userId, todayDateStr)

                val dueWordsList: List<Word> = app.database.wordDao().getAllWordsDueForReviewByUser(userId, currentTime).first()
                var dueCount: Int = dueWordsList.size
                if (isTestAlarm == true) {
                    dueCount = 3
                }

                val hasStudied: Boolean
                if (progress == null) {
                    hasStudied = false
                } else {
                    if (progress.newWordsLearned > 0 || progress.wordsReviewed > 0) {
                        hasStudied = true
                    } else {
                        hasStudied = false
                    }
                }

                launch(Dispatchers.Main) {
                    val statusText: String
                    if (hasStudied == true) {
                        statusText = "Rồi"
                    } else {
                        statusText = "Chưa"
                    }
                    val toastMessage: String = "Báo thức MinLish đã kích hoạt!\n- Đã học hôm nay: " + statusText + "\n- Số từ đến hạn ôn: " + dueCount
                    Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
                }

                if (dailyEnabled) {
                    var needsReminder: Boolean = false
                    if (isTestAlarm == true) {
                        needsReminder = true
                    } else if (progress == null) {
                        needsReminder = true
                    } else {
                        if (progress.newWordsLearned == 0 && progress.wordsReviewed == 0) {
                            needsReminder = true
                        }
                    }

                    if (needsReminder) {
                        if (pushEnabled) {
                            NotificationHelper.showDailyReminder(context)
                        }
                        if (emailAlertEnabled) {
                            val subject: String = "MinLish - Đã đến giờ học tiếng Anh rồi! (Báo thức test)"
                            val emailBody: String = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 12px; background-color: #f9f9f9;\">" +
                                    "<h2 style=\"color: #1976d2; text-align: center;\">MinLish - Học Tập Mỗi Ngày</h2>" +
                                    "<hr style=\"border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                                    "<p style=\"font-size: 16px; color: #333;\">Xin chào,</p>" +
                                    "<p style=\"font-size: 16px; color: #333;\">Hôm nay bạn chưa học từ vựng nào trên MinLish. Hãy dành 5 phút vào học để duy trì streak học tập và không bỏ lỡ thói quen học tập hàng ngày nhé!</p>" +
                                    "<div style=\"text-align: center; margin: 30px 0;\">" +
                                    "<a href=\"#\" style=\"font-size: 18px; font-weight: bold; color: #ffffff; background-color: #1976d2; padding: 12px 24px; text-decoration: none; border-radius: 8px; display: inline-block;\">Mở App MinLish Ngay</a>" +
                                    "</div>" +
                                    "<hr style=\"border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                                    "<p style=\"font-size: 12px; color: #777; text-align: center;\">Đây là email được gửi tự động từ MinLish, vui lòng không phản hồi lại email này.</p>" +
                                    "</div>"
                            EmailSender.sendReminderEmail(email, subject, emailBody)
                        }
                    }
                }

                if (dueEnabled) {
                    val dueWordsList: List<Word> = app.database.wordDao().getAllWordsDueForReviewByUser(userId, currentTime).first()
                    var dueCount: Int = dueWordsList.size
                    if (isTestAlarm == true) {
                        dueCount = 3
                    }
                    if (dueCount > 0) {
                        if (pushEnabled) {
                            NotificationHelper.showDueWordsReminder(context, dueCount)
                        }
                        if (emailAlertEnabled) {
                            val subject: String = "MinLish - Có " + dueCount + " từ vựng đến hạn ôn tập! (Báo thức test)"
                            val emailBody: String = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 12px; background-color: #f9f9f9;\">" +
                                    "<h2 style=\"color: #e53935; text-align: center;\">MinLish - Đến Hạn Ôn Tập</h2>" +
                                    "<hr style=\"border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                                    "<p style=\"font-size: 16px; color: #333;\">Xin chào,</p>" +
                                    "<p style=\"font-size: 16px; color: #333;\">Bạn có <strong>" + dueCount + "</strong> từ vựng đã đến hạn ôn tập theo thuật toán lặp lại ngắt quãng (Spaced Repetition). Hãy ôn tập ngay để tránh bị quên các từ này nhé!</p>" +
                                    "<div style=\"text-align: center; margin: 30px 0;\">" +
                                    "<a href=\"#\" style=\"font-size: 18px; font-weight: bold; color: #ffffff; background-color: #e53935; padding: 12px 24px; text-decoration: none; border-radius: 8px; display: inline-block;\">Ôn Tập Ngay</a>" +
                                    "</div>" +
                                    "<hr style=\"border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                                    "<p style=\"font-size: 12px; color: #777; text-align: center;\">Đây là email được gửi tự động từ MinLish, vui lòng không phản hồi lại email này.</p>" +
                                    "</div>"
                            EmailSender.sendReminderEmail(email, subject, emailBody)
                        }
                    }
                }

                if (isTestAlarm == false) {
                    if (dailyEnabled == true) {
                        val reminderTime: String = app.settingsManager.dailyReminderTimeFlow.first()
                        AlarmScheduler.scheduleDailyAlarm(context, reminderTime)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
