package vn.edu.hcmute.minlish.ui.vocabulary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Màn hình nhập thông tin để thêm một từ vựng mới vào bộ từ.
 *
 * Các trường bắt buộc:
 * - Word
 * - Meaning
 *
 * Các trường còn lại có thể để trống.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreen(
    deckId: Int,
    viewModel: VocabViewModel,
    onNavigateBack: () -> Unit,
    onWordSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    var word by remember {
        mutableStateOf("")
    }

    var pronunciation by remember {
        mutableStateOf("")
    }

    var meaning by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var example by remember {
        mutableStateOf("")
    }

    var collocations by remember {
        mutableStateOf("")
    }

    var relatedWords by remember {
        mutableStateOf("")
    }

    var note by remember {
        mutableStateOf("")
    }

    var wordError by remember {
        mutableStateOf<String?>(null)
    }

    var meaningError by remember {
        mutableStateOf<String?>(null)
    }

    /**
     * Lắng nghe thông báo từ ViewModel.
     *
     * Nếu thêm từ thành công thì quay lại màn hình danh sách từ.
     * Nếu có lỗi thì chỉ hiển thị Snackbar và giữ nguyên màn hình nhập.
     */
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val successMessage = uiState.successMessage
        val errorMessage = uiState.errorMessage

        if (successMessage != null) {
            snackbarHostState.showSnackbar(successMessage)
            viewModel.clearMessage()
            onWordSaved()
        }

        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thêm từ vựng",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(
                        onClick = onNavigateBack
                    ) {
                        Text(text = "Quay lại", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InputField(
                    value = word,
                    onValueChange = {
                        word = it
                        wordError = null
                    },
                    label = "Word",
                    placeholder = "Ví dụ: abandon",
                    isError = wordError != null,
                    supportingText = wordError,
                    singleLine = true
                )
            }

            item {
                InputField(
                    value = pronunciation,
                    onValueChange = {
                        pronunciation = it
                    },
                    label = "Pronunciation",
                    placeholder = "Ví dụ: /əˈbændən/",
                    singleLine = true
                )
            }

            item {
                InputField(
                    value = meaning,
                    onValueChange = {
                        meaning = it
                        meaningError = null
                    },
                    label = "Meaning",
                    placeholder = "Ví dụ: từ bỏ",
                    isError = meaningError != null,
                    supportingText = meaningError,
                    singleLine = true
                )
            }

            item {
                InputField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    label = "Description",
                    placeholder = "Giải thích ngắn bằng tiếng Anh",
                    minLines = 2,
                    maxLines = 4
                )
            }

            item {
                InputField(
                    value = example,
                    onValueChange = {
                        example = it
                    },
                    label = "Example",
                    placeholder = "Ví dụ: He abandoned the plan.",
                    minLines = 2,
                    maxLines = 4
                )
            }

            item {
                InputField(
                    value = collocations,
                    onValueChange = {
                        collocations = it
                    },
                    label = "Collocation",
                    placeholder = "Ví dụ: abandon a plan",
                    minLines = 1,
                    maxLines = 3
                )
            }

            item {
                InputField(
                    value = relatedWords,
                    onValueChange = {
                        relatedWords = it
                    },
                    label = "Related words",
                    placeholder = "Ví dụ: give up, quit",
                    minLines = 1,
                    maxLines = 3
                )
            }

            item {
                InputField(
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

            item {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        var hasError = false

                        if (word.isBlank()) {
                            wordError = "Word không được để trống"
                            hasError = true
                        }

                        if (meaning.isBlank()) {
                            meaningError = "Meaning không được để trống"
                            hasError = true
                        }

                        if (!hasError) {
                            viewModel.addWord(
                                deckId = deckId,
                                word = word,
                                pronunciation = pronunciation,
                                meaning = meaning,
                                description = description,
                                example = example,
                                collocations = collocations,
                                relatedWords = relatedWords,
                                note = note
                            )
                        }
                    },
                    enabled = !uiState.isLoading,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(text = "Lưu từ vựng")
                    }
                }
            }
        }
    }
}

/**
 * TextField dùng chung cho form thêm từ vựng.
 *
 * Việc tách composable này giúp form gọn hơn
 * và dễ chỉnh sửa giao diện đồng bộ.
 */
@Composable
private fun InputField(
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
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = label)
            },
            placeholder = {
                Text(text = placeholder)
            },
            isError = isError,
            supportingText = {
                if (supportingText != null) {
                    Text(text = supportingText)
                }
            },
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines
        )
    }
}