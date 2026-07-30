package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import com.example.data.TransactionEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StoreViewModel(private val repository: GameRepository) : ViewModel() {

    private val _storeMessage = MutableStateFlow<String?>(null)
    val storeMessage: StateFlow<String?> = _storeMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Fixed deposit payment Vodacom Cash number
    val vodafoneCashNumber = "01288889090"

    fun getUserTransactions(username: String): Flow<List<TransactionEntity>> {
        return repository.getUserTransactions(username)
    }

    /**
     * Rate: 6,000 coins = 30 EGP (200 coins / EGP)
     */
    fun submitDeposit(username: String, egpAmount: Double, txId: String, senderNum: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _storeMessage.value = null

            if (egpAmount <= 0) {
                _storeMessage.value = "يرجى إدخال مبلغ صحيح بالجنيه!"
                _isLoading.value = false
                return@launch
            }
            if (txId.trim().isEmpty()) {
                _storeMessage.value = "يرجى كتابة رقم العملية المراد تأكيدها!"
                _isLoading.value = false
                return@launch
            }

            val coinsToReceive = (egpAmount * 200).toLong()
            val result = repository.createDepositRequest(
                username = username,
                amountCoins = coinsToReceive,
                amountEgp = egpAmount,
                txId = txId.trim(),
                cashNum = senderNum.trim().ifEmpty { vodafoneCashNumber }
            )

            result.onSuccess {
                _storeMessage.value = "تم إرسال طلب إيداع $coinsToReceive كوينز بنجاح! سيتم مراجعته وإضافة الرصيد فوراً."
            }.onFailure { ex ->
                _storeMessage.value = ex.message
            }
            _isLoading.value = false
        }
    }

    /**
     * Withdrawal Pricing Rates:
     * - 60,000 coins = 15 EGP
     * - 100,000 coins = 26 EGP
     * - Standard rate: 4,000 coins = 1 EGP
     */
    fun calculateWithdrawalEgp(coinsAmount: Long): Double {
        return when {
            coinsAmount == 100000L -> 26.0
            coinsAmount == 60000L -> 15.0
            coinsAmount >= 100000L -> (coinsAmount / 100000.0) * 26.0
            else -> coinsAmount / 4000.0
        }
    }

    fun submitWithdrawal(username: String, coinsAmount: Long, cashWithdrawalNum: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _storeMessage.value = null

            if (coinsAmount < 10000) {
                _storeMessage.value = "الحد الأدنى للسحب هو 10,000 كوينز!"
                _isLoading.value = false
                return@launch
            }
            if (cashWithdrawalNum.trim().length < 10) {
                _storeMessage.value = "يرجى إدخال رقم كاش السحب بشكل صحيح (مثل 012...)"
                _isLoading.value = false
                return@launch
            }

            val egpValue = calculateWithdrawalEgp(coinsAmount)
            val result = repository.createWithdrawRequest(
                username = username,
                amountCoins = coinsAmount,
                amountEgp = egpValue,
                cashNum = cashWithdrawalNum.trim()
            )

            result.onSuccess {
                _storeMessage.value = "تم تقديم طلب سحب %,d كوينز (صافي %.2f جـ كاش) على الرقم %s بنجاح!".format(coinsAmount, egpValue, cashWithdrawalNum)
            }.onFailure { ex ->
                _storeMessage.value = ex.message
            }
            _isLoading.value = false
        }
    }

    fun clearStoreMessage() {
        _storeMessage.value = null
    }

    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StoreViewModel(repository) as T
        }
    }
}
