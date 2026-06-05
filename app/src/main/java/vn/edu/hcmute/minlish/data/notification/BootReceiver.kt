package vn.edu.hcmute.minlish.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import vn.edu.hcmute.minlish.MinLishApplication

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action: String? = intent.action
        if (Intent.ACTION_BOOT_COMPLETED == action) {
            val pendingResult: BroadcastReceiver.PendingResult = goAsync()
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val app: MinLishApplication = context.applicationContext as MinLishApplication
                    val dailyEnabled: Boolean = app.settingsManager.dailyReminderEnabledFlow.first()
                    
                    if (dailyEnabled) {
                        val reminderTime: String = app.settingsManager.dailyReminderTimeFlow.first()
                        AlarmScheduler.scheduleDailyAlarm(context, reminderTime)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
