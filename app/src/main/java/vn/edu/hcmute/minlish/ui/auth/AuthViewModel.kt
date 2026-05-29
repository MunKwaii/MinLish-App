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
import vn.edu.hcmute.minlish.data.util.EmailSender

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

    private val _showOtpScreen = MutableStateFlow(false)
    val showOtpScreen: StateFlow<Boolean> = _showOtpScreen.asStateFlow()

    private val _generatedOtp = MutableStateFlow<String?>(null)
    val generatedOtp: StateFlow<String?> = _generatedOtp.asStateFlow()

    private var tempUser: User? = null
    private var otpExpiryTime = 0L

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
                val existing = userRepository.getUserByEmail(email)
                if (existing != null) {
                    _uiState.value = AuthUiState.Error("Email đã tồn tại trong hệ thống!")
                } else {
                    tempUser = User(
                        email = email,
                        name = name,
                        passwordHash = passwordHash,
                        learningGoal = learningGoal,
                        level = level
                    )
                    val otp = generateRandomOtp()
                    
                    // Gửi email thật chứa mã OTP
                    val emailResult = EmailSender.sendOtpEmail(email, otp)
                    emailResult.onSuccess {
                        _generatedOtp.value = otp
                        otpExpiryTime = System.currentTimeMillis() + 120_000
                        _showOtpScreen.value = true
                        _uiState.value = AuthUiState.Idle
                    }.onFailure { exception ->
                        _uiState.value = AuthUiState.Error("Không gửi được email xác thực: ${exception.localizedMessage}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Đã xảy ra lỗi hệ thống")
            }
        }
    }

    fun verifyOtp(enteredOtp: String) {
        if (enteredOtp.length != 6) {
            _uiState.value = AuthUiState.Error("Mã OTP phải có đúng 6 chữ số!")
            return
        }

        if (System.currentTimeMillis() > otpExpiryTime) {
            _uiState.value = AuthUiState.Error("Mã OTP đã hết hạn! Vui lòng gửi lại mã mới.")
            return
        }

        if (enteredOtp != _generatedOtp.value) {
            _uiState.value = AuthUiState.Error("Mã OTP nhập vào không chính xác!")
            return
        }

        val userToRegister = tempUser
        if (userToRegister == null) {
            _uiState.value = AuthUiState.Error("Không tìm thấy thông tin đăng ký!")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val result = userRepository.registerUser(userToRegister)
                result.onSuccess { id ->
                    val registeredUser = userToRegister.copy(userId = id.toInt())
                    _currentUser.value = registeredUser
                    _uiState.value = AuthUiState.Success(registeredUser)
                    _showOtpScreen.value = false
                    _generatedOtp.value = null
                    tempUser = null
                }
                result.onFailure { exception ->
                    _uiState.value = AuthUiState.Error(exception.message ?: "Đăng ký thất bại!")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Đã xảy ra lỗi hệ thống")
            }
        }
    }

    fun resendOtp() {
        if (tempUser == null) {
            _uiState.value = AuthUiState.Error("Không tìm thấy thông tin đăng ký để gửi lại mã!")
            return
        }
        
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val email = tempUser?.email ?: ""
            val otp = generateRandomOtp()
            val emailResult = EmailSender.sendOtpEmail(email, otp)
            emailResult.onSuccess {
                _generatedOtp.value = otp
                otpExpiryTime = System.currentTimeMillis() + 120_000
                _uiState.value = AuthUiState.Idle
            }.onFailure { exception ->
                _uiState.value = AuthUiState.Error("Không gửi lại được mã OTP: ${exception.localizedMessage}")
            }
        }
    }

    fun cancelRegistration() {
        _showOtpScreen.value = false
        _generatedOtp.value = null
        tempUser = null
        _uiState.value = AuthUiState.Idle
    }

    private fun generateRandomOtp(): String {
        return (1..6).map { ('0'..'9').random() }.joinToString("")
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
