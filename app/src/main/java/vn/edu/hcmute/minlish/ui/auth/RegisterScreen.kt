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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    // Chuyển hướng khi đăng ký thành công
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onRegisterSuccess()
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

            // Card Form đăng ký
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
                        text = "ĐĂNG KÝ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF202124),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Ô nhập Họ và Tên
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
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
                        onValueChange = { 
                            email = it
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
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
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
                            val image = if (passwordVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = "Toggle password visibility")
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
