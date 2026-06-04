package vn.edu.hcmute.minlish.ui.vocabulary

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import vn.edu.hcmute.minlish.data.local.entity.Deck

/**
 * Màn hình hiển thị danh sách các bộ từ vựng của người dùng.
 *
 * Nhiệm vụ:
 * - Tải danh sách bộ từ theo userId.
 * - Hiển thị từng bộ từ dưới dạng Card.
 * - Cho phép tạo bộ từ mới thông qua AddDeckDialog.
 * - Gửi sự kiện chọn bộ từ ra ngoài để điều hướng sang màn hình danh sách từ.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    userId: Int,
    viewModel: VocabViewModel,
    onDeckClick: (Deck) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddDeckDialog by remember {
        mutableStateOf(false)
    }

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    /**
     * Tải danh sách bộ từ khi màn hình được mở.
     *
     * LaunchedEffect(userId) đảm bảo chỉ tải lại khi userId thay đổi,
     * tránh gọi loadDecks liên tục khi Compose recomposition.
     */
    LaunchedEffect(userId) {
        viewModel.loadDecks(userId)
    }

    /**
     * Hiển thị thông báo thành công hoặc lỗi bằng Snackbar.
     *
     * Sau khi hiển thị xong cần gọi clearMessage()
     * để tránh thông báo bị lặp lại khi UI recomposition.
     */
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val message = uiState.successMessage ?: uiState.errorMessage

        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bộ từ vựng",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddDeckDialog = true
                },
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
                uiState.isLoading && uiState.decks.isEmpty() -> {
                    LoadingContent()
                }

                uiState.decks.isEmpty() -> {
                    EmptyDeckContent(
                        onCreateDeckClick = {
                            showAddDeckDialog = true
                        }
                    )
                }

                else -> {
                    DeckListContent(
                        decks = uiState.decks,
                        onDeckClick = { deck ->
                            viewModel.selectDeck(deck)
                            onDeckClick(deck)
                        }
                    )
                }
            }
        }
    }

    if (showAddDeckDialog) {
        AddDeckDialog(
            isLoading = uiState.isLoading,
            onDismiss = {
                showAddDeckDialog = false
            },
            onConfirm = { name, description, tags ->
                viewModel.createDeck(
                    userId = userId,
                    name = name,
                    description = description,
                    tags = tags
                )

                showAddDeckDialog = false
            }
        )
    }
}

/**
 * Nội dung danh sách bộ từ vựng.
 */
@Composable
private fun DeckListContent(
    decks: List<Deck>,
    onDeckClick: (Deck) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = decks,
            key = { deck ->
                deck.deckId
            }
        ) { deck ->
            DeckItem(
                deck = deck,
                onClick = {
                    onDeckClick(deck)
                }
            )
        }
    }
}

/**
 * Card hiển thị thông tin một bộ từ vựng.
 */
@Composable
private fun DeckItem(
    deck: Deck,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
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
                text = deck.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (deck.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = deck.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (deck.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tags: ${deck.tags}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Nội dung hiển thị khi chưa có bộ từ nào.
 */
@Composable
private fun EmptyDeckContent(
    onCreateDeckClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Chưa có bộ từ vựng nào",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Hãy tạo bộ từ đầu tiên để bắt đầu lưu và học từ vựng.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCreateDeckClick
        ) {
            Text(text = "Tạo bộ từ")
        }
    }
}

/**
 * Nội dung hiển thị khi đang tải dữ liệu.
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Dialog nhập thông tin để tạo bộ từ vựng mới.
 *
 * Các trường:
 * - Tên bộ từ: bắt buộc.
 * - Mô tả: không bắt buộc.
 * - Tags: không bắt buộc, có thể nhập dạng IELTS, Business, Travel.
 */
@Composable
private fun AddDeckDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        description: String,
        tags: String
    ) -> Unit
) {
    var name by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var tags by remember {
        mutableStateOf("")
    }

    var nameError by remember {
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
                text = "Tạo bộ từ vựng",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Tên bộ từ")
                    },
                    placeholder = {
                        Text(text = "Ví dụ: IELTS Vocabulary")
                    },
                    isError = nameError != null,
                    supportingText = {
                        if (nameError != null) {
                            Text(text = nameError ?: "")
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Mô tả")
                    },
                    placeholder = {
                        Text(text = "Ví dụ: Từ vựng thường gặp trong IELTS")
                    },
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tags,
                    onValueChange = {
                        tags = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Tags")
                    },
                    placeholder = {
                        Text(text = "Ví dụ: IELTS, Academic, Reading")
                    },
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