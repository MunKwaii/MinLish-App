package vn.edu.hcmute.minlish.data.util

import android.content.Context
import android.net.Uri
import vn.edu.hcmute.minlish.data.local.entity.Word
import java.io.BufferedReader
import java.io.OutputStreamWriter

object VocabularyCsvUtil {

    /**
     * Đọc file CSV từ bộ nhớ điện thoại.
     *
     * File CSV có thể ở dạng:
     * word
     * apple
     * book
     * student
     *
     * Hoặc:
     * word,pronunciation,meaning,description,example,collocations,relatedWords,note
     *
     * Ở bước này chỉ lấy cột đầu tiên là "word" để gọi Dictionary API tự động điền dữ liệu.
     */
    fun readWordsFromCsv(
        context: Context,
        uri: Uri
    ): List<String> {
        val words = mutableListOf<String>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(inputStream.reader()).useLines { lines ->
                lines.forEachIndexed { index, line ->
                    val cleanLine = line.trim()

                    if (cleanLine.isBlank()) return@forEachIndexed

                    val columns = parseCsvLine(cleanLine)
                    val firstColumn = columns.firstOrNull()?.trim().orEmpty()

                    // Bỏ qua dòng tiêu đề nếu có
                    if (index == 0 && firstColumn.equals("word", ignoreCase = true)) {
                        return@forEachIndexed
                    }

                    if (firstColumn.isNotBlank()) {
                        words.add(firstColumn)
                    }
                }
            }
        }

        return words.distinct()
    }

    /**
     * Xuất danh sách từ vựng ra file CSV.
     *
     * Dữ liệu được ghi theo đúng các trường trong form thêm từ:
     * Word, Pronunciation, Meaning, Description, Example,
     * Collocations, RelatedWords, Note.
     */
    fun writeWordsToCsv(
        context: Context,
        uri: Uri,
        words: List<Word>
    ) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                writer.appendLine(
                    listOf(
                        "word",
                        "pronunciation",
                        "meaning",
                        "description",
                        "example",
                        "collocations",
                        "relatedWords",
                        "note"
                    ).joinToString(",")
                )

                words.forEach { word ->
                    writer.appendLine(
                        listOf(
                            word.word,
                            word.pronunciation,
                            word.meaning,
                            word.description.orEmpty(),
                            word.example.orEmpty(),
                            word.collocations.orEmpty(),
                            word.relatedWords.orEmpty(),
                            word.note.orEmpty()
                        ).joinToString(",") { escapeCsvValue(it) }
                    )
                }
            }
        }
    }

    /**
     * Tách một dòng CSV thành danh sách cột.
     *
     * Hàm này xử lý được trường hợp dữ liệu có dấu phẩy nằm trong dấu ngoặc kép.
     * Ví dụ: "apple","quả táo, trái táo","example"
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var insideQuotes = false
        var i = 0

        while (i < line.length) {
            val char = line[i]

            when {
                char == '"' -> {
                    if (insideQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        insideQuotes = !insideQuotes
                    }
                }

                char == ',' && !insideQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }

                else -> {
                    current.append(char)
                }
            }

            i++
        }

        result.add(current.toString())
        return result
    }

    /**
     * Chuẩn hóa dữ liệu khi ghi ra CSV.
     *
     * Nếu giá trị có dấu phẩy, dấu xuống dòng hoặc dấu ngoặc kép
     * thì cần bọc bằng dấu ngoặc kép để Excel/Google Sheets đọc đúng.
     */
    private fun escapeCsvValue(value: String): String {
        val escapedValue = value.replace("\"", "\"\"")

        return if (
            escapedValue.contains(",") ||
            escapedValue.contains("\"") ||
            escapedValue.contains("\n")
        ) {
            "\"$escapedValue\""
        } else {
            escapedValue
        }
    }
}