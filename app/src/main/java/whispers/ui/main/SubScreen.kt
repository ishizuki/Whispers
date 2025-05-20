package whispers.ui.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun AnimatedTitleScreenWithBackground(onStartClick: () -> Unit) {
    val scope = rememberCoroutineScope()

    // タイトルとボタンの表示状態
    var showTitle by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = tween(1000),
        label = "titleAlpha"
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (showButton) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    // アニメーション開始タイミング
    LaunchedEffect(Unit) {
        delay(500)
        showTitle = true
        delay(800)
        showButton = true
    }

    // 星の座標リスト
    val starCount = 100
    val stars = remember<SnapshotStateList<Offset>> {
        List(starCount) {
            Offset(
                x = Random.nextFloat() * 1080f,
                y = Random.nextFloat() * 1920f
            )
        }.toMutableStateList()
    }

    // 星を下方向に動かすアニメーション
    LaunchedEffect(Unit) {
        while (true) {
            delay(4L) // 60fps 相当
            for (i in stars.indices) {
                val star = stars[i]
                stars[i] = star.copy(y = (star.y + 1f) % 1920f)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 🎆 背景アニメーション (Canvasで描画)

        Canvas(modifier = Modifier.fillMaxSize()) {
            stars.forEach { star ->
                drawCircle(
                    color = Color.Black.copy(alpha = 0.5f),
                    radius = 20f,
                    center = star
                )
            }
        }

        // 🎮 中央のタイトルとボタン
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "星空ゲーム",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                modifier = Modifier.alpha(alphaAnim)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onStartClick,
                modifier = Modifier.graphicsLayer(scaleX = scaleAnim, scaleY = scaleAnim)
            ) {
                Text("スタート", fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun FlappyBirdGame() {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    var birdY by remember { mutableStateOf(screenHeight / 2f) }
    var velocity by remember { mutableStateOf(0f) }
    var score by remember { mutableStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    data class Pipe(val x: Float, val gapY: Float)
    val pipes = remember { mutableStateListOf<Pipe>() }

    // ゲームループ
    LaunchedEffect(!isGameOver) {
        if (!isGameOver) {
            pipes.clear()
            birdY = screenHeight / 2f
            velocity = 0f
            score = 0
        }

        while (!isGameOver) {
            delay(16L)
            velocity += 0.5f
            birdY += velocity

            // パイプ移動
            for (i in pipes.indices) {
                pipes[i] = pipes[i].copy(x = pipes[i].x - 6f)
            }

            // パイプ消去・生成
            if (pipes.isEmpty() || pipes.last().x < screenWidth - 500f) {
                val gapY = Random.nextFloat() * (screenHeight - 400f) + 200f
                pipes.add(Pipe(screenWidth, gapY))
            }
            if (pipes.firstOrNull()?.x ?: 0f < -200f) pipes.removeFirst()

            // 当たり判定
            val birdX = screenWidth / 3f
            pipes.forEach { pipe ->
                if (pipe.x in (birdX - 40f)..(birdX + 40f)) {
                    if (birdY < pipe.gapY - 150f || birdY > pipe.gapY + 150f) {
                        isGameOver = true
                    }
                }
            }

            // 地面・天井
            if (birdY < 0 || birdY > screenHeight) isGameOver = true

            // スコア加算（パイプ通過）
            pipes.filter { it.x + 6f in (birdX - 3f)..(birdX + 3f) }.forEach {
                score++
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan)
            .pointerInput(isGameOver) {
                detectTapGestures {
                    if (isGameOver) {
                        isGameOver = false // リスタート
                    } else {
                        velocity = -12f
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 鳥
            drawCircle(
                color = Color.Yellow,
                radius = 40f,
                center = Offset(x = size.width / 3f, y = birdY)
            )

            // パイプ
            pipes.forEach { pipe ->
                val pipeWidth = 120f
                val gapHeight = 300f
                drawRect(
                    color = Color.Green,
                    topLeft = Offset(pipe.x, 0f),
                    size = Size(pipeWidth, pipe.gapY - gapHeight / 2)
                )
                drawRect(
                    color = Color.Green,
                    topLeft = Offset(pipe.x, pipe.gapY + gapHeight / 2),
                    size = Size(pipeWidth, size.height - pipe.gapY - gapHeight / 2)
                )
            }
        }

        // スコア表示
        Text(
            text = "Score: $score",
            fontSize = 28.sp,
            color = Color.White,
            modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
        )

        if (isGameOver) {
            Text(
                text = "Game Over\nタップで再スタート",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
@Composable
fun PuzzleGameScreen() {
    val gridSize = 4
    val tileCount = gridSize * gridSize
    val tiles = remember { mutableStateListOf<Int>() }

    // 初期化（1〜15＋空白）
    LaunchedEffect(Unit) {
        val list = (1 until tileCount).toMutableList().apply { add(0) }
        list.shuffle()
        tiles.clear()
        tiles.addAll(list)
    }

    fun isSolved(): Boolean {
        return tiles.dropLast(1) == (1 until tileCount).toList()
    }

    fun swap(index: Int) {
        val emptyIndex = tiles.indexOf(0)
        val diff = abs(index - emptyIndex)
        val sameRow = index / gridSize == emptyIndex / gridSize
        if ((diff == 1 && sameRow) || diff == gridSize) {
            tiles[emptyIndex] = tiles[index]
            tiles[index] = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSolved()) "🎉 クリア！" else "15パズル",
            fontSize = 28.sp,
            modifier = Modifier.padding(8.dp)
        )

        for (row in 0 until gridSize) {
            Row {
                for (col in 0 until gridSize) {
                    val index = row * gridSize + col
                    val value = tiles[index]
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(64.dp)
                            .background(
                                if (value == 0) Color.LightGray else Color.Blue,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { swap(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (value != 0) {
                            Text(
                                text = value.toString(),
                                fontSize = 24.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val list = (1 until tileCount).toMutableList().apply { add(0) }
            list.shuffle()
            tiles.clear()
            tiles.addAll(list)
        }) {
            Text("リセット")
        }
    }
}

@Composable
fun GameApp(viewModel: MainScreenViewModel) {
    var isGameStarted by remember { mutableStateOf(false) }

    if (!isGameStarted) {
        AnimatedTitleScreenWithBackground(onStartClick = { isGameStarted = true })
    } else {
//        PuzzleGameScreen()
        MainScreenEntryPoint(viewModel)
    }
}