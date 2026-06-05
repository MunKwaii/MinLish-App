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
import kotlinx.coroutines.launch
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    showBackButton: Boolean = false
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()

    val context: Context = LocalContext.current
    var isSavingProfile: Boolean by remember { mutableStateOf(false) }

    val avatarBitmap: ImageBitmap? = remember(currentUser?.avatarPath) {
        val path: String? = currentUser?.avatarPath
        if (path != null) {
            val file: File = File(path)
            if (file.exists() == true) {
                val bitmap: Bitmap? = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    val imageBitmap: ImageBitmap = bitmap.asImageBitmap()
                    return@remember imageBitmap
                }
            }
        }
        return@remember null
    }

    var showDialog: Boolean by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val userId: Int = currentUser?.userId ?: 0
            val savedPath: String? = saveUriToInternalStorage(context, uri, userId)
            if (savedPath != null) {
                val oldPath: String? = currentUser?.avatarPath
                if (oldPath != null) {
                    val oldFile: File = File(oldPath)
                    if (oldFile.exists() == true) {
                        oldFile.delete()
                    }
                }
                authViewModel.updateAvatar(savedPath)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val userId: Int = currentUser?.userId ?: 0
            val savedPath: String? = saveBitmapToInternalStorage(context, bitmap, userId)
            if (savedPath != null) {
                val oldPath: String? = currentUser?.avatarPath
                if (oldPath != null) {
                    val oldFile: File = File(oldPath)
                    if (oldFile.exists() == true) {
                        oldFile.delete()
                    }
                }
                authViewModel.updateAvatar(savedPath)
            }
        }
    }

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
            if (isSavingProfile == true) {
                isSavingProfile = false
                authViewModel.resetUiState()
                onNavigateBack()
            } else {
                authViewModel.resetUiState()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hồ sơ cá nhân",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back Icon",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.background
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(98.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable {
                                        showDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (avatarBitmap != null) {
                                    Image(
                                        bitmap = avatarBitmap,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Avatar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(54.dp)
                                    )
                                }
                            }
                            // Small edit camera icon overlay
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .align(Alignment.BottomEnd)
                                    .clickable {
                                        showDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Avatar",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (showDialog == true) {
                            AlertDialog(
                                onDismissRequest = {
                                    showDialog = false
                                },
                                title = {
                                    Text(
                                        text = "Thay đổi ảnh đại diện",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                },
                                text = {
                                    Column {
                                        TextButton(
                                            onClick = {
                                                showDialog = false
                                                galleryLauncher.launch("image/*")
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Chọn từ Thư viện",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        TextButton(
                                            onClick = {
                                                showDialog = false
                                                cameraLauncher.launch(null)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Chụp ảnh mới",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showDialog = false
                                        }
                                    ) {
                                        Text(
                                            text = "Hủy",
                                            color = Color.Gray
                                        )
                                    }
                                }
                            )
                        }
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
                                    tint = MaterialTheme.colorScheme.primary
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
                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Chọn Đối tượng người dùng
                        Text(
                            text = "Đối tượng người dùng:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
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
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Chọn Mục tiêu học tập
                        Text(
                            text = "Mục tiêu học tập:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
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
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Chọn Trình độ
                        Text(
                            text = "Trình độ tiếng Anh:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
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
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hiển thị lỗi nếu có
                        if (uiState is AuthUiState.Error) {
                            Text(
                                text = (uiState as AuthUiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        // Nút Lưu thay đổi
                        Button(
                            onClick = {
                                isSavingProfile = true
                                authViewModel.updateProfile(
                                    name = name.trim(),
                                    userType = selectedUserType,
                                    learningGoal = selectedGoal,
                                    level = selectedLevel
                                )
                            },
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
                                    text = "Lưu thay đổi",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val width: Int = bitmap.width
    val height: Int = bitmap.height
    if (width <= maxDimension && height <= maxDimension) {
        return bitmap
    }
    val srcRatio: Float = width.toFloat() / height.toFloat()
    var newWidth: Int = maxDimension
    var newHeight: Int = maxDimension
    if (srcRatio > 1.0f) {
        newHeight = (maxDimension / srcRatio).toInt()
    } else {
        newWidth = (maxDimension * srcRatio).toInt()
    }
    val scaledBitmap: Bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    return scaledBitmap
}

private fun saveUriToInternalStorage(context: Context, uri: Uri, userId: Int): String? {
    try {
        val contentResolver = context.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        if (inputStream != null) {
            val originalBitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap != null) {
                val resizedBitmap: Bitmap = resizeBitmap(originalBitmap, 512)
                val fileName: String = "avatar_user_" + userId + "_" + System.currentTimeMillis() + ".jpg"
                val file: File = File(context.filesDir, fileName)
                val outputStream: FileOutputStream = FileOutputStream(file)
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                outputStream.flush()
                outputStream.close()
                if (resizedBitmap != originalBitmap) {
                    originalBitmap.recycle()
                }
                val absolutePath: String = file.absolutePath
                return absolutePath
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, userId: Int): String? {
    try {
        val resizedBitmap: Bitmap = resizeBitmap(bitmap, 512)
        val fileName: String = "avatar_user_" + userId + "_" + System.currentTimeMillis() + ".jpg"
        val file: File = File(context.filesDir, fileName)
        val outputStream: FileOutputStream = FileOutputStream(file)
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        outputStream.flush()
        outputStream.close()
        if (resizedBitmap != bitmap) {
            bitmap.recycle()
        }
        val absolutePath: String = file.absolutePath
        return absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}
