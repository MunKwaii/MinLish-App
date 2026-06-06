package vn.edu.hcmute.minlish.ui.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.edu.hcmute.minlish.data.local.entity.Deck
import vn.edu.hcmute.minlish.data.local.entity.Word
import vn.edu.hcmute.minlish.data.remote.dictionary.DictionaryApiDataSource
import vn.edu.hcmute.minlish.data.remote.dictionary.DictionaryLookupResult
import vn.edu.hcmute.minlish.data.repository.VocabularyRepository

data class DictionaryUiState(
    val query: String = "",
    val suggestions: List<String> = emptyList(),
    val isSuggestLoading: Boolean = false,

    val lookupResult: DictionaryLookupResult? = null,
    val isLookupLoading: Boolean = false,
    val lookupError: String? = null,
    val canGoBack: Boolean = false,

    // Trạng thái lưu từ vựng và ghi đè
    val decks: List<Deck> = emptyList(),
    val userWords: List<Word> = emptyList(),
    val showDeckSheet: Boolean = false,
    val duplicateWordForCompare: Word? = null,
    val targetDeckIdForSave: Int? = null,
    val showOverwriteDialog: Boolean = false,
    val saveSuccessMessage: String? = null,
    val saveErrorMessage: String? = null
)

class DictionaryViewModel(
    private val vocabularyRepository: VocabularyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DictionaryUiState())
    val uiState: StateFlow<DictionaryUiState> = _uiState.asStateFlow()

    private val dictionaryApi = DictionaryApiDataSource()
    private var suggestJob: Job? = null
    private var lookupJob: Job? = null
    private var decksJob: Job? = null
    private var wordsJob: Job? = null

    // Ngăn xếp lưu lịch sử tra cứu nhanh
    private val historyStack = mutableListOf<DictionaryLookupResult>()

    fun loadUserData(userId: Int) {
        decksJob?.cancel()
        decksJob = viewModelScope.launch {
            try {
                vocabularyRepository.getDecksByUser(userId).collect { list ->
                    _uiState.update { it.copy(decks = list) }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }

        wordsJob?.cancel()
        wordsJob = viewModelScope.launch {
            try {
                vocabularyRepository.getAllWordsByUser(userId).collect { list ->
                    _uiState.update { it.copy(userWords = list) }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.update { 
            it.copy(
                query = newQuery,
                lookupError = null
            ) 
        }

        suggestJob?.cancel()
        if (newQuery.trim().isEmpty()) {
            _uiState.update {
                it.copy(suggestions = emptyList(), isSuggestLoading = false)
            }
            return
        }

        suggestJob = viewModelScope.launch {
            delay(300)
            _uiState.update { it.copy(isSuggestLoading = true) }
            try {
                val results = dictionaryApi.suggestWords(newQuery)
                _uiState.update {
                    it.copy(suggestions = results, isSuggestLoading = false)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(suggestions = emptyList(), isSuggestLoading = false)
                }
            }
        }
    }

    fun lookupWord(word: String, saveToHistory: Boolean = true) {
        if (word.isBlank()) return

        lookupJob?.cancel()
        lookupJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLookupLoading = true, lookupError = null)
            }

            try {
                val result = dictionaryApi.lookupWord(word)
                if (result.exists) {
                    if (saveToHistory) {
                        val currentResult = _uiState.value.lookupResult
                        if (currentResult != null && currentResult.exists) {
                            historyStack.add(currentResult)
                        }
                    }
                    _uiState.update {
                        it.copy(
                            lookupResult = result,
                            query = result.word ?: word,
                            isLookupLoading = false,
                            canGoBack = historyStack.isNotEmpty()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLookupLoading = false,
                            lookupError = "Không tìm thấy từ '$word' trong từ điển"
                        )
                    }
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

    fun navigateBack() {
        if (historyStack.isNotEmpty()) {
            val prevResult = historyStack.removeAt(historyStack.lastIndex)
            _uiState.update {
                it.copy(
                    lookupResult = prevResult,
                    query = prevResult.word ?: "",
                    canGoBack = historyStack.isNotEmpty(),
                    lookupError = null
                )
            }
        } else {
            clearLookup()
        }
    }

    fun clearLookup() {
        historyStack.clear()
        _uiState.update {
            it.copy(
                lookupResult = null,
                query = "",
                suggestions = emptyList(),
                canGoBack = false,
                lookupError = null
            )
        }
    }

    fun setShowDeckSheet(show: Boolean) {
        _uiState.update { it.copy(showDeckSheet = show) }
    }

    fun onDeckSelected(deckId: Int, result: DictionaryLookupResult) {
        val wordName = result.word ?: return
        val duplicate = _uiState.value.userWords.firstOrNull {
            it.deckId == deckId && it.word.equals(wordName, ignoreCase = true)
        }

        if (duplicate != null) {
            // Đã tồn tại trong bộ từ này -> Hiện Dialog so sánh để ghi đè
            _uiState.update {
                it.copy(
                    duplicateWordForCompare = duplicate,
                    targetDeckIdForSave = deckId,
                    showOverwriteDialog = true,
                    showDeckSheet = false
                )
            }
        } else {
            // Chưa tồn tại -> Lưu từ mới trực tiếp
            saveNewWord(deckId, result)
        }
    }

    private fun saveNewWord(deckId: Int, result: DictionaryLookupResult) {
        viewModelScope.launch {
            _uiState.update { it.copy(showDeckSheet = false) }
            val mapped = mapLookupResultToWordInput(deckId, result)
            val saveResult = vocabularyRepository.addWord(
                deckId = mapped.deckId,
                word = mapped.word,
                pronunciation = mapped.pronunciation,
                meaning = mapped.meaning,
                description = mapped.description,
                example = mapped.example,
                collocations = mapped.collocations,
                relatedWords = mapped.relatedWords,
                note = mapped.note
            )

            saveResult
                .onSuccess {
                    _uiState.update {
                        it.copy(saveSuccessMessage = "Đã lưu từ vựng vào bộ từ thành công")
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(saveErrorMessage = exception.message ?: "Lưu từ vựng thất bại")
                    }
                }
        }
    }

    fun confirmOverwrite(result: DictionaryLookupResult) {
        val duplicate = _uiState.value.duplicateWordForCompare ?: return
        val deckId = _uiState.value.targetDeckIdForSave ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(showOverwriteDialog = false) }
            val mapped = mapLookupResultToWordInput(deckId, result)
            val updatedWord = duplicate.copy(
                pronunciation = mapped.pronunciation,
                meaning = mapped.meaning,
                description = mapped.description,
                example = mapped.example,
                collocations = mapped.collocations,
                relatedWords = mapped.relatedWords,
                note = mapped.note
            )

            val updateResult = vocabularyRepository.updateWord(updatedWord)
            updateResult
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            saveSuccessMessage = "Đã ghi đè cập nhật từ vựng thành công",
                            duplicateWordForCompare = null,
                            targetDeckIdForSave = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            saveErrorMessage = exception.message ?: "Ghi đè thất bại",
                            duplicateWordForCompare = null,
                            targetDeckIdForSave = null
                        )
                    }
                }
        }
    }

    fun cancelOverwrite() {
        _uiState.update {
            it.copy(
                showOverwriteDialog = false,
                duplicateWordForCompare = null,
                targetDeckIdForSave = null
            )
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                saveSuccessMessage = null,
                saveErrorMessage = null
            )
        }
    }

    private fun mapLookupResultToWordInput(deckId: Int, result: DictionaryLookupResult): Word {
        val firstResult = result.results?.firstOrNull()
        val firstMeaning = firstResult?.meanings?.firstOrNull { !it.definition.isNullOrBlank() }

        val pronunciation = firstResult?.pronunciations?.firstOrNull()?.ipa.orEmpty()
        val meaning = firstMeaning?.definition.orEmpty().ifBlank { "Chưa có định nghĩa" }

        val description = firstMeaning?.let {
            listOfNotNull(it.pos, it.source).joinToString(" - ").ifBlank { null }
        }

        val example = firstMeaning?.example

        val relatedWords = firstResult?.relations
            ?.mapNotNull { it.related_word }
            ?.distinct()
            ?.joinToString(", ")
            ?.ifBlank { null }

        return Word(
            deckId = deckId,
            word = result.word ?: "",
            pronunciation = pronunciation,
            meaning = meaning,
            description = description,
            example = example,
            collocations = null,
            relatedWords = relatedWords,
            note = "Lưu từ từ điển"
        )
    }
}

class DictionaryViewModelFactory(
    private val vocabularyRepository: VocabularyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DictionaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DictionaryViewModel(vocabularyRepository) as T
        }
        throw IllegalArgumentException("Không xác định được ViewModel class")
    }
}
