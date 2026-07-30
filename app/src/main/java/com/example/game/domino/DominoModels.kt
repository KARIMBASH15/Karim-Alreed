package com.example.game.domino

data class DominoTile(
    val id: Int,
    val left: Int, // 0..6
    val right: Int // 0..6
) {
    val isDouble: Boolean get() = left == right
    val totalPips: Int get() = left + right
}

data class DominoPlayer(
    val username: String,
    val hand: List<DominoTile> = emptyList(),
    val isAi: Boolean = false,
    val avatarId: Int = 1
) {
    val remainingPips: Int get() = hand.sumOf { it.totalPips }
}

data class PlacedDomino(
    val tile: DominoTile,
    val isFlipped: Boolean = false
)

data class DominoGameState(
    val players: List<DominoPlayer>,
    val currentTurnIndex: Int = 0,
    val boardChain: List<PlacedDomino> = emptyList(),
    val leftEnd: Int? = null,
    val rightEnd: Int? = null,
    val boneyard: List<DominoTile> = emptyList(),
    val winnerPlayer: DominoPlayer? = null,
    val stakeAmount: Long = 500L,
    val statusText: String = "بدأت المباراة! اختر حجراً مناسباً للعب."
)
