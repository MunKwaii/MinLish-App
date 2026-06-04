package vn.edu.hcmute.minlish.ui.learning

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import vn.edu.hcmute.minlish.data.local.entity.FlashcardProgress
import vn.edu.hcmute.minlish.data.local.entity.Word

enum class CardDifficulty {
    AGAIN, HARD, GOOD, EASY
}

data class LearningUiState(
    val words: List<Word> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isFinished: Boolean = false,
    val progressMap: Map<Int, FlashcardProgress> = emptyMap() // Lưu tiến trình học: wordId -> FlashcardProgress
)

class LearningViewModel(
    private val spacedRepetitionStrategy: SpacedRepetitionStrategy = SM2Algorithm()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        val mockWords = listOf(
            Word(
                wordId = 1,
                deckId = 1,
                word = "Ephemeral",
                pronunciation = "/ɪˈfem.ər.əl/",
                meaning = "Phù du, chóng tàn, ngắn ngủi",
                description = "Kéo dài trong một khoảng thời gian rất ngắn.",
                example = "Fame in the age of social media is often ephemeral.",
                collocations = "ephemeral pleasure, ephemeral nature",
                relatedWords = "transitory, fleeting, temporary"
            ),
            Word(
                wordId = 2,
                deckId = 1,
                word = "Meticulous",
                pronunciation = "/məˈtɪk.jə.ləs/",
                meaning = "Tỉ mỉ, kỹ càng, quá cẩn thận",
                description = "Rất cẩn thận và chú ý đến từng chi tiết nhỏ nhất.",
                example = "Many hours of meticulous preparation have gone into writing the plan.",
                collocations = "meticulous attention, meticulous planning",
                relatedWords = "thorough, precise, diligent"
            ),
            Word(
                wordId = 3,
                deckId = 1,
                word = "Eloquent",
                pronunciation = "/ˈel.ə.kwənt/",
                meaning = "Hùng biện, có tài ăn nói, lưu loát",
                description = "Có khả năng biểu đạt ý kiến, cảm xúc một cách rõ ràng, mạnh mẽ và đầy thuyết phục.",
                example = "She made an eloquent appeal for action on climate change.",
                collocations = "eloquent speaker, eloquent speech",
                relatedWords = "fluent, expressive, persuasive"
            ),
            Word(
                wordId = 4,
                deckId = 1,
                word = "Plausible",
                pronunciation = "/ˈplɔː.zə.bəl/",
                meaning = "Hợp lý, đáng tin cậy",
                description = "Dường như đúng, có khả năng đúng hoặc có thể tin tưởng được.",
                example = "Her explanation of the accident sounded quite plausible.",
                collocations = "plausible excuse, plausible explanation",
                relatedWords = "reasonable, credible, believable"
            ),
            Word(
                wordId = 5,
                deckId = 1,
                word = "Resilient",
                pronunciation = "/rɪˈzɪl.jənt/",
                meaning = "Kiên cường, bền bỉ, đàn hồi",
                description = "Có khả năng phục hồi nhanh chóng từ khó khăn, nghịch cảnh hoặc chấn thương.",
                example = "He is a resilient character and will soon recover from this setback.",
                collocations = "resilient economy, resilient spirit",
                relatedWords = "tough, strong, adaptable"
            )
        )
        _uiState.update { it.copy(words = mockWords) }
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
            userId = 1, // Tạm thời mock userId là 1
            wordId = currentWord.wordId,
            easeFactor = 2.5f,
            interval = 0,
            nextReviewTime = 0L
        )

        // 3. Sử dụng Strategy để tính toán tiến trình mới theo thuật toán SM-2
        val newProgress = spacedRepetitionStrategy.calculateNextReview(currentProgress, quality)

        // 4. In log kiểm thử toán học
        println("SM-2 [Word: '${currentWord.word}']: Đánh giá $difficulty (Quality: $quality). EF cũ: ${currentProgress.easeFactor} -> EF mới: ${newProgress.easeFactor}. Interval cũ: ${currentProgress.interval} -> Interval mới: ${newProgress.interval} ngày.")

        // 5. Cập nhật Map tiến trình học tập
        val updatedProgressMap = currentState.progressMap.toMutableMap().apply {
            put(currentWord.wordId, newProgress)
        }

        // 6. Chuyển sang thẻ tiếp theo
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
        _uiState.update {
            it.copy(
                currentIndex = 0,
                isFlipped = false,
                isFinished = false
            )
        }
    }
}

