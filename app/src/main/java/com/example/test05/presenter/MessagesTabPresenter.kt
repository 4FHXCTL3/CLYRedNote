package com.example.test05.presenter

import com.example.CLYRedNote.model.MessageType
import com.example.test05.ui.tabs.messages.ConversationItem
import com.example.test05.ui.tabs.messages.MessagesTabContract
import com.example.test05.utils.JsonDataLoader
import kotlinx.coroutines.*

class MessagesTabPresenter(
    private val dataLoader: JsonDataLoader
) : MessagesTabContract.Presenter {
    
    private var view: MessagesTabContract.View? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override fun attachView(view: MessagesTabContract.View) {
        this.view = view
    }
    
    override fun detachView() {
        this.view = null
        scope.cancel()
    }
    
    override fun loadConversations() {
        view?.showLoading(true)
        
        scope.launch {
            try {
                val conversations = withContext(Dispatchers.IO) {
                    generateConversations()
                }
                view?.showConversations(conversations)
            } catch (e: Exception) {
                view?.showError("加载对话列表失败: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }
    
    override fun onConversationClicked(conversationId: String) {
        // Extract user ID from conversation and navigate to MessageDetail
        // For group conversations, we don't navigate to MessageDetail
        if (!conversationId.contains("group")) {
            val userId = when (conversationId) {
                "conv_system" -> "user_001"
                "conv_service" -> "user_002" 
                "conv_activity" -> "user_003"
                "conv_friend_001" -> "user_001"
                "conv_friend_002" -> "user_002"
                else -> "user_001" // default
            }
            view?.navigateToMessageDetail(userId)
        }
    }
    
    override fun onSearchClicked() {
        // TODO: Open search screen
    }
    
    override fun onAddClicked() {
        // TODO: Open add friend/create group screen
    }
    
    override fun markAsRead(conversationId: String) {
        view?.updateUnreadCount(conversationId, 0)
    }
    
    private fun generateConversations(): List<ConversationItem> {
        val users = dataLoader.loadUsers()
        val messages = dataLoader.loadMessages()
        
        // Create conversations using our existing virtual data
        return listOf(
            ConversationItem(
                id = "conv_system",
                user = users.find { it.id == "user_001" } ?: users.first(),
                lastMessage = messages.find { it.id == "msg_system_001" } ?: createMockMessage("订单已发货", "系统消息"),
                unreadCount = 0
            ),
            ConversationItem(
                id = "conv_service",
                user = users.find { it.id == "user_002" } ?: users.first(),
                lastMessage = messages.find { it.id == "msg_service_001" } ?: createMockMessage("Nike旗舰店：您有一笔订单待确认...", "客服消息"),
                unreadCount = 0
            ),
            ConversationItem(
                id = "conv_activity",
                user = users.find { it.id == "user_003" } ?: users.first(),
                lastMessage = messages.find { it.id == "msg_activity_001" } ?: createMockMessage("发布运动笔记，参与活动赢奖品", "活动消息"),
                unreadCount = 0
            ),
            ConversationItem(
                id = "conv_group_001",
                user = users.find { it.id == "user_004" } ?: users.first(),
                lastMessage = messages.find { it.id == "msg_group_001" } ?: createMockMessage("如收到违规信息，长按消息可以举报", "运动爱好者群"),
                unreadCount = 0,
                isGroup = true,
                groupName = "运动爱好者群"
            ),
            ConversationItem(
                id = "conv_group_002",
                user = users.find { it.id == "user_005" } ?: users.first(),
                lastMessage = messages.find { it.id == "msg_group_002" } ?: createMockMessage("[3条]小美: 今天的穿搭分享", "穿搭交流群"),
                unreadCount = 1,
                isGroup = true,
                groupName = "穿搭交流群"
            ),
            ConversationItem(
                id = "conv_group_003",
                user = users.find { it.id == "user_006" } ?: users.first(),
                lastMessage = messages.find { it.id == "msg_group_003" } ?: createMockMessage("[2条]美食达人: [图片]", "美食分享群"),
                unreadCount = 1,
                isGroup = true,
                groupName = "美食分享群"
            ),
            ConversationItem(
                id = "conv_friend_001",
                user = users.find { it.id == "user_001" } ?: users.first(),
                lastMessage = messages.find { it.id == "msg_friend_001" } ?: createMockMessage("[图片]", users.find { it.id == "user_001" }?.nickname ?: "小飞"),
                unreadCount = 0
            ),
            ConversationItem(
                id = "conv_friend_002",
                user = users.find { it.id == "user_002" } ?: users.first(),
                lastMessage = messages.find { it.id == "msg_friend_002" } ?: createMockMessage("晚安~", users.find { it.id == "user_002" }?.nickname ?: "小雨"),
                unreadCount = 0
            )
        )
    }
    
    private fun createMockMessage(content: String, senderName: String): com.example.CLYRedNote.model.Message {
        val mockSender = com.example.CLYRedNote.model.User(
            id = "system",
            username = senderName,
            nickname = senderName
        )
        val mockReceiver = com.example.CLYRedNote.model.User(
            id = "user_current",
            username = "CLY",
            nickname = "CLY"
        )
        
        return com.example.CLYRedNote.model.Message(
            id = "mock_${System.currentTimeMillis()}",
            content = content,
            type = MessageType.TEXT,
            sender = mockSender,
            receiver = mockReceiver,
            conversationId = "mock_conversation"
        )
    }
}