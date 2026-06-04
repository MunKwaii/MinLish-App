package vn.edu.hcmute.minlish.data.repository

import vn.edu.hcmute.minlish.data.local.entity.FlashcardProgress
import vn.edu.hcmute.minlish.data.local.entity.StudyProgress

/**
 * Repository quản lý dữ liệu tiến trình học tập.
 *
 * Cung cấp dữ liệu cho Dashboard:
 * - Thống kê tổng hợp (tổng từ, accuracy)
 * - Dữ liệu biểu đồ (daily activity, retention rate)
 * - Danh sách ngày học (để tính streak)
 * - Ghi nhận phiên học mới
 * - Dữ liệu FlashcardProgress (để phân loại maturity theo Anki)
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

    // Lấy toàn bộ FlashcardProgress của user (Anki maturity)
    suspend fun getFlashcardProgressByUser(userId: Int): List<FlashcardProgress>

    // Đếm tổng số từ trong tất cả bộ từ của user
    suspend fun getTotalWordCountByUser(userId: Int): Int

    suspend fun getFlashcardProgress(userId: Int, wordId: Int): FlashcardProgress?

    suspend fun saveFlashcardProgress(progress: FlashcardProgress): Result<Long>
}

