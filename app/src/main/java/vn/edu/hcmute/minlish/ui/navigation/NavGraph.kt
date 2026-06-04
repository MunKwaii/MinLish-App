package vn.edu.hcmute.minlish.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import vn.edu.hcmute.minlish.ui.auth.AuthViewModel
import vn.edu.hcmute.minlish.ui.auth.LoginScreen
import vn.edu.hcmute.minlish.ui.auth.ProfileScreen
import vn.edu.hcmute.minlish.ui.auth.RegisterScreen
import vn.edu.hcmute.minlish.ui.dashboard.DashboardScreen
import vn.edu.hcmute.minlish.ui.dashboard.DashboardViewModel
import vn.edu.hcmute.minlish.ui.learning.FlashcardScreen
import vn.edu.hcmute.minlish.ui.vocabulary.AddWordScreen
import vn.edu.hcmute.minlish.ui.vocabulary.DeckListScreen
import vn.edu.hcmute.minlish.ui.vocabulary.VocabViewModel
import vn.edu.hcmute.minlish.ui.vocabulary.WordListScreen

/**
 * Quản lý điều hướng chính trong ứng dụng.
 *
 * AuthViewModel được dùng cho các màn đăng nhập/hồ sơ.
 * VocabViewModel được dùng cho các màn quản lý bộ từ và từ vựng.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    vocabViewModel: VocabViewModel,
    dashboardViewModel: DashboardViewModel,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) {
                            inclusive = true
                        }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                authViewModel = authViewModel,
                dashboardViewModel = dashboardViewModel,
                onLogout = {
                    authViewModel.logout()

                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToLearning = {
                    navController.navigate(Screen.Learning.route)
                },
                onNavigateToVocabulary = {
                    navController.navigate(Screen.Vocabulary.route)
                },
                onToggleTheme = onToggleTheme,
                isDarkTheme = isDarkTheme
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Learning.route,
            arguments = listOf(
                navArgument("deckId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val deckIdString = backStackEntry.arguments?.getString("jwt_token") // Wait, the argument key is deckId! Let's get deckId.
            // Oh wait, in Routes.kt we have learning?deckId={deckId}, so key is "deckId".
            val deckIdParam = backStackEntry.arguments?.getString("deckId")
            val deckId = deckIdParam?.toIntOrNull()

            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as vn.edu.hcmute.minlish.MinLishApplication
            val currentUser by authViewModel.currentUser.collectAsState()
            val user = currentUser

            if (user != null) {
                val learningViewModel: vn.edu.hcmute.minlish.ui.learning.LearningViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = vn.edu.hcmute.minlish.ui.learning.LearningViewModelFactory(
                        userId = user.userId,
                        deckId = deckId,
                        vocabularyRepository = app.vocabularyRepository,
                        progressRepository = app.progressRepository,
                        settingsManager = app.settingsManager
                    )
                )
                FlashcardScreen(
                    viewModel = learningViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                MissingUserContent(
                    onBackToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Learning.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }

        composable(Screen.Vocabulary.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            val user = currentUser

            if (user == null) {
                MissingUserContent(
                    onBackToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Vocabulary.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            } else {
                DeckListScreen(
                    userId = user.userId,
                    viewModel = vocabViewModel,
                    onDeckClick = { deck ->
                        navController.navigate(
                            Screen.WordList.createRoute(deck.deckId)
                        )
                    }
                )
            }
        }

        composable(
            route = Screen.WordList.route,
            arguments = listOf(
                navArgument("deckId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getInt("deckId")

            if (deckId != null) {
                WordListScreen(
                    deckId = deckId,
                    viewModel = vocabViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onAddWordClick = {
                        navController.navigate(
                            Screen.AddWord.createRoute(deckId)
                        )
                    },
                    onStartLearningClick = { id ->
                        navController.navigate(
                            Screen.Learning.createRoute(id)
                        )
                    }
                )
            }
        }

        composable(
            route = Screen.AddWord.route,
            arguments = listOf(
                navArgument("deckId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getInt("deckId")

            if (deckId != null) {
                AddWordScreen(
                    deckId = deckId,
                    viewModel = vocabViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onWordSaved = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

/**
 * Hiển thị khi không lấy được thông tin người dùng hiện tại.
 *
 * Trường hợp này có thể xảy ra khi phiên đăng nhập hết hạn
 * hoặc currentUser chưa được khôi phục thành công.
 */
@Composable
private fun MissingUserContent(
    onBackToLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onBackToLogin
        ) {
            Text(text = "Không tìm thấy người dùng. Quay lại đăng nhập")
        }
    }
}