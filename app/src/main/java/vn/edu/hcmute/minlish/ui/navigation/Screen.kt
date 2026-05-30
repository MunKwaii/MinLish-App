package vn.edu.hcmute.minlish.ui.navigation

/**
 * Khai báo các route dùng cho Navigation trong ứng dụng.
 *
 * Các route có tham số cần có hàm createRoute()
 * để truyền dữ liệu khi điều hướng.
 */
sealed class Screen(val route: String) {

    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Dashboard : Screen("dashboard")
    data object Profile : Screen("profile")

    data object Vocabulary : Screen("vocabulary")

    data object WordList : Screen("word_list/{deckId}") {
        fun createRoute(deckId: Int): String {
            return "word_list/$deckId"
        }
    }

    data object AddWord : Screen("add_word/{deckId}") {
        fun createRoute(deckId: Int): String {
            return "add_word/$deckId"
        }
    }
}