package vn.edu.hcmute.minlish.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()

    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    
    val userTypes = listOf(
        "Học sinh, sinh viên",
        "Người học IELTS / TOEIC",
        "Người đi làm cần nâng cao từ vựng"
    )
    var selectedUserType by remember { mutableStateOf(currentUser?.userType ?: userTypes[0]) }

    val goals = listOf("IELTS", "TOEIC", "Giao tiếp", "Đọc tài liệu", "Du lịch")
    var selectedGoal by remember { mutableStateOf(currentUser?.learningGoal ?: goals[0]) }

    val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
    var selectedLevel by remember { mutableStateOf(currentUser?.level ?: levels[0]) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            authViewModel.resetUiState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hồ sơ cá nhân",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back Icon",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A73E8)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD),
                            Color(0xFFFFFFFF)
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = Color(0xFF1A73E8),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Tên người dùng
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
                                    contentDescription = "Person Icon",
                                    tint = Color(0xFF1A73E8)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email (không cho sửa)
                        OutlinedTextField(
                            value = currentUser?.email ?: "",
                            onValueChange = {},
                            label = { Text("Địa chỉ Email") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Icon",
                                    tint = Color.Gray
                                )
                            },
                            singleLine = true,
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Gray,
                                disabledBorderColor = Color.LightGray,
                                disabledLabelColor = Color.Gray
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Chọn Đối tượng người dùng
                        Text(
                            text = "Đối tượng người dùng:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF202124),
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            userTypes.forEach { type ->
                                val isSelected = selectedUserType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedUserType = type
                                        authViewModel.clearError()
                                    },
                                    label = { Text(type) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFE3F2FD),
                                        selectedLabelColor = Color(0xFF1A73E8)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Chọn Mục tiêu học tập
                        Text(
                            text = "Mục tiêu học tập:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF202124),
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            goals.forEach { goal ->
                                val isSelected = selectedGoal == goal
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedGoal = goal
                                        authViewModel.clearError()
                                    },
                                    label = { Text(goal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFE3F2FD),
                                        selectedLabelColor = Color(0xFF1A73E8)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Chọn Trình độ
                        Text(
                            text = "Trình độ tiếng Anh:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF202124),
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            levels.forEach { lvl ->
                                val isSelected = selectedLevel == lvl
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedLevel = lvl
                                        authViewModel.clearError()
                                    },
                                    label = { Text(lvl) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFE3F2FD),
                                        selectedLabelColor = Color(0xFF1A73E8)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Hiển thị lỗi nếu có
                        if (uiState is AuthUiState.Error) {
                            Text(
                                text = (uiState as AuthUiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        // Nút Lưu thay đổi
                        Button(
                            onClick = {
                                authViewModel.updateProfile(
                                    name = name.trim(),
                                    userType = selectedUserType,
                                    learningGoal = selectedGoal,
                                    level = selectedLevel
                                )
                            },
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
                                    text = "Lưu thay đổi",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
