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
                        Color(0xFFE3F2FD), // Xanh dương nhạt
                        Color(0xFFFFFFFF)  // Trắng
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
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A73E8),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Học tiếng Anh thông minh mỗi ngày",
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(40.dp))

            // Card chứa Form đăng nhập
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ĐĂNG NHẬP",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF202124),
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
                                tint = Color(0xFF1A73E8)
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
                                tint = Color(0xFF1A73E8)
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // Thông báo lỗi nếu có
                    if (uiState is AuthUiState.Error) {
                        Text(
                            text = (uiState as AuthUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "Đăng nhập",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Đường gạch ngang "Hoặc"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                        Text(
                            text = " hoặc ",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
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
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFDADCE0)),
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
                                color = Color(0xFF3C4043),
                                fontSize = 15.sp,
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
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                TextButton(
                    onClick = {
                        authViewModel.clearError()
                        onNavigateToRegister()
                    }
                ) {
                    Text(
                        text = "Đăng ký ngay",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A73E8)
                    )
                }
            }
        }
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
