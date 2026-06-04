package vn.edu.hcmute.minlish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import vn.edu.hcmute.minlish.ui.auth.AuthViewModel
import vn.edu.hcmute.minlish.ui.auth.AuthViewModelFactory
import vn.edu.hcmute.minlish.ui.dashboard.DashboardViewModel
import vn.edu.hcmute.minlish.ui.dashboard.DashboardViewModelFactory
import vn.edu.hcmute.minlish.ui.navigation.NavGraph
import vn.edu.hcmute.minlish.ui.theme.MinLishTheme
import vn.edu.hcmute.minlish.ui.vocabulary.VocabViewModel
import vn.edu.hcmute.minlish.ui.vocabulary.VocabViewModelFactory

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        val app = application as MinLishApplication
        AuthViewModelFactory(app.userRepository, app.sessionManager)
    }

    private val vocabViewModel: VocabViewModel by viewModels {
        val app = application as MinLishApplication
        VocabViewModelFactory(app.vocabularyRepository)
    }

    private val dashboardViewModel: DashboardViewModel by viewModels {
        val app = application as MinLishApplication
        DashboardViewModelFactory(app.progressRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Tự động khôi phục phiên đăng nhập từ JWT Token nếu có.
        authViewModel.autoLogin()

        setContent {
            MinLishTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        NavGraph(
                            navController = navController,
                            authViewModel = authViewModel,
                            vocabViewModel = vocabViewModel,
                            dashboardViewModel = dashboardViewModel
                        )
                    }
                }
            }
        }
    }
}