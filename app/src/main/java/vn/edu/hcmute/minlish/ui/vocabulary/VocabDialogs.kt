package vn.edu.hcmute.minlish.ui.vocabulary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.edu.hcmute.minlish.data.local.entity.Deck
import vn.edu.hcmute.minlish.data.local.entity.Word

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                enabled = !isLoading,
                onClick = onConfirm
            ) {
                Text(text = "Xóa")
            }
        },
        dismissButton = {
            OutlinedButton(
                enabled = !isLoading,
                onClick = onDismiss
            ) {
                Text(text = "Hủy")
            }
        }
    )
}

@Composable
fun EditDeckDialog(
    deck: Deck,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, tags: String) -> Unit
) {
    var name by remember(deck.deckId) {
        mutableStateOf(deck.name)
    }
    var description by remember(deck.deckId) {
        mutableStateOf(deck.description)
    }
    var tags by remember(deck.deckId) {
        mutableStateOf(deck.tags)
    }
    var nameError by remember(deck.deckId) {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        },
        title = {
            Text(
                text = "Sửa bộ từ vựng",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                DialogTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = "Tên bộ từ",
                    placeholder = "Ví dụ: IELTS Vocabulary",
                    isError = nameError != null,
                    supportingText = nameError,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                DialogTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    label = "Mô tả",
                    placeholder = "Ví dụ: Từ vựng thường gặp trong IELTS",
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                DialogTextField(
                    value = tags,
                    onValueChange = {
                        tags = it
                    },
                    label = "Tags",
                    placeholder = "Ví dụ: IELTS, Academic, Reading",
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !isLoading,
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Tên bộ từ không được để trống"
                        return@Button
                    }

                    onConfirm(
                        name.trim(),
                        description.trim(),
                        tags.trim()
                    )
                }
            ) {
                Text(text = "Lưu")
            }
        },
        dismissButton = {
            OutlinedButton(
                enabled = !isLoading,
                onClick = onDismiss
            ) {
                Text(text = "Hủy")
            }
        }
    )
}

@Composable
fun EditWordDialog(
    word: Word,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        word: String,
        pronunciation: String,
        meaning: String,
        description: String,
        example: String,
        collocations: String,
        relatedWords: String,
        note: String
    ) -> Unit
) {
    var wordText by remember(word.wordId) {
        mutableStateOf(word.word)
    }
    var pronunciation by remember(word.wordId) {
        mutableStateOf(word.pronunciation)
    }
    var meaning by remember(word.wordId) {
        mutableStateOf(word.meaning)
    }
    var description by remember(word.wordId) {
        mutableStateOf(word.description.orEmpty())
    }
    var example by remember(word.wordId) {
        mutableStateOf(word.example.orEmpty())
    }
    var collocations by remember(word.wordId) {
        mutableStateOf(word.collocations.orEmpty())
    }
    var relatedWords by remember(word.wordId) {
        mutableStateOf(word.relatedWords.orEmpty())
    }
    var note by remember(word.wordId) {
        mutableStateOf(word.note.orEmpty())
    }
    var wordError by remember(word.wordId) {
        mutableStateOf<String?>(null)
    }
    var meaningError by remember(word.wordId) {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        },
        title = {
            Text(
                text = "Sửa từ vựng",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                DialogTextField(
                    value = wordText,
                    onValueChange = {
                        wordText = it
                        wordError = null
                    },
                    label = "Word",
                    placeholder = "Ví dụ: abandon",
                    isError = wordError != null,
                    supportingText = wordError,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                DialogTextField(
                    value = pronunciation,
                    onValueChange = {
                        pronunciation = it
                    },
                    label = "Pronunciation",
                    placeholder = "Ví dụ: /əˈbændən/",
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                DialogTextField(
                    value = meaning,
                    onValueChange = {
                        meaning = it
                        meaningError = null
                    },
                    label = "Meaning",
                    placeholder = "Ví dụ: từ bỏ",
                    isError = meaningError != null,
                    supportingText = meaningError,
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                DialogTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    label = "Description",
                    placeholder = "Giải thích ngắn bằng tiếng Anh",
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(12.dp))

                DialogTextField(
                    value = example,
                    onValueChange = {
                        example = it
                    },
                    label = "Example",
                    placeholder = "Ví dụ: He abandoned the plan.",
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(12.dp))

                DialogTextField(
                    value = collocations,
                    onValueChange = {
                        collocations = it
                    },
                    label = "Collocations",
                    placeholder = "Ví dụ: abandon a plan",
                    minLines = 1,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                DialogTextField(
                    value = relatedWords,
                    onValueChange = {
                        relatedWords = it
                    },
                    label = "Related words",
                    placeholder = "Ví dụ: give up, quit",
                    minLines = 1,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                DialogTextField(
                    value = note,
                    onValueChange = {
                        note = it
                    },
                    label = "Note",
                    placeholder = "Ghi chú riêng của bạn",
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !isLoading,
                onClick = {
                    var hasError = false

                    if (wordText.isBlank()) {
                        wordError = "Từ vựng không được để trống"
                        hasError = true
                    }

                    if (meaning.isBlank()) {
                        meaningError = "Nghĩa của từ không được để trống"
                        hasError = true
                    }

                    if (hasError) return@Button

                    onConfirm(
                        wordText.trim(),
                        pronunciation.trim(),
                        meaning.trim(),
                        description.trim(),
                        example.trim(),
                        collocations.trim(),
                        relatedWords.trim(),
                        note.trim()
                    )
                }
            ) {
                Text(text = "Lưu")
            }
        },
        dismissButton = {
            OutlinedButton(
                enabled = !isLoading,
                onClick = onDismiss
            ) {
                Text(text = "Hủy")
            }
        }
    )
}

@Composable
private fun DialogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = {
            Text(text = label)
        },
        placeholder = {
            Text(text = placeholder)
        },
        isError = isError,
        supportingText = {
            if (!supportingText.isNullOrBlank()) {
                Text(text = supportingText)
            }
        },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines
    )
}
