package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.data.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodels.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val searchQuery by adminViewModel.searchQuery.collectAsState()
    val searchedUsers by adminViewModel.searchedUsers.collectAsState()
    val allTransactions by adminViewModel.allTransactions.collectAsState()
    val resultMsg by adminViewModel.adminActionResult.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Users & Balance, 1 = Pending Requests

    var selectedUserForBalance by remember { mutableStateOf<UserEntity?>(null) }
    var balanceAdjustmentInput by remember { mutableStateOf("10000") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة الإدارة الشاملة (Admin) 👑", fontWeight = FontWeight.Bold, color = GoldPrimary) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Action Feedback Banner
            resultMsg?.let { msg ->
                Surface(
                    color = GoldPrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = msg, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { adminViewModel.clearResult() }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = GoldPrimary)
                        }
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = DarkNavySurface,
                contentColor = GoldPrimary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("إدارة الأعضاء والرصيد", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("طلبات الشحن والسحب", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TAB 0: SEARCH & BALANCE MODIFICATION
            if (activeTab == 0) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { adminViewModel.onSearchQueryChanged(it) },
                    label = { Text("ابحث باليوزر أو البريد الإلكتروني") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("admin_search_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(searchedUsers) { user ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(GoldPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(user.username.take(1), fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(user.username, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(
                                            "الرصيد: %,d كوينز | البريد: %s".format(user.balance, user.email),
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Button(
                                    onClick = { selectedUserForBalance = user },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("تعديل الرصيد", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: TRANSACTIONS APPROVAL/REJECTION
            if (activeTab == 1) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val pendingList = allTransactions.filter { it.status == "PENDING" }

                    if (pendingList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✨ لا توجد طلبات معلقة حالياً", color = TextMuted, fontSize = 14.sp)
                            }
                        }
                    }

                    items(allTransactions) { tx ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${if (tx.type == "DEPOSIT") "إيداع" else "سحب"} | العميل: ${tx.username}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "%,d كوينز (%.2f جـ)".format(tx.amountCoins, tx.amountEgp),
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "رقم العملية: ${tx.transactionId} | رقم الكاش: ${tx.cashNumber}",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                if (tx.status == "PENDING") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = { adminViewModel.approveTransaction(tx.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("قبول ✅", color = Color.White, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { adminViewModel.rejectTransaction(tx.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("رفض ❌", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "الحالة: ${if (tx.status == "APPROVED") "تم القبول ✅" else "تم الرفض ❌"}",
                                        color = if (tx.status == "APPROVED") Color.Green else Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Balance Adjustment Dialog
    selectedUserForBalance?.let { user ->
        AlertDialog(
            onDismissRequest = { selectedUserForBalance = null },
            containerColor = DarkNavySurface,
            title = { Text("إضافة/خصم رصيد لـ ${user.username}", color = GoldPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("الرصيد الحالي: %,d كوينز".format(user.balance), color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = balanceAdjustmentInput,
                        onValueChange = { balanceAdjustmentInput = it },
                        label = { Text("المبلغ (استخدم - للخصم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = balanceAdjustmentInput.toLongOrNull() ?: 0L
                        adminViewModel.modifyUserBalance(user.username, amount)
                        selectedUserForBalance = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("تأكيد التعديل", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForBalance = null }) {
                    Text("إلغاء", color = TextMuted)
                }
            }
        )
    }
}
