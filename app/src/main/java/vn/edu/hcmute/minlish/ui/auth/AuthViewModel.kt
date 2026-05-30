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
import vn.edu.hcmute.minlish.data.util.JwtHelper
import vn.edu.hcmute.minlish.data.util.SessionManager

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

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
                if (user != null) {
                    var isPasswordCorrect = false
                    try {
                        isPasswordCorrect = org.mindrot.jbcrypt.BCrypt.checkpw(passwordHash, user.passwordHash)
                    } catch (e: Exception) {
                        isPasswordCorrect = false
                    }

                    if (isPasswordCorrect) {
                        val token = JwtHelper.generateToken(user.email, user.userId)
                        sessionManager.saveToken(token)
                        _currentUser.value = user
                        _uiState.value = AuthUiState.Success(user)
                    } else {
                        _uiState.value = AuthUiState.Error("Email hoặc mật khẩu không chính xác!")
                    }
                } else {
                    _uiState.value = AuthUiState.Error("Email hoặc mật khẩu không chính xác!")
                }
            } catch (e: Exception) {
                val errMsg = e.localizedMessage
                if (errMsg != null) {
                    _uiState.value = AuthUiState.Error(errMsg)
                } else {
                    _uiState.value = AuthUiState.Error("Đã xảy ra lỗi hệ thống")
                }
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
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    if (user != null) {
                        val token = JwtHelper.generateToken(user.email, user.userId)
                        sessionManager.saveToken(token)
                        _currentUser.value = user
                        _uiState.value = AuthUiState.Success(user)
                    } else {
                        _uiState.value = AuthUiState.Error("Đăng nhập Google thất bại!")
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    val msg = exception?.message
                    if (msg != null) {
                        _uiState.value = AuthUiState.Error(msg)
                    } else {
                        _uiState.value = AuthUiState.Error("Đăng nhập Google thất bại!")
                    }
                }
            } catch (e: Exception) {
                val errMsg = e.localizedMessage
                if (errMsg != null) {
                    _uiState.value = AuthUiState.Error(errMsg)
                } else {
                    _uiState.value = AuthUiState.Error("Đã xảy ra lỗi hệ thống")
                }
            }
        }
    }

    fun register(
        email: String,
        name: String,
        passwordHash: String,
        learningGoal: String,
        level: String,
        userType: String
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
                    val userToSave = User(
                        email = email,
                        name = name,
                        passwordHash = passwordHash,
                        learningGoal = learningGoal,
                        level = level,
                        userType = userType
                    )
                    tempUser = userToSave
                    val otp = generateRandomOtp()
                    
                    // Gửi email thật chứa mã OTP
                    val emailResult = EmailSender.sendOtpEmail(email, otp)
                    if (emailResult.isSuccess) {
                        _generatedOtp.value = otp
                        otpExpiryTime = System.currentTimeMillis() + 120_000
                        _showOtpScreen.value = true
                        _uiState.value = AuthUiState.Idle
                    } else {
                        val exception = emailResult.exceptionOrNull()
                        val msg = exception?.localizedMessage
                        if (msg != null) {
                            _uiState.value = AuthUiState.Error("Không gửi được email xác thực: " + msg)
                        } else {
                            _uiState.value = AuthUiState.Error("Không gửi được email xác thực!")
                        }
                    }
                }
            } catch (e: Exception) {
                val errMsg = e.localizedMessage
                if (errMsg != null) {
                    _uiState.value = AuthUiState.Error(errMsg)
                } else {
                    _uiState.value = AuthUiState.Error("Đã xảy ra lỗi hệ thống")
                }
            }
        }
    }

    fun verifyOtp(enteredOtp: String) {
        if (enteredOtp.length != 6) {
            _uiState.value = AuthUiState.Error("Mã OTP phải có đúng 6 chữ số!")
            return
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime > otpExpiryTime) {
            _uiState.value = AuthUiState.Error("Mã OTP đã hết hạn! Vui lòng gửi lại mã mới.")
            return
        }

        val expectedOtp = _generatedOtp.value
        if (enteredOtp != expectedOtp) {
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
                if (result.isSuccess) {
                    val id = result.getOrNull()
                    if (id != null) {
                        val registeredUser = userToRegister.copy(userId = id.toInt())
                        val token = JwtHelper.generateToken(registeredUser.email, registeredUser.userId)
                        sessionManager.saveToken(token)
                        _currentUser.value = registeredUser
                        _uiState.value = AuthUiState.Success(registeredUser)
                        _showOtpScreen.value = false
                        _generatedOtp.value = null
                        tempUser = null
                    } else {
                        _uiState.value = AuthUiState.Error("Đăng ký thất bại!")
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    val msg = exception?.message
                    if (msg != null) {
                        _uiState.value = AuthUiState.Error(msg)
                    } else {
                        _uiState.value = AuthUiState.Error("Đăng ký thất bại!")
                    }
                }
            } catch (e: Exception) {
                val errMsg = e.localizedMessage
                if (errMsg != null) {
                    _uiState.value = AuthUiState.Error(errMsg)
                } else {
                    _uiState.value = AuthUiState.Error("Đã xảy ra lỗi hệ thống")
                }
            }
        }
    }

    fun resendOtp() {
        val user = tempUser
        if (user == null) {
            _uiState.value = AuthUiState.Error("Không tìm thấy thông tin đăng ký để gửi lại mã!")
            return
        }
        
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val email = user.email
            val otp = generateRandomOtp()
            val emailResult = EmailSender.sendOtpEmail(email, otp)
            if (emailResult.isSuccess) {
                _generatedOtp.value = otp
                otpExpiryTime = System.currentTimeMillis() + 120_000
                _uiState.value = AuthUiState.Idle
            } else {
                val exception = emailResult.exceptionOrNull()
                val msg = exception?.localizedMessage
                if (msg != null) {
                    _uiState.value = AuthUiState.Error("Không gửi lại được mã OTP: " + msg)
                } else {
                    _uiState.value = AuthUiState.Error("Không gửi lại được mã OTP!")
                }
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
        var otp = ""
        for (i in 1..6) {
            val randomDigit = ('0'..'9').random()
            otp = otp + randomDigit
        }
        return otp
    }

    fun autoLogin() {
        val token = sessionManager.getToken()
        if (token != null) {
            val payload = JwtHelper.validateAndParseToken(token)
            if (payload != null) {
                val email = payload.optString("email")
                if (email.isNotEmpty()) {
                    _uiState.value = AuthUiState.Loading
                    viewModelScope.launch {
                        try {
                            val user = userRepository.getUserByEmail(email)
                            if (user != null) {
                                _currentUser.value = user
                                _uiState.value = AuthUiState.Success(user)
                            } else {
                                _uiState.value = AuthUiState.Error("Không tìm thấy thông tin tài khoản phiên đăng nhập!")
                            }
                        } catch (e: Exception) {
                            _uiState.value = AuthUiState.Error("Lỗi tự động đăng nhập")
                        }
                    }
                }
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _currentUser.value = null
        _uiState.value = AuthUiState.Idle
    }

    fun updateProfile(
        name: String,
        userType: String,
        learningGoal: String,
        level: String
    ) {
        val user = _currentUser.value
        if (user == null) {
            _uiState.value = AuthUiState.Error("Không tìm thấy thông tin người dùng!")
            return
        }

        if (name.isBlank()) {
            _uiState.value = AuthUiState.Error("Tên không được để trống!")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val updatedUser = user.copy(
                    name = name,
                    userType = userType,
                    learningGoal = learningGoal,
                    level = level
                )
                val result = userRepository.updateUser(updatedUser)
                if (result.isSuccess) {
                    _currentUser.value = updatedUser
                    _uiState.value = AuthUiState.Success(updatedUser)
                } else {
                    val exception = result.exceptionOrNull()
                    val msg = exception?.message
                    if (msg != null) {
                        _uiState.value = AuthUiState.Error(msg)
                    } else {
                        _uiState.value = AuthUiState.Error("Cập nhật hồ sơ thất bại!")
                    }
                }
            } catch (e: Exception) {
                val errMsg = e.localizedMessage
                if (errMsg != null) {
                    _uiState.value = AuthUiState.Error(errMsg)
                } else {
                    _uiState.value = AuthUiState.Error("Đã xảy ra lỗi hệ thống")
                }
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AuthUiState.Idle
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}

class AuthViewModelFactory(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val factoryResult = AuthViewModel(userRepository, sessionManager) as T
            return factoryResult
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
