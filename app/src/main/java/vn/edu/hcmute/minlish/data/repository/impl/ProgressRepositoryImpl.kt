package vn.edu.hcmute.minlish.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vn.edu.hcmute.minlish.data.local.dao.StudyProgressDao
import vn.edu.hcmute.minlish.data.local.entity.FlashcardProgress
import vn.edu.hcmute.minlish.data.local.entity.StudyProgress
import vn.edu.hcmute.minlish.data.repository.ProgressRepository

/**
 * Implementation của ProgressRepository.
 *
 * Tất cả các thao tác truy vấn đều chạy trên Dispatchers.IO
 * để tránh block main thread.
 */
class ProgressRepositoryImpl(
    private val studyProgressDao: StudyProgressDao
) : ProgressRepository {

    override suspend fun getTotalWordsLearned(userId: Int): Int {
        return withContext(Dispatchers.IO) {
            studyProgressDao.getTotalWordsLearned(userId)
        }
    }

    override suspend fun getTotalCorrectAnswers(userId: Int): Int {
        return withContext(Dispatchers.IO) {
            studyProgressDao.getTotalCorrectAnswers(userId)
        }
    }

    override suspend fun getTotalAnswers(userId: Int): Int {
        return withContext(Dispatchers.IO) {
            studyProgressDao.getTotalAnswers(userId)
        }
    }

    override suspend fun getStudyDates(userId: Int): List<String> {
        return withContext(Dispatchers.IO) {
            studyProgressDao.getStudyDates(userId)
        }
    }

    override suspend fun getProgressByDateRange(
        userId: Int,
        startDate: String,
        endDate: String
    ): List<StudyProgress> {
        return withContext(Dispatchers.IO) {
            studyProgressDao.getProgressByDateRange(userId, startDate, endDate)
        }
    }

    override suspend fun getProgressByDate(userId: Int, date: String): StudyProgress? {
        return withContext(Dispatchers.IO) {
            studyProgressDao.getProgressByDate(userId, date)
        }
    }

    override suspend fun recordStudySession(progress: StudyProgress): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val id = studyProgressDao.insertProgress(progress)
                Result.success(id)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getFlashcardProgressByUser(userId: Int): List<FlashcardProgress> {
        return withContext(Dispatchers.IO) {
            studyProgressDao.getFlashcardProgressByUser(userId)
        }
    }

    override suspend fun getTotalWordCountByUser(userId: Int): Int {
        return withContext(Dispatchers.IO) {
            studyProgressDao.getTotalWordCountByUser(userId)
        }
    }
}

