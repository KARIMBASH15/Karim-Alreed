package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodels.GameViewModel

@Composable
fun HomeScreen(
    currentUser: UserEntity,
    gameViewModel: GameViewModel,
    onNavigateToStore: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToChat: () -> Unit,
    onSelectGame: (GameViewModel.GameType) -> Unit,
    onLogout: () -> Unit
) {
    val snackMsg by gameViewModel.snackMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackMsg) {
        snackMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            gameViewModel.clearSnackMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkNavyBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- TOP USER BAR ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // User Avatar & Name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = DarkNavyBackground,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentUser.username,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = if (currentUser.isAdmin) "⭐ مدير النظام (Admin)" else "لاعب مميز",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (currentUser.isAdmin) GoldPrimary else TextMuted
                                    )
                                )
                            }
                        }

                        // Balance Chip
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = DarkNavyCard,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .clickable { onNavigateToStore() }
                                    .border(1.dp, GoldPrimary, RoundedCornerShape(16.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "%,d".format(currentUser.balance),
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPrimary,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.AddCircle,
                                        contentDescription = "شراء رصيد",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(onClick = onLogout) {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = "تسجيل خروج",
                                    tint = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // --- HERO BANNER ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.hero_banner_games_1785445451146),
                            contentDescription = "Hero Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, DarkNavyBackground.copy(alpha = 0.85f))
                                    )
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Column {
                                Text(
                                    text = "🏆 بطولات لودو ودومينو الكبرى",
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "تحدى أصدقاءك في أونلاين مباشر مع شات صوتي!",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // --- DAILY REWARD CARD ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            gameViewModel.claimDailyReward(currentUser.username)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                    border = CardDefaults.outlinedCardBorder(enabled = true)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CardGiftcard,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "الهدية اليومية المجانية 🎁",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "احصل على 2,500 كوينز مجاناً كل 24 ساعة!",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Button(
                            onClick = { gameViewModel.claimDailyReward(currentUser.username) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("استلام 2500", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- GAME SELECTION TITLE ---
            item {
                Text(
                    text = "اختر اللعبة وابدأ الشوط 🎲",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // --- LUDO GAME CARD ---
            item {
                GameCardItem(
                    title = "لعبة لودو احترافية (Ludo Star)",
                    subtitle = "4 ألوان • مراهنات من 500 إلى 200,000 كوينز • شات صوتي",
                    badgeText = "الأكثر شعبية 🔥",
                    gradientColors = listOf(Color(0xFF8E0E00), Color(0xFF1F1C2C)),
                    icon = Icons.Default.Casino,
                    onPlayClick = { onSelectGame(GameViewModel.GameType.LUDO) },
                    testTag = "play_ludo_button"
                )
            }

            // --- DOMINO GAME CARD ---
            item {
                GameCardItem(
                    title = "لعبة دومينو أونلاين (Dominoes)",
                    subtitle = "طاولة احترافية • حبسة وأرقام • لعب مع الكمبيوتر أو أونلاين",
                    badgeText = "مميز ✨",
                    gradientColors = listOf(Color(0xFF000428), Color(0xFF004E92)),
                    icon = Icons.Default.GridOn,
                    onPlayClick = { onSelectGame(GameViewModel.GameType.DOMINO) },
                    testTag = "play_domino_button"
                )
            }

            // --- MONOPOLY / CITY MERCHANT GAME CARD ---
            item {
                GameCardItem(
                    title = "تاجر المدينة (بنك الحظ - Monopoly)",
                    subtitle = "شراء عقارات المدن • جمع الإيجارات • صاديق الحظ وبنك مصر",
                    badgeText = "جديد وحصري 🎩",
                    gradientColors = listOf(Color(0xFF135058), Color(0xFFF12711)),
                    icon = Icons.Default.Domain,
                    onPlayClick = { onSelectGame(GameViewModel.GameType.MONOPOLY) },
                    testTag = "play_monopoly_button"
                )
            }

            // --- EXTRA QUICK NAVIGATION BUTTONS ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Chat Room Button
                    OutlinedButton(
                        onClick = onNavigateToChat,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الشات العام")
                    }

                    // Store Button
                    Button(
                        onClick = onNavigateToStore,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("المتجر والتحويل", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- ADMIN PANEL LAUNCHER (IF ADMIN) ---
            if (currentUser.isAdmin) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAdmin() },
                        colors = CardDefaults.cardColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "لوحة تحكم الأدمن (Admin Panel)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "إضافة وتعديل رصيد الاعضاء ومراجعة طلبات السحب والشراء",
                                        color = Color.DarkGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameCardItem(
    title: String,
    subtitle: String,
    badgeText: String,
    gradientColors: List<Color>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onPlayClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onPlayClick() }
            .testTag(testTag),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = GoldPrimary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(26.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )

                Text(
                    text = subtitle,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                Button(
                    onClick = onPlayClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text(
                        text = "دخول الشوط وبدء اللعب",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
