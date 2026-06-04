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

// Màu sắc cho biểu đồ
private val ChartBlue = Color(0xFF1A73E8)
private val ChartGreen = Color(0xFF34A853)
private val ChartLightBlue = Color(0xFFBBDEFB)
private val ChartLightGreen = Color(0xFFC8E6C9)
private val ChartGridLine = Color(0xFFE0E0E0)

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

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Hoạt Động Hàng Ngày",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202124)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Legend
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color = ChartBlue)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Từ mới", fontSize = 11.sp, color = Color.Gray)

                Spacer(modifier = Modifier.width(12.dp))

                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color = ChartGreen)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Ôn tập", fontSize = 11.sp, color = Color.Gray)
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
                        color = ChartGridLine,
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
                            color = ChartBlue,
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
                            color = ChartGreen,
                            topLeft = Offset(x + barWidth + gap, chartHeight - reviewedHeight),
                            size = Size(barWidth, reviewedHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    // Label ngày
                    val labelText = item.label
                    val textLayoutResult = textMeasurer.measure(
                        text = labelText,
                        style = TextStyle(fontSize = 10.sp, color = Color.Gray)
                    )
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

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tỷ Lệ Ghi Nhớ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202124)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "% trả lời đúng trong 7 ngày qua",
                fontSize = 11.sp,
                color = Color.Gray
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
                val gridValues = listOf(0, 25, 50, 75, 100)
                for (value in gridValues) {
                    val y = chartHeight * (1f - value / maxPercent)
                    drawLine(
                        color = ChartGridLine,
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f
                    )

                    // Nhãn %
                    val labelResult = textMeasurer.measure(
                        text = "${value}%",
                        style = TextStyle(fontSize = 9.sp, color = Color.Gray)
                    )
                    drawText(
                        textLayoutResult = labelResult,
                        topLeft = Offset(0f, y - labelResult.size.height / 2f)
                    )
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
                        color = ChartBlue.copy(alpha = 0.1f)
                    )
                }

                // Vẽ đường line
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = ChartBlue,
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
                        color = ChartBlue,
                        radius = 3.5.dp.toPx(),
                        center = point
                    )
                }

                // Label ngày bên dưới
                data.forEachIndexed { index, item ->
                    val x = leftPadding + (usableWidth / (data.size - 1).coerceAtLeast(1)) * index
                    val labelResult = textMeasurer.measure(
                        text = item.label,
                        style = TextStyle(fontSize = 10.sp, color = Color.Gray)
                    )
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

/**
 * Hiển thị badge trình độ với màu sắc tương ứng.
 */
@Composable
fun LevelBadge(
    level: String,
    modifier: Modifier = Modifier
) {
    val badgeColor = when (level) {
        "Advanced" -> Color(0xFF34A853)
        "Intermediate" -> Color(0xFFF9AB00)
        else -> Color(0xFF1A73E8)
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
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = badgeColor,
            textAlign = TextAlign.Center
        )
    }
}
