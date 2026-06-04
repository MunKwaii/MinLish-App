package vn.edu.hcmute.minlish.ui.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.edu.hcmute.minlish.data.local.entity.FlashcardProgress
import vn.edu.hcmute.minlish.data.local.entity.Word
import vn.edu.hcmute.minlish.data.repository.ProgressRepository
import vn.edu.hcmute.minlish.data.repository.VocabularyRepository
import vn.edu.hcmute.minlish.data.util.SettingsManager

enum class CardDifficulty {
    AGAIN, HARD, GOOD, EASY
}

data class LearningUiState(
    val words: List<Word> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isFinished: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val progressMap: Map<Int, FlashcardProgress> = emptyMap() // Lưu tiến trình học: wordId -> FlashcardProgress
)

class LearningViewModel(
    private val userId: Int,
    private val deckId: Int?,
    private val vocabularyRepository: VocabularyRepository,
    private val progressRepository: ProgressRepository,
    private val settingsManager: SettingsManager,
    private val spacedRepetitionStrategy: SpacedRepetitionStrategy = SM2Algorithm()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    init {
        loadRealData()
    }

    private fun loadRealData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 1. Tải giới hạn từ mới từ SettingsManager (DataStore)
                val limit = settingsManager.newWordsLimitFlow.first()

                // 2. Tải Daily Study Deck gộp từ mới và từ cần ôn tập
                val wordsList = vocabularyRepository.getDailyStudyDeck(
                    userId = userId,
                    deckId = if (deckId != null && deckId != -1) deckId else null,
                    newWordsLimit = limit,
                    currentTimestamp = System.currentTimeMillis()
                )

                // 3. Tải toàn bộ FlashcardProgress của User để tra cứu tiến trình ôn tập
                val allProgress = progressRepository.getFlashcardProgressByUser(userId)
                val progressMap = allProgress.associateBy { it.wordId }

                _uiState.update {
                    it.copy(
                        words = wordsList,
                        progressMap = progressMap,
                        isLoading = false,
                        isFinished = wordsList.isEmpty(),
                        currentIndex = 0,
                        isFlipped = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Đã xảy ra lỗi khi tải dữ liệu"
                    )
                }
            }
        }
    }

    fun flipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun evaluateCard(difficulty: CardDifficulty) {
        val currentState = _uiState.value
        if (currentState.currentIndex >= currentState.words.size) return

        val currentWord = currentState.words[currentState.currentIndex]

        // 1. Quy đổi từ CardDifficulty sang điểm chất lượng SM-2 (0 đến 5)
        val quality = when (difficulty) {
            CardDifficulty.AGAIN -> 0 // Học lại (Sai hoàn toàn)
            CardDifficulty.HARD -> 3  // Khó (Nhớ nhưng cần nỗ lực nhiều)
            CardDifficulty.GOOD -> 4  // Tốt (Nhớ và trả lời nhanh)
            CardDifficulty.EASY -> 5  // Dễ (Phản xạ ngay tức thì)
        }

        // 2. Lấy thông tin progress cũ của từ (hoặc tạo mặc định nếu chưa có)
        val currentProgress = currentState.progressMap[currentWord.wordId] ?: FlashcardProgress(
            userId = userId,
            wordId = currentWord.wordId,
            easeFactor = 2.5f,
            interval = 0,
            nextReviewTime = 0L
        )

        // 3. Sử dụng Strategy để tính toán tiến trình mới theo thuật toán SM-2
        val newProgress = spacedRepetitionStrategy.calculateNextReview(currentProgress, quality)

        // 4. In log kiểm thử toán học
        println("SM-2 [Word: '${currentWord.word}']: Đánh giá $difficulty (Quality: $quality). EF cũ: ${currentProgress.easeFactor} -> EF mới: ${newProgress.easeFactor}. Interval cũ: ${currentProgress.interval} -> Interval mới: ${newProgress.interval} ngày.")

        // 5. Lưu tiến trình mới vào SQLite DB thật bất đồng bộ
        viewModelScope.launch {
            progressRepository.saveFlashcardProgress(newProgress)
        }

        // 6. Cập nhật Map tiến trình học tập tạm thời trên UI
        val updatedProgressMap = currentState.progressMap.toMutableMap().apply {
            put(currentWord.wordId, newProgress)
        }

        // 7. Chuyển sang thẻ tiếp theo
        val nextIndex = currentState.currentIndex + 1

        if (nextIndex < currentState.words.size) {
            _uiState.update {
                it.copy(
                    currentIndex = nextIndex,
                    isFlipped = false, // Luôn reset trạng thái lật khi sang từ mới
                    progressMap = updatedProgressMap
                )
            }
        } else {
            // Đã học hết danh sách từ vựng hiện tại
            _uiState.update {
                it.copy(
                    isFinished = true,
                    progressMap = updatedProgressMap
                )
            }
        }
    }

    fun restartSession() {
        // Tải lại dữ liệu thật để cập nhật tiến trình ôn tập mới nhất của các thẻ từ
        loadRealData()
    }
}

