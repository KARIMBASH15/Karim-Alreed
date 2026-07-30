package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ChatMessageEntity
import com.example.data.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: GameRepository) : ViewModel() {

    val globalMessages: StateFlow<List<ChatMessageEntity>> = repository.getGlobalMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun sendMessage(username: String, text: String, avatarId: Int) {
        if (text.trim().isEmpty()) return
        viewModelScope.launch {
            repository.sendChatMessage(username, text.trim(), avatarId)
        }
    }

    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(repository) as T
        }
    }
}
