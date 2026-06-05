package vn.edu.hcmute.minlish.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import vn.edu.hcmute.minlish.data.notification.NotificationHelper
import vn.edu.hcmute.minlish.data.notification.AlarmScheduler
import vn.edu.hcmute.minlish.data.util.EmailSender
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import vn.edu.hcmute.minlish.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as vn.edu.hcmute.minlish.MinLishApplication
    val settingsManager = remember { app.settingsManager }
    val currentLimit by settingsManager.newWordsLimitFlow.collectAsState(initial = 10)
    val dailyEnabled by settingsManager.dailyReminderEnabledFlow.collectAsState(initial = true)
    val dailyTime by settingsManager.dailyReminderTimeFlow.collectAsState(initial = "20:00")
    val dueEnabled by settingsManager.dueWordsReminderEnabledFlow.collectAsState(initial = true)
    val emailEnabled by settingsManager.emailNotificationEnabledFlow.collectAsState(initial = false)
    val pushEnabled by settingsManager.pushNotificationEnabledFlow.collectAsState(initial = true)
    val coroutineScope = rememberCoroutineScope()

    var newWordsLimit by remember { mutableStateOf(10) }
    var limitInputText by remember { mutableStateOf("10") }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentLimit) {
        newWordsLimit = currentLimit
        limitInputText = currentLimit.toString()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cài đặt",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Section 1: Giao diện
                        Text(
                            text = "Giao diện",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Chế độ tối (Dark Mode)",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { onToggleTheme() }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Đăng nhập bằng vân tay",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val biometricEnabled: Boolean by settingsManager.biometricEnabledFlow.collectAsState(initial = false)
                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = { checked ->
                                    val biometricManager: androidx.biometric.BiometricManager = androidx.biometric.BiometricManager.from(context)
                                    val canAuth: Int = biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
                                    if (canAuth == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
                                        coroutineScope.launch {
                                            settingsManager.saveBiometricEnabled(checked)
                                        }
                                    } else {
                                        Toast.makeText(context, "Thiết bị không hỗ trợ hoặc chưa cài đặt vân tay!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(20.dp))

                        // Section 2: Cấu hình học tập
                        Text(
                            text = "Cài đặt học tập",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Slider(
                                value = newWordsLimit.toFloat(),
                                onValueChange = { floatVal ->
                                    val intVal = floatVal.toInt().coerceIn(5, 50)
                                    newWordsLimit = intVal
                                    limitInputText = intVal.toString()
                                },
                                valueRange = 5f..50f,
                                steps = 45,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = limitInputText,
                                onValueChange = { text ->
                                    val filtered = text.filter { it.isDigit() }
                                    limitInputText = filtered
                                    val parsed = filtered.toIntOrNull()
                                    if (parsed != null) {
                                        newWordsLimit = parsed.coerceIn(5, 50)
                                    }
                                },
                                label = { Text("Số từ") },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.width(90.dp)
                            )
                        }

                        Text(
                            text = "Giới hạn từ mới mỗi ngày: $newWordsLimit từ (Tối thiểu 5, tối đa 50)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val finalLimit = limitInputText.toIntOrNull()?.coerceIn(5, 50) ?: 10
                                newWordsLimit = finalLimit
                                limitInputText = finalLimit.toString()
                                coroutineScope.launch {
                                    settingsManager.saveNewWordsLimit(finalLimit)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Lưu cài đặt học tập")
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(20.dp))

                        // Section 3: Cấu hình thông báo
                        Text(
                            text = "Cấu hình thông báo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Switch: Nhắc học mỗi ngày
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Nhắc học mỗi ngày",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = dailyEnabled,
                                onCheckedChange = { checkedVal ->
                                    coroutineScope.launch {
                                        settingsManager.saveDailyReminderEnabled(checkedVal)
                                        if (checkedVal == true) {
                                            AlarmScheduler.scheduleDailyAlarm(context, dailyTime)
                                        } else {
                                            AlarmScheduler.cancelDailyAlarm(context)
                                        }
                                    }
                                }
                            )
                        }

                        // Nếu Nhắc học mỗi ngày được bật, hiển thị chọn giờ bằng bánh xe
                        if (dailyEnabled == true) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Giờ nhắc học tập (Bấm để chọn):",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = { showTimePickerDialog = true },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(text = dailyTime)
                                }
                            }
                        }

                        if (showTimePickerDialog == true) {
                            val timeParts = dailyTime.split(":")
                            var initHour = 20
                            var initMinute = 0
                            if (timeParts.size == 2) {
                                initHour = timeParts[0].toIntOrNull() ?: 20
                                initMinute = timeParts[1].toIntOrNull() ?: 0
                            }
                            WheelTimePickerDialog(
                                initialHour = initHour,
                                initialMinute = initMinute,
                                onDismiss = {
                                    showTimePickerDialog = false
                                },
                                onConfirm = { hour, minute ->
                                    showTimePickerDialog = false
                                    val newHourStr = if (hour < 10) "0$hour" else "$hour"
                                    val newMinStr = if (minute < 10) "0$minute" else "$minute"
                                    val newTimeStr = "$newHourStr:$newMinStr"
                                    coroutineScope.launch {
                                        settingsManager.saveDailyReminderTime(newTimeStr)
                                        AlarmScheduler.scheduleDailyAlarm(context, newTimeStr)
                                        Toast.makeText(context, "Đã cập nhật giờ nhắc học thành " + newTimeStr, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Switch: Nhắc từ đến hạn ôn
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Nhắc từ đến hạn ôn",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = dueEnabled,
                                onCheckedChange = { checkedVal ->
                                    coroutineScope.launch {
                                        settingsManager.saveDueWordsReminderEnabled(checkedVal)
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Switch: Nhận thông báo Push
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Thông báo thiết bị (Push)",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = pushEnabled,
                                onCheckedChange = { checkedVal ->
                                    coroutineScope.launch {
                                        settingsManager.savePushNotificationEnabled(checkedVal)
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Switch: Nhận thông báo qua Email
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Thông báo qua Email",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = emailEnabled,
                                onCheckedChange = { checkedVal ->
                                    coroutineScope.launch {
                                        settingsManager.saveEmailNotificationEnabled(checkedVal)
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(20.dp))

                        // Section 3.5: TEST LAB
                        Text(
                            text = "Test Lab (Khu vực thử nghiệm)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Khu vực kiểm tra nhanh các loại thông báo ngắt quãng và hẹn giờ mà không cần chờ đợi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val currentUser by authViewModel.currentUser.collectAsState()

                        // Nút Test 1: Nhắc học mỗi ngày
                        Button(
                            onClick = {
                                if (pushEnabled == true) {
                                    NotificationHelper.showDailyReminder(context)
                                }
                                val userEmail = currentUser?.email
                                if (userEmail != null && userEmail.isNotEmpty() && emailEnabled == true) {
                                    coroutineScope.launch {
                                        val testSubject = "MinLish - Đã đến giờ học tiếng Anh rồi! (Test nhanh)"
                                        val testBody = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 12px; background-color: #f9f9f9;\">" +
                                                "<h2 style=\"color: #1976d2; text-align: center;\">MinLish - Học Tập Mỗi Ngày (Test)</h2>" +
                                                "<hr style=\"border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                                                "<p style=\"font-size: 16px; color: #333;\">Xin chào,</p>" +
                                                "<p style=\"font-size: 16px; color: #333;\">Hôm nay bạn chưa học từ vựng nào trên MinLish. Hãy dành 5 phút vào học để duy trì streak học tập và không bỏ lỡ thói quen học tập hàng ngày nhé!</p>" +
                                                "<hr style=\"border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                                                "<p style=\"font-size: 12px; color: #777; text-align: center;\">Cảm ơn bạn đã sử dụng MinLish.</p>" +
                                                "</div>"
                                        val result = EmailSender.sendReminderEmail(userEmail, testSubject, testBody)
                                        if (result.isSuccess) {
                                            Toast.makeText(context, "Đã gửi thông báo test học mỗi ngày qua email!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Lỗi gửi email!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Đã đẩy thông báo Push Nhắc học!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(text = "Test: Nhắc học mỗi ngày")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Nút Test 2: Nhắc ôn tập từ vựng
                        Button(
                            onClick = {
                                if (pushEnabled == true) {
                                    NotificationHelper.showDueWordsReminder(context, 5)
                                }
                                val userEmail = currentUser?.email
                                if (userEmail != null && userEmail.isNotEmpty() && emailEnabled == true) {
                                    coroutineScope.launch {
                                        val testSubject = "MinLish - Có 5 từ vựng đến hạn ôn tập! (Test nhanh)"
                                        val testBody = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 12px; background-color: #f9f9f9;\">" +
                                                "<h2 style=\"color: #e53935; text-align: center;\">MinLish - Đến Hạn Ôn Tập (Test)</h2>" +
                                                "<hr style=\"border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                                                "<p style=\"font-size: 16px; color: #333;\">Xin chào,</p>" +
                                                "<p style=\"font-size: 16px; color: #333;\">Bạn có <strong>5</strong> từ vựng đã đến hạn ôn tập theo thuật toán lặp lại ngắt quãng (Spaced Repetition).</p>" +
                                                "<hr style=\"border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                                                "<p style=\"font-size: 12px; color: #777; text-align: center;\">Cảm ơn bạn đã sử dụng MinLish.</p>" +
                                                "</div>"
                                        val result = EmailSender.sendReminderEmail(userEmail, testSubject, testBody)
                                        if (result.isSuccess) {
                                            Toast.makeText(context, "Đã gửi thông báo test từ đến hạn qua email!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Lỗi gửi email!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Đã đẩy thông báo Push Ôn tập!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(text = "Test: Nhắc ôn tập từ vựng")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Nút Test 3: Đặt báo thức sau 10 giây
                        Button(
                            onClick = {
                                AlarmScheduler.scheduleTestAlarmInTenSeconds(context)
                                Toast.makeText(context, "Báo thức đã hẹn sau 10 giây. Hãy đóng ứng dụng và khóa màn hình để kiểm tra!", Toast.LENGTH_LONG).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text(text = "Đặt báo thức sau 10 giây")
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(20.dp))

                        // Section 4: Tài khoản
                        Text(
                            text = "Tài khoản",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onLogout,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Đăng xuất",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WheelPicker(
    items: List<String>,
    initialIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val selectedIndex = remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }

    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (lazyListState.isScrollInProgress == false) {
            val index = lazyListState.firstVisibleItemIndex
            lazyListState.animateScrollToItem(index)
            onItemSelected(index)
        }
    }

    Box(
        modifier = modifier
            .height(150.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                )
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 55.dp)
        ) {
            items(items.size) { index ->
                val isSelected = (index == selectedIndex.value)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun WheelTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val hoursList = ArrayList<String>()
    for (i in 0..23) {
        val str = if (i < 10) "0$i" else "$i"
        hoursList.add(str)
    }

    val minutesList = ArrayList<String>()
    for (i in 0..59) {
        val str = if (i < 10) "0$i" else "$i"
        minutesList.add(str)
    }

    var selectedHourIndex by remember { mutableStateOf(initialHour.coerceIn(0, 23)) }
    var selectedMinuteIndex by remember { mutableStateOf(initialMinute.coerceIn(0, 59)) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Chọn giờ nhắc nhở học tập",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WheelPicker(
                        items = hoursList,
                        initialIndex = selectedHourIndex,
                        onItemSelected = { index ->
                            selectedHourIndex = index
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    WheelPicker(
                        items = minutesList,
                        initialIndex = selectedMinuteIndex,
                        onItemSelected = { index ->
                            selectedMinuteIndex = index
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Hủy")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(selectedHourIndex, selectedMinuteIndex)
                        }
                    ) {
                        Text(text = "Đồng ý")
                    }
                }
            }
        }
    }
}
