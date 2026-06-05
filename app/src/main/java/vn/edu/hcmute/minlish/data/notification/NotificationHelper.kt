package vn.edu.hcmute.minlish.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import vn.edu.hcmute.minlish.MainActivity

object NotificationHelper {

    private const val CHANNEL_DAILY_ID: String = "daily_study_reminder"
    private const val CHANNEL_DAILY_NAME: String = "Nhắc nhở học tập hàng ngày"
    private const val CHANNEL_DAILY_DESC: String = "Thông báo nhắc nhở người dùng học tập mỗi ngày để giữ streak"

    private const val CHANNEL_DUE_ID: String = "due_words_reminder"
    private const val CHANNEL_DUE_NAME: String = "Nhắc nhở ôn tập từ vựng"
    private const val CHANNEL_DUE_DESC: String = "Thông báo khi có từ vựng đến hạn ôn tập"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val dailyChannel: NotificationChannel = NotificationChannel(
                CHANNEL_DAILY_ID,
                CHANNEL_DAILY_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            dailyChannel.description = CHANNEL_DAILY_DESC

            val dueChannel: NotificationChannel = NotificationChannel(
                CHANNEL_DUE_ID,
                CHANNEL_DUE_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            dueChannel.description = CHANNEL_DUE_DESC

            val notificationManager: NotificationManager? = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(dailyChannel)
                notificationManager.createNotificationChannel(dueChannel)
            }
        }
    }

    fun showDailyReminder(context: Context) {
        val intent: Intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        
        val flags: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            flags = PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            flags
        )

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, CHANNEL_DAILY_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Đã đến giờ học tiếng Anh rồi!")
            .setContentText("Hãy vào MinLish học từ mới để duy trì streak học tập nhé!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(1, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showDueWordsReminder(context: Context, dueCount: Int) {
        val intent: Intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        
        val flags: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            flags = PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            1002,
            intent,
            flags
        )

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, CHANNEL_DUE_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Đến hạn ôn tập từ vựng!")
            .setContentText("Bạn có " + dueCount + " từ vựng cần ôn tập ngay hôm nay.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(2, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
