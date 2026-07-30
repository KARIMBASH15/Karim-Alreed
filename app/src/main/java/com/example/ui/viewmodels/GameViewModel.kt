package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import com.example.game.domino.*
import com.example.game.ludo.*
import com.example.game.monopoly.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    // --- GAME TYPE & MODE ---
    enum class GameType { NONE, LUDO, DOMINO, MONOPOLY }

    private val _activeGameType = MutableStateFlow(GameType.NONE)
    val activeGameType: StateFlow<GameType> = _activeGameType.asStateFlow()

    private val _selectedStake = MutableStateFlow(500L)
    val selectedStake: StateFlow<Long> = _selectedStake.asStateFlow()

    private val _isSearchingMatch = MutableStateFlow(false)
    val isSearchingMatch: StateFlow<Boolean> = _isSearchingMatch.asStateFlow()

    private val _searchCountdown = MutableStateFlow(3)
    val searchCountdown: StateFlow<Int> = _searchCountdown.asStateFlow()

    // --- LUDO STATE ---
    private val _ludoState = MutableStateFlow<LudoBoardState?>(null)
    val ludoState: StateFlow<LudoBoardState?> = _ludoState.asStateFlow()

    // --- DOMINO STATE ---
    private val _dominoState = MutableStateFlow<DominoGameState?>(null)
    val dominoState: StateFlow<DominoGameState?> = _dominoState.asStateFlow()

    // --- MONOPOLY (BANK EL HAZZ) STATE ---
    private val _monopolyState = MutableStateFlow<MonopolyGameState?>(null)
    val monopolyState: StateFlow<MonopolyGameState?> = _monopolyState.asStateFlow()

    // --- VOICE CHAT & IN-GAME CHAT ---
    private val _isMicMuted = MutableStateFlow(false)
    val isMicMuted: StateFlow<Boolean> = _isMicMuted.asStateFlow()

    private val _isSpeakerMuted = MutableStateFlow(false)
    val isSpeakerMuted: StateFlow<Boolean> = _isSpeakerMuted.asStateFlow()

    private val _activeSpeakerUsername = MutableStateFlow<String?>("أحمد_الملك")
    val activeSpeakerUsername: StateFlow<String?> = _activeSpeakerUsername.asStateFlow()

    private val _inGameMessages = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val inGameMessages: StateFlow<List<Pair<String, String>>> = _inGameMessages.asStateFlow()

    // --- DAILY REWARD MSG ---
    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()

    private var aiLoopJob: Job? = null

    init {
        // Periodic Voice Speaker Simulation for fun audio wave effect
        viewModelScope.launch {
            val names = listOf("أحمد_الملك", "سارة_لودو", "كابوس_الدومينو", "تاجر_القاهرة", null)
            while (true) {
                delay(4000)
                _activeSpeakerUsername.value = names.random()
            }
        }
    }

    fun setStake(stake: Long) {
        _selectedStake.value = stake
    }

    fun startMatchmaking(gameType: GameType, vsAi: Boolean, currentUsername: String, currentAvatarId: Int) {
        if (vsAi) {
            initGame(gameType, vsAi = true, currentUsername, currentAvatarId)
        } else {
            viewModelScope.launch {
                _isSearchingMatch.value = true
                _searchCountdown.value = 3
                for (i in 3 downTo 1) {
                    _searchCountdown.value = i
                    delay(1000)
                }
                _isSearchingMatch.value = false
                initGame(gameType, vsAi = false, currentUsername, currentAvatarId)
            }
        }
    }

    private fun initGame(gameType: GameType, vsAi: Boolean, currentUsername: String, currentAvatarId: Int) {
        _activeGameType.value = gameType
        val stake = _selectedStake.value
        _inGameMessages.value = emptyList()

        when (gameType) {
            GameType.LUDO -> {
                _ludoState.value = LudoGameLogic.createInitialGame(currentUsername, currentAvatarId, stake, vsAi)
                startAiLoopForLudo(currentUsername)
            }
            GameType.DOMINO -> {
                _dominoState.value = DominoGameLogic.startNewGame(currentUsername, currentAvatarId, stake, vsAi)
                startAiLoopForDomino(currentUsername)
            }
            GameType.MONOPOLY -> {
                _monopolyState.value = MonopolyGameLogic.startNewGame(currentUsername, currentAvatarId, stake, vsAi)
                startAiLoopForMonopoly(currentUsername)
            }
            GameType.NONE -> {}
        }
    }

    // --- LUDO GAME ACTIONS ---
    fun rollLudoDice() {
        val state = _ludoState.value ?: return
        if (!state.canRoll || state.winnerPlayer != null) return

        val nextState = LudoGameLogic.rollDice(state)
        _ludoState.value = nextState

        // Auto move if only 1 pawn can move
        if (!nextState.canRoll) {
            val currPlayer = nextState.players[nextState.currentTurnIndex]
            val movablePawns = LudoGameLogic.getMovablePawns(currPlayer, nextState.diceValue)
            if (movablePawns.size == 1) {
                viewModelScope.launch {
                    delay(600)
                    moveLudoPawn(movablePawns.first().id)
                }
            }
        }
    }

    fun moveLudoPawn(pawnId: Int) {
        val state = _ludoState.value ?: return
        if (state.canRoll || state.winnerPlayer != null) return

        val nextState = LudoGameLogic.movePawn(state, pawnId)
        _ludoState.value = nextState

        checkLudoMatchEnd(nextState)
    }

    private fun checkLudoMatchEnd(state: LudoBoardState) {
        if (state.winnerPlayer != null) {
            val isUserWin = !state.winnerPlayer.isAi
            val totalPot = state.stakeAmount * 4
            viewModelScope.launch {
                val user = repository.getUserByUsername(state.players[0].username)
                if (user != null) {
                    val delta = if (isUserWin) totalPot - state.stakeAmount else -state.stakeAmount
                    repository.recordGameResult(user.username, isUserWin, delta)
                }
            }
        }
    }

    private fun startAiLoopForLudo(userUsername: String) {
        aiLoopJob?.cancel()
        aiLoopJob = viewModelScope.launch {
            while (true) {
                delay(800)
                val state = _ludoState.value ?: break
                if (state.winnerPlayer != null) break

                val currPlayer = state.players[state.currentTurnIndex]
                if (currPlayer.isAi) {
                    // AI roll
                    if (state.canRoll) {
                        delay(700)
                        val rolledState = LudoGameLogic.rollDice(state)
                        _ludoState.value = rolledState

                        if (!rolledState.canRoll) {
                            delay(800)
                            val movables = LudoGameLogic.getMovablePawns(currPlayer, rolledState.diceValue)
                            if (movables.isNotEmpty()) {
                                val chosenPawn = movables.random()
                                val movedState = LudoGameLogic.movePawn(rolledState, chosenPawn.id)
                                _ludoState.value = movedState
                                checkLudoMatchEnd(movedState)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DOMINO GAME ACTIONS ---
    fun playDominoTile(tile: DominoTile, side: DominoGameLogic.PlaySide) {
        val state = _dominoState.value ?: return
        if (state.winnerPlayer != null) return

        val nextState = DominoGameLogic.playTile(state, tile, side)
        _dominoState.value = nextState

        checkDominoMatchEnd(nextState)
    }

    fun drawDominoFromBoneyard() {
        val state = _dominoState.value ?: return
        if (state.winnerPlayer != null) return

        val nextState = DominoGameLogic.drawFromBoneyard(state)
        _dominoState.value = nextState
    }

    fun passDominoTurn() {
        val state = _dominoState.value ?: return
        if (state.winnerPlayer != null) return

        val nextState = DominoGameLogic.passTurn(state)
        _dominoState.value = nextState
        checkDominoMatchEnd(nextState)
    }

    private fun checkDominoMatchEnd(state: DominoGameState) {
        if (state.winnerPlayer != null) {
            val isUserWin = !state.winnerPlayer.isAi
            val totalPot = state.stakeAmount * 2
            viewModelScope.launch {
                val user = repository.getUserByUsername(state.players[0].username)
                if (user != null) {
                    val delta = if (isUserWin) totalPot - state.stakeAmount else -state.stakeAmount
                    repository.recordGameResult(user.username, isUserWin, delta)
                }
            }
        }
    }

    private fun startAiLoopForDomino(userUsername: String) {
        aiLoopJob?.cancel()
        aiLoopJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val state = _dominoState.value ?: break
                if (state.winnerPlayer != null) break

                val currPlayer = state.players[state.currentTurnIndex]
                if (currPlayer.isAi) {
                    delay(1000)
                    // AI finds playable tile
                    val playableTile = currPlayer.hand.find { DominoGameLogic.canPlayTile(it, state.leftEnd, state.rightEnd) != null }
                    if (playableTile != null) {
                        val side = DominoGameLogic.canPlayTile(playableTile, state.leftEnd, state.rightEnd)!!
                        val nextState = DominoGameLogic.playTile(state, playableTile, side)
                        _dominoState.value = nextState
                        checkDominoMatchEnd(nextState)
                    } else if (state.boneyard.isNotEmpty()) {
                        val drawnState = DominoGameLogic.drawFromBoneyard(state)
                        _dominoState.value = drawnState
                    } else {
                        val passedState = DominoGameLogic.passTurn(state)
                        _dominoState.value = passedState
                        checkDominoMatchEnd(passedState)
                    }
                }
            }
        }
    }

    // --- MONOPOLY GAME ACTIONS ---
    fun rollMonopolyDice() {
        val state = _monopolyState.value ?: return
        if (state.winnerPlayer != null || state.pendingPurchaseTile != null) return

        val nextState = MonopolyGameLogic.rollDiceAndMove(state)
        _monopolyState.value = nextState

        checkMonopolyMatchEnd(nextState)
    }

    fun buyPendingMonopolyTile(buy: Boolean) {
        val state = _monopolyState.value ?: return
        val nextState = MonopolyGameLogic.buyPendingTile(state, buy)
        _monopolyState.value = nextState

        checkMonopolyMatchEnd(nextState)
    }

    private fun checkMonopolyMatchEnd(state: MonopolyGameState) {
        if (state.winnerPlayer != null) {
            val isUserWin = !state.winnerPlayer.isAi
            val totalPot = state.stakeAmount * 2
            viewModelScope.launch {
                val user = repository.getUserByUsername(state.players[0].username)
                if (user != null) {
                    val delta = if (isUserWin) totalPot - state.stakeAmount else -state.stakeAmount
                    repository.recordGameResult(user.username, isUserWin, delta)
                }
            }
        }
    }

    private fun startAiLoopForMonopoly(userUsername: String) {
        aiLoopJob?.cancel()
        aiLoopJob = viewModelScope.launch {
            while (true) {
                delay(1200)
                val state = _monopolyState.value ?: break
                if (state.winnerPlayer != null) break

                val currPlayer = state.players[state.currentTurnIndex]
                if (currPlayer.isAi) {
                    delay(1000)
                    if (state.pendingPurchaseTile != null) {
                        val nextState = MonopolyGameLogic.buyPendingTile(state, true)
                        _monopolyState.value = nextState
                        checkMonopolyMatchEnd(nextState)
                    } else {
                        val rolledState = MonopolyGameLogic.rollDiceAndMove(state)
                        _monopolyState.value = rolledState
                        checkMonopolyMatchEnd(rolledState)
                    }
                }
            }
        }
    }

    fun exitGame() {
        aiLoopJob?.cancel()
        _activeGameType.value = GameType.NONE
        _ludoState.value = null
        _dominoState.value = null
        _monopolyState.value = null
    }

    // --- DAILY GIFT ---
    fun claimDailyReward(username: String) {
        viewModelScope.launch {
            val res = repository.claimDailyReward(username, 2500L)
            res.onSuccess { reward ->
                _snackMessage.value = "مبروك! تم استلام الهدية اليومية: +$reward كوينز! 🎉"
            }.onFailure { ex ->
                _snackMessage.value = ex.message
            }
        }
    }

    fun clearSnackMessage() {
        _snackMessage.value = null
    }

    // --- IN-GAME CHAT & VOICE ---
    fun sendInGameQuickChat(sender: String, messageText: String) {
        _inGameMessages.value = _inGameMessages.value + Pair(sender, messageText)
    }

    fun toggleMic() {
        _isMicMuted.value = !_isMicMuted.value
    }

    fun toggleSpeaker() {
        _isSpeakerMuted.value = !_isSpeakerMuted.value
    }

    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameViewModel(repository) as T
        }
    }
}
