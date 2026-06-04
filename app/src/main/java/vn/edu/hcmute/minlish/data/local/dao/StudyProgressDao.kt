package vn.edu.hcmute.minlish.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import vn.edu.hcmute.minlish.data.local.entity.StudyProgress

@Dao
interface StudyProgressDao {

    // Lấy toàn bộ tiến trình của user
    @Query("SELECT * FROM study_progress WHERE userId = :userId ORDER BY date DESC")
    suspend fun getProgressByUser(userId: Int): List<StudyProgress>

    // Lấy tiến trình theo khoảng ngày (dùng cho biểu đồ 7 ngày)
    @Query(
        "SELECT * FROM study_progress WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC"
    )
    suspend fun getProgressByDateRange(
        userId: Int,
        startDate: String,
        endDate: String
    ): List<StudyProgress>

    // Tổng số từ mới đã học
    @Query("SELECT COALESCE(SUM(newWordsLearned), 0) FROM study_progress WHERE userId = :userId")
    suspend fun getTotalWordsLearned(userId: Int): Int

    // Tổng số câu trả lời đúng
    @Query("SELECT COALESCE(SUM(correctAnswers), 0) FROM study_progress WHERE userId = :userId")
    suspend fun getTotalCorrectAnswers(userId: Int): Int

    // Tổng số câu trả lời
    @Query("SELECT COALESCE(SUM(totalAnswers), 0) FROM study_progress WHERE userId = :userId")
    suspend fun getTotalAnswers(userId: Int): Int

    // Danh sách ngày đã học (dùng để tính streak)
    @Query("SELECT DISTINCT date FROM study_progress WHERE userId = :userId ORDER BY date DESC")
    suspend fun getStudyDates(userId: Int): List<String>

    // Lấy tiến trình của một ngày cụ thể
    @Query("SELECT * FROM study_progress WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getProgressByDate(userId: Int, date: String): StudyProgress?

    // Thêm bản ghi tiến trình mới
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: StudyProgress): Long

    // Cập nhật bản ghi tiến trình
    @Update
    suspend fun updateProgress(progress: StudyProgress)
}
