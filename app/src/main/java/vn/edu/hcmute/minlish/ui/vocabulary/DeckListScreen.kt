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
    var deckToEdit by remember {
        mutableStateOf<Deck?>(null)
    }
    var deckToDelete by remember {
        mutableStateOf<Deck?>(null)
    }

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(userId) {
        viewModel.loadDecks(userId)
    }

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
                        },
                        onEditDeck = { deck ->
                            deckToEdit = deck
                        },
                        onDeleteDeck = { deck ->
                            deckToDelete = deck
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

    deckToEdit?.let { deck ->
        EditDeckDialog(
            deck = deck,
            isLoading = uiState.isLoading,
            onDismiss = {
                deckToEdit = null
            },
            onConfirm = { name, description, tags ->
                viewModel.updateDeck(
                    deck = deck,
                    name = name,
                    description = description,
                    tags = tags
                )
                deckToEdit = null
            }
        )
    }

    deckToDelete?.let { deck ->
        ConfirmDeleteDialog(
            title = "Xóa bộ từ",
            message = "Bạn có chắc muốn xóa bộ từ \"${deck.name}\" không?",
            isLoading = uiState.isLoading,
            onDismiss = {
                deckToDelete = null
            },
            onConfirm = {
                viewModel.deleteDeck(deck)
                deckToDelete = null
            }
        )
    }
}

@Composable
private fun DeckListContent(
    decks: List<Deck>,
    onDeckClick: (Deck) -> Unit,
    onEditDeck: (Deck) -> Unit,
    onDeleteDeck: (Deck) -> Unit
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
                },
                onEditClick = {
                    onEditDeck(deck)
                },
                onDeleteClick = {
                    onDeleteDeck(deck)
                }
            )
        }
    }
}

@Composable
private fun DeckItem(
    deck: Deck,
    onClick: () -> Unit,
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onClick()
                    }
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

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

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
