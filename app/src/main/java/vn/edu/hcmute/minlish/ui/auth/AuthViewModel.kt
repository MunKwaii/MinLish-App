package vn.edu.hcmute.minlish.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vn.edu.hcmute.minlish.data.local.entity.User
import vn.edu.hcmute.minlish.data.repository.UserRepository

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun login(email: String, passwordHash: String) {
        if (email.isBlank() || passwordHash.isBlank()) {
            _uiState.value = AuthUiState.Error("Vui lòng điền đầy đủ thông tin đăng nhập!")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val user = userRepository.getUserByEmail(email)
                if (user != null && user.passwordHash == passwordHash) {
                    _currentUser.value = user
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    _uiState.value = AuthUiState.Error("Email hoặc mật khẩu không chính xác!")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Đã xảy ra lỗi hệ thống")
            }
        }
    }

    fun loginWithGoogle(email: String, name: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Tài khoản Google không hợp lệ!")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val result = userRepository.loginWithGoogle(email, name)
                result.onSuccess { user ->
                    _currentUser.value = user
                    _uiState.value = AuthUiState.Success(user)
                }
                result.onFailure { exception ->
                    _uiState.value = AuthUiState.Error(exception.message ?: "Đăng nhập Google thất bại!")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Đã xảy ra lỗi hệ thống")
            }
        }
    }

    fun register(
        email: String,
        name: String,
        passwordHash: String,
        learningGoal: String,
        level: String
    ) {
        if (email.isBlank() || name.isBlank() || passwordHash.isBlank()) {
            _uiState.value = AuthUiState.Error("Vui lòng nhập đầy đủ email, họ tên và mật khẩu!")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val newUser = User(
                    email = email,
                    name = name,
                    passwordHash = passwordHash,
                    learningGoal = learningGoal,
                    level = level
                )
                val result = userRepository.registerUser(newUser)
                result.onSuccess { id ->
                    val registeredUser = newUser.copy(userId = id.toInt())
                    _currentUser.value = registeredUser
                    _uiState.value = AuthUiState.Success(registeredUser)
                }
                result.onFailure { exception ->
                    _uiState.value = AuthUiState.Error(exception.message ?: "Đăng ký thất bại!")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Đã xảy ra lỗi hệ thống")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _uiState.value = AuthUiState.Idle
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}

class AuthViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
