package com.example.game.domino

import kotlin.random.Random

object DominoGameLogic {

    fun generateFullDeck(): List<DominoTile> {
        val list = mutableListOf<DominoTile>()
        var idCounter = 0
        for (i in 0..6) {
            for (j in i..6) {
                list.add(DominoTile(idCounter++, i, j))
            }
        }
        return list.shuffled()
    }

    fun startNewGame(
        userUsername: String,
        userAvatarId: Int,
        stakeAmount: Long,
        vsAi: Boolean
    ): DominoGameState {
        val deck = generateFullDeck().toMutableList()
        val player1Hand = deck.take(7)
        deck.removeAll(player1Hand)

        val player2Hand = deck.take(7)
        deck.removeAll(player2Hand)

        val p1 = DominoPlayer(userUsername, player1Hand, isAi = false, avatarId = userAvatarId)
        val p2 = DominoPlayer("الكمبيوتر_الدومينو", player2Hand, isAi = true, avatarId = 3)

        val players = listOf(p1, p2)

        // Find starting player: player with highest double or highest tile
        val p1MaxDouble = p1.hand.filter { it.isDouble }.maxByOrNull { it.left }
        val p2MaxDouble = p2.hand.filter { it.isDouble }.maxByOrNull { it.left }

        val startTurnIndex = when {
            p1MaxDouble != null && p2MaxDouble != null -> if (p1MaxDouble.left >= p2MaxDouble.left) 0 else 1
            p1MaxDouble != null -> 0
            p2MaxDouble != null -> 1
            else -> 0
        }

        return DominoGameState(
            players = players,
            currentTurnIndex = startTurnIndex,
            boneyard = deck,
            stakeAmount = stakeAmount,
            statusText = "بدأت لعبة الدومينو! دور ${players[startTurnIndex].username}"
        )
    }

    fun canPlayTile(tile: DominoTile, leftEnd: Int?, rightEnd: Int?): PlaySide? {
        if (leftEnd == null || rightEnd == null) return PlaySide.ANY // First tile on board
        val fitsLeft = tile.left == leftEnd || tile.right == leftEnd
        val fitsRight = tile.left == rightEnd || tile.right == rightEnd

        return when {
            fitsLeft && fitsRight -> PlaySide.BOTH
            fitsLeft -> PlaySide.LEFT
            fitsRight -> PlaySide.RIGHT
            else -> null
        }
    }

    enum class PlaySide { LEFT, RIGHT, BOTH, ANY }

    fun playTile(
        state: DominoGameState,
        tile: DominoTile,
        targetSide: PlaySide
    ): DominoGameState {
        val currPlayerIndex = state.currentTurnIndex
        val player = state.players[currPlayerIndex]

        if (!player.hand.contains(tile)) return state

        var newLeftEnd = state.leftEnd
        var newRightEnd = state.rightEnd
        val newBoardChain = state.boardChain.toMutableList()
        var isFlipped = false

        if (state.boardChain.isEmpty()) {
            newLeftEnd = tile.left
            newRightEnd = tile.right
            newBoardChain.add(PlacedDomino(tile, false))
        } else {
            val playOnLeft = (targetSide == PlaySide.LEFT || (targetSide == PlaySide.BOTH && Random.nextBoolean()))
            if (playOnLeft && newLeftEnd != null) {
                if (tile.right == newLeftEnd) {
                    newLeftEnd = tile.left
                    isFlipped = false
                } else {
                    newLeftEnd = tile.right
                    isFlipped = true
                }
                newBoardChain.add(0, PlacedDomino(tile, isFlipped))
            } else if (newRightEnd != null) {
                if (tile.left == newRightEnd) {
                    newRightEnd = tile.right
                    isFlipped = false
                } else {
                    newRightEnd = tile.left
                    isFlipped = true
                }
                newBoardChain.add(PlacedDomino(tile, isFlipped))
            }
        }

        // Remove tile from hand
        val updatedHand = player.hand.filter { it.id != tile.id }
        val updatedPlayer = player.copy(hand = updatedHand)
        val updatedPlayers = state.players.mapIndexed { idx, p -> if (idx == currPlayerIndex) updatedPlayer else p }

        // Check if player won by emptying hand
        if (updatedHand.isEmpty()) {
            return state.copy(
                players = updatedPlayers,
                boardChain = newBoardChain,
                leftEnd = newLeftEnd,
                rightEnd = newRightEnd,
                winnerPlayer = updatedPlayer,
                statusText = "دومينو! فاز ${updatedPlayer.username} بالشرط وجائزة قدرها ${state.stakeAmount * 2} كوينز!"
            )
        }

        // Next turn
        val nextIndex = (currPlayerIndex + 1) % updatedPlayers.size
        return state.copy(
            players = updatedPlayers,
            boardChain = newBoardChain,
            leftEnd = newLeftEnd,
            rightEnd = newRightEnd,
            currentTurnIndex = nextIndex,
            statusText = "لعب ${player.username} حجراً. الآن دور ${updatedPlayers[nextIndex].username}"
        )
    }

    fun drawFromBoneyard(state: DominoGameState): DominoGameState {
        if (state.boneyard.isEmpty()) return state
        val currPlayerIndex = state.currentTurnIndex
        val player = state.players[currPlayerIndex]

        val drawnTile = state.boneyard.first()
        val remainingBoneyard = state.boneyard.drop(1)

        val updatedHand = player.hand + drawnTile
        val updatedPlayer = player.copy(hand = updatedHand)
        val updatedPlayers = state.players.mapIndexed { idx, p -> if (idx == currPlayerIndex) updatedPlayer else p }

        return state.copy(
            players = updatedPlayers,
            boneyard = remainingBoneyard,
            statusText = "${player.username} سحب حجراً من السحب."
        )
    }

    fun passTurn(state: DominoGameState): DominoGameState {
        val nextIndex = (state.currentTurnIndex + 1) % state.players.size
        // Check if both players are blocked and boneyard empty
        val p1Blocked = state.players[0].hand.none { canPlayTile(it, state.leftEnd, state.rightEnd) != null }
        val p2Blocked = state.players[1].hand.none { canPlayTile(it, state.leftEnd, state.rightEnd) != null }

        if (state.boneyard.isEmpty() && p1Blocked && p2Blocked) {
            val p1Sum = state.players[0].remainingPips
            val p2Sum = state.players[1].remainingPips
            val winner = if (p1Sum <= p2Sum) state.players[0] else state.players[1]
            return state.copy(
                winnerPlayer = winner,
                statusText = "انتهت اللعبة بالحبسة! الفائز بأقل عدد نقاط هو ${winner.username}!"
            )
        }

        return state.copy(
            currentTurnIndex = nextIndex,
            statusText = "مرر ${state.players[state.currentTurnIndex].username} الدور لـ ${state.players[nextIndex].username}"
        )
    }
}
