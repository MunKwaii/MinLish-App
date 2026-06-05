package vn.edu.hcmute.minlish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.lifecycle.lifecycleScope
import vn.edu.hcmute.minlish.ui.auth.AuthViewModel
import vn.edu.hcmute.minlish.ui.auth.AuthViewModelFactory
import vn.edu.hcmute.minlish.ui.dashboard.DashboardViewModel
import vn.edu.hcmute.minlish.ui.dashboard.DashboardViewModelFactory
import vn.edu.hcmute.minlish.ui.navigation.NavGraph
import vn.edu.hcmute.minlish.ui.theme.MinLishTheme
import vn.edu.hcmute.minlish.ui.vocabulary.VocabViewModel
import vn.edu.hcmute.minlish.ui.vocabulary.VocabViewModelFactory

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Không cần làm gì khi nhận được kết quả cấp quyền
    }

    private val authViewModel: AuthViewModel by viewModels {
        val app = application as MinLishApplication
        AuthViewModelFactory(app.userRepository, app.sessionManager)
    }

    private val vocabViewModel: VocabViewModel by viewModels {
        val app = application as MinLishApplication
        VocabViewModelFactory(app.vocabularyRepository)
    }

    private val dashboardViewModel: DashboardViewModel by viewModels {
        val app: MinLishApplication = application as MinLishApplication
        DashboardViewModelFactory(app.progressRepository, app.database.wordDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Khởi tạo kênh thông báo (Notification Channel)
        vn.edu.hcmute.minlish.data.notification.NotificationHelper.createNotificationChannels(this)

        // Yêu cầu quyền gửi thông báo trên Android 13 trở lên
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permission: String = android.Manifest.permission.POST_NOTIFICATIONS
            val checkResult: Int = androidx.core.content.ContextCompat.checkSelfPermission(this, permission)
            if (checkResult != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(permission)
            }
        }

        // Tự động khôi phục phiên đăng nhập từ JWT Token nếu có.
        authViewModel.autoLogin()

        val settingsManager = (application as MinLishApplication).settingsManager

        // Tự động lên lịch báo thức nhắc học tập hàng ngày nếu được bật
        lifecycleScope.launch {
            val enabled: Boolean = settingsManager.dailyReminderEnabledFlow.first()
            if (enabled == true) {
                val time: String = settingsManager.dailyReminderTimeFlow.first()
                vn.edu.hcmute.minlish.data.notification.AlarmScheduler.scheduleDailyAlarm(this@MainActivity, time)
            }
        }

        setContent {
            val coroutineScope = rememberCoroutineScope()
            val isDarkModePref by settingsManager.darkModeFlow.collectAsState(initial = null)
            val isDarkTheme = isDarkModePref ?: isSystemInDarkTheme()

            MinLishTheme(darkTheme = isDarkTheme) {
                Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize()
                ) {
                    NavGraph(
                        navController = rememberNavController(),
                        authViewModel = authViewModel,
                        vocabViewModel = vocabViewModel,
                        dashboardViewModel = dashboardViewModel,
                        onToggleTheme = {
                            coroutineScope.launch {
                                settingsManager.toggleDarkMode(isDarkModePref)
                            }
                        },
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}