package vn.edu.hcmute.minlish.ui.learning

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import vn.edu.hcmute.minlish.data.local.entity.Word

enum class CardDifficulty {
    AGAIN, HARD, GOOD, EASY
}

data class LearningUiState(
    val words: List<Word> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isFinished: Boolean = false
)

class LearningViewModel : ViewModel() {

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
        // Trong Spaced Repetition thực tế, đánh giá này sẽ cập nhật thuật toán SuperMemo (SM-2)
        // Hiện tại để test UI, chúng ta chỉ cần chuyển sang thẻ tiếp theo
        val currentState = _uiState.value
        val nextIndex = currentState.currentIndex + 1

        if (nextIndex < currentState.words.size) {
            _uiState.update {
                it.copy(
                    currentIndex = nextIndex,
                    isFlipped = false // Luôn reset trạng thái lật khi sang từ mới
                )
            }
        } else {
            // Đã học hết danh sách từ vựng hiện tại
            _uiState.update {
                it.copy(isFinished = true)
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
