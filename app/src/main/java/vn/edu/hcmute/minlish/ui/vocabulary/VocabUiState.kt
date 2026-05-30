package vn.edu.hcmute.minlish.ui.vocabulary

import vn.edu.hcmute.minlish.data.local.entity.Deck
import vn.edu.hcmute.minlish.data.local.entity.Word

/**
 * Lưu toàn bộ dữ liệu trạng thái cần thiết cho các màn hình Vocabulary.
 *
 * UI chỉ nên đọc dữ liệu từ state này và gửi hành động của người dùng
 * về cho VocabViewModel xử lý.
 */
data class VocabUiState(
    val decks: List<Deck> = emptyList(),
    val words: List<Word> = emptyList(),

    val selectedDeck: Deck? = null,

    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)