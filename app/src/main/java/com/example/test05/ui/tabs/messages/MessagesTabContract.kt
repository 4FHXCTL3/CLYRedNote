package com.example.test05.ui.tabs.messages

import com.example.CLYRedNote.model.Message
import com.example.CLYRedNote.model.User

data class ConversationItem(
    val id: String,
    val user: User,
    val lastMessage: Message,
    val unreadCount: Int = 0,
    val isGroup: Boolean = false,
    val groupName: String? = null
)

interface MessagesTabContract {
    interface View {
        fun showConversations(conversations: List<ConversationItem>)
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun updateUnreadCount(conversationId: String, count: Int)
        fun navigateToMessageDetail(userId: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadConversations()
        fun onConversationClicked(conversationId: String)
        fun onSearchClicked()
        fun onAddClicked()
        fun markAsRead(conversationId: String)
    }
}