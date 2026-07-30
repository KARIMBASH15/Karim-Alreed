package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.ui.screens.*
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LudoDominoTheme
import com.example.ui.viewmodels.*

class MainActivity : ComponentActivity() {

    enum class Screen {
        AUTH,
        HOME,
        LUDO,
        DOMINO,
        MONOPOLY,
        STORE,
        ADMIN,
        CHAT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = GameRepository(database.appDao())

        setContent {
            LudoDominoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(repository))
                    val gameViewModel: GameViewModel = viewModel(factory = GameViewModel.Factory(repository))
                    val storeViewModel: StoreViewModel = viewModel(factory = StoreViewModel.Factory(repository))
                    val adminViewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory(repository))
                    val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory(repository))

                    val currentUser by authViewModel.currentUser.collectAsState()
                    val activeGameType by gameViewModel.activeGameType.collectAsState()
                    val isSearchingMatch by gameViewModel.isSearchingMatch.collectAsState()
                    val searchCountdown by gameViewModel.searchCountdown.collectAsState()

                    var currentScreen by remember { mutableStateOf(Screen.AUTH) }
                    var pendingStakeGameType by remember { mutableStateOf<GameViewModel.GameType?>(null) }

                    // Navigation Sync
                    LaunchedEffect(currentUser) {
                        currentScreen = if (currentUser == null) Screen.AUTH else Screen.HOME
                    }

                    LaunchedEffect(activeGameType) {
                        when (activeGameType) {
                            GameViewModel.GameType.LUDO -> currentScreen = Screen.LUDO
                            GameViewModel.GameType.DOMINO -> currentScreen = Screen.DOMINO
                            GameViewModel.GameType.MONOPOLY -> currentScreen = Screen.MONOPOLY
                            GameViewModel.GameType.NONE -> {
                                if (currentUser != null && (currentScreen == Screen.LUDO || currentScreen == Screen.DOMINO || currentScreen == Screen.MONOPOLY)) {
                                    currentScreen = Screen.HOME
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentScreen) {
                            Screen.AUTH -> {
                                AuthScreen(viewModel = authViewModel)
                            }
                            Screen.HOME -> {
                                currentUser?.let { user ->
                                    HomeScreen(
                                        currentUser = user,
                                        gameViewModel = gameViewModel,
                                        onNavigateToStore = { currentScreen = Screen.STORE },
                                        onNavigateToAdmin = { currentScreen = Screen.ADMIN },
                                        onNavigateToChat = { currentScreen = Screen.CHAT },
                                        onSelectGame = { type -> pendingStakeGameType = type },
                                        onLogout = { authViewModel.logout() }
                                    )
                                }
                            }
                            Screen.LUDO -> {
                                LudoGameScreen(
                                    viewModel = gameViewModel,
                                    onExitGame = { gameViewModel.exitGame() }
                                )
                            }
                            Screen.DOMINO -> {
                                DominoGameScreen(
                                    viewModel = gameViewModel,
                                    onExitGame = { gameViewModel.exitGame() }
                                )
                            }
                            Screen.MONOPOLY -> {
                                MonopolyGameScreen(
                                    viewModel = gameViewModel,
                                    onExitGame = { gameViewModel.exitGame() }
                                )
                            }
                            Screen.STORE -> {
                                currentUser?.let { user ->
                                    StoreScreen(
                                        currentUser = user,
                                        storeViewModel = storeViewModel,
                                        onBack = { currentScreen = Screen.HOME }
                                    )
                                }
                            }
                            Screen.ADMIN -> {
                                AdminPanelScreen(
                                    adminViewModel = adminViewModel,
                                    onBack = { currentScreen = Screen.HOME }
                                )
                            }
                            Screen.CHAT -> {
                                currentUser?.let { user ->
                                    ChatScreen(
                                        currentUser = user,
                                        chatViewModel = chatViewModel,
                                        onBack = { currentScreen = Screen.HOME }
                                    )
                                }
                            }
                        }

                        // Stake Selection Dialog Modal
                        pendingStakeGameType?.let { gameType ->
                            currentUser?.let { user ->
                                StakeSelectionModal(
                                    gameType = gameType,
                                    userBalance = user.balance,
                                    onDismiss = { pendingStakeGameType = null },
                                    onStartMatch = { stake, vsAi ->
                                        gameViewModel.setStake(stake)
                                        gameViewModel.startMatchmaking(
                                            gameType = gameType,
                                            vsAi = vsAi,
                                            currentUsername = user.username,
                                            currentAvatarId = user.avatarId
                                        )
                                        pendingStakeGameType = null
                                    }
                                )
                            }
                        }

                        // Search Matchmaking Loading Overlay
                        if (isSearchingMatch) {
                            MatchmakingLoadingOverlay(countdown = searchCountdown)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchmakingLoadingOverlay(countdown: Int) {
    Surface(
        color = Color.Black.copy(alpha = 0.85f),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = GoldPrimary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "جاري البحث عن لاعبين أونلاين... 🌐",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
            Text(
                text = "يبدأ الشوط خلال $countdown ثوانٍ...",
                color = GoldPrimary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
