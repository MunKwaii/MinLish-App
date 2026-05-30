package vn.edu.hcmute.minlish.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Các lựa chọn Mục tiêu học tập
    val goals = listOf("Giao tiếp", "IELTS/TOEIC", "Đọc tài liệu", "Du lịch")
    var selectedGoal by remember { mutableStateOf(goals[0]) }

    // Các lựa chọn Trình độ hiện tại
    val levels = listOf("Cơ bản (A1-A2)", "Trung cấp (B1-B2)", "Nâng cao (C1-C2)")
    var selectedLevel by remember { mutableStateOf(levels[0]) }

    val uiState by authViewModel.uiState.collectAsState()

    // OTP States
    val showOtpScreen by authViewModel.showOtpScreen.collectAsState()
    val generatedOtp by authViewModel.generatedOtp.collectAsState()
    var timeLeft by remember { mutableStateOf(60) }
    val otpValues = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = remember { List(6) { FocusRequester() } }

    // Chuyển hướng khi đăng ký thành công
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onRegisterSuccess()
        }
    }

    LaunchedEffect(showOtpScreen) {
        if (showOtpScreen) {
            timeLeft = 60
            for (i in 0..5) {
                otpValues[i] = ""
            }
            delay(100L)
            try {
                focusRequesters[0].requestFocus()
            } catch (e: Exception) {
                // Ignore focus request errors
            }
        }
    }

    LaunchedEffect(timeLeft, showOtpScreen) {
        if (showOtpScreen && timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9), // Xanh lá nhạt
                        Color(0xFFFFFFFF)  // Trắng
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tiêu đề
            Text(
                text = "MinLish",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2E7D32), // Màu xanh lá đậm
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tạo tài khoản mới của bạn",
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Card Form đăng ký hoặc OTP
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                if (showOtpScreen) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "XÁC THỰC EMAIL",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF202124),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "Vui lòng nhập mã OTP 6 chữ số đã được gửi đến email:",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        Text(
                            text = email,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // 6 input cells
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0..5) {
                                OutlinedTextField(
                                    value = otpValues[i],
                                    onValueChange = { newValue ->
                                        val digits = newValue.filter { it.isDigit() }
                                        if (digits.length == 6) {
                                            for (j in 0..5) {
                                                otpValues[j] = digits[j].toString()
                                            }
                                            focusRequesters[5].requestFocus()
                                        } else {
                                            if (digits.isNotEmpty()) {
                                                otpValues[i] = digits.last().toString()
                                                if (i < 5) {
                                                    focusRequesters[i + 1].requestFocus()
                                                }
                                            } else {
                                                otpValues[i] = ""
                                            }
                                        }
                                        authViewModel.clearError()
                                    },
                                    modifier = Modifier
                                        .width(42.dp)
                                        .height(56.dp)
                                        .focusRequester(focusRequesters[i])
                                        .onKeyEvent { keyEvent ->
                                            if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Backspace) {
                                                if (otpValues[i].isEmpty()) {
                                                    if (i > 0) {
                                                        otpValues[i - 1] = ""
                                                        focusRequesters[i - 1].requestFocus()
                                                        true
                                                    } else {
                                                        false
                                                    }
                                                } else {
                                                    otpValues[i] = ""
                                                    false
                                                }
                                            } else {
                                                false
                                            }
                                        },
                                    textStyle = LocalTextStyle.current.copy(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2E7D32),
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Countdown or resend
                        if (timeLeft > 0) {
                            Text(
                                text = "Gửi lại mã sau ${timeLeft}s",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            TextButton(
                                onClick = {
                                    authViewModel.resendOtp()
                                    timeLeft = 60
                                }
                            ) {
                                Text(
                                    text = "Gửi lại mã OTP",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Error message if any
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

                        // Verify button
                        Button(
                            onClick = {
                                val enteredOtp = otpValues.joinToString("")
                                authViewModel.verifyOtp(enteredOtp)
                            },
                            enabled = uiState !is AuthUiState.Loading && otpValues.all { it.isNotEmpty() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
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
                                    text = "Xác thực",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Cancel / Go back button
                        OutlinedButton(
                            onClick = {
                                authViewModel.cancelRegistration()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                        ) {
                            Text(
                                text = "Quay lại",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ĐĂNG KÝ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF202124),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Ô nhập Họ và Tên
                        OutlinedTextField(
                            value = name,
                            onValueChange = { textVal ->
                                name = textVal
                                authViewModel.clearError()
                            },
                            label = { Text("Họ và tên") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Name Icon",
                                    tint = Color(0xFF2E7D32)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

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
                                    tint = Color(0xFF2E7D32)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

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
                                    tint = Color(0xFF2E7D32)
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Chọn Mục tiêu học tập
                        Text(
                            text = "Mục tiêu học tập của bạn:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            goals.forEach { goal ->
                                val isSelected = selectedGoal == goal
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedGoal = goal },
                                    label = { Text(goal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFE8F5E9),
                                        selectedLabelColor = Color(0xFF2E7D32)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chọn trình độ
                        Text(
                            text = "Trình độ tiếng Anh hiện tại:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            levels.forEach { lvl ->
                                val isSelected = selectedLevel == lvl
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedLevel = lvl },
                                    label = { Text(lvl) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFE8F5E9),
                                        selectedLabelColor = Color(0xFF2E7D32)
                                    )
                                )
                            }
                        }

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

                        // Nút Đăng ký
                        Button(
                            onClick = {
                                authViewModel.register(
                                    email = email.trim(),
                                    name = name.trim(),
                                    passwordHash = password,
                                    learningGoal = selectedGoal,
                                    level = selectedLevel
                                )
                            },
                            enabled = uiState !is AuthUiState.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
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
                                    text = "Đăng ký",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Liên kết đăng nhập
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đã có tài khoản? ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                TextButton(
                    onClick = {
                        authViewModel.clearError()
                        onNavigateToLogin()
                    }
                ) {
                    Text(
                        text = "Đăng nhập ngay",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}
