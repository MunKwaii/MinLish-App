package vn.edu.hcmute.minlish.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()

    var showForgotEmailDialog: Boolean by remember { mutableStateOf(false) }
    var forgotEmailText: String by remember { mutableStateOf("") }
    var newPasswordText: String by remember { mutableStateOf("") }
    var newPasswordVisible: Boolean by remember { mutableStateOf(false) }
    var enteredForgotOtp: String by remember { mutableStateOf("") }

    val showForgotPasswordOtp: Boolean by authViewModel.showForgotPasswordOtp.collectAsState()
    val showResetPasswordScreen: Boolean by authViewModel.showResetPasswordScreen.collectAsState()
    val forgotPasswordEmail: String? by authViewModel.forgotPasswordEmail.collectAsState()

    // Cấu hình Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("710966620041-dbpou1jis4h9ucihsi6kpcc8da8sp18i.apps.googleusercontent.com") // Web Client ID
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    // Xóa bộ nhớ đệm Google Sign-In khi vào màn hình Đăng nhập để lần sau người dùng có thể chọn tài khoản khác
    LaunchedEffect(Unit) {
        googleSignInClient.signOut()
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val userEmail = account?.email ?: ""
            val displayName = account?.displayName ?: "Google User"
            authViewModel.loginWithGoogle(userEmail, displayName)
        } catch (e: ApiException) {
            authViewModel.clearError()
        }
    }

    // Điều hướng khi đăng nhập thành công
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / Tiêu đề ứng dụng
            Text(
                text = "MinLish",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Học tiếng Anh thông minh mỗi ngày",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(40.dp))

            // Card chứa Form đăng nhập
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ĐĂNG NHẬP",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Ô nhập Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { textVal ->
                            email = textVal
                            authViewModel.clearError()
                        },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email Icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ô nhập Mật khẩu
                    val visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    }

                    val passwordIcon = if (passwordVisible) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { textVal ->
                            password = textVal
                            authViewModel.clearError()
                        },
                        label = { Text("Mật khẩu") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password Icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = passwordIcon, contentDescription = "Toggle password visibility")
                            }
                        },
                        visualTransformation = visualTransformation,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                showForgotEmailDialog = true
                                authViewModel.clearError()
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Quên mật khẩu?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Thông báo lỗi nếu có
                    if (uiState is AuthUiState.Error) {
                        Text(
                            text = (uiState as AuthUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                    }

                    // Nút Đăng nhập
                    Button(
                        onClick = { authViewModel.login(email.trim(), password) },
                        enabled = uiState !is AuthUiState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Đăng nhập",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Đường gạch ngang "Hoặc"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = " hoặc ",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nút Đăng nhập bằng Google
                    OutlinedButton(
                        onClick = {
                            val signInIntent = googleSignInClient.signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        },
                        enabled = uiState !is AuthUiState.Loading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleLogoIcon(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Tiếp tục với Google",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Liên kết đăng ký tài khoản mới
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chưa có tài khoản? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = {
                        authViewModel.clearError()
                        onNavigateToRegister()
                    }
                ) {
                    Text(
                        text = "Đăng ký ngay",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // 1. Dialog Nhập Email Khôi Phục
    if (showForgotEmailDialog == true) {
        AlertDialog(
            onDismissRequest = {
                showForgotEmailDialog = false
                authViewModel.clearError()
            },
            title = {
                Text(
                    text = "Khôi phục mật khẩu",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Vui lòng nhập email đăng ký tài khoản của bạn để nhận mã xác minh OTP.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = forgotEmailText,
                        onValueChange = { textVal ->
                            forgotEmailText = textVal
                            authViewModel.clearError()
                        },
                        label = { Text("Email khôi phục") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState is AuthUiState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (uiState as AuthUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.sendForgotPasswordOtp(forgotEmailText.trim())
                    },
                    enabled = uiState !is AuthUiState.Loading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Gửi mã OTP")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showForgotEmailDialog = false
                        authViewModel.clearError()
                    }
                ) {
                    Text(text = "Hủy")
                }
            }
        )
    }

    // 2. Dialog Nhập OTP Khôi Phục
    if (showForgotPasswordOtp == true) {
        LaunchedEffect(Unit) {
            showForgotEmailDialog = false
        }

        AlertDialog(
            onDismissRequest = {
                authViewModel.cancelForgotPassword()
            },
            title = {
                Text(
                    text = "Xác nhận mã OTP",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Mã xác minh đã được gửi về email: " + (forgotPasswordEmail ?: ""),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = enteredForgotOtp,
                        onValueChange = { textVal ->
                            enteredForgotOtp = textVal
                            authViewModel.clearError()
                        },
                        label = { Text("Mã xác minh (6 số)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState is AuthUiState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (uiState as AuthUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.verifyForgotPasswordOtp(enteredForgotOtp.trim())
                    },
                    enabled = uiState !is AuthUiState.Loading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Xác nhận")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        authViewModel.cancelForgotPassword()
                        enteredForgotOtp = ""
                    }
                ) {
                    Text(text = "Hủy")
                }
            }
        )
    }

    // 3. Dialog Đặt Lại Mật Khẩu Mới
    if (showResetPasswordScreen == true) {
        AlertDialog(
            onDismissRequest = {
                authViewModel.cancelForgotPassword()
            },
            title = {
                Text(
                    text = "Đặt lại mật khẩu mới",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Mã xác minh hợp lệ. Hãy thiết lập mật khẩu mới cho tài khoản của bạn.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val transformation = if (newPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    }
                    val passIcon = if (newPasswordVisible) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    }

                    OutlinedTextField(
                        value = newPasswordText,
                        onValueChange = { textVal ->
                            newPasswordText = textVal
                            authViewModel.clearError()
                        },
                        label = { Text("Mật khẩu mới") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(imageVector = passIcon, contentDescription = null)
                            }
                        },
                        visualTransformation = transformation,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState is AuthUiState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (uiState as AuthUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.resetPassword(newPasswordText.trim())
                    },
                    enabled = uiState !is AuthUiState.Loading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Đặt lại mật khẩu")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        authViewModel.cancelForgotPassword()
                        newPasswordText = ""
                    }
                ) {
                    Text(text = "Hủy")
                }
            }
        )
    }
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val size = size.minDimension
        val r = size / 2f
        
        // Top Red
        val pathRed = Path()
        pathRed.moveTo(r, r)
        pathRed.lineTo(r - r * 0.707f, r - r * 0.707f)
        pathRed.arcTo(
            rect = androidx.compose.ui.geometry.Rect(0f, 0f, size, size),
            startAngleDegrees = 225f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        pathRed.close()
        drawPath(pathRed, Color(0xFFEA4335))
        
        // Left Yellow
        val pathYellow = Path()
        pathYellow.moveTo(r, r)
        pathYellow.lineTo(r - r * 0.707f, r + r * 0.707f)
        pathYellow.arcTo(
            rect = androidx.compose.ui.geometry.Rect(0f, 0f, size, size),
            startAngleDegrees = 135f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        pathYellow.close()
        drawPath(pathYellow, Color(0xFFFBBC05))

        // Bottom Green
        val pathGreen = Path()
        pathGreen.moveTo(r, r)
        pathGreen.lineTo(r + r * 0.707f, r + r * 0.707f)
        pathGreen.arcTo(
            rect = androidx.compose.ui.geometry.Rect(0f, 0f, size, size),
            startAngleDegrees = 45f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        pathGreen.close()
        drawPath(pathGreen, Color(0xFF34A853))

        // Right Blue (including the horizontal bar)
        val pathBlue = Path()
        pathBlue.moveTo(r, r)
        pathBlue.lineTo(r + r * 0.707f, r - r * 0.707f)
        pathBlue.arcTo(
            rect = androidx.compose.ui.geometry.Rect(0f, 0f, size, size),
            startAngleDegrees = -45f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        pathBlue.close()
        drawPath(pathBlue, Color(0xFF4285F4))
        
        // Draw the inner white/cutout circle
        drawCircle(Color.White, radius = r * 0.65f)
        
        // Draw the blue bar
        val barWidth = r * 0.35f
        val barPath = Path()
        barPath.moveTo(r, r - barWidth / 2)
        barPath.lineTo(r * 1.85f, r - barWidth / 2)
        barPath.lineTo(r * 1.85f, r + barWidth / 2)
        barPath.lineTo(r, r + barWidth / 2)
        barPath.close()
        drawPath(barPath, Color(0xFF4285F4))
    }
}
