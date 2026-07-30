package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.DarkNavySurface
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodels.GameViewModel

@Composable
fun StakeSelectionModal(
    gameType: GameViewModel.GameType,
    userBalance: Long,
    onDismiss: () -> Unit,
    onStartMatch: (stake: Long, vsAi: Boolean) -> Unit
) {
    val stakesList = listOf(500L, 1000L, 5000L, 10000L, 50000L, 100000L, 200000L)
    var selectedStake by remember { mutableStateOf(500L) }
    var vsAi by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkNavySurface,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (gameType == GameViewModel.GameType.LUDO) "تحدي شوط اللودو 🎲" else "تحدي طاولة الدومينو 🀄",
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    fontSize = 20.sp
                )
                Text(
                    text = "اختر مبلغ المراهنة بالكوينز وطريقة اللعب",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "حدد الرهان (من 500 إلى 200,000 كوينز):",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    items(stakesList) { stake ->
                        val isSelected = selectedStake == stake
                        val canAfford = userBalance >= stake

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) GoldPrimary else DarkNavyCard)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color.White else if (canAfford) GoldPrimary.copy(alpha = 0.4f) else Color.Red,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = canAfford) {
                                    selectedStake = stake
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "%,d".format(stake),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else if (canAfford) GoldPrimary else Color.Gray,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "كوينز",
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.DarkGray else Color.LightGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "طريقة المنافسة:",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !vsAi,
                        onClick = { vsAi = false },
                        label = { Text("بحث عشوائي أونلاين 🌐", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Color.Black
                        )
                    )

                    FilterChip(
                        selected = vsAi,
                        onClick = { vsAi = true },
                        label = { Text("ضد الكمبيوتر 🤖", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }

                if (userBalance < selectedStake) {
                    Text(
                        text = "⚠️ رصيدك الحالي لا يكفي لهذا الرهان! يمكنك شحن رصيدك من المتجر.",
                        color = Color.Red,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onStartMatch(selectedStake, vsAi)
                },
                enabled = userBalance >= selectedStake,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "بدء الشوط الآن 🔥",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        }
    )
}
