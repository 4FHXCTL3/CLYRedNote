package com.example.test05.presenter

import com.example.CLYRedNote.model.Message
import com.example.CLYRedNote.model.MessageType
import com.example.CLYRedNote.model.User
import com.example.test05.ui.tabs.messagedetail.MessageDetailContract
import com.example.test05.utils.JsonDataLoader
import com.example.test05.utils.MessageStorage
import kotlinx.coroutines.*
import java.util.Date

class MessageDetailPresenter(
    private val dataLoader: JsonDataLoader,
    private val messageStorage: MessageStorage
) : MessageDetailContract.Presenter {
    
    private var view: MessageDetailContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var messages: List<Message> = emptyList()
    private var users: List<User> = emptyList()
    private var currentUser: User? = null
    private var chatUser: User? = null
    
    override fun attachView(view: MessageDetailContract.View) {
        this.view = view
        loadUserData()
    }

    override fun detachView() {
        view = null
        presenterScope.cancel()
    }

    private fun loadUserData() {
        presenterScope.launch {
            try {
                users = dataLoader.loadUsers()
                currentUser = users.find { it.id == "user_current" }
            } catch (e: Exception) {
                view?.showError("Failed to load user data: ${e.message}")
            }
        }
    }

    override fun loadUserInfo(userId: String) {
        presenterScope.launch {
            try {
                chatUser = users.find { it.id == userId }
                chatUser?.let { user ->
                    view?.updateUserInfo(user)
                }
            } catch (e: Exception) {
                view?.showError("Failed to load user info: ${e.message}")
            }
        }
    }

    override fun loadMessages(conversationId: String) {
        view?.showLoading(true)
        presenterScope.launch {
            try {
                // Create mock messages for the conversation
                val mockMessages = createMockMessages(conversationId)
                messages = mockMessages
                view?.showMessages(messages)
                view?.scrollToBottom()
                
            } catch (e: Exception) {
                view?.showError("Failed to load messages: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }
    
    private fun createMockMessages(conversationId: String): List<Message> {
        val currentUserObj = currentUser ?: return emptyList()
        val chatUserObj = chatUser ?: return emptyList()
        
        // Create different conversation content based on user ID
        return when (chatUserObj.id) {
            "user_001" -> createConversationForUser001(conversationId, currentUserObj, chatUserObj)
            "user_002" -> createConversationForUser002(conversationId, currentUserObj, chatUserObj)
            "user_003" -> createConversationForUser003(conversationId, currentUserObj, chatUserObj)
            "user_004" -> createConversationForUser004(conversationId, currentUserObj, chatUserObj)
            "user_005" -> createConversationForUser005(conversationId, currentUserObj, chatUserObj)
            else -> createDefaultConversation(conversationId, currentUserObj, chatUserObj)
        }
    }
    
    private fun createConversationForUser001(conversationId: String, currentUser: User, chatUser: User): List<Message> {
        return listOf(
            Message(
                id = "msg_001_1",
                content = "你好！看到你的穿搭笔记了，很不错呢",
                type = MessageType.TEXT,
                sender = chatUser,
                receiver = currentUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 7200000) // 2 hours ago
            ),
            Message(
                id = "msg_001_2",
                content = "谢谢！😊",
                type = MessageType.TEXT,
                sender = currentUser,
                receiver = chatUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 7100000)
            ),
            Message(
                id = "msg_001_3",
                content = "能问一下那件外套在哪买的吗？",
                type = MessageType.TEXT,
                sender = chatUser,
                receiver = currentUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 3600000)
            ),
            Message(
                id = "msg_001_4",
                content = "是在Zara买的，刚好打折～",
                type = MessageType.TEXT,
                sender = currentUser,
                receiver = chatUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 1800000)
            )
        )
    }
    
    private fun createConversationForUser002(conversationId: String, currentUser: User, chatUser: User): List<Message> {
        return listOf(
            Message(
                id = "msg_002_1",
                content = "",
                type = MessageType.IMAGE,
                sender = chatUser,
                receiver = currentUser,
                conversationId = conversationId,
                isRead = true,
                images = listOf("image/food1.jpg"),
                createdAt = Date(System.currentTimeMillis() - 5400000) // 1.5 hours ago
            ),
            Message(
                id = "msg_002_2",
                content = "今天做的蛋糕怎么样？",
                type = MessageType.TEXT,
                sender = chatUser,
                receiver = currentUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 5300000)
            ),
            Message(
                id = "msg_002_3",
                content = "看起来好棒！一定很好吃",
                type = MessageType.TEXT,
                sender = currentUser,
                receiver = chatUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 2700000)
            ),
            Message(
                id = "msg_002_4",
                content = "🍰",
                type = MessageType.EMOJI,
                sender = currentUser,
                receiver = chatUser,
                conversationId = conversationId,
                isRead = true,
                emoji = "🍰",
                createdAt = Date(System.currentTimeMillis() - 900000)
            )
        )
    }
    
    private fun createConversationForUser003(conversationId: String, currentUser: User, chatUser: User): List<Message> {
        return listOf(
            Message(
                id = "msg_003_1",
                content = "明天一起去跑步吗？",
                type = MessageType.TEXT,
                sender = chatUser,
                receiver = currentUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 10800000) // 3 hours ago
            ),
            Message(
                id = "msg_003_2",
                content = "好啊，几点？",
                type = MessageType.TEXT,
                sender = currentUser,
                receiver = chatUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 9000000)
            ),
            Message(
                id = "msg_003_3",
                content = "早上7点，老地方见",
                type = MessageType.TEXT,
                sender = chatUser,
                receiver = currentUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 7200000)
            ),
            Message(
                id = "msg_003_4",
                content = "收到！💪",
                type = MessageType.TEXT,
                sender = currentUser,
                receiver = chatUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 1200000)
            )
        )
    }
    
    private fun createConversationForUser004(conversationId: String, currentUser: User, chatUser: User): List<Message> {
        return listOf(
            Message(
                id = "msg_004_1",
                content = "你的护肤分享太实用了！",
                type = MessageType.TEXT,
                sender = chatUser,
                receiver = currentUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 14400000) // 4 hours ago
            ),
            Message(
                id = "msg_004_2",
                content = "那个精华真的很好用吗？",
                type = MessageType.TEXT,
                sender = chatUser,
                receiver = currentUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 12600000)
            ),
            Message(
                id = "msg_004_3",
                content = "真的！我用了两个月，效果很明显",
                type = MessageType.TEXT,
                sender = currentUser,
                receiver = chatUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 3600000)
            )
        )
    }
    
    private fun createConversationForUser005(conversationId: String, currentUser: User, chatUser: User): List<Message> {
        return listOf(
            Message(
                id = "msg_005_1",
                content = "周末去看电影吗？",
                type = MessageType.TEXT,
                sender = chatUser,
                receiver = currentUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 18000000) // 5 hours ago
            ),
            Message(
                id = "msg_005_2",
                content = "看什么电影？",
                type = MessageType.TEXT,
                sender = currentUser,
                receiver = chatUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 16200000)
            ),
            Message(
                id = "msg_005_3",
                content = "新上映的那部爱情片，据说很不错",
                type = MessageType.TEXT,
                sender = chatUser,
                receiver = currentUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 14400000)
            ),
            Message(
                id = "msg_005_4",
                content = "好的，约起来！",
                type = MessageType.TEXT,
                sender = currentUser,
                receiver = chatUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 600000)
            )
        )
    }
    
    private fun createDefaultConversation(conversationId: String, currentUser: User, chatUser: User): List<Message> {
        return listOf(
            Message(
                id = "msg_default_1",
                content = "你好",
                type = MessageType.TEXT,
                sender = chatUser,
                receiver = currentUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 3600000)
            ),
            Message(
                id = "msg_default_2",
                content = "你好！",
                type = MessageType.TEXT,
                sender = currentUser,
                receiver = chatUser,
                conversationId = conversationId,
                isRead = true,
                createdAt = Date(System.currentTimeMillis() - 1800000)
            )
        )
    }

    override fun sendTextMessage(content: String, receiverId: String) {
        if (content.isBlank() || currentUser == null) return
        
        presenterScope.launch {
            try {
                val receiver = users.find { it.id == receiverId } ?: return@launch
                
                val newMessage = Message(
                    id = "msg_${System.currentTimeMillis()}",
                    content = content,
                    type = MessageType.TEXT,
                    sender = currentUser!!,
                    receiver = receiver,
                    conversationId = "conv_${currentUser!!.id}_$receiverId",
                    isRead = false,
                    createdAt = Date()
                )
                
                messages = messages + newMessage
                view?.showMessageSent(newMessage)
                view?.scrollToBottom()
                
                // Save to both assets and internal storage
                messageStorage.saveMessageToAssets(newMessage)
                messageStorage.saveMessageToInternalStorage(newMessage)
                
            } catch (e: Exception) {
                view?.showError("Failed to send message: ${e.message}")
            }
        }
    }

    override fun sendEmojiMessage(emoji: String, receiverId: String) {
        if (currentUser == null) return
        
        presenterScope.launch {
            try {
                val receiver = users.find { it.id == receiverId } ?: return@launch
                
                val newMessage = Message(
                    id = "msg_${System.currentTimeMillis()}",
                    content = emoji,
                    type = MessageType.EMOJI,
                    sender = currentUser!!,
                    receiver = receiver,
                    conversationId = "conv_${currentUser!!.id}_$receiverId",
                    isRead = false,
                    emoji = emoji,
                    createdAt = Date()
                )
                
                messages = messages + newMessage
                view?.showMessageSent(newMessage)
                view?.scrollToBottom()
                
                // Save to both assets and internal storage
                messageStorage.saveMessageToAssets(newMessage)
                messageStorage.saveMessageToInternalStorage(newMessage)
                
            } catch (e: Exception) {
                view?.showError("Failed to send emoji: ${e.message}")
            }
        }
    }

    override fun sendImageMessage(imagePath: String, receiverId: String) {
        if (currentUser == null) return
        
        presenterScope.launch {
            try {
                val receiver = users.find { it.id == receiverId } ?: return@launch
                
                val newMessage = Message(
                    id = "msg_${System.currentTimeMillis()}",
                    content = "[图片]",
                    type = MessageType.IMAGE,
                    sender = currentUser!!,
                    receiver = receiver,
                    conversationId = "conv_${currentUser!!.id}_$receiverId",
                    isRead = false,
                    images = listOf(imagePath),
                    createdAt = Date()
                )
                
                messages = messages + newMessage
                view?.showMessageSent(newMessage)
                view?.scrollToBottom()
                
                // Save to both assets and internal storage
                messageStorage.saveMessageToAssets(newMessage)
                messageStorage.saveMessageToInternalStorage(newMessage)
                
            } catch (e: Exception) {
                view?.showError("Failed to send image: ${e.message}")
            }
        }
    }

    override fun markMessagesAsRead(conversationId: String) {
        // Mark messages as read - in a real app this would update the database
    }

    override fun onBackClicked() {
        // Handle back navigation
    }

    override fun onMoreOptionsClicked() {
        // Handle more options menu
    }
    
}