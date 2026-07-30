package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import com.example.data.TransactionEntity
import com.example.data.UserEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminViewModel(private val repository: GameRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchedUsers: StateFlow<List<UserEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllUsers()
            else repository.searchUsers(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _adminActionResult = MutableStateFlow<String?>(null)
    val adminActionResult: StateFlow<String?> = _adminActionResult.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun modifyUserBalance(targetUsername: String, amountToAdd: Long) {
        viewModelScope.launch {
            repository.addBalance(targetUsername, amountToAdd)
            _adminActionResult.value = "تم إضافة/خصم $amountToAdd كوينز للمستخدم $targetUsername بنجاح!"
        }
    }

    fun approveTransaction(txId: Int) {
        viewModelScope.launch {
            val res = repository.approveTransaction(txId)
            res.onSuccess {
                _adminActionResult.value = "تم قبول العملية #$txId بنجاح والإضافة للعميل!"
            }.onFailure { ex ->
                _adminActionResult.value = ex.message
            }
        }
    }

    fun rejectTransaction(txId: Int) {
        viewModelScope.launch {
            val res = repository.rejectTransaction(txId)
            res.onSuccess {
                _adminActionResult.value = "تم رفض العملية #$txId وإعادة الرصيد إن كان سحباً."
            }.onFailure { ex ->
                _adminActionResult.value = ex.message
            }
        }
    }

    fun clearResult() {
        _adminActionResult.value = null
    }

    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminViewModel(repository) as T
        }
    }
}
