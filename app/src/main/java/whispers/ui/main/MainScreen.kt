package whispers.ui.main

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    var showAboutDialog by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF2196F3))
                Text("Whisper App", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                LanguageLabel(
                    languageCode = viewModel.selectedLanguage,
                    selectedModel = viewModel.selectedModel,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        },
        actions = {
            IconButton(onClick = { showAboutDialog = true }) {
                Icon(Icons.Default.Info, contentDescription = "アプリ情報")
            }
            ConfigButtonWithDialog(viewModel)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFFF176),
            titleContentColor = Color.Black,
            actionIconContentColor = Color.DarkGray
        )
    )

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("このアプリについて") },
        text = {
            Column {
                Text("Whisper App v1.0")
                Spacer(Modifier.height(8.dp))
                Text("このアプリは Whisper.cpp を使用して音声認識を行うオフライン録音アプリです。")
                Spacer(Modifier.height(4.dp))
                Text("対応言語: 日本語 / 英語 / スワヒリ語")
                Spacer(Modifier.height(8.dp))
                Text("開発者: Shu Ishizuki (石附 支)")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}

@Composable
private fun LanguageLabel(
    languageCode: String,
    selectedModel: String,
    modifier: Modifier = Modifier
) {
    val label = mapOf(
        "ja" to "日本語",
        "en" to "English",
        "sw" to "Swahili",
        "es" to "Spanish (Español)",
        "fr" to "French (Français)",
        "de" to "German (Deutsch)",
    )[languageCode] ?: "Unknown"

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 2.dp,
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp) // TopAppBarと似た高さに合わせる
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text("Language: $label", style = MaterialTheme.typography.labelSmall)
            Text("Model: $selectedModel", style = MaterialTheme.typography.labelSmall)
        }
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier
//                .padding(horizontal = 8.dp)
//                .heightIn(min = 32.dp) // Textが潰れない最低限の高さを確保
//        ) {
//            Text("Lang: $label", style = MaterialTheme.typography.labelSmall)
//            Spacer(modifier = Modifier.width(6.dp))
//            Text("Model: $selectedModel", style = MaterialTheme.typography.labelSmall)
//        }
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

//MODEL_NAMES=("ggml-small-q8_0.bin" "ggml-small-q5_1.bin" "ggml-base-q8_0.bin" "ggml-base-q5_1.bin" "ggml-tiny-q8_0.bin" "ggml-tiny-q5_1.bin")

@Composable
fun ConfigButtonWithDialog(viewModel: MainScreenViewModel) {
    val languageOptions = listOf(
        "ja" to "日本語",
        "en" to "English",
        "sw" to "Swahili",
        "es" to "Spanish (Español)",
        "fr" to "French (Français)",
        "de" to "German (Deutsch)",
    )
    val modelOptions = listOf(
        "ggml-tiny-q5_1.bin"  to "Tiny 5 Bits",
        "ggml-tiny-q8_0.bin"  to "Tiny 8 Bits",
        "ggml-base-q5_1.bin"  to "Base 5 Bits",
        "ggml-base-q8_0.bin"  to "Base 8 Bits",
        "ggml-small-q5_1.bin" to "Small 5 Bits",
        "ggml-small-q8_0.bin" to "Small 8 Bits"
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

                    Divider()

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = viewModel.translateToEnglish,
                            onCheckedChange = { viewModel.updateTranslate(it) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("英語に翻訳する")
                    }
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
