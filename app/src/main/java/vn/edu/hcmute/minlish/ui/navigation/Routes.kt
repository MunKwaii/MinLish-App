package vn.edu.hcmute.minlish.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Profile : Screen("profile")
    object Learning : Screen("learning")

    // Màn hình danh sách bộ từ vựng
    object Vocabulary : Screen("vocabulary")

    // Màn hình danh sách từ vựng theo deckId
    object WordList : Screen("word_list/{deckId}") {
        fun createRoute(deckId: Int): String {
            return "word_list/$deckId"
        }
    }

    // Màn hình thêm từ vựng theo deckId
    object AddWord : Screen("add_word/{deckId}") {
        fun createRoute(deckId: Int): String {
            return "add_word/$deckId"
        }
    }
}