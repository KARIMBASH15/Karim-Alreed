package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.ludo.*
import com.example.ui.theme.*
import com.example.ui.viewmodels.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LudoGameScreen(
    viewModel: GameViewModel,
    onExitGame: () -> Unit
) {
    val state by viewModel.ludoState.collectAsState()
    val isMicMuted by viewModel.isMicMuted.collectAsState()
    val isSpeakerMuted by viewModel.isSpeakerMuted.collectAsState()
    val activeSpeaker by viewModel.activeSpeakerUsername.collectAsState()
    val inGameMessages by viewModel.inGameMessages.collectAsState()

    var showQuickChatSheet by remember { mutableStateOf(false) }

    val ludo = state ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "شوط لودو احترافي 🎲",
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "الرهان: %,d كوينز".format(ludo.stakeAmount),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onExitGame) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "خروج", tint = Color.White)
                    }
                },
                actions = {
                    // Voice Chat Quick Controls
                    IconButton(onClick = { viewModel.toggleMic() }) {
                        Icon(
                            if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "المايك",
                            tint = if (isMicMuted) Color.Red else GoldPrimary
                        )
                    }
                    IconButton(onClick = { viewModel.toggleSpeaker() }) {
                        Icon(
                            if (isSpeakerMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "الصوت",
                            tint = if (isSpeakerMuted) Color.Gray else Color.White
                        )
                    }
                    IconButton(onClick = { showQuickChatSheet = true }) {
                        Icon(Icons.Default.Chat, contentDescription = "شات", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavySurface)
            )
        },
        containerColor = DarkNavyBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Voice Wave Bar
            Surface(
                color = DarkNavySurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = if (activeSpeaker != null) GoldPrimary else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (activeSpeaker != null) "🎤 يتحدث الآن: $activeSpeaker" else "الغرفة الصوتية نشطة",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                    // Animated Wave indicator
                    if (activeSpeaker != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            repeat(4) { idx ->
                                val infiniteTransition = rememberInfiniteTransition()
                                val height by infiniteTransition.animateFloat(
                                    initialValue = 6f,
                                    targetValue = 18f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(400 + idx * 100, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(height.dp)
                                        .background(GoldPrimary, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Banner
            Surface(
                color = DarkNavyCard,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = ludo.lastActionText,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(10.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Top Players Row (Green & Yellow)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PlayerCardWidget(
                    player = ludo.players[1],
                    isCurrentTurn = ludo.currentTurnIndex == 1,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                PlayerCardWidget(
                    player = ludo.players[2],
                    isCurrentTurn = ludo.currentTurnIndex == 2,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ludo Board Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(3.dp, DarkNavyCard, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                LudoBoardCanvas(
                    boardState = ludo,
                    onPawnClicked = { pawnId ->
                        viewModel.moveLudoPawn(pawnId)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Players Row (Red & Blue)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PlayerCardWidget(
                    player = ludo.players[0],
                    isCurrentTurn = ludo.currentTurnIndex == 0,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                PlayerCardWidget(
                    player = ludo.players[3],
                    isCurrentTurn = ludo.currentTurnIndex == 3,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dice Roll Control Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isMyTurn = ludo.currentTurnIndex == 0
                val canRoll = ludo.canRoll && isMyTurn

                // Dice Box with Animation
                val scaleAnim by animateFloatAsState(
                    targetValue = if (canRoll) 1.1f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )

                Button(
                    onClick = {
                        if (canRoll) viewModel.rollLudoDice()
                    },
                    enabled = canRoll,
                    modifier = Modifier
                        .scale(scaleAnim)
                        .height(56.dp)
                        .padding(horizontal = 16.dp)
                        .testTag("roll_ludo_dice_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🎲 النرد: ${ludo.diceValue}",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 18.sp
                        )
                        if (canRoll) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(ارمي النرد!)",
                                color = Color.DarkGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // --- QUICK CHAT BOTTOM SHEET ---
    if (showQuickChatSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQuickChatSheet = false },
            containerColor = DarkNavySurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "عبارات الشات السريعة 💬",
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val quickPhrases = listOf(
                    "بالتوفيق للجميع! 🍀",
                    "العب بسرعة من فضلك! ⚡",
                    "هههههه أكلت حجر! 😂",
                    "شكراً جزيلاً! 🤝",
                    "حظ سعيد الشوط الجاي! 🔥",
                    "لعبة ممتازة جداً! 👏"
                )

                quickPhrases.forEach { phrase ->
                    Surface(
                        color = DarkNavyCard,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.sendInGameQuickChat(ludo.players[0].username, phrase)
                                showQuickChatSheet = false
                            }
                    ) {
                        Text(
                            text = phrase,
                            color = Color.White,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }

    // --- WINNER DIALOG ---
    ludo.winnerPlayer?.let { winner ->
        AlertDialog(
            onDismissRequest = { },
            containerColor = DarkNavySurface,
            title = {
                Text(
                    text = if (!winner.isAi) "🏆 مبروك! لقد فزت بالشوط!" else "🏅 انتهت المباراة",
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "الفائز هو: ${winner.username}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "الجائزة الكبرى: %,d كوينز".format(ludo.stakeAmount * 4),
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onExitGame,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("العودة للرئيسية", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun PlayerCardWidget(
    player: LudoPlayer,
    isCurrentTurn: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isCurrentTurn) DarkNavyCard else DarkNavySurface,
        shape = RoundedCornerShape(12.dp),
        border = if (isCurrentTurn) StrokeBorder(2.dp, GoldPrimary) else null,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(player.color.hexColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.username.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = player.username,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Text(
                    text = "الواصل: ${player.finishedCount}/4",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun LudoBoardCanvas(
    boardState: LudoBoardState,
    onPawnClicked: (pawnId: Int) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(boardState) {
                detectTapGestures { offset ->
                    val sizePx = size.width.toFloat()
                    val cellSize = sizePx / 15f

                    // Find if any pawn was tapped
                    val currPlayer = boardState.players[boardState.currentTurnIndex]
                    for (pawn in currPlayer.pawns) {
                        val pawnPos = getPawnCellCoords(pawn, currPlayer, cellSize)
                        if (pawnPos != null) {
                            val dist = (offset - pawnPos).getDistance()
                            if (dist <= cellSize * 0.9f) {
                                onPawnClicked(pawn.id)
                                break
                            }
                        }
                    }
                }
            }
    ) {
        val cellSize = size.width / 15f

        // Draw 4 Yards (Red=TopLeft, Green=TopRight, Yellow=BottomRight, Blue=BottomLeft)
        drawRect(Color(PawnColor.RED.hexColor), Offset(0f, 0f), Size(cellSize * 6, cellSize * 6))
        drawRect(Color(PawnColor.GREEN.hexColor), Offset(cellSize * 9, 0f), Size(cellSize * 6, cellSize * 6))
        drawRect(Color(PawnColor.YELLOW.hexColor), Offset(cellSize * 9, cellSize * 9), Size(cellSize * 6, cellSize * 6))
        drawRect(Color(PawnColor.BLUE.hexColor), Offset(0f, cellSize * 9), Size(cellSize * 6, cellSize * 6))

        // Draw White yard inner boxes
        drawRect(Color.White, Offset(cellSize * 1, cellSize * 1), Size(cellSize * 4, cellSize * 4))
        drawRect(Color.White, Offset(cellSize * 10, cellSize * 1), Size(cellSize * 4, cellSize * 4))
        drawRect(Color.White, Offset(cellSize * 10, cellSize * 10), Size(cellSize * 4, cellSize * 4))
        drawRect(Color.White, Offset(cellSize * 1, cellSize * 10), Size(cellSize * 4, cellSize * 4))

        // Draw Center Home Triangles
        val center = Offset(size.width / 2f, size.height / 2f)
        drawPath(
            Path().apply {
                moveTo(cellSize * 6, cellSize * 6)
                lineTo(cellSize * 9, cellSize * 6)
                lineTo(center.x, center.y)
                close()
            },
            Color(PawnColor.RED.hexColor)
        )
        drawPath(
            Path().apply {
                moveTo(cellSize * 9, cellSize * 6)
                lineTo(cellSize * 9, cellSize * 9)
                lineTo(center.x, center.y)
                close()
            },
            Color(PawnColor.GREEN.hexColor)
        )
        drawPath(
            Path().apply {
                moveTo(cellSize * 9, cellSize * 9)
                lineTo(cellSize * 6, cellSize * 9)
                lineTo(center.x, center.y)
                close()
            },
            Color(PawnColor.YELLOW.hexColor)
        )
        drawPath(
            Path().apply {
                moveTo(cellSize * 6, cellSize * 9)
                lineTo(cellSize * 6, cellSize * 6)
                lineTo(center.x, center.y)
                close()
            },
            Color(PawnColor.BLUE.hexColor)
        )

        // Draw Grid Lines & Colored Home Tracks
        for (i in 0..15) {
            drawLine(Color.LightGray, Offset(i * cellSize, 0f), Offset(i * cellSize, size.height), strokeWidth = 1f)
            drawLine(Color.LightGray, Offset(0f, i * cellSize), Offset(size.width, i * cellSize), strokeWidth = 1f)
        }

        // Color Home stretch paths
        for (i in 1..5) {
            drawRect(Color(PawnColor.RED.hexColor).copy(alpha = 0.5f), Offset(i * cellSize, cellSize * 7), Size(cellSize, cellSize))
            drawRect(Color(PawnColor.GREEN.hexColor).copy(alpha = 0.5f), Offset(cellSize * 7, i * cellSize), Size(cellSize, cellSize))
            drawRect(Color(PawnColor.YELLOW.hexColor).copy(alpha = 0.5f), Offset((14 - i) * cellSize, cellSize * 7), Size(cellSize, cellSize))
            drawRect(Color(PawnColor.BLUE.hexColor).copy(alpha = 0.5f), Offset(cellSize * 7, (14 - i) * cellSize), Size(cellSize, cellSize))
        }

        // Draw Pawns
        for (player in boardState.players) {
            for (pawn in player.pawns) {
                val coords = getPawnCellCoords(pawn, player, cellSize)
                if (coords != null) {
                    drawCircle(
                        color = Color.Black,
                        radius = cellSize * 0.38f,
                        center = coords
                    )
                    drawCircle(
                        color = Color(player.color.hexColor),
                        radius = cellSize * 0.32f,
                        center = coords
                    )
                    drawCircle(
                        color = Color.White,
                        radius = cellSize * 0.12f,
                        center = coords
                    )
                }
            }
        }
    }
}

private fun getPawnCellCoords(pawn: LudoPawn, player: LudoPlayer, cellSize: Float): Offset? {
    if (pawn.position == -1) {
        // Yard positions
        return when (player.color) {
            PawnColor.RED -> when (pawn.id) {
                0 -> Offset(cellSize * 2f, cellSize * 2f)
                1 -> Offset(cellSize * 4f, cellSize * 2f)
                2 -> Offset(cellSize * 2f, cellSize * 4f)
                else -> Offset(cellSize * 4f, cellSize * 4f)
            }
            PawnColor.GREEN -> when (pawn.id) {
                0 -> Offset(cellSize * 11f, cellSize * 2f)
                1 -> Offset(cellSize * 13f, cellSize * 2f)
                2 -> Offset(cellSize * 11f, cellSize * 4f)
                else -> Offset(cellSize * 13f, cellSize * 4f)
            }
            PawnColor.YELLOW -> when (pawn.id) {
                0 -> Offset(cellSize * 11f, cellSize * 11f)
                1 -> Offset(cellSize * 13f, cellSize * 11f)
                2 -> Offset(cellSize * 11f, cellSize * 13f)
                else -> Offset(cellSize * 13f, cellSize * 13f)
            }
            PawnColor.BLUE -> when (pawn.id) {
                0 -> Offset(cellSize * 2f, cellSize * 11f)
                1 -> Offset(cellSize * 4f, cellSize * 11f)
                2 -> Offset(cellSize * 2f, cellSize * 13f)
                else -> Offset(cellSize * 4f, cellSize * 13f)
            }
        }
    }

    if (pawn.position == 57) {
        // Finished inside center
        return Offset(cellSize * 7.5f, cellSize * 7.5f)
    }

    // Main 52 track loop mapping
    val absIndex = LudoGameLogic.getAbsoluteTrackIndex(pawn, player)
    val gridCoord = mainTrackGridCoords[absIndex % 52]
    return Offset((gridCoord.first + 0.5f) * cellSize, (gridCoord.second + 0.5f) * cellSize)
}

private val mainTrackGridCoords = listOf(
    Pair(1, 6), Pair(2, 6), Pair(3, 6), Pair(4, 6), Pair(5, 6),
    Pair(6, 5), Pair(6, 4), Pair(6, 3), Pair(6, 2), Pair(6, 1), Pair(6, 0),
    Pair(7, 0), Pair(8, 0),
    Pair(8, 1), Pair(8, 2), Pair(8, 3), Pair(8, 4), Pair(8, 5),
    Pair(9, 6), Pair(10, 6), Pair(11, 6), Pair(12, 6), Pair(13, 6), Pair(14, 6),
    Pair(14, 7), Pair(14, 8),
    Pair(13, 8), Pair(12, 8), Pair(11, 8), Pair(10, 8), Pair(9, 8),
    Pair(8, 9), Pair(8, 10), Pair(8, 11), Pair(8, 12), Pair(8, 13), Pair(8, 14),
    Pair(7, 14), Pair(6, 14),
    Pair(6, 13), Pair(6, 12), Pair(6, 11), Pair(6, 10), Pair(6, 9),
    Pair(5, 8), Pair(4, 8), Pair(3, 8), Pair(2, 8), Pair(1, 8), Pair(0, 8),
    Pair(0, 7), Pair(0, 6)
)

@Composable
fun StrokeBorder(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)
