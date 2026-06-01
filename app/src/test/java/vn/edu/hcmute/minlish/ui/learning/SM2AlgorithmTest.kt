package vn.edu.hcmute.minlish.ui.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.edu.hcmute.minlish.data.local.entity.FlashcardProgress

class SM2AlgorithmTest {

    private val algorithm: SpacedRepetitionStrategy = SM2Algorithm()

    @Test
    fun testAgainDifficultyResetsIntervalTo1() {
        // GIVEN: Một tiến trình ôn tập đang ở mức cao (interval = 10, easeFactor = 2.5f)
        val progress = FlashcardProgress(
            userId = 1,
            wordId = 1,
            easeFactor = 2.5f,
            interval = 10,
            nextReviewTime = 0L
        )

        // WHEN: Người dùng chọn Again (quality = 0)
        val result = algorithm.calculateNextReview(progress, 0)

        // THEN: Số ngày ôn tập giãn cách (interval) phải reset về 1 ngày
        assertEquals(1, result.interval)
        // Ease factor phải giảm đi đáng kể nhưng không dưới 1.3
        assertTrue(result.easeFactor < progress.easeFactor)
        assertTrue(result.nextReviewTime > System.currentTimeMillis())
    }

    @Test
    fun testGoodDifficultyIncreasesInterval() {
        // GIVEN: Một tiến trình ôn tập ở ngày đầu tiên (interval = 1, easeFactor = 2.5f)
        val progress = FlashcardProgress(
            userId = 1,
            wordId = 1,
            easeFactor = 2.5f,
            interval = 1,
            nextReviewTime = 0L
        )

        // WHEN: Người dùng chọn Good (quality = 4)
        val result = algorithm.calculateNextReview(progress, 4)

        // THEN: Đối với lần ôn tập thứ 2 (interval hiện tại là 1), interval tiếp theo phải là 6 ngày
        assertEquals(6, result.interval)
        // Ease factor giảm nhẹ (với q = 4, EF = 2.5f + (0.1f - 1 * (0.08f + 1 * 0.02f)) = 2.5f + (0.1f - 0.1f) = 2.5f)
        // Khoan đã! q = 4 => 5 - q = 1.
        // Phép tính: EF' = 2.5 + (0.1 - 1 * (0.08 + 1 * 0.02)) = 2.5 + (0.1 - 0.1) = 2.5f.
        assertEquals(2.5f, result.easeFactor, 0.01f)
    }

    @Test
    fun testEasyDifficultyBoostsEaseFactorAndInterval() {
        // GIVEN: Tiến trình ôn tập lần thứ 3 trở lên (interval = 6, easeFactor = 2.5f)
        val progress = FlashcardProgress(
            userId = 1,
            wordId = 1,
            easeFactor = 2.5f,
            interval = 6,
            nextReviewTime = 0L
        )

        // WHEN: Người dùng chọn Easy (quality = 5)
        val result = algorithm.calculateNextReview(progress, 5)

        // THEN:
        // - Ease Factor mới: 2.5f + (0.1f - 0) = 2.6f (tăng lên)
        assertEquals(2.6f, result.easeFactor, 0.01f)
        // - Interval mới: 6 * 2.5f = 15 ngày
        assertEquals(15, result.interval)
    }
}
