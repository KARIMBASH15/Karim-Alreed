package com.example.game.ludo

import androidx.compose.ui.graphics.Color

enum class PawnColor(val displayNameAr: String, val hexColor: Long, val startTrackIndex: Int, val homeEntranceIndex: Int) {
    RED("الأحمر", 0xFFE53935, 0, 50),
    GREEN("الأخضر", 0xFF43A047, 13, 11),
    YELLOW("الأصفر", 0xFFFDD835, 26, 24),
    BLUE("الأزرق", 0xFF1E88E5, 39, 37)
}

data class LudoPawn(
    val id: Int,
    val color: PawnColor,
    val position: Int = -1 // -1 = Yard, 0..51 = Main Track, 52..56 = Home Stretch, 57 = Finished Home
)

data class LudoPlayer(
    val username: String,
    val color: PawnColor,
    val isAi: Boolean = false,
    val avatarId: Int = 1,
    val pawns: List<LudoPawn> = (0..3).map { LudoPawn(it, color) }
) {
    val finishedCount: Int
        get() = pawns.count { it.position == 57 }
}

data class LudoBoardState(
    val players: List<LudoPlayer>,
    val currentTurnIndex: Int = 0,
    val diceValue: Int = 1,
    val canRoll: Boolean = true,
    val isRolling: Boolean = false,
    val extraTurn: Boolean = false,
    val winnerPlayer: LudoPlayer? = null,
    val stakeAmount: Long = 500L,
    val lastActionText: String = "حان دورك للرمي!"
)
