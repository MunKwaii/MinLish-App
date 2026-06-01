package vn.edu.hcmute.minlish.ui.learning

import vn.edu.hcmute.minlish.data.local.entity.FlashcardProgress

interface SpacedRepetitionStrategy {
    /**
     * Calculates the next review parameters based on previous progress and response quality.
     * @param progress The previous learning progress parameters (easeFactor, interval, nextReviewTime).
     * @param quality The quality of response from 0 (forgot completely) to 5 (perfect recall).
     * @return A new FlashcardProgress instance with updated parameters.
     */
    fun calculateNextReview(progress: FlashcardProgress, quality: Int): FlashcardProgress
}
