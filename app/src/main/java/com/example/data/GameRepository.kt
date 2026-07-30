package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GameRepository(private val dao: AppDao) {

    suspend fun initDefaultData() = withContext(Dispatchers.IO) {
        // Create admin user if not existing
        val adminUser = dao.getUserByUsername("admin")
        if (adminUser == null) {
            dao.insertUser(
                UserEntity(
                    username = "admin",
                    email = "admin@ludostar.com",
                    passwordHash = "8090",
                    balance = 1_000_000L,
                    avatarId = 1,
                    isAdmin = true,
                    isOnline = true
                )
            )
        }

        // Create initial sample online players if empty
        val samplePlayers = listOf(
            UserEntity("أحمد_الملك", "ahmed@mail.com", "123456", 150000L, avatarId = 2),
            UserEntity("سارة_لودو", "sara@mail.com", "123456", 85000L, avatarId = 3),
            UserEntity("كابوس_الدومينو", "kabous@mail.com", "123456", 320000L, avatarId = 4),
            UserEntity("أمير_الصعيد", "amir@mail.com", "123456", 45000L, avatarId = 5)
        )
        for (player in samplePlayers) {
            if (dao.getUserByUsername(player.username) == null) {
                dao.insertUser(player)
            }
        }

        // Insert initial welcome global message if empty
        val initialMsg = ChatMessageEntity(
            senderUsername = "النظام",
            senderAvatarId = 1,
            messageText = "مرحباً بكم في لعبة لودو ودومينو ستار! استمتعوا بالألعاب والأشواط مع أصدقائكم.",
            roomCode = "GLOBAL"
        )
        dao.insertChatMessage(initialMsg)
    }

    // --- AUTH ---
    suspend fun getUserByUsername(username: String) = dao.getUserByUsername(username)
    suspend fun getUserByEmail(email: String) = dao.getUserByEmail(email)
    fun observeUser(username: String): Flow<UserEntity?> = dao.observeUser(username)

    suspend fun registerUser(user: UserEntity): Result<Unit> = withContext(Dispatchers.IO) {
        if (dao.getUserByUsername(user.username) != null) {
            return@withContext Result.failure(Exception("اسم المستخدم مستخدم بالفعل! اختر اسماً آخر."))
        }
        if (dao.getUserByEmail(user.email) != null) {
            return@withContext Result.failure(Exception("البريد الإلكتروني مسجل بالفعل! استخدم بريداً آخر."))
        }
        dao.insertUser(user)
        Result.success(Unit)
    }

    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)

    suspend fun addBalance(username: String, amount: Long) = dao.addBalance(username, amount)

    suspend fun setBalance(username: String, newBalance: Long) = dao.updateBalance(username, newBalance)

    suspend fun claimDailyReward(username: String, reward: Long = 2500L): Result<Long> = withContext(Dispatchers.IO) {
        val user = dao.getUserByUsername(username) ?: return@withContext Result.failure(Exception("المستخدم غير موجود"))
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L
        if (now - user.lastDailyClaim < dayInMillis) {
            val remainingHours = ((dayInMillis - (now - user.lastDailyClaim)) / (1000 * 60 * 60)).toInt()
            val remainingMins = (((dayInMillis - (now - user.lastDailyClaim)) / (1000 * 60)) % 60).toInt()
            return@withContext Result.failure(Exception("لقد استلمت الهدية اليومية بالفعل! يتبقى $remainingHours ساعة و $remainingMins دقيقة."))
        }
        dao.claimDailyReward(username, now, reward)
        Result.success(reward)
    }

    suspend fun recordGameResult(username: String, isWin: Boolean, coinDelta: Long) = withContext(Dispatchers.IO) {
        dao.addBalance(username, coinDelta)
        dao.updateGameStats(username, if (isWin) 1 else 0)
    }

    // --- TRANSACTIONS ---
    suspend fun createDepositRequest(username: String, amountCoins: Long, amountEgp: Double, txId: String, cashNum: String): Result<Unit> = withContext(Dispatchers.IO) {
        val tx = TransactionEntity(
            username = username,
            type = "DEPOSIT",
            amountCoins = amountCoins,
            amountEgp = amountEgp,
            cashNumber = cashNum,
            transactionId = txId,
            status = "PENDING"
        )
        dao.insertTransaction(tx)
        Result.success(Unit)
    }

    suspend fun createWithdrawRequest(username: String, amountCoins: Long, amountEgp: Double, cashNum: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = dao.getUserByUsername(username) ?: return@withContext Result.failure(Exception("المستخدم غير موجود"))
        if (user.balance < amountCoins) {
            return@withContext Result.failure(Exception("رصيدك الحالي لا يكفي لإتمام عملية السحب."))
        }
        // Deduct balance immediately
        dao.addBalance(username, -amountCoins)
        val tx = TransactionEntity(
            username = username,
            type = "WITHDRAW",
            amountCoins = amountCoins,
            amountEgp = amountEgp,
            cashNumber = cashNum,
            transactionId = "W-${System.currentTimeMillis().toString().takeLast(6)}",
            status = "PENDING"
        )
        dao.insertTransaction(tx)
        Result.success(Unit)
    }

    fun getUserTransactions(username: String): Flow<List<TransactionEntity>> = dao.getUserTransactions(username)
    fun getAllTransactions(): Flow<List<TransactionEntity>> = dao.getAllTransactions()

    suspend fun approveTransaction(txId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val tx = dao.getTransactionById(txId) ?: return@withContext Result.failure(Exception("العملية غير موجودة"))
        if (tx.status != "PENDING") return@withContext Result.failure(Exception("العملية معالجة بالفعل"))

        if (tx.type == "DEPOSIT") {
            dao.addBalance(tx.username, tx.amountCoins)
        }
        dao.updateTransactionStatus(txId, "APPROVED")
        Result.success(Unit)
    }

    suspend fun rejectTransaction(txId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val tx = dao.getTransactionById(txId) ?: return@withContext Result.failure(Exception("العملية غير موجودة"))
        if (tx.status != "PENDING") return@withContext Result.failure(Exception("العملية معالجة بالفعل"))

        if (tx.type == "WITHDRAW") {
            // Refund user coins
            dao.addBalance(tx.username, tx.amountCoins)
        }
        dao.updateTransactionStatus(txId, "REJECTED")
        Result.success(Unit)
    }

    // --- ADMIN ---
    fun getAllUsers(): Flow<List<UserEntity>> = dao.getAllUsers()
    fun searchUsers(query: String): Flow<List<UserEntity>> = dao.searchUsers(query)

    // --- CHAT ---
    fun getGlobalMessages(): Flow<List<ChatMessageEntity>> = dao.getRoomMessages("GLOBAL")
    suspend fun sendChatMessage(username: String, text: String, avatarId: Int) = withContext(Dispatchers.IO) {
        val msg = ChatMessageEntity(
            senderUsername = username,
            senderAvatarId = avatarId,
            messageText = text,
            roomCode = "GLOBAL"
        )
        dao.insertChatMessage(msg)
    }
}
