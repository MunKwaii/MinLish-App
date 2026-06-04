package vn.edu.hcmute.minlish.ui.vocabulary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
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
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.ImeAction
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

    var pronunciationSuggestions by remember { mutableStateOf(emptyList<String>()) }
    var meaningSuggestions by remember { mutableStateOf(emptyList<String>()) }
    var descriptionSuggestions by remember { mutableStateOf(emptyList<String>()) }
    var exampleSuggestions by remember { mutableStateOf(emptyList<String>()) }
    var relatedWordsSuggestions by remember { mutableStateOf(emptyList<String>()) }

    var lastLookedUpWord by remember { mutableStateOf("") }

    val triggerLookup = {
        val trimmed = word.trim()
        if (trimmed.isNotBlank() && trimmed != lastLookedUpWord) {
            lastLookedUpWord = trimmed
            viewModel.lookupWordDetails(trimmed)
        }
    }

    // Reset kết quả tra cứu khi bắt đầu vào màn hình
    LaunchedEffect(Unit) {
        viewModel.resetLookupResult()
    }

    // Lắng nghe lỗi từ chức năng tra cứu
    LaunchedEffect(uiState.lookupError) {
        val lookupError = uiState.lookupError
        if (lookupError != null) {
            snackbarHostState.showSnackbar(lookupError)
            lastLookedUpWord = "" // reset để có thể tra cứu lại
            viewModel.resetLookupResult()
        }
    }

    // Tự động điền (Autofill) khi có kết quả tra cứu từ API
    LaunchedEffect(uiState.lookupResult) {
        val result = uiState.lookupResult
        if (result != null) {
            if (result.exists) {
                val results = result.results.orEmpty()

                // Lấy gợi ý phiên âm (Pronunciation)
                val pronList = results.flatMap { it.pronunciations.orEmpty() }
                    .mapNotNull { it.ipa }
                    .filter { it.isNotBlank() }
                    .distinct()
                pronunciationSuggestions = pronList
                if (pronunciation.isBlank() && pronList.isNotEmpty()) {
                    pronunciation = pronList.first()
                }

                // Lấy gợi ý định nghĩa (Meaning)
                val meaningsList = results.flatMap { it.meanings.orEmpty() }
                val definitions = meaningsList
                    .mapNotNull { it.definition }
                    .filter { it.isNotBlank() }
                    .distinct()
                meaningSuggestions = definitions
                if (meaning.isBlank() && definitions.isNotEmpty()) {
                    meaning = definitions.first()
                }

                // Lấy gợi ý mô tả (Description = POS - Source)
                val descList = meaningsList
                    .map { m ->
                        listOfNotNull(m.pos, m.source)
                            .joinToString(" - ")
                            .trim()
                    }
                    .filter { it.isNotBlank() }
                    .distinct()
                descriptionSuggestions = descList
                if (description.isBlank() && descList.isNotEmpty()) {
                    description = descList.first()
                }

                // Lấy gợi ý ví dụ (Example)
                val exList = meaningsList
                    .mapNotNull { it.example }
                    .filter { it.isNotBlank() }
                    .distinct()
                exampleSuggestions = exList
                if (example.isBlank() && exList.isNotEmpty()) {
                    example = exList.first()
                }

                // Lấy gợi ý từ liên quan (Related words)
                val relList = results.flatMap { it.relations.orEmpty() }
                    .mapNotNull { it.related_word }
                    .filter { it.isNotBlank() }
                    .distinct()
                relatedWordsSuggestions = relList
                if (relatedWords.isBlank() && relList.isNotEmpty()) {
                    relatedWords = relList.joinToString(", ")
                }
            } else {
                snackbarHostState.showSnackbar("Không tìm thấy từ vựng này trong từ điển trực tuyến")
                lastLookedUpWord = "" // reset để có thể tra cứu lại
                viewModel.resetLookupResult()
            }
        }
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
                var hasFocusedBefore by remember { mutableStateOf(false) }

                InputField(
                    value = word,
                    onValueChange = {
                        word = it
                        wordError = null
                    },
                    label = "Word",
                    placeholder = "Ví dụ: abandon",
                    required = true,
                    isError = wordError != null,
                    supportingText = wordError,
                    singleLine = true,
                    trailingIcon = {
                        if (uiState.isLookupLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    onFocusChanged = { focusState ->
                        if (focusState.isFocused) {
                            hasFocusedBefore = true
                        }
                        if (!focusState.isFocused && hasFocusedBefore) {
                            triggerLookup()
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            triggerLookup()
                        }
                    )
                )
            }

            item {
                DropdownInputField(
                    value = pronunciation,
                    onValueChange = {
                        pronunciation = it
                    },
                    label = "Pronunciation",
                    placeholder = "Ví dụ: /əˈbændən/",
                    suggestions = pronunciationSuggestions,
                    singleLine = true
                )
            }

            item {
                DropdownInputField(
                    value = meaning,
                    onValueChange = {
                        meaning = it
                        meaningError = null
                    },
                    label = "Meaning",
                    placeholder = "Ví dụ: từ bỏ",
                    required = true,
                    isError = meaningError != null,
                    supportingText = meaningError,
                    suggestions = meaningSuggestions,
                    singleLine = true
                )
            }

            item {
                DropdownInputField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    label = "Description",
                    placeholder = "Giải thích ngắn bằng tiếng Anh",
                    suggestions = descriptionSuggestions,
                    minLines = 2,
                    maxLines = 4
                )
            }

            item {
                DropdownInputField(
                    value = example,
                    onValueChange = {
                        example = it
                    },
                    label = "Example",
                    placeholder = "Ví dụ: He abandoned the plan.",
                    suggestions = exampleSuggestions,
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
                DropdownInputField(
                    value = relatedWords,
                    onValueChange = {
                        relatedWords = it
                    },
                    label = "Related words",
                    placeholder = "Ví dụ: give up, quit",
                    suggestions = relatedWordsSuggestions,
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
    required: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 1,
    trailingIcon: @Composable (() -> Unit)? = null,
    onFocusChanged: (FocusState) -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged(onFocusChanged),
            label = {
                Text(
                    text = buildAnnotatedString {
                        append(label)
                        if (required) {
                            withStyle(SpanStyle(color = Color.Red)) {
                                append(" *")
                            }
                        }
                    }
                )
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
            maxLines = maxLines,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
    }
}

/**
 * TextField hỗ trợ dropdown gợi ý khi có dữ liệu từ API.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded && suggestions.isNotEmpty(),
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true),
                label = {
                    Text(
                        text = buildAnnotatedString {
                            append(label)
                            if (required) {
                                withStyle(SpanStyle(color = Color.Red)) {
                                    append(" *")
                                }
                            }
                        }
                    )
                },
                placeholder = { Text(text = placeholder) },
                isError = isError,
                supportingText = {
                    if (supportingText != null) {
                        Text(text = supportingText)
                    }
                },
                singleLine = singleLine,
                minLines = minLines,
                maxLines = maxLines,
                trailingIcon = {
                    if (suggestions.isNotEmpty()) {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = expanded && suggestions.isNotEmpty(),
                onDismissRequest = { expanded = false }
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(text = suggestion) },
                        onClick = {
                            onValueChange(suggestion)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}