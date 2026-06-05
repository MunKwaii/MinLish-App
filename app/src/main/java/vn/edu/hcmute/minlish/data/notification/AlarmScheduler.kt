package vn.edu.hcmute.minlish.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object AlarmScheduler {

    fun scheduleDailyAlarm(context: Context, timeStr: String) {
        val alarmManager: AlarmManager? = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            return
        }

        val intent: Intent = Intent(context, NotificationReceiver::class.java)
        val flags: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            flags = PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            intent,
            flags
        )

        val parts: List<String> = timeStr.split(":")
        if (parts.size != 2) {
            return
        }

        val hourStr: String = parts[0]
        val minuteStr: String = parts[1]
        
        val hour: Int = hourStr.toIntOrNull() ?: 20
        val minute: Int = minuteStr.toIntOrNull() ?: 0

        val calendar: Calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelDailyAlarm(context: Context) {
        val alarmManager: AlarmManager? = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            return
        }

        val intent: Intent = Intent(context, NotificationReceiver::class.java)
        val flags: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            flags = PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            intent,
            flags
        )

        alarmManager.cancel(pendingIntent)
    }
}
