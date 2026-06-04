package vn.edu.hcmute.minlish.ui.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import vn.edu.hcmute.minlish.data.repository.ProgressRepository
import vn.edu.hcmute.minlish.data.repository.VocabularyRepository
import vn.edu.hcmute.minlish.data.util.SettingsManager

class LearningViewModelFactory(
    private val userId: Int,
    private val deckId: Int?,
    private val vocabularyRepository: VocabularyRepository,
    private val progressRepository: ProgressRepository,
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LearningViewModel::class.java)) {
            return LearningViewModel(
                userId = userId,
                deckId = deckId,
                vocabularyRepository = vocabularyRepository,
                progressRepository = progressRepository,
                settingsManager = settingsManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
