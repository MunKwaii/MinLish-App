package vn.edu.hcmute.minlish.ui.learning

import vn.edu.hcmute.minlish.data.local.entity.FlashcardProgress
import kotlin.math.max
import kotlin.math.roundToInt

class SM2Algorithm : SpacedRepetitionStrategy {
    override fun calculateNextReview(progress: FlashcardProgress, quality: Int): FlashcardProgress {
        // Giới hạn chất lượng từ 0 đến 5
        val q = quality.coerceIn(0, 5)

        // 1. Tính toán Ease Factor mới (EF')
        // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        val oldEF = progress.easeFactor
        var newEF = oldEF + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f))
        
        // EF không được nhỏ hơn 1.3f (Quy chuẩn SM-2)
        newEF = max(1.3f, newEF)

        // 2. Tính toán số ngày ôn tập giãn cách mới (Interval)
        val newInterval = if (q < 3) {
            // Nếu đánh giá kém (Again/quality < 3), reset interval về 1 ngày
            1
        } else {
            when (progress.interval) {
                0 -> 1 // Học lần đầu tiên
                1 -> 6 // Ôn tập lần thứ hai
                else -> (progress.interval * oldEF).roundToInt() // Ôn tập các lần tiếp theo
            }
        }

        // 3. Tính toán thời điểm ôn tập tiếp theo (nextReviewTime bằng mili-giây)
        val oneDayMillis = 24L * 60L * 60L * 1000L
        val newNextReviewTime = System.currentTimeMillis() + (newInterval * oneDayMillis)

        return progress.copy(
            easeFactor = newEF,
            interval = newInterval,
            nextReviewTime = newNextReviewTime
        )
    }
}
