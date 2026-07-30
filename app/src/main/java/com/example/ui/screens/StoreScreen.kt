package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.data.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodels.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    currentUser: UserEntity,
    storeViewModel: StoreViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val storeMsg by storeViewModel.storeMessage.collectAsState()
    val isLoading by storeViewModel.isLoading.collectAsState()
    val userTransactions by storeViewModel.getUserTransactions(currentUser.username).collectAsState(emptyList())

    var selectedTab by remember { mutableStateOf(0) } // 0 = Deposit (Buy), 1 = Withdraw (Sell)

    // Deposit state
    var egpDepositInput by remember { mutableStateOf("30") }
    var txIdInput by remember { mutableStateOf("") }
    var senderNumberInput by remember { mutableStateOf("") }

    // Withdraw state
    var coinsWithdrawInput by remember { mutableStateOf("12000") }
    var cashWithdrawNumberInput by remember { mutableStateOf("") }

    val vodafoneNum = storeViewModel.vodafoneCashNumber

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("متجر العملات وصرف الأرباح 🛒", fontWeight = FontWeight.Bold, color = GoldPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavySurface)
            )
        },
        containerColor = DarkNavyBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkNavySurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("رصيدك الحسابي الحالي", color = TextMuted, fontSize = 12.sp)
                            Text(
                                "%,d كوينز".format(currentUser.balance),
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                fontSize = 22.sp
                            )
                        }
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Message Banner
            storeMsg?.let { msg ->
                item {
                    Surface(
                        color = GoldPrimary.copy(alpha = 0.2f),
                        border = StrokeBorder(1.dp, GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = msg, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { storeViewModel.clearStoreMessage() }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = GoldPrimary)
                            }
                        }
                    }
                }
            }

            // Tab Selector
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkNavySurface,
                    contentColor = GoldPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("شراء كوينز 💳", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("صرف كوينز (سحب) 💸", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // --- TAB 0: BUY / DEPOSIT ---
            if (selectedTab == 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkNavySurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("رقم محفظة الكاش للتحويل:", fontWeight = FontWeight.Bold, color = Color.White)

                            Surface(
                                color = DarkNavyCard,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = GoldPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(vodafoneNum, fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 18.sp)
                                    }
                                    IconButton(onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Cash Number", vodafoneNum)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "تم نسخ رقم الكاش!", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ الرقم", tint = Color.White)
                                    }
                                }
                            }

                            Text(
                                text = "💡 سعر الصرف: كل 30 جنيه = 6,000 كوينز",
                                color = TextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            OutlinedTextField(
                                value = egpDepositInput,
                                onValueChange = { egpDepositInput = it },
                                label = { Text("المبلغ بالجنيه (مثال: 30 جـ)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            val calculatedCoins = (egpDepositInput.toDoubleOrNull() ?: 0.0) * 200
                            Text(
                                text = "ستحصل على: %,.0f كوينز".format(calculatedCoins),
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            OutlinedTextField(
                                value = txIdInput,
                                onValueChange = { txIdInput = it },
                                label = { Text("رقم العملية / الرقم المرجعي للتحويل") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tx_id_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = senderNumberInput,
                                onValueChange = { senderNumberInput = it },
                                label = { Text("رقم محفظتك التي حولت منها (اختياري)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val amountEgp = egpDepositInput.toDoubleOrNull() ?: 0.0
                                    storeViewModel.submitDeposit(currentUser.username, amountEgp, txIdInput, senderNumberInput)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_deposit_button"),
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("تأكيد طلب الشراء والدفع", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // --- TAB 1: SELL / WITHDRAW ---
            if (selectedTab == 1) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkNavySurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("صرف كوينز الأرباح إلى كاش 💵", fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                "⚡ أسعار السحب المباشر للكاش:\n• 60,000 كوينز = 15 جنيه كاش\n• 100,000 كوينز = 26 جنيه كاش (عرض مميز)",
                                color = GoldPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            // Preset Quick Selection Chips
                            Text("اختر باقة سحب سريعة:", color = TextMuted, fontSize = 12.sp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = coinsWithdrawInput == "60000",
                                    onClick = { coinsWithdrawInput = "60000" },
                                    label = { Text("60,000 كوينز ➔ 15 جـ", fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldPrimary,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                                FilterChip(
                                    selected = coinsWithdrawInput == "100000",
                                    onClick = { coinsWithdrawInput = "100000" },
                                    label = { Text("100,000 كوينز ➔ 26 جـ 🔥", fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldPrimary,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = coinsWithdrawInput,
                                onValueChange = { coinsWithdrawInput = it },
                                label = { Text("أو أدخل عدد الكوينز المراد صرفها") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            val enteredCoins = coinsWithdrawInput.toLongOrNull() ?: 0L
                            val egpReceived = storeViewModel.calculateWithdrawalEgp(enteredCoins)
                            Text(
                                text = "ستستلم صافي كاش: %,.2f جنيه".format(egpReceived),
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            OutlinedTextField(
                                value = cashWithdrawNumberInput,
                                onValueChange = { cashWithdrawNumberInput = it },
                                label = { Text("رقم كاش السحب الخاص بك (مثال: 012...)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cash_withdraw_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val coins = coinsWithdrawInput.toLongOrNull() ?: 0L
                                    storeViewModel.submitWithdrawal(currentUser.username, coins, cashWithdrawNumberInput)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_withdraw_button"),
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("إرسال طلب السحب للكاش", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // --- TRANSACTION HISTORY TITLE ---
            item {
                Text("سجل المعاملات والعمليات 📜", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            }

            // Transaction History Items
            items(userTransactions) { tx ->
                TransactionCardWidget(tx = tx)
            }
        }
    }
}

@Composable
fun TransactionCardWidget(tx: TransactionEntity) {
    Surface(
        color = DarkNavySurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (tx.type == "DEPOSIT") "شراء كوينز 📥" else "سحب أرباح 📤",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "%,d كوينز".format(tx.amountCoins),
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "المبلغ: %.2f جـ | العملية: %s | الكاش: %s".format(tx.amountEgp, tx.transactionId, tx.cashNumber),
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            val statusColor = when (tx.status) {
                "APPROVED" -> Color.Green
                "REJECTED" -> Color.Red
                else -> GoldPrimary
            }
            val statusText = when (tx.status) {
                "APPROVED" -> "مقبول ✅"
                "REJECTED" -> "مرفوض ❌"
                else -> "قيد المراجعة ⏳"
            }

            Surface(
                color = statusColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
