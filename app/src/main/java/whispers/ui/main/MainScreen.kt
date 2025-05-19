package whispers.ui.main

import androidx.compose.animation.core.*
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
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainScreenEntryPoint(viewModel: MainScreenViewModel) {
    var selectedIndex by remember { mutableStateOf(-1) }

    MainScreen(
        viewModel = viewModel,
        canTranscribe = viewModel.canTranscribe,
        isRecording = viewModel.isRecording,
        selectedIndex = selectedIndex,
        onSelect = { selectedIndex = it },
        onRecordTapped = {
            selectedIndex = viewModel.myRecords.lastIndex
            viewModel.toggleRecord { selectedIndex = it }
        },
        onCardClick = viewModel::playRecording
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    viewModel: MainScreenViewModel,
    canTranscribe: Boolean,
    isRecording: Boolean,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onRecordTapped: () -> Unit,
    onCardClick: (String, Int) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteIndex by remember { mutableStateOf(-1) }
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
                records = viewModel.myRecords,
                listState = listState,
                selectedIndex = selectedIndex,
                canTranscribe = canTranscribe,
                onSelect = onSelect,
                onCardClick = onCardClick,
                onDeleteRequest = {
                    pendingDeleteIndex = it
                    showDeleteDialog = true
                },
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
                Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF2196F3))
                Spacer(Modifier.width(8.dp))
                Text("Whisper App", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                Spacer(Modifier.width(12.dp))
                LanguageLabel(viewModel.selectedLanguage, viewModel.selectedModel)
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
            Text("Language: $label", style = MaterialTheme.typography.labelMedium)
            Text("Model: $selectedModel", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordingList(
    records: List<myRecord>,
    listState: LazyListState,
    selectedIndex: Int,
    canTranscribe: Boolean,
    onSelect: (Int) -> Unit,
    onCardClick: (String, Int) -> Unit,
    onDeleteRequest: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(records.size, records.lastOrNull()?.logs) {
        if (records.isNotEmpty()) listState.animateScrollToItem(records.lastIndex)
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        itemsIndexed(records) { index, record ->
            val isSelected = index == selectedIndex
            val dismissState = rememberDismissState(confirmValueChange = {
                if (it == DismissValue.DismissedToStart || it == DismissValue.DismissedToEnd) {
                    onDeleteRequest(index)
                    false
                } else true
            })
            val animatedCorner by animateDpAsState(
                targetValue = if (isSelected && !canTranscribe) 48.dp else 16.dp,
                animationSpec = tween(400), label = "cornerAnim"
            )

            val scale = if (isSelected && !canTranscribe) {
                rememberInfiniteTransition().animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(tween(800, easing = EaseInOut), RepeatMode.Reverse),
                    label = "pulse"
                ).value
            } else 1f

            val tapModifier = if (canTranscribe) {
                Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onSelect(index) },
                        onDoubleTap = {
                            onSelect(index)
                            onCardClick(record.absolutePath, index)
                        }
                    )
                }
            } else Modifier

            SwipeToDismiss(
                state = dismissState,
                directions = setOf(DismissDirection.StartToEnd),
                background = {
                    Box(Modifier.fillMaxSize().padding(start = 20.dp), contentAlignment = Alignment.CenterStart) {
                        Text("削除", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissContent = {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .then(tapModifier),
                        shape = RoundedCornerShape(animatedCorner),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                canTranscribe -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Text(record.logs, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
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
        colors = ButtonDefaults.buttonColors(containerColor = color),
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
        "ggml-tiny-q5_1.bin" to "Tiny",
        "ggml-base-q5_1.bin" to "Base",
        "ggml-small-q8_0.bin" to "Small",
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
                    DropdownSelector(
                        label = "言語を選択してください",
                        selected = languageOptions.find { it.first == viewModel.selectedLanguage }?.second ?: "選択",
                        options = languageOptions,
                        onSelect = { viewModel.updateSelectedLanguage(it) },
                        expanded = expandedLang,
                        onExpand = { expandedLang = true },
                        onDismiss = { expandedLang = false }
                    )

                    Divider()

                    DropdownSelector(
                        label = "モデルを選択してください",
                        selected = modelOptions.find { it.first == viewModel.selectedModel }?.second ?: "選択",
                        options = modelOptions,
                        onSelect = { viewModel.updateSelectedModel(it) },
                        expanded = expandedModel,
                        onExpand = { expandedModel = true },
                        onDismiss = { expandedModel = false }
                    )
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.closeConfigDialog() }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { viewModel.closeConfigDialog() }) { Text("キャンセル") } }
        )
    }
}

@Composable
private fun DropdownSelector(
    label: String,
    selected: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit
) {
    Text(label)
    Box {
        TextButton(onClick = onExpand) { Text(selected) }
        DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
            options.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelect(key)
                        onDismiss()
                    }
                )
            }
        }
    }
}
