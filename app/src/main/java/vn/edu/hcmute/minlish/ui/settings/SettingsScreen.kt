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

                        // Nếu Nhắc học mỗi ngày được bật, hiển thị chọn giờ
                        if (dailyEnabled == true) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Giờ nhắc học tập:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                var dropdownExpanded by remember { mutableStateOf(false) }
                                val timesList: List<String> = listOf("08:00", "12:00", "15:00", "18:00", "20:00", "22:00")
                                
                                Box {
                                    OutlinedButton(
                                        onClick = { dropdownExpanded = true },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(text = dailyTime)
                                    }
                                    
                                    DropdownMenu(
                                        expanded = dropdownExpanded,
                                        onDismissRequest = { dropdownExpanded = false }
                                    ) {
                                        for (timeOption in timesList) {
                                            DropdownMenuItem(
                                                text = { Text(text = timeOption) },
                                                onClick = {
                                                    dropdownExpanded = false
                                                    coroutineScope.launch {
                                                        settingsManager.saveDailyReminderTime(timeOption)
                                                        AlarmScheduler.scheduleDailyAlarm(context, timeOption)
                                                        Toast.makeText(context, "Đã cập nhật giờ nhắc học thành " + timeOption, Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nút Gửi thử thông báo
                        val currentUser by authViewModel.currentUser.collectAsState()
                        Button(
                            onClick = {
                                if (pushEnabled == true) {
                                    NotificationHelper.showDailyReminder(context)
                                    NotificationHelper.showDueWordsReminder(context, 3)
                                }

                                val userEmail: String? = currentUser?.email
                                if (userEmail != null && userEmail.isNotEmpty()) {
                                    if (emailEnabled == true) {
                                        coroutineScope.launch {
                                            val testSubject: String = "MinLish - Kiểm tra hệ thống thông báo"
                                            val testBody: String = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 12px; background-color: #f9f9f9;\">" +
                                                    "<h2 style=\"color: #4caf50; text-align: center;\">MinLish - Test Thông Báo</h2>" +
                                                    "<hr style=\"border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                                                    "<p style=\"font-size: 16px; color: #333;\">Xin chào <strong>" + (currentUser?.name ?: "") + "</strong>,</p>" +
                                                    "<p style=\"font-size: 16px; color: #333;\">Đây là email gửi thử nghiệm để xác nhận hệ thống thông báo qua email của ứng dụng MinLish hoạt động hoàn toàn ổn định!</p>" +
                                                    "<hr style=\"border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;\">" +
                                                    "<p style=\"font-size: 12px; color: #777; text-align: center;\">Cảm ơn bạn đã sử dụng MinLish.</p>" +
                                                    "</div>"
                                            val emailResult: Result<Unit> = EmailSender.sendReminderEmail(userEmail, testSubject, testBody)
                                            if (emailResult.isSuccess) {
                                                Toast.makeText(context, "Đã gửi thử thông báo Push và Email thành công!", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Đã gửi thử thông báo Push. Gửi Email thất bại!", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Đã gửi thử thông báo Push! (Tính năng thông báo qua Email chưa được bật)", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Đã gửi thử thông báo Push! (Không tìm thấy email đăng nhập để gửi test mail)", Toast.LENGTH_LONG).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(text = "Gửi thử thông báo thử nghiệm")
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
