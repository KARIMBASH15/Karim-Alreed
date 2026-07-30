package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val email: String,
    val passwordHash: String,
    val balance: Long = 10000L,
    val avatarId: Int = 1,
    val isOnline: Boolean = true,
    val lastDailyClaim: Long = 0L,
    val isAdmin: Boolean = false,
    val totalGamesPlayed: Int = 0,
    val totalWins: Int = 0
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val type: String, // "DEPOSIT" or "WITHDRAW"
    val amountCoins: Long,
    val amountEgp: Double,
    val cashNumber: String,
    val transactionId: String,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderUsername: String,
    val senderAvatarId: Int = 1,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isGlobal: Boolean = true,
    val roomCode: String = "GLOBAL"
)
