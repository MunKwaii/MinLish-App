package vn.edu.hcmute.minlish.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vn.edu.hcmute.minlish.data.repository.ProgressRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Dữ liệu hoạt động theo ngày — dùng cho biểu đồ Daily Activity.
 *
 * @param label Nhãn ngày (VD: "T2", "T3", ...)
 * @param wordsLearned Số từ đã học trong ngày
 * @param wordsReviewed Số từ đã ôn tập trong ngày
 */
data class DailyActivityData(
    val label: String,
    val wordsLearned: Int,
    val wordsReviewed: Int
)

/**
 * Dữ liệu retention rate theo ngày — dùng cho biểu đồ Retention Rate.
 *
 * @param label Nhãn ngày
 * @param accuracyPercent Tỷ lệ chính xác (0–100)
 */
data class RetentionData(
    val label: String,
    val accuracyPercent: Float
)

/**
 * State tổng hợp của màn hình Dashboard.
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalWordsLearned: Int = 0,
    val currentStreak: Int = 0,
    val accuracyPercent: Float = 0f,
    val estimatedLevel: String = "Beginner",
    val dailyActivity: List<DailyActivityData> = emptyList(),
    val retentionData: List<RetentionData> = emptyList()
)

/**
 * ViewModel cho màn hình Dashboard.
 *
 * Chịu trách nhiệm:
 * - Truy vấn dữ liệu tiến trình từ Repository
 * - Tính toán Streak (chuỗi ngày học liên tục)
 * - Tính toán Accuracy (% câu trả lời đúng)
 * - Chuẩn bị dữ liệu cho biểu đồ
 * - Đánh giá trình độ (Beginner / Intermediate / Advanced)
 */
class DashboardViewModel(
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Tải toàn bộ dữ liệu dashboard cho user hiện tại.
     */
    fun loadDashboardData(userId: Int) {
        viewModelScope.launch {
            _uiState.value = DashboardUiState(isLoading = true)

            try {
                // 1. Lấy tổng số từ đã học
                val totalWords = progressRepository.getTotalWordsLearned(userId)

                // 2. Tính accuracy
                val correctAnswers = progressRepository.getTotalCorrectAnswers(userId)
                val totalAnswers = progressRepository.getTotalAnswers(userId)
                val accuracy = if (totalAnswers > 0) {
                    (correctAnswers.toFloat() / totalAnswers.toFloat()) * 100f
                } else {
                    0f
                }

                // 3. Tính streak
                val studyDates = progressRepository.getStudyDates(userId)
                val streak = calculateStreak(studyDates)

                // 4. Lấy dữ liệu 7 ngày gần nhất cho biểu đồ
                val calendar = Calendar.getInstance()
                val endDate = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_YEAR, -6)
                val startDate = dateFormat.format(calendar.time)

                val progressList = progressRepository.getProgressByDateRange(
                    userId, startDate, endDate
                )

                // 5. Chuẩn bị dữ liệu biểu đồ Daily Activity
                val dailyActivity = buildDailyActivityData(startDate)
                val progressMap = progressList.associateBy { it.date }

                val filledDailyActivity = dailyActivity.map { day ->
                    val progress = progressMap[day.label]
                    if (progress != null) {
                        DailyActivityData(
                            label = getDayLabel(day.label),
                            wordsLearned = progress.newWordsLearned,
                            wordsReviewed = progress.wordsReviewed
                        )
                    } else {
                        DailyActivityData(
                            label = getDayLabel(day.label),
                            wordsLearned = 0,
                            wordsReviewed = 0
                        )
                    }
                }

                // 6. Chuẩn bị dữ liệu biểu đồ Retention Rate
                val filledRetention = dailyActivity.map { day ->
                    val progress = progressMap[day.label]
                    val dayAccuracy = if (progress != null && progress.totalAnswers > 0) {
                        (progress.correctAnswers.toFloat() / progress.totalAnswers.toFloat()) * 100f
                    } else {
                        0f
                    }
                    RetentionData(
                        label = getDayLabel(day.label),
                        accuracyPercent = dayAccuracy
                    )
                }

                // 7. Đánh giá trình độ
                val level = estimateLevel(totalWords, accuracy, streak)

                _uiState.value = DashboardUiState(
                    isLoading = false,
                    totalWordsLearned = totalWords,
                    currentStreak = streak,
                    accuracyPercent = accuracy,
                    estimatedLevel = level,
                    dailyActivity = filledDailyActivity,
                    retentionData = filledRetention
                )
            } catch (e: Exception) {
                // Nếu lỗi thì vẫn hiển thị dashboard với dữ liệu mặc định
                _uiState.value = DashboardUiState(isLoading = false)
            }
        }
    }

    /**
     * Tính streak — chuỗi ngày học liên tục tính từ hôm nay ngược lại.
     *
     * Ví dụ: Nếu user học vào ngày 4, 3, 2 (tháng 6) thì streak = 3.
     * Nếu ngày hôm nay chưa học nhưng hôm qua có học thì tính từ hôm qua.
     */
    private fun calculateStreak(studyDates: List<String>): Int {
        if (studyDates.isEmpty()) return 0

        val calendar = Calendar.getInstance()
        val today = dateFormat.format(calendar.time)

        // Kiểm tra hôm nay hoặc hôm qua có trong danh sách không
        val hasToday = studyDates.contains(today)

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = dateFormat.format(calendar.time)
        val hasYesterday = studyDates.contains(yesterday)

        if (!hasToday && !hasYesterday) return 0

        // Bắt đầu đếm từ ngày gần nhất
        var streak = 0
        val startCalendar = Calendar.getInstance()
        if (!hasToday) {
            startCalendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        val dateSet = studyDates.toSet()
        for (i in 0..365) {
            val checkDate = dateFormat.format(startCalendar.time)
            if (dateSet.contains(checkDate)) {
                streak++
                startCalendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        return streak
    }

    /**
     * Đánh giá trình độ dựa trên 3 tiêu chí.
     *
     * Hệ thống điểm:
     * - Mỗi tiêu chí cho điểm 1 (Beginner), 2 (Intermediate), 3 (Advanced)
     * - Lấy trung bình rồi phân loại
     *
     * | Tiêu chí        | Beginner  | Intermediate | Advanced |
     * |-----------------|-----------|--------------|----------|
     * | Tổng từ đã học  | < 50      | 50–200       | > 200    |
     * | Accuracy        | < 60%     | 60–80%       | > 80%    |
     * | Streak          | < 3 ngày  | 3–14 ngày    | > 14 ngày|
     */
    private fun estimateLevel(totalWords: Int, accuracy: Float, streak: Int): String {
        // Điểm cho số từ đã học
        val wordScore = when {
            totalWords >= 200 -> 3
            totalWords >= 50 -> 2
            else -> 1
        }

        // Điểm cho accuracy
        val accuracyScore = when {
            accuracy >= 80f -> 3
            accuracy >= 60f -> 2
            else -> 1
        }

        // Điểm cho streak
        val streakScore = when {
            streak >= 14 -> 3
            streak >= 3 -> 2
            else -> 1
        }

        // Tính trung bình và phân loại
        val average = (wordScore + accuracyScore + streakScore) / 3.0

        return when {
            average >= 2.5 -> "Advanced"
            average >= 1.5 -> "Intermediate"
            else -> "Beginner"
        }
    }

    /**
     * Tạo danh sách 7 ngày gần nhất làm khung dữ liệu biểu đồ.
     */
    private fun buildDailyActivityData(startDate: String): List<DailyActivityData> {
        val result = mutableListOf<DailyActivityData>()
        val calendar = Calendar.getInstance()

        try {
            val parsedDate = dateFormat.parse(startDate)
            if (parsedDate != null) {
                calendar.time = parsedDate
            }
        } catch (e: Exception) {
            calendar.add(Calendar.DAY_OF_YEAR, -6)
        }

        for (i in 0..6) {
            val date = dateFormat.format(calendar.time)
            result.add(DailyActivityData(label = date, wordsLearned = 0, wordsReviewed = 0))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }

    /**
     * Chuyển đổi ngày (yyyy-MM-dd) sang nhãn ngắn (T2, T3, ..., CN).
     */
    private fun getDayLabel(dateString: String): String {
        return try {
            val parsedDate = dateFormat.parse(dateString)
            if (parsedDate != null) {
                val cal = Calendar.getInstance()
                cal.time = parsedDate
                when (cal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "T2"
                    Calendar.TUESDAY -> "T3"
                    Calendar.WEDNESDAY -> "T4"
                    Calendar.THURSDAY -> "T5"
                    Calendar.FRIDAY -> "T6"
                    Calendar.SATURDAY -> "T7"
                    Calendar.SUNDAY -> "CN"
                    else -> "?"
                }
            } else {
                "?"
            }
        } catch (e: Exception) {
            "?"
        }
    }
}

/**
 * Factory để tạo DashboardViewModel với dependency ProgressRepository.
 */
class DashboardViewModelFactory(
    private val progressRepository: ProgressRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(progressRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
