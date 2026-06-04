package vn.edu.hcmute.minlish.data.repository

import vn.edu.hcmute.minlish.data.local.entity.StudyProgress

/**
 * Repository quản lý dữ liệu tiến trình học tập.
 *
 * Cung cấp dữ liệu cho Dashboard:
 * - Thống kê tổng hợp (tổng từ, accuracy)
 * - Dữ liệu biểu đồ (daily activity, retention rate)
 * - Danh sách ngày học (để tính streak)
 * - Ghi nhận phiên học mới
 */
interface ProgressRepository {

    // Lấy tổng số từ đã học
    suspend fun getTotalWordsLearned(userId: Int): Int

    // Lấy tổng số câu đúng
    suspend fun getTotalCorrectAnswers(userId: Int): Int

    // Lấy tổng số câu trả lời
    suspend fun getTotalAnswers(userId: Int): Int

    // Lấy danh sách ngày đã học (để tính streak)
    suspend fun getStudyDates(userId: Int): List<String>

    // Lấy dữ liệu tiến trình theo khoảng ngày
    suspend fun getProgressByDateRange(
        userId: Int,
        startDate: String,
        endDate: String
    ): List<StudyProgress>

    // Lấy tiến trình của ngày cụ thể
    suspend fun getProgressByDate(userId: Int, date: String): StudyProgress?

    // Ghi nhận phiên học
    suspend fun recordStudySession(progress: StudyProgress): Result<Long>
}
