package vn.edu.hcmute.minlish.ui.vocabulary

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import vn.edu.hcmute.minlish.data.local.entity.Word
import vn.edu.hcmute.minlish.data.util.VocabularyCsvUtil
import vn.edu.hcmute.minlish.data.util.VocabularyExcelUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    deckId: Int,
    viewModel: VocabViewModel,
    onNavigateBack: () -> Unit,
    onAddWordClick: () -> Unit,
    onStartLearningClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var importMenuExpanded by remember { mutableStateOf(false) }
    var exportMenuExpanded by remember { mutableStateOf(false) }
    var wordToEdit by remember { mutableStateOf<Word?>(null) }
    var wordToDelete by remember { mutableStateOf<Word?>(null) }

    val importCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        runCatching {
            VocabularyCsvUtil.readWordsFromCsv(context, uri)
        }.onSuccess { words ->
            if (words.isEmpty()) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("File CSV không có từ hợp lệ")
                }
            } else {
                viewModel.importWordsFromDictionary(deckId, words)
            }
        }.onFailure { exception ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    exception.message ?: "Không thể đọc file CSV"
                )
            }
        }
    }

    val importExcelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        runCatching {
            VocabularyExcelUtil.readWordsFromExcel(context, uri)
        }.onSuccess { words ->
            if (words.isEmpty()) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("File Excel không có từ hợp lệ")
                }
            } else {
                viewModel.importWordsFromDictionary(deckId, words)
            }
        }.onFailure { exception ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    exception.message ?: "Không thể đọc file Excel"
                )
            }
        }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        runCatching {
            VocabularyCsvUtil.writeWordsToCsv(
                context = context,
                uri = uri,
                words = uiState.words
            )
        }.onSuccess {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Đã export CSV thành công")
            }
        }.onFailure { exception ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    exception.message ?: "Export CSV thất bại"
                )
            }
        }
    }

    val exportExcelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        runCatching {
            VocabularyExcelUtil.writeWordsToExcel(
                context = context,
                uri = uri,
                words = uiState.words
            )
        }.onSuccess {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Đã export Excel thành công")
            }
        }.onFailure { exception ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    exception.message ?: "Export Excel thất bại"
                )
            }
        }
    }

    LaunchedEffect(deckId) {
        viewModel.loadWords(deckId)
    }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val message = uiState.successMessage ?: uiState.errorMessage

        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.selectedDeck?.name ?: "Danh sách từ vựng",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    TextButton(
                        onClick = onNavigateBack
                    ) {
                        Text(
                            text = "Quay lại",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onStartLearningClick(deckId) },
                        enabled = uiState.words.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Học Flashcard",
                            tint = if (uiState.words.isNotEmpty()) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                            }
                        )
                    }

                    Box {
                        TextButton(
                            onClick = {
                                importMenuExpanded = true
                            }
                        ) {
                            Text(
                                text = "Import",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = importMenuExpanded,
                            onDismissRequest = {
                                importMenuExpanded = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(text = "Import CSV")
                                },
                                onClick = {
                                    importMenuExpanded = false
                                    importCsvLauncher.launch(
                                        arrayOf(
                                            "text/*",
                                            "text/csv",
                                            "application/csv",
                                            "application/vnd.ms-excel",
                                            "application/octet-stream"
                                        )
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(text = "Import Excel")
                                },
                                onClick = {
                                    importMenuExpanded = false
                                    importExcelLauncher.launch(
                                        arrayOf(
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                            "application/vnd.ms-excel",
                                            "application/octet-stream"
                                        )
                                    )
                                }
                            )
                        }
                    }

                    Box {
                        TextButton(
                            enabled = uiState.words.isNotEmpty(),
                            onClick = {
                                exportMenuExpanded = true
                            }
                        ) {
                            Text(
                                text = "Export",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = exportMenuExpanded,
                            onDismissRequest = {
                                exportMenuExpanded = false
                            }
                        ) {
                            DropdownMenuItem(
                                enabled = uiState.words.isNotEmpty(),
                                text = {
                                    Text(text = "Export CSV")
                                },
                                onClick = {
                                    exportMenuExpanded = false
                                    exportCsvLauncher.launch("minlish_deck_$deckId.csv")
                                }
                            )

                            DropdownMenuItem(
                                enabled = uiState.words.isNotEmpty(),
                                text = {
                                    Text(text = "Export Excel")
                                },
                                onClick = {
                                    exportMenuExpanded = false
                                    exportExcelLauncher.launch("minlish_deck_$deckId.xlsx")
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddWordClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading && uiState.words.isEmpty() -> {
                    LoadingWordsContent()
                }

                uiState.words.isEmpty() -> {
                    EmptyWordsContent(
                        onAddWordClick = onAddWordClick
                    )
                }

                else -> {
                    WordListContent(
                        words = uiState.words,
                        onEditWord = { word ->
                            wordToEdit = word
                        },
                        onDeleteWord = { word ->
                            wordToDelete = word
                        }
                    )
                }
            }
        }
    }

    wordToEdit?.let { word ->
        EditWordDialog(
            word = word,
            isLoading = uiState.isLoading,
            onDismiss = {
                wordToEdit = null
            },
            onConfirm = { wordText, pronunciation, meaning, description, example, collocations, relatedWords, note ->
                viewModel.updateWord(
                    originalWord = word,
                    word = wordText,
                    pronunciation = pronunciation,
                    meaning = meaning,
                    description = description,
                    example = example,
                    collocations = collocations,
                    relatedWords = relatedWords,
                    note = note
                )
                wordToEdit = null
            }
        )
    }

    wordToDelete?.let { word ->
        ConfirmDeleteDialog(
            title = "Xóa từ vựng",
            message = "Bạn có chắc muốn xóa từ \"${word.word}\" không?",
            isLoading = uiState.isLoading,
            onDismiss = {
                wordToDelete = null
            },
            onConfirm = {
                viewModel.deleteWord(word)
                wordToDelete = null
            }
        )
    }
}

@Composable
private fun WordListContent(
    words: List<Word>,
    onEditWord: (Word) -> Unit,
    onDeleteWord: (Word) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = words,
            key = { word -> word.wordId }
        ) { word ->
            WordItem(
                word = word,
                onEditClick = {
                    onEditWord(word)
                },
                onDeleteClick = {
                    onDeleteWord(word)
                }
            )
        }
    }
}

@Composable
private fun WordItem(
    word: Word,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = word.word,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = word.meaning,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (word.pronunciation.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Phát âm: ${word.pronunciation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!word.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Mô tả: ${word.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!word.example.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ví dụ: ${word.example}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!word.collocations.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Collocation: ${word.collocations}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!word.relatedWords.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Từ liên quan: ${word.relatedWords}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!word.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Ghi chú: ${word.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onEditClick
                ) {
                    Text(text = "Sửa")
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(
                    onClick = onDeleteClick
                ) {
                    Text(
                        text = "Xóa",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyWordsContent(
    onAddWordClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Chưa có từ vựng nào",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Hãy thêm từ đầu tiên hoặc import từ file CSV/Excel.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onAddWordClick
        ) {
            Text(text = "Thêm từ vựng")
        }
    }
}

@Composable
private fun LoadingWordsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
