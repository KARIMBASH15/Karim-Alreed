package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.domino.*
import com.example.ui.theme.*
import com.example.ui.viewmodels.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DominoGameScreen(
    viewModel: GameViewModel,
    onExitGame: () -> Unit
) {
    val state by viewModel.dominoState.collectAsState()
    val isMicMuted by viewModel.isMicMuted.collectAsState()
    val isSpeakerMuted by viewModel.isSpeakerMuted.collectAsState()
    val activeSpeaker by viewModel.activeSpeakerUsername.collectAsState()

    var selectedTile by remember { mutableStateOf<DominoTile?>(null) }

    val domino = state ?: return

    val myPlayer = domino.players[0]
    val opponentPlayer = domino.players[1]
    val isMyTurn = domino.currentTurnIndex == 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "طاولة الدومينو 🀄",
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "الرهان: %,d كوينز | السحب: ${domino.boneyard.size} أحجار".format(domino.stakeAmount),
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
                    IconButton(onClick = { viewModel.toggleMic() }) {
                        Icon(
                            if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            tint = if (isMicMuted) Color.Red else GoldPrimary,
                            contentDescription = "المايك"
                        )
                    }
                    IconButton(onClick = { viewModel.toggleSpeaker() }) {
                        Icon(
                            if (isSpeakerMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            tint = Color.White,
                            contentDescription = "الصوت"
                        )
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
                    Text(
                        text = if (activeSpeaker != null) "🎤 صوت أونلاين: $activeSpeaker" else "الصوت مباشر",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Text(
                        text = domino.statusText,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        fontSize = 12.sp
                    )
                }
            }

            // Opponent Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = if (!isMyTurn) DarkNavyCard else DarkNavySurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(opponentPlayer.username.take(1), fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(opponentPlayer.username, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("عدد الأحجار: ${opponentPlayer.hand.size}", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                    if (!isMyTurn) {
                        CircularProgressIndicator(color = GoldPrimary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Green Felt Domino Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F522E)) // Green Felt
                    .border(4.dp, Color(0xFF5D4037), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (domino.boardChain.isEmpty()) {
                    Text(
                        text = "🀄 الطاولة خالية، اختر حجراً للبدء",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(domino.boardChain) { placed ->
                            DominoTileWidget(
                                tile = placed.tile,
                                isFlipped = placed.isFlipped,
                                onClick = { }
                            )
                        }
                    }
                }
            }

            // Placement Selection Modal/Buttons if tile selected
            selectedTile?.let { tile ->
                val sideOptions = DominoGameLogic.canPlayTile(tile, domino.leftEnd, domino.rightEnd)
                if (sideOptions != null && isMyTurn) {
                    Surface(
                        color = DarkNavyCard,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("أين تريد وضع الحجر [${tile.left}|${tile.right}]؟", color = GoldPrimary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (sideOptions == DominoGameLogic.PlaySide.LEFT || sideOptions == DominoGameLogic.PlaySide.BOTH || sideOptions == DominoGameLogic.PlaySide.ANY) {
                                    Button(
                                        onClick = {
                                            viewModel.playDominoTile(tile, DominoGameLogic.PlaySide.LEFT)
                                            selectedTile = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                                    ) {
                                        Text("يسار ⬅️", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (sideOptions == DominoGameLogic.PlaySide.RIGHT || sideOptions == DominoGameLogic.PlaySide.BOTH || sideOptions == DominoGameLogic.PlaySide.ANY) {
                                    Button(
                                        onClick = {
                                            viewModel.playDominoTile(tile, DominoGameLogic.PlaySide.RIGHT)
                                            selectedTile = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                                    ) {
                                        Text("يمين ➡️", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // User Hand & Action Controls Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("أحجارك (${myPlayer.hand.size}):", fontWeight = FontWeight.Bold, color = Color.White)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Draw button
                        Button(
                            onClick = { viewModel.drawDominoFromBoneyard() },
                            enabled = isMyTurn && domino.boneyard.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkNavyCard)
                        ) {
                            Text("سحب (${domino.boneyard.size})", color = GoldPrimary)
                        }

                        // Pass button
                        OutlinedButton(
                            onClick = { viewModel.passDominoTurn() },
                            enabled = isMyTurn,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("تمرار الدور")
                        }
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("domino_hand_list")
                ) {
                    items(myPlayer.hand) { tile ->
                        val canPlay = DominoGameLogic.canPlayTile(tile, domino.leftEnd, domino.rightEnd) != null
                        val isSelected = selectedTile?.id == tile.id

                        DominoTileWidget(
                            tile = tile,
                            isSelected = isSelected,
                            isPlayable = canPlay && isMyTurn,
                            onClick = {
                                if (isMyTurn && canPlay) {
                                    selectedTile = tile
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // --- WINNER DIALOG ---
    domino.winnerPlayer?.let { winner ->
        AlertDialog(
            onDismissRequest = { },
            containerColor = DarkNavySurface,
            title = {
                Text(
                    text = if (!winner.isAi) "🏆 مبروك! فزت بشوط الدومينو!" else "🏅 انتهت مباراة الدومينو",
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text("الفائز: ${winner.username}", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("الجائزة: %,d كوينز".format(domino.stakeAmount * 2), color = GoldPrimary, modifier = Modifier.padding(top = 6.dp))
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
fun DominoTileWidget(
    tile: DominoTile,
    isSelected: Boolean = false,
    isPlayable: Boolean = true,
    isFlipped: Boolean = false,
    onClick: () -> Unit
) {
    val displayLeft = if (isFlipped) tile.right else tile.left
    val displayRight = if (isFlipped) tile.left else tile.right

    Surface(
        color = if (isSelected) GoldPrimary else Color.White,
        shape = RoundedCornerShape(8.dp),
        border = if (isPlayable) StrokeBorder(2.dp, GoldPrimary) else null,
        modifier = Modifier
            .width(52.dp)
            .height(84.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            DominoHalfPips(displayLeft)
            Divider(color = Color.Black, thickness = 2.dp, modifier = Modifier.fillMaxWidth(0.8f))
            DominoHalfPips(displayRight)
        }
    }
}

@Composable
fun DominoHalfPips(count: Int) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(Color(0xFFFAFAFA)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count == 0) "•" else "$count",
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = 16.sp
        )
    }
}
