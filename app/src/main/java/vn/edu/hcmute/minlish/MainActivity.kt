package vn.edu.hcmute.minlish

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
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
import vn.edu.hcmute.minlish.ui.dictionary.DictionaryViewModel
import vn.edu.hcmute.minlish.ui.dictionary.DictionaryViewModelFactory

class MainActivity : FragmentActivity() {

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

    private val dictionaryViewModel: DictionaryViewModel by viewModels {
        val app = application as MinLishApplication
        DictionaryViewModelFactory(app.vocabularyRepository)
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

        val settingsManager = (application as MinLishApplication).settingsManager
        val sessionManager = (application as MinLishApplication).sessionManager

        // Tự động khôi phục phiên đăng nhập từ JWT Token nếu được phép bảo mật sinh trắc
        lifecycleScope.launch {
            val biometricEnabled: Boolean = settingsManager.biometricEnabledFlow.first()
            val hasToken: Boolean = sessionManager.getToken() != null

            if (biometricEnabled == true && hasToken == true) {
                val biometricManager: BiometricManager = BiometricManager.from(this@MainActivity)
                val canAuthenticate: Int = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                    showBiometricPrompt()
                } else {
                    authViewModel.autoLogin()
                }
            } else {
                authViewModel.autoLogin()
            }
        }

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
                        dictionaryViewModel = dictionaryViewModel,
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

    private fun showBiometricPrompt() {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val callback: BiometricPrompt.AuthenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // Giữ nguyên token để người dùng có thể thử lại bằng nút vân tay ở LoginScreen
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                authViewModel.autoLogin()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        }

        val biometricPrompt: BiometricPrompt = BiometricPrompt(this, executor, callback)
        val promptInfo: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Đăng nhập vân tay")
            .setSubtitle("Xác thực vân tay của bạn để tiếp tục vào MinLish")
            .setNegativeButtonText("Sử dụng mật khẩu")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}