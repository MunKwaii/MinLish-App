package vn.edu.hcmute.minlish.ui.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.edu.hcmute.minlish.data.local.entity.Deck
import vn.edu.hcmute.minlish.data.local.entity.Word
import vn.edu.hcmute.minlish.data.remote.dictionary.DictionaryApiDataSource
import vn.edu.hcmute.minlish.data.repository.VocabularyRepository

class VocabViewModel(
    private val vocabularyRepository: VocabularyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VocabUiState())
    val uiState: StateFlow<VocabUiState> = _uiState.asStateFlow()

    private var deckJob: Job? = null
    private var wordJob: Job? = null

    // Tải danh sách bộ từ vựng theo user hiện tại
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
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Không thể tải danh sách bộ từ"
                    )
                }
            }
        }
    }

    // Chọn deck và tải danh sách từ thuộc deck đó
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

    // Tải danh sách từ vựng theo deckId
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
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Không thể tải danh sách từ vựng"
                    )
                }
            }
        }
    }

    // Tạo bộ từ vựng mới
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
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
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

    // Thêm từ vựng thủ công từ form nhập liệu
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
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
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

    // Import danh sách từ bằng Dictionary API rồi lưu vào Room Database
    fun importWordsFromDictionary(deckId: Int, words: List<String>) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                val dictionaryApi = DictionaryApiDataSource()
                val importedWords = mutableListOf<Word>()

                for (rawWord in words) {
                    val cleanWord = rawWord.trim()
                    if (cleanWord.isBlank()) continue

                    val lookup = dictionaryApi.lookupWord(cleanWord)
                    if (!lookup.exists) continue

                    val firstResult = lookup.results?.firstOrNull() ?: continue

                    val firstMeaning = firstResult.meanings
                        ?.firstOrNull { !it.definition.isNullOrBlank() }
                        ?: continue

                    val pronunciation = firstResult.pronunciations
                        ?.firstOrNull()
                        ?.ipa
                        .orEmpty()

                    val meaning = firstMeaning.definition.orEmpty()
                    if (meaning.isBlank()) continue

                    val description = listOfNotNull(
                        firstMeaning.pos,
                        firstMeaning.source
                    ).joinToString(" - ").ifBlank { null }

                    val relatedWords = firstResult.relations
                        ?.mapNotNull { it.related_word }
                        ?.distinct()
                        ?.joinToString(", ")
                        ?.ifBlank { null }

                    importedWords.add(
                        Word(
                            deckId = deckId,
                            word = lookup.word?.ifBlank { cleanWord } ?: cleanWord,
                            pronunciation = pronunciation,
                            meaning = meaning,
                            description = description,
                            example = firstMeaning.example,
                            collocations = null,
                            relatedWords = relatedWords,
                            note = "Imported from dict.minhqnd.com"
                        )
                    )
                }

                if (importedWords.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Không tìm thấy từ hợp lệ để import"
                        )
                    }
                    return@launch
                }

                val result = vocabularyRepository.importWords(deckId, importedWords)

                result
                    .onSuccess {
                        loadWords(deckId)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Đã import ${importedWords.size} từ vựng"
                            )
                        }
                    }
                    .onFailure { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = exception.message ?: "Import thất bại"
                            )
                        }
                    }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Không thể import từ dictionary"
                    )
                }
            }
        }
    }

    // Xóa thông báo sau khi UI đã hiển thị xong
    fun clearMessage() {
        _uiState.update {
            it.copy(
                successMessage = null,
                errorMessage = null
            )
        }
    }

    // Tra cứu chi tiết từ vựng từ API
    fun lookupWordDetails(word: String) {
        if (word.isBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLookupLoading = true, lookupError = null)
            }

            try {
                val dictionaryApi = DictionaryApiDataSource()
                val result = dictionaryApi.lookupWord(word)
                _uiState.update {
                    it.copy(
                        lookupResult = result,
                        isLookupLoading = false
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLookupLoading = false,
                        lookupError = e.message ?: "Không thể tra cứu từ vựng"
                    )
                }
            }
        }
    }

    // Reset kết quả tra cứu từ vựng
    fun resetLookupResult() {
        _uiState.update {
            it.copy(
                lookupResult = null,
                isLookupLoading = false,
                lookupError = null
            )
        }
    }
}

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