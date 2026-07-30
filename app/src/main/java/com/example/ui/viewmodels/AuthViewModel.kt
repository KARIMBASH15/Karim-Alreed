package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import com.example.data.UserEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: GameRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authStateMessage = MutableStateFlow<String?>(null)
    val authStateMessage: StateFlow<String?> = _authStateMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initDefaultData()
            // Auto login as admin or guest for convenience if needed, default to guest
        }
    }

    fun login(identifier: String, passwordInput: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authStateMessage.value = null

            val user = repository.getUserByUsername(identifier.trim())
                ?: repository.getUserByEmail(identifier.trim())

            if (user == null) {
                _authStateMessage.value = "اسم المستخدم أو البريد الإلكتروني غير موجود!"
                _isLoading.value = false
                return@launch
            }

            if (user.passwordHash != passwordInput) {
                _authStateMessage.value = "كلمة المرور غير صحيحة!"
                _isLoading.value = false
                return@launch
            }

            // Observe logged in user in real time for balance updates
            repository.observeUser(user.username).collectLatest { updatedUser ->
                _currentUser.value = updatedUser
            }
            _isLoading.value = false
        }
    }

    fun register(usernameInput: String, emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authStateMessage.value = null

            val cleanUsername = usernameInput.trim()
            val cleanEmail = emailInput.trim()

            if (cleanUsername.isEmpty() || cleanEmail.isEmpty() || passwordInput.isEmpty()) {
                _authStateMessage.value = "يرجى ملء جميع الحقول المطلوبة!"
                _isLoading.value = false
                return@launch
            }

            val newUser = UserEntity(
                username = cleanUsername,
                email = cleanEmail,
                passwordHash = passwordInput,
                balance = 10000L, // Starter bonus coins
                avatarId = (1..6).random(),
                isOnline = true
            )

            val result = repository.registerUser(newUser)
            result.onSuccess {
                login(cleanUsername, passwordInput)
            }.onFailure { ex ->
                _authStateMessage.value = ex.message ?: "حدث خطأ أثناء التسجيل"
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        _currentUser.value = null
        _authStateMessage.value = null
    }

    fun clearMessage() {
        _authStateMessage.value = null
    }

    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repository) as T
        }
    }
}
