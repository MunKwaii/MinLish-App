package vn.edu.hcmute.minlish.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material3.MaterialTheme


/**
 * Biểu đồ cột hiển thị hoạt động học tập hàng ngày (7 ngày).
 *
 * Mỗi ngày hiển thị 2 cột:
 * - Xanh dương: Từ mới đã học
 * - Xanh lá: Từ đã ôn tập
 */
@Composable
fun DailyActivityChart(
    data: List<DailyActivityData>,
    modifier: Modifier = Modifier
) {
    // Animation: bar từ từ mọc lên
    var animationTriggered by remember { mutableStateOf(false) }
    val animationProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "barAnimation"
    )
    LaunchedEffect(Unit) { animationTriggered = true }

    val textMeasurer = rememberTextMeasurer()

    val chartBlue = MaterialTheme.colorScheme.primary
    val chartGreen = MaterialTheme.colorScheme.secondary
    val chartGridLine = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val textLayoutResults = remember(data, labelColor) {
        data.map { item ->
            textMeasurer.measure(
                text = item.label,
                style = TextStyle(fontSize = 10.sp, color = labelColor)
            )
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Hoạt Động Hàng Ngày",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Legend
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color = chartBlue)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Từ mới", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.width(12.dp))

                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color = chartGreen)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Ôn tập", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vẽ biểu đồ cột
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (data.isEmpty()) return@Canvas

                val chartWidth = size.width
                val chartHeight = size.height - 24.dp.toPx() // Chừa chỗ cho label

                // Tìm giá trị max để scale
                val maxValue = data.maxOf { maxOf(it.wordsLearned, it.wordsReviewed, 1) }

                val barGroupWidth = chartWidth / data.size
                val barWidth = barGroupWidth * 0.3f
                val gap = barGroupWidth * 0.05f

                // Vẽ đường grid ngang
                for (i in 0..3) {
                    val y = chartHeight * (1f - i / 4f)
                    drawLine(
                        color = chartGridLine,
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f
                    )
                }

                // Vẽ từng cặp bar
                data.forEachIndexed { index, item ->
                    val x = barGroupWidth * index + barGroupWidth * 0.15f

                    // Bar Từ mới (xanh dương)
                    val learnedHeight =
                        (item.wordsLearned.toFloat() / maxValue) * chartHeight * animationProgress
                    if (learnedHeight > 0) {
                        drawRoundRect(
                            color = chartBlue,
                            topLeft = Offset(x, chartHeight - learnedHeight),
                            size = Size(barWidth, learnedHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    // Bar Ôn tập (xanh lá)
                    val reviewedHeight =
                        (item.wordsReviewed.toFloat() / maxValue) * chartHeight * animationProgress
                    if (reviewedHeight > 0) {
                        drawRoundRect(
                            color = chartGreen,
                            topLeft = Offset(x + barWidth + gap, chartHeight - reviewedHeight),
                            size = Size(barWidth, reviewedHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    // Label ngày
                    val textLayoutResult = textLayoutResults.getOrNull(index)
                    if (textLayoutResult != null) {
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(
                                x + barWidth - textLayoutResult.size.width / 2f,
                                chartHeight + 6.dp.toPx()
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Biểu đồ đường hiển thị Retention Rate (% chính xác) theo ngày.
 *
 * Hiển thị đường cong mượt với vùng gradient bên dưới.
 */
@Composable
fun RetentionRateChart(
    data: List<RetentionData>,
    modifier: Modifier = Modifier
) {
    // Animation
    var animationTriggered by remember { mutableStateOf(false) }
    val animationProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "lineAnimation"
    )
    LaunchedEffect(Unit) { animationTriggered = true }

    val textMeasurer = rememberTextMeasurer()

    val chartBlue = MaterialTheme.colorScheme.primary
    val chartGridLine = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val gridValues = listOf(0, 25, 50, 75, 100)
    val gridLabelResults = remember(labelColor) {
        gridValues.map { value ->
            textMeasurer.measure(
                text = "${value}%",
                style = TextStyle(fontSize = 9.sp, color = labelColor)
            )
        }
    }

    val dayLabelResults = remember(data, labelColor) {
        data.map { item ->
            textMeasurer.measure(
                text = item.label,
                style = TextStyle(fontSize = 10.sp, color = labelColor)
            )
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tỷ Lệ Ghi Nhớ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "% trả lời đúng trong 7 ngày qua",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Vẽ biểu đồ đường
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (data.isEmpty()) return@Canvas

                val chartWidth = size.width
                val chartHeight = size.height - 24.dp.toPx()
                val maxPercent = 100f

                // Vẽ đường grid ngang + nhãn %
                gridValues.forEachIndexed { index, value ->
                    val y = chartHeight * (1f - value / maxPercent)
                    drawLine(
                        color = chartGridLine,
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f
                    )

                    // Nhãn %
                    val labelResult = gridLabelResults.getOrNull(index)
                    if (labelResult != null) {
                        drawText(
                            textLayoutResult = labelResult,
                            topLeft = Offset(0f, y - labelResult.size.height / 2f)
                        )
                    }
                }

                val pointSpacing = chartWidth / (data.size - 1).coerceAtLeast(1)
                val leftPadding = 28.dp.toPx()
                val usableWidth = chartWidth - leftPadding

                // Tính tọa độ các điểm
                val points = data.mapIndexed { index, item ->
                    val x = leftPadding + (usableWidth / (data.size - 1).coerceAtLeast(1)) * index
                    val y = chartHeight * (1f - (item.accuracyPercent / maxPercent) * animationProgress)
                    Offset(x, y)
                }

                // Vẽ vùng gradient dưới đường
                if (points.size >= 2) {
                    val fillPath = Path().apply {
                        moveTo(points.first().x, chartHeight)
                        for (point in points) {
                            lineTo(point.x, point.y)
                        }
                        lineTo(points.last().x, chartHeight)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        color = chartBlue.copy(alpha = 0.1f)
                    )
                }

                // Vẽ đường line
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = chartBlue,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Vẽ các điểm tròn
                for (point in points) {
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = chartBlue,
                        radius = 3.5.dp.toPx(),
                        center = point
                    )
                }

                // Label ngày bên dưới
                data.forEachIndexed { index, item ->
                    val x = leftPadding + (usableWidth / (data.size - 1).coerceAtLeast(1)) * index
                    val labelResult = dayLabelResults.getOrNull(index)
                    if (labelResult != null) {
                        drawText(
                            textLayoutResult = labelResult,
                            topLeft = Offset(
                                x - labelResult.size.width / 2f,
                                chartHeight + 6.dp.toPx()
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Hiển thị badge trình độ với màu sắc tương ứng.
 */
@Composable
fun LevelBadge(
    level: String,
    modifier: Modifier = Modifier
) {
    val badgeColor = when (level) {
        "Advanced" -> MaterialTheme.colorScheme.secondary
        "Intermediate" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    val levelVi = when (level) {
        "Advanced" -> "Nâng cao"
        "Intermediate" -> "Trung cấp"
        else -> "Sơ cấp"
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.15f))
    ) {
        Text(
            text = levelVi,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = badgeColor,
            textAlign = TextAlign.Center
        )
    }
}

// Màu sắc cho Card Maturity (theo Anki)
private val MaturityNew = Color(0xFF90CAF9)       // Xanh nhạt — New
private val MaturityLearning = Color(0xFFFF8A65)   // Cam — Learning
private val MaturityYoung = Color(0xFF81C784)      // Xanh lá nhạt — Young
private val MaturityMature = Color(0xFF4CAF50)     // Xanh lá đậm — Mature

/**
 * Biểu đồ Card Maturity theo phong cách Anki.
 *
 * Hiển thị thanh ngang stacked bar thể hiện phân bố:
 * New → Learning → Young → Mature
 */
@Composable
fun CardMaturityChart(
    maturity: CardMaturityData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Card Maturity (Anki)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Phân bố trạng thái thẻ dựa trên interval SM-2",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stacked horizontal bar
            if (maturity.totalCount > 0) {
                // Animation
                var animationTriggered by remember { mutableStateOf(false) }
                val animationProgress by animateFloatAsState(
                    targetValue = if (animationTriggered) 1f else 0f,
                    animationSpec = tween(durationMillis = 800),
                    label = "maturityBarAnimation"
                )
                LaunchedEffect(Unit) { animationTriggered = true }

                val outlineVariant = MaterialTheme.colorScheme.outlineVariant
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    val barHeight = size.height
                    val totalWidth = size.width * animationProgress
                    val cornerRadius = CornerRadius(8.dp.toPx())

                    // Background
                    drawRoundRect(
                        color = outlineVariant.copy(alpha = 0.2f),
                        size = Size(size.width, barHeight),
                        cornerRadius = cornerRadius
                    )

                    // Tính width từng phần
                    val newWidth = (maturity.newPercent / 100f) * totalWidth
                    val learningWidth = (maturity.learningPercent / 100f) * totalWidth
                    val youngWidth = (maturity.youngPercent / 100f) * totalWidth
                    val matureWidth = (maturity.maturePercent / 100f) * totalWidth

                    var currentX = 0f

                    // Vẽ New
                    if (newWidth > 0) {
                        drawRect(
                            color = MaturityNew,
                            topLeft = Offset(currentX, 0f),
                            size = Size(newWidth, barHeight)
                        )
                        currentX += newWidth
                    }

                    // Vẽ Learning
                    if (learningWidth > 0) {
                        drawRect(
                            color = MaturityLearning,
                            topLeft = Offset(currentX, 0f),
                            size = Size(learningWidth, barHeight)
                        )
                        currentX += learningWidth
                    }

                    // Vẽ Young
                    if (youngWidth > 0) {
                        drawRect(
                            color = MaturityYoung,
                            topLeft = Offset(currentX, 0f),
                            size = Size(youngWidth, barHeight)
                        )
                        currentX += youngWidth
                    }

                    // Vẽ Mature
                    if (matureWidth > 0) {
                        drawRect(
                            color = MaturityMature,
                            topLeft = Offset(currentX, 0f),
                            size = Size(matureWidth, barHeight)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Legend với số lượng
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MaturityLegendItem(
                        color = MaturityNew,
                        label = "New",
                        count = maturity.newCount,
                        percent = maturity.newPercent
                    )
                    MaturityLegendItem(
                        color = MaturityLearning,
                        label = "Learning",
                        count = maturity.learningCount,
                        percent = maturity.learningPercent
                    )
                    MaturityLegendItem(
                        color = MaturityYoung,
                        label = "Young",
                        count = maturity.youngCount,
                        percent = maturity.youngPercent
                    )
                    MaturityLegendItem(
                        color = MaturityMature,
                        label = "Mature",
                        count = maturity.matureCount,
                        percent = maturity.maturePercent
                    )
                }
            } else {
                Text(
                    text = "Chưa có dữ liệu",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * Item legend cho biểu đồ Card Maturity.
 */
@Composable
private fun MaturityLegendItem(
    color: Color,
    label: String,
    count: Int,
    percent: Float
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(color = color)
            }
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${percent.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

