package com.example.game.monopoly

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

enum class TileType {
    START, PROPERTY, CHANCE, TAX, JAIL, UTILITY
}

data class MonopolyTile(
    val id: Int,
    val name: String,
    val type: TileType,
    val price: Long = 0L,
    val rent: Long = 0L,
    val colorHex: Color = Color.Gray,
    var ownerIndex: Int? = null
)

data class MonopolyPlayer(
    val username: String,
    val avatarId: Int,
    val position: Int = 0,
    val cash: Long = 10000L,
    val ownedPropertyIds: List<Int> = emptyList(),
    val inJail: Boolean = false,
    val jailTurns: Int = 0,
    val isAi: Boolean = false
)

data class MonopolyGameState(
    val players: List<MonopolyPlayer>,
    val currentTurnIndex: Int = 0,
    val diceValue1: Int = 1,
    val diceValue2: Int = 1,
    val boardTiles: List<MonopolyTile>,
    val stakeAmount: Long = 1000L,
    val statusText: String = "بدء لعبة تاجر المدينة! ارمِ النرد للتحرك.",
    val winnerPlayer: MonopolyPlayer? = null,
    val latestChanceCard: String? = null,
    val pendingPurchaseTile: MonopolyTile? = null
)

object MonopolyGameLogic {

    fun createInitialBoard(): List<MonopolyTile> {
        return listOf(
            MonopolyTile(0, "بداية البنك 🏛️", TileType.START, colorHex = Color(0xFF4CAF50)),
            MonopolyTile(1, "القاهرة 🏙️", TileType.PROPERTY, price = 2000L, rent = 500L, colorHex = Color(0xFFE91E63)),
            MonopolyTile(2, "صندوق الحظ 🎁", TileType.CHANCE, colorHex = Color(0xFFFF9800)),
            MonopolyTile(3, "الإسكندرية 🌊", TileType.PROPERTY, price = 2500L, rent = 600L, colorHex = Color(0xFFE91E63)),
            MonopolyTile(4, "سجن المدينة 🔒", TileType.JAIL, colorHex = Color(0xFF607D8B)),
            MonopolyTile(5, "شرم الشيخ 🌴", TileType.PROPERTY, price = 3500L, rent = 850L, colorHex = Color(0xFF2196F3)),
            MonopolyTile(6, "محطة القطار 🚆", TileType.UTILITY, price = 2000L, rent = 400L, colorHex = Color(0xFF9E9E9E)),
            MonopolyTile(7, "الغردقة 🏖️", TileType.PROPERTY, price = 4000L, rent = 1000L, colorHex = Color(0xFF2196F3)),
            MonopolyTile(8, "بنك مصر 💰", TileType.START, colorHex = Color(0xFFFFD700)),
            MonopolyTile(9, "الأقصر 🏛️", TileType.PROPERTY, price = 4500L, rent = 1200L, colorHex = Color(0xFF9C27B0)),
            MonopolyTile(10, "ضريبة الدخل 💸", TileType.TAX, colorHex = Color(0xFFF44336)),
            MonopolyTile(11, "أسوان ⛵", TileType.PROPERTY, price = 5000L, rent = 1400L, colorHex = Color(0xFF9C27B0)),
            MonopolyTile(12, "فرصة ونصيب 🎲", TileType.CHANCE, colorHex = Color(0xFFFF9800)),
            MonopolyTile(13, "المنصورة 🌸", TileType.PROPERTY, price = 3000L, rent = 700L, colorHex = Color(0xFF00CCD6)),
            MonopolyTile(14, "طنطا 🕌", TileType.PROPERTY, price = 3200L, rent = 750L, colorHex = Color(0xFF00CCD6)),
            MonopolyTile(15, "ميدان التحرير 🇪🇬", TileType.PROPERTY, price = 6000L, rent = 1800L, colorHex = Color(0xFFFF9800))
        )
    }

    fun startNewGame(username: String, avatarId: Int, stake: Long, vsAi: Boolean): MonopolyGameState {
        val player1 = MonopolyPlayer(username = username, avatarId = avatarId, cash = stake * 10, isAi = false)
        val player2 = MonopolyPlayer(username = if (vsAi) "التاجر_الذكاء_الاصطناعي 🤖" else "الكابتن_محمود 🎮", avatarId = 2, cash = stake * 10, isAi = vsAi)

        return MonopolyGameState(
            players = listOf(player1, player2),
            boardTiles = createInitialBoard(),
            stakeAmount = stake,
            statusText = "دورك الآن يا $username! ارمِ النرد لشراء العقارات."
        )
    }

    fun rollDiceAndMove(state: MonopolyGameState): MonopolyGameState {
        if (state.winnerPlayer != null || state.pendingPurchaseTile != null) return state

        val currPlayerIndex = state.currentTurnIndex
        val player = state.players[currPlayerIndex]

        val d1 = Random.nextInt(1, 7)
        val d2 = Random.nextInt(1, 7)
        val totalMove = d1 + d2

        val boardSize = state.boardTiles.size
        val newPos = (player.position + totalMove) % boardSize
        val passedStart = (player.position + totalMove) >= boardSize

        // Salary for passing START
        var newCash = player.cash + if (passedStart) 2000L else 0L

        var newStatus = "${player.username} ألقى النرد ($d1 + $d2 = $totalMove)"
        if (passedStart) {
            newStatus += " ومر على بداية البنك وجمع +2,000 كوينز!"
        }

        val landedTile = state.boardTiles[newPos]
        var pendingBuy: MonopolyTile? = null
        var chanceCardMsg: String? = null

        val updatedTiles = state.boardTiles.toMutableList()
        val updatedPlayers = state.players.toMutableList()

        when (landedTile.type) {
            TileType.PROPERTY, TileType.UTILITY -> {
                val owner = landedTile.ownerIndex
                if (owner == null) {
                    if (newCash >= landedTile.price) {
                        if (player.isAi) {
                            // AI automatically buys
                            newCash -= landedTile.price
                            updatedTiles[newPos] = landedTile.copy(ownerIndex = currPlayerIndex)
                            val newOwned = player.ownedPropertyIds + newPos
                            updatedPlayers[currPlayerIndex] = player.copy(
                                position = newPos,
                                cash = newCash,
                                ownedPropertyIds = newOwned
                            )
                            newStatus += " واشترى عقار [${landedTile.name}] بـ %,d كوينز!".format(landedTile.price)
                        } else {
                            // Human prompt
                            pendingBuy = landedTile
                            updatedPlayers[currPlayerIndex] = player.copy(position = newPos, cash = newCash)
                            newStatus += " وهبط على عقار [${landedTile.name}]. هل تريد الشراء؟"
                        }
                    } else {
                        updatedPlayers[currPlayerIndex] = player.copy(position = newPos, cash = newCash)
                        newStatus += " وهبط على [${landedTile.name}] ولكن لا يملك كاش كافٍ للشراء."
                    }
                } else if (owner != currPlayerIndex) {
                    // Pay rent
                    val rent = landedTile.rent
                    newCash -= rent
                    val ownerPlayer = updatedPlayers[owner]
                    updatedPlayers[owner] = ownerPlayer.copy(cash = ownerPlayer.cash + rent)
                    updatedPlayers[currPlayerIndex] = player.copy(position = newPos, cash = newCash)
                    newStatus += " وهبط على ملكية ${ownerPlayer.username} ودفع إيجار %,d كوينز!".format(rent)
                } else {
                    updatedPlayers[currPlayerIndex] = player.copy(position = newPos, cash = newCash)
                    newStatus += " وهبط على عقاره الخاص [${landedTile.name}]."
                }
            }
            TileType.CHANCE -> {
                val chanceEvents = listOf(
                    "فزت بجائزة الاستثمار العقاري! +1,500 كوينز 🎁" to 1500L,
                    "دفعت تكاليف صيانة العقارات! -800 كوينز 🛠️" to -800L,
                    "مكافأة من البنك الأهلي! +2,000 كوينz 💰" to 2000L,
                    "تبرع خيرات الجمعيات! -500 كوينز ❤️" to -500L
                )
                val (msg, valDelta) = chanceEvents.random()
                newCash += valDelta
                chanceCardMsg = msg
                updatedPlayers[currPlayerIndex] = player.copy(position = newPos, cash = newCash)
                newStatus += " وهبط على صندوق الحظ: $msg"
            }
            TileType.TAX -> {
                val tax = 1000L
                newCash -= tax
                updatedPlayers[currPlayerIndex] = player.copy(position = newPos, cash = newCash)
                newStatus += " وهبط على خانة الضرائب ودفع 1,000 كوينز للحكومة!"
            }
            TileType.JAIL -> {
                updatedPlayers[currPlayerIndex] = player.copy(position = newPos, cash = newCash, inJail = true)
                newStatus += " ودخل سجن المدينة مؤقتاً!"
            }
            TileType.START -> {
                updatedPlayers[currPlayerIndex] = player.copy(position = newPos, cash = newCash)
            }
        }

        // Check for bankruptcy / Winner
        var winner: MonopolyPlayer? = null
        if (updatedPlayers[currPlayerIndex].cash <= 0) {
            val otherPlayerIndex = (currPlayerIndex + 1) % 2
            winner = updatedPlayers[otherPlayerIndex]
        }

        val nextTurnIndex = if (pendingBuy != null) currPlayerIndex else (currPlayerIndex + 1) % 2

        return state.copy(
            players = updatedPlayers,
            currentTurnIndex = nextTurnIndex,
            diceValue1 = d1,
            diceValue2 = d2,
            boardTiles = updatedTiles,
            statusText = newStatus,
            winnerPlayer = winner,
            latestChanceCard = chanceCardMsg,
            pendingPurchaseTile = pendingBuy
        )
    }

    fun buyPendingTile(state: MonopolyGameState, buy: Boolean): MonopolyGameState {
        val tile = state.pendingPurchaseTile ?: return state
        val currIndex = state.currentTurnIndex
        val player = state.players[currIndex]

        val updatedTiles = state.boardTiles.toMutableList()
        val updatedPlayers = state.players.toMutableList()

        var status = state.statusText

        if (buy && player.cash >= tile.price) {
            val newCash = player.cash - tile.price
            val newOwned = player.ownedPropertyIds + tile.id
            updatedTiles[tile.id] = tile.copy(ownerIndex = currIndex)
            updatedPlayers[currIndex] = player.copy(cash = newCash, ownedPropertyIds = newOwned)
            status = "تمت الشراء بنجاح! أصبح [${tile.name}] ملكاً لـ ${player.username} 🏰"
        } else {
            status = "تم تجاوز شراء العقار [${tile.name}]."
        }

        val nextTurnIndex = (currIndex + 1) % 2

        return state.copy(
            players = updatedPlayers,
            currentTurnIndex = nextTurnIndex,
            boardTiles = updatedTiles,
            statusText = status,
            pendingPurchaseTile = null
        )
    }
}
