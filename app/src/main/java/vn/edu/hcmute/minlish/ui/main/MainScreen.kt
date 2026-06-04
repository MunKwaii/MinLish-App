package vn.edu.hcmute.minlish.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import vn.edu.hcmute.minlish.ui.auth.AuthViewModel
import vn.edu.hcmute.minlish.ui.auth.ProfileScreen
import vn.edu.hcmute.minlish.ui.dashboard.DashboardScreen
import vn.edu.hcmute.minlish.ui.dashboard.DashboardViewModel
import vn.edu.hcmute.minlish.ui.settings.SettingsScreen
import vn.edu.hcmute.minlish.ui.vocabulary.DeckListScreen
import vn.edu.hcmute.minlish.ui.vocabulary.VocabViewModel

enum class MainTab {
    Home,
    Vocabulary,
    Profile,
    Settings
}

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    vocabViewModel: VocabViewModel,
    onLogout: () -> Unit,
    onNavigateToLearning: () -> Unit,
    onNavigateToWordList: (Int) -> Unit,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(MainTab.Home) }
    val currentUser by authViewModel.currentUser.collectAsState()
    val userId = currentUser?.userId ?: 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == MainTab.Home,
                    onClick = { selectedTab = MainTab.Home },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Vocabulary,
                    onClick = { selectedTab = MainTab.Vocabulary },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Bộ từ") },
                    label = { Text("Bộ từ") }
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Profile,
                    onClick = { selectedTab = MainTab.Profile },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Hồ sơ") },
                    label = { Text("Hồ sơ") }
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.Settings,
                    onClick = { selectedTab = MainTab.Settings },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Cài đặt") },
                    label = { Text("Cài đặt") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedTab) {
                MainTab.Home -> {
                    DashboardScreen(
                        authViewModel = authViewModel,
                        dashboardViewModel = dashboardViewModel,
                        onLogout = onLogout,
                        onNavigateToProfile = { selectedTab = MainTab.Profile },
                        onNavigateToLearning = onNavigateToLearning,
                        onNavigateToVocabulary = { selectedTab = MainTab.Vocabulary },
                        onToggleTheme = onToggleTheme,
                        isDarkTheme = isDarkTheme
                    )
                }
                MainTab.Vocabulary -> {
                    DeckListScreen(
                        userId = userId,
                        viewModel = vocabViewModel,
                        onDeckClick = { deck ->
                            onNavigateToWordList(deck.deckId)
                        }
                    )
                }
                MainTab.Profile -> {
                    ProfileScreen(
                        authViewModel = authViewModel,
                        onNavigateBack = { selectedTab = MainTab.Home },
                        showBackButton = false
                    )
                }
                MainTab.Settings -> {
                    SettingsScreen(
                        authViewModel = authViewModel,
                        onLogout = onLogout,
                        onToggleTheme = onToggleTheme,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}
