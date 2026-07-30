package com.example.game.ludo

import kotlin.random.Random

object LudoGameLogic {

    // Safe track positions on the 0..51 main board loop
    val SAFE_POSITIONS = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    fun createInitialGame(
        userUsername: String,
        userAvatarId: Int,
        stakeAmount: Long,
        modeVsAi: Boolean
    ): LudoBoardState {
        val players = if (modeVsAi) {
            listOf(
                LudoPlayer(userUsername, PawnColor.RED, isAi = false, avatarId = userAvatarId),
                LudoPlayer("الكمبيوتر 1", PawnColor.GREEN, isAi = true, avatarId = 2),
                LudoPlayer("الكمبيوتر 2", PawnColor.YELLOW, isAi = true, avatarId = 3),
                LudoPlayer("الكمبيوتر 3", PawnColor.BLUE, isAi = true, avatarId = 4)
            )
        } else {
            listOf(
                LudoPlayer(userUsername, PawnColor.RED, isAi = false, avatarId = userAvatarId),
                LudoPlayer("أحمد_المحترف", PawnColor.GREEN, isAi = true, avatarId = 2),
                LudoPlayer("سارة_الملكة", PawnColor.YELLOW, isAi = true, avatarId = 3),
                LudoPlayer("كابوس_اللعبة", PawnColor.BLUE, isAi = true, avatarId = 4)
            )
        }
        return LudoBoardState(
            players = players,
            currentTurnIndex = 0,
            stakeAmount = stakeAmount,
            lastActionText = "بدأ الشوط بـ $stakeAmount كوينز! دور ${players[0].username}"
        )
    }

    /**
     * Converts a player's pawn internal relative position (-1, 0..51, 52..56, 57) to absolute track index or status.
     * Relative pos 0 = start color track index.
     */
    fun getAbsoluteTrackIndex(pawn: LudoPawn, player: LudoPlayer): Int {
        if (pawn.position < 0 || pawn.position >= 52) return pawn.position
        val start = player.color.startTrackIndex
        return (start + pawn.position) % 52
    }

    fun canMovePawn(pawn: LudoPawn, dice: Int): Boolean {
        if (pawn.position == 57) return false // Already finished
        if (pawn.position == -1) return dice == 6 // Need 6 to exit yard
        if (pawn.position + dice > 57) return false // Can't overshoot home
        return true
    }

    fun getMovablePawns(player: LudoPlayer, dice: Int): List<LudoPawn> {
        return player.pawns.filter { canMovePawn(it, dice) }
    }

    fun rollDice(currentState: LudoBoardState): LudoBoardState {
        if (!currentState.canRoll || currentState.winnerPlayer != null) return currentState
        val dice = Random.nextInt(1, 7)
        val currentPlayer = currentState.players[currentState.currentTurnIndex]
        val movablePawns = getMovablePawns(currentPlayer, dice)

        var statusMsg = "${currentPlayer.username} رمى النرد وحصل على $dice!"

        if (movablePawns.isEmpty()) {
            statusMsg += " لا يوجد أحجار قابلة للتحريك."
            // Pass turn if no moves
            val nextTurnIndex = (currentState.currentTurnIndex + 1) % currentState.players.size
            return currentState.copy(
                diceValue = dice,
                canRoll = true,
                currentTurnIndex = nextTurnIndex,
                lastActionText = "$statusMsg انتقل الدور إلى ${currentState.players[nextTurnIndex].username}"
            )
        } else {
            return currentState.copy(
                diceValue = dice,
                canRoll = false, // Must pick pawn or move auto
                lastActionText = statusMsg
            )
        }
    }

    fun movePawn(currentState: LudoBoardState, pawnId: Int): LudoBoardState {
        val currPlayerIndex = currentState.currentTurnIndex
        val currentPlayer = currentState.players[currPlayerIndex]
        val dice = currentState.diceValue

        val pawnToMove = currentPlayer.pawns.find { it.id == pawnId } ?: return currentState
        if (!canMovePawn(pawnToMove, dice)) return currentState

        val newPosition = if (pawnToMove.position == -1) {
            0 // Move out of yard onto start square
        } else {
            pawnToMove.position + dice
        }

        val updatedPawn = pawnToMove.copy(position = newPosition)
        val updatedPlayerPawns = currentPlayer.pawns.map { if (it.id == pawnId) updatedPawn else it }
        val updatedCurrentPlayer = currentPlayer.copy(pawns = updatedPlayerPawns)

        var gotExtraRoll = (dice == 6) || (newPosition == 57)
        var captureOccurred = false
        var captureMsg = ""

        // Check capture if on main track (0..51)
        val playersAfterCapture = currentState.players.mapIndexed { pIndex, p ->
            if (pIndex == currPlayerIndex) {
                updatedCurrentPlayer
            } else {
                val absNewPos = getAbsoluteTrackIndex(updatedPawn, updatedCurrentPlayer)
                val isNewPosSafe = (newPosition in 0..51) && SAFE_POSITIONS.contains(absNewPos)

                if (newPosition in 0..51 && !isNewPosSafe) {
                    val capturedPawns = p.pawns.map { enemyPawn ->
                        if (enemyPawn.position in 0..51 && getAbsoluteTrackIndex(enemyPawn, p) == absNewPos) {
                            captureOccurred = true
                            captureMsg = " وقام بأكل حجر ${p.username}!"
                            enemyPawn.copy(position = -1) // Send back to yard
                        } else {
                            enemyPawn
                        }
                    }
                    p.copy(pawns = capturedPawns)
                } else {
                    p
                }
            }
        }

        if (captureOccurred) {
            gotExtraRoll = true
        }

        // Check if player won (all 4 pawns finished, or 2 pawns for fast play)
        val updatedWinner = playersAfterCapture.find { p -> p.pawns.count { it.position == 57 } >= 4 }

        var nextTurnIndex = currPlayerIndex
        var actionText = "${currentPlayer.username} تحرك بـ $dice خطوة.$captureMsg"

        if (updatedWinner != null) {
            actionText = "مبروك! الفائز بالشوط هو ${updatedWinner.username} وهدية الشوط ${currentState.stakeAmount * 4} كوينز!"
        } else if (gotExtraRoll) {
            actionText += " حصل على رمية إضافية!"
        } else {
            nextTurnIndex = (currPlayerIndex + 1) % playersAfterCapture.size
            actionText += " انتقل الدور إلى ${playersAfterCapture[nextTurnIndex].username}."
        }

        return currentState.copy(
            players = playersAfterCapture,
            currentTurnIndex = nextTurnIndex,
            canRoll = true,
            winnerPlayer = updatedWinner,
            lastActionText = actionText
        )
    }
}
