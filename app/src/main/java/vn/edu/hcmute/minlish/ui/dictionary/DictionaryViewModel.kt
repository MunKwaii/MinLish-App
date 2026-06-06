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
    val canGoBack: Boolean = false
)

class DictionaryViewModel(
    private val vocabularyRepository: VocabularyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DictionaryUiState())
    val uiState: StateFlow<DictionaryUiState> = _uiState.asStateFlow()

    private val dictionaryApi = DictionaryApiDataSource()
    private var suggestJob: Job? = null
    private var lookupJob: Job? = null

    // Ngăn xếp lưu lịch sử tra cứu nhanh
    private val historyStack = mutableListOf<DictionaryLookupResult>()

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }

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
