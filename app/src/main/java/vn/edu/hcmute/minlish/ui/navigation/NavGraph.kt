package vn.edu.hcmute.minlish.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import vn.edu.hcmute.minlish.ui.auth.AuthViewModel
import vn.edu.hcmute.minlish.ui.auth.LoginScreen
import vn.edu.hcmute.minlish.ui.auth.RegisterScreen
import vn.edu.hcmute.minlish.ui.auth.ProfileScreen
import vn.edu.hcmute.minlish.ui.dashboard.DashboardScreen
import vn.edu.hcmute.minlish.ui.learning.FlashcardScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
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
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToLearning = {
                    navController.navigate(Screen.Learning.route)
                }
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
        composable(Screen.Learning.route) {
            FlashcardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

