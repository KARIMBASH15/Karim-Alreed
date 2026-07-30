package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.monopoly.*
import com.example.ui.theme.*
import com.example.ui.viewmodels.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonopolyGameScreen(
    viewModel: GameViewModel,
    onExitGame: () -> Unit
) {
    val state by viewModel.monopolyState.collectAsState()
    val isMicMuted by viewModel.isMicMuted.collectAsState()
    val isSpeakerMuted by viewModel.isSpeakerMuted.collectAsState()
    val activeSpeaker by viewModel.activeSpeakerUsername.collectAsState()

    val game = state ?: return

    val myPlayer = game.players[0]
    val opponentPlayer = game.players[1]
    val isMyTurn = game.currentTurnIndex == 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "تاجر المدينة - بنك الحظ 🏙️💰",
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "الرهان: %,d كوينز | رصيدك: %,d".format(game.stakeAmount, myPlayer.cash),
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
            // Voice & Turn Header Bar
            Surface(
                color = DarkNavySurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeSpeaker != null) "صوت: $activeSpeaker" else "الصوت مباشر",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = if (isMyTurn) "دورك للرمي! 🎲" else "انتظر دور المنافس ⏳",
                        fontWeight = FontWeight.Bold,
                        color = if (isMyTurn) GoldPrimary else TextMuted,
                        fontSize = 13.sp
                    )
                }
            }

            // Players Balance Comparison Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // My Player Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (isMyTurn) DarkNavyCard else DarkNavySurface),
                    border = if (isMyTurn) StrokeBorder(2.dp, GoldPrimary) else null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👤", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(myPlayer.username, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                        Text("كاش: %,d كوينز".format(myPlayer.cash), color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("عقارات: ${myPlayer.ownedPropertyIds.size}", color = TextMuted, fontSize = 10.sp)
                    }
                }

                // Opponent Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (!isMyTurn) DarkNavyCard else DarkNavySurface),
                    border = if (!isMyTurn) StrokeBorder(2.dp, GoldPrimary) else null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🤖", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(opponentPlayer.username, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                        Text("كاش: %,d كوينز".format(opponentPlayer.cash), color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("عقارات: ${opponentPlayer.ownedPropertyIds.size}", color = TextMuted, fontSize = 10.sp)
                    }
                }
            }

            // Status Banner Message
            Surface(
                color = DarkNavySurface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = game.statusText,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // --- THE MONOPOLY BOARD GRID (16 Tiles) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkNavySurface)
                    .border(2.dp, GoldPrimary, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(game.boardTiles) { index, tile ->
                        MonopolyTileCard(
                            tile = tile,
                            index = index,
                            myPlayer = myPlayer,
                            opponentPlayer = opponentPlayer
                        )
                    }
                }
            }

            // Prompt Dialog for Pending Purchase
            game.pendingPurchaseTile?.let { pendingTile ->
                if (isMyTurn) {
                    Surface(
                        color = DarkNavyCard,
                        border = StrokeBorder(2.dp, GoldPrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🏰 فرصة شراء عقار [${pendingTile.name}]!",
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "السعر: %,d كوينز | الإيجار: %,d كوينز".format(pendingTile.price, pendingTile.rent),
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.buyPendingMonopolyTile(true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("شراء العقار الآن 💰", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.buyPendingMonopolyTile(false) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("تجاوز ❌")
                                }
                            }
                        }
                    }
                }
            }

            // Control Actions & Roll Dice Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dice values box
                Surface(
                    color = DarkNavySurface,
                    shape = RoundedCornerShape(12.dp),
                    border = StrokeBorder(1.dp, GoldPrimary)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎲 النرد: ", color = TextMuted, fontSize = 12.sp)
                        Text(
                            "[ ${game.diceValue1} ] [ ${game.diceValue2} ]",
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            fontSize = 16.sp
                        )
                    }
                }

                // Roll Dice Button
                Button(
                    onClick = { viewModel.rollMonopolyDice() },
                    enabled = isMyTurn && game.pendingPurchaseTile == null,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("roll_monopoly_dice"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Casino, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ارِمِ النرد 🎲", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }

    // Winner Dialog
    game.winnerPlayer?.let { winner ->
        AlertDialog(
            onDismissRequest = { },
            containerColor = DarkNavySurface,
            title = {
                Text(
                    text = if (!winner.isAi) "🏆 مبروك! أصبحت ملك عقارات المدينة!" else "🏅 انتهت مباراة تاجر المدينة",
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text("الفائز: ${winner.username}", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("الجائزة: %,d كوينز".format(game.stakeAmount * 2), color = GoldPrimary, modifier = Modifier.padding(top = 6.dp))
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
fun MonopolyTileCard(
    tile: MonopolyTile,
    index: Int,
    myPlayer: MonopolyPlayer,
    opponentPlayer: MonopolyPlayer
) {
    val isMyPosition = myPlayer.position == index
    val isOpponentPosition = opponentPlayer.position == index

    val ownerColor = when (tile.ownerIndex) {
        0 -> GoldPrimary
        1 -> Color.Red
        else -> Color.Transparent
    }

    Surface(
        color = DarkNavyCard,
        shape = RoundedCornerShape(8.dp),
        border = if (ownerColor != Color.Transparent) StrokeBorder(2.dp, ownerColor) else StrokeBorder(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Strip for Tile Color
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(tile.colorHex)
            )

            Text(
                text = tile.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            if (tile.type == TileType.PROPERTY || tile.type == TileType.UTILITY) {
                Text(
                    text = if (tile.ownerIndex != null) "إيجار %d".format(tile.rent) else "%,d ك".format(tile.price),
                    fontSize = 9.sp,
                    color = if (tile.ownerIndex != null) GoldPrimary else TextMuted
                )
            } else {
                Text(text = "خاصة", fontSize = 8.sp, color = TextMuted)
            }

            // Player Avatar Pins on Tile
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isMyPosition) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 8.sp)
                    }
                }
                if (isOpponentPosition) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤖", fontSize = 8.sp)
                    }
                }
            }
        }
    }
}
