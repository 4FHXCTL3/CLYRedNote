package com.example.test05.ui.tabs.messagedetail

import com.example.CLYRedNote.model.Message
import com.example.CLYRedNote.model.User

interface MessageDetailContract {
    interface View {
        fun showMessages(messages: List<Message>)
        fun showMessageSent(message: Message)
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun scrollToBottom()
        fun updateUserInfo(user: User)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadMessages(conversationId: String)
        fun loadUserInfo(userId: String)
        fun sendTextMessage(content: String, receiverId: String)
        fun sendEmojiMessage(emoji: String, receiverId: String)
        fun sendImageMessage(imagePath: String, receiverId: String)
        fun markMessagesAsRead(conversationId: String)
        fun onBackClicked()
        fun onMoreOptionsClicked()
    }
}