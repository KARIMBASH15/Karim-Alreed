package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- USER QUERIES ---
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username")
    fun observeUser(username: String): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY balance DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%'")
    fun searchUsers(query: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET balance = balance + :amount WHERE username = :username")
    suspend fun addBalance(username: String, amount: Long)

    @Query("UPDATE users SET balance = :newBalance WHERE username = :username")
    suspend fun updateBalance(username: String, newBalance: Long)

    @Query("UPDATE users SET lastDailyClaim = :claimTime, balance = balance + :reward WHERE username = :username")
    suspend fun claimDailyReward(username: String, claimTime: Long, reward: Long)

    @Query("UPDATE users SET totalGamesPlayed = totalGamesPlayed + 1, totalWins = totalWins + :winCount WHERE username = :username")
    suspend fun updateGameStats(username: String, winCount: Int)


    // --- TRANSACTION QUERIES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE username = :username ORDER BY timestamp DESC")
    fun getUserTransactions(username: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("UPDATE transactions SET status = :status WHERE id = :id")
    suspend fun updateTransactionStatus(id: Int, status: String)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Int): TransactionEntity?


    // --- CHAT QUERIES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages WHERE roomCode = :roomCode ORDER BY timestamp DESC LIMIT 50")
    fun getRoomMessages(roomCode: String): Flow<List<ChatMessageEntity>>
}
