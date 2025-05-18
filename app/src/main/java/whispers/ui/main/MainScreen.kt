package whispers.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainScreenEntryPoint(viewModel: MainScreenViewModel) {
    MainScreen(
        viewModel = viewModel,
        canTranscribe = viewModel.canTranscribe,
        isRecording = viewModel.isRecording,
        onRecordTapped = viewModel::toggleRecord,
        onCardClick = viewModel::playRecording
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    viewModel: MainScreenViewModel,
    canTranscribe: Boolean,
    isRecording: Boolean,
    onRecordTapped: () -> Unit,
    onCardClick: (String, Int) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteIndex by remember { mutableStateOf(-1) }
    var selectedIndex by remember { mutableStateOf(-1) }
    val listState = rememberLazyListState()

    Scaffold(topBar = { TopBar(viewModel) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RecordingList(
                viewModel = viewModel,
                listState = listState,
                selectedIndex = selectedIndex,
                onSelect = { selectedIndex = it },
                onCardClick = onCardClick,
                onDeleteRequest = {
                    pendingDeleteIndex = it
                    showDeleteDialog = true
                },
                canTranscribe = canTranscribe,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )

            StyledButton(
                text = if (isRecording) "Stop" else "Record",
                onClick = onRecordTapped,
                enabled = canTranscribe,
                color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showDeleteDialog && pendingDeleteIndex != -1) {
        ConfirmDeleteDialog(
            onConfirm = {
                viewModel.removeRecordAt(pendingDeleteIndex)
                showDeleteDialog = false
                pendingDeleteIndex = -1
            },
            onCancel = {
                showDeleteDialog = false
                pendingDeleteIndex = -1
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(viewModel: MainScreenViewModel) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Whisper App",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(12.dp))
                LanguageLabel(viewModel.selectedLanguage,viewModel.selectedModel)
            }
        },
        actions = { ConfigButtonWithDialog(viewModel) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFFF176),
            titleContentColor = Color.Black,
            actionIconContentColor = Color.DarkGray
        )
    )
}

@Composable
private fun LanguageLabel(languageCode: String, selectedModel: String) {
    val label = mapOf("en" to "English", "ja" to "Japanese", "sw" to "Swahili")[languageCode] ?: "Unknown"
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 2.dp,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                text = "Language: $label",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Model: $selectedModel",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordingList(
    viewModel: MainScreenViewModel,
    listState: LazyListState,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onCardClick: (String, Int) -> Unit,
    onDeleteRequest: (Int) -> Unit,
    canTranscribe: Boolean,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(viewModel.myRecords.size, viewModel.myRecords.lastOrNull()?.logs) {
        if (viewModel.myRecords.isNotEmpty()) listState.animateScrollToItem(viewModel.myRecords.lastIndex)
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        itemsIndexed(viewModel.myRecords) { index, record ->
            val isSelected = index == selectedIndex
            val dismissState = rememberDismissState(confirmValueChange = {
                if (it == DismissValue.DismissedToStart || it == DismissValue.DismissedToEnd) {
                    onDeleteRequest(index)
                    false
                } else true
            })

            SwipeToDismiss(
                state = dismissState,
                background = {
                    Box(modifier = Modifier.fillMaxSize().padding(start = 20.dp), contentAlignment = Alignment.CenterStart) {
                        Text("削除", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                directions = setOf(DismissDirection.StartToEnd),
                dismissContent = {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (canTranscribe) Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { onSelect(index) },
                                    onDoubleTap = {
                                        onSelect(index)
                                        onCardClick(viewModel.myRecords[index].absolutePath, index)
                                    }
                                )
                            } else Modifier),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                canTranscribe -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Text(
                            text = record.logs,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun ConfirmDeleteDialog(onConfirm: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("録音の削除") },
        text = { Text("この録音を削除してもよろしいですか？") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("削除", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onCancel) { Text("キャンセル") } }
    )
}

@Composable
fun StyledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(6.dp),
        modifier = modifier
    ) {
        Text(text)
    }
}

@Composable
fun ConfigButtonWithDialog(viewModel: MainScreenViewModel) {

    val languageOptions = listOf("ja" to "日本語", "en" to "English", "sw" to "Swahili")
    val modelOptions = listOf(
        "ggml-tiny-q5_1.bin"  to "Tiny",
        "ggml-base-q5_1.bin"  to "Base",
        //"ggml-small-q8_0.bin" to "Small",
    )

    var expandedLang by remember { mutableStateOf(false) }
    var expandedModel by remember { mutableStateOf(false) }

    IconButton(onClick = { viewModel.openConfigDialog() }) {
        Icon(Icons.Default.Settings, contentDescription = "設定")
    }

    if (viewModel.isConfigDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeConfigDialog() },
            title = { Text("設定") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("言語を選択してください")
                    Box {
                        TextButton(onClick = { expandedLang = true }) {
                            Text(languageOptions.find { it.first == viewModel.selectedLanguage }?.second ?: "選択")
                        }
                        DropdownMenu(
                            expanded = expandedLang,
                            onDismissRequest = { expandedLang = false }
                        ) {
                            languageOptions.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.updateSelectedLanguage(code)
                                        expandedLang = false
                                    }
                                )
                            }
                        }
                    }
                    Divider()
                    Text("モデルを選択してください")
                    Box {
                        TextButton(onClick = { expandedModel = true }) {
                            Text(modelOptions.find { it.first == viewModel.selectedModel }?.second ?: "選択")
                        }
                        DropdownMenu(
                            expanded = expandedModel,
                            onDismissRequest = { expandedModel = false }
                        ) {
                            modelOptions.forEach { (file, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.updateSelectedModel(file)
                                        expandedModel = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.closeConfigDialog() }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeConfigDialog() }) {
                    Text("キャンセル")
                }
            }
        )
    }
}
