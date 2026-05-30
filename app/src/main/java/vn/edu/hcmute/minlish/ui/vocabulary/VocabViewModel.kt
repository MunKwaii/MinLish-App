package vn.edu.hcmute.minlish.ui.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.edu.hcmute.minlish.data.local.entity.Deck
import vn.edu.hcmute.minlish.data.repository.VocabularyRepository

/**
 * ViewModel xử lý nghiệp vụ cho module quản lý từ vựng.
 *
 * Nhiệm vụ chính:
 * - Tải danh sách bộ từ vựng theo người dùng.
 * - Tải danh sách từ vựng theo bộ từ.
 * - Kiểm tra dữ liệu nhập trước khi tạo bộ từ.
 * - Kiểm tra dữ liệu nhập trước khi thêm từ vựng.
 *
 * UI không nên gọi trực tiếp DAO hoặc Repository.
 * UI chỉ gửi sự kiện về ViewModel, sau đó ViewModel cập nhật lại state.
 */
class VocabViewModel(
    private val vocabularyRepository: VocabularyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VocabUiState())
    val uiState: StateFlow<VocabUiState> = _uiState.asStateFlow()

    private var deckJob: Job? = null
    private var wordJob: Job? = null

    /**
     * Tải toàn bộ bộ từ vựng thuộc về người dùng hiện tại.
     *
     * userId sẽ được lấy từ thông tin đăng nhập của người dùng.
     */
    fun loadDecks(userId: Int) {
        deckJob?.cancel()

        deckJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            try {
                vocabularyRepository.getDecksByUser(userId).collect { decks ->
                    _uiState.update {
                        it.copy(
                            decks = decks,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Không thể tải danh sách bộ từ"
                    )
                }
            }
        }
    }

    /**
     * Chọn một bộ từ và tải danh sách từ vựng bên trong bộ từ đó.
     */
    fun selectDeck(deck: Deck) {
        _uiState.update {
            it.copy(
                selectedDeck = deck,
                words = emptyList(),
                errorMessage = null,
                successMessage = null
            )
        }

        loadWords(deck.deckId)
    }

    /**
     * Tải danh sách từ vựng theo mã bộ từ.
     */
    fun loadWords(deckId: Int) {
        wordJob?.cancel()

        wordJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            try {
                vocabularyRepository.getWordsByDeck(deckId).collect { words ->
                    _uiState.update {
                        it.copy(
                            words = words,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Không thể tải danh sách từ vựng"
                    )
                }
            }
        }
    }

    /**
     * Tạo một bộ từ vựng mới.
     */
    fun createDeck(
        userId: Int,
        name: String,
        description: String,
        tags: String
    ) {
        if (name.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Tên bộ từ không được để trống")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, successMessage = null)
            }

            val result = vocabularyRepository.createDeck(
                userId = userId,
                name = name,
                description = description,
                tags = tags
            )

            result
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Đã tạo bộ từ vựng thành công"
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Tạo bộ từ thất bại"
                        )
                    }
                }
        }
    }

    /**
     * Thêm một từ vựng mới vào bộ từ đang chọn.
     */
    fun addWord(
        deckId: Int,
        word: String,
        pronunciation: String,
        meaning: String,
        description: String,
        example: String,
        collocations: String,
        relatedWords: String,
        note: String
    ) {
        if (word.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Từ vựng không được để trống")
            }
            return
        }

        if (meaning.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Nghĩa của từ không được để trống")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, successMessage = null)
            }

            val result = vocabularyRepository.addWord(
                deckId = deckId,
                word = word,
                pronunciation = pronunciation,
                meaning = meaning,
                description = description.ifBlank { null },
                example = example.ifBlank { null },
                collocations = collocations.ifBlank { null },
                relatedWords = relatedWords.ifBlank { null },
                note = note.ifBlank { null }
            )

            result
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Đã thêm từ vựng thành công"
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Thêm từ vựng thất bại"
                        )
                    }
                }
        }
    }

    /**
     * Xóa thông báo sau khi UI đã hiển thị xong.
     *
     * Hàm này giúp tránh việc Toast/Snackbar bị hiển thị lại
     * khi màn hình được recomposition.
     */
    fun clearMessage() {
        _uiState.update {
            it.copy(
                successMessage = null,
                errorMessage = null
            )
        }
    }
}

/**
 * Factory dùng để khởi tạo VocabViewModel với Repository.
 *
 * Vì VocabViewModel cần truyền VocabularyRepository vào constructor,
 * nên không thể dùng ViewModel mặc định không tham số.
 */
class VocabViewModelFactory(
    private val vocabularyRepository: VocabularyRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VocabViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VocabViewModel(vocabularyRepository) as T
        }

        throw IllegalArgumentException("Không xác định được ViewModel class")
    }
}