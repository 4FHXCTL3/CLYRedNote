package com.example.test05.ui.tabs.messages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test05.presenter.MessagesTabPresenter
import com.example.test05.utils.JsonDataLoader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

@Composable
fun MessagesTabScreen(
    onCommentAtClicked: () -> Unit = {},
    onMessageClicked: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val presenter = remember { MessagesTabPresenter(dataLoader) }
    
    var conversations by remember { mutableStateOf<List<ConversationItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val view = object : MessagesTabContract.View {
        override fun showConversations(conversationList: List<ConversationItem>) {
            conversations = conversationList
        }

        override fun showLoading(loading: Boolean) {
            isLoading = loading
        }

        override fun showError(message: String) {
            errorMessage = message
        }

        override fun updateUnreadCount(conversationId: String, count: Int) {
            conversations = conversations.map { conversation ->
                if (conversation.id == conversationId) {
                    conversation.copy(unreadCount = count)
                } else {
                    conversation
                }
            }
        }

        override fun navigateToMessageDetail(userId: String) {
            onMessageClicked(userId)
        }
    }

    LaunchedEffect(Unit) {
        presenter.attachView(view)
        presenter.loadConversations()
    }

    DisposableEffect(Unit) {
        onDispose {
            presenter.detachView()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Header
        TopHeader(
            onSearchClicked = { presenter.onSearchClicked() },
            onAddClicked = { presenter.onAddClicked() }
        )

        // Function Icons Row
        FunctionIconsRow(onCommentAtClicked = onCommentAtClicked)

        // Conversations List
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(conversations) { conversation ->
                    ConversationCard(
                        conversation = conversation,
                        onClick = { 
                            presenter.onConversationClicked(conversation.id)
                            if (conversation.unreadCount > 0) {
                                presenter.markAsRead(conversation.id)
                            }
                        }
                    )
                }
            }
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun TopHeader(
    onSearchClicked: () -> Unit,
    onAddClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(48.dp))
        
        Text(
            text = "消息",
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        Row {
            IconButton(onClick = onSearchClicked) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onAddClicked) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun FunctionIconsRow(
    onCommentAtClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FunctionIcon(
            title = "赞和收藏",
            emoji = "❤️",
            backgroundColor = Color(0xFF8B4513)
        ) { }
        
        FunctionIcon(
            title = "新增关注",
            emoji = "👤",
            backgroundColor = Color(0xFF4682B4)
        ) { }
        
        FunctionIcon(
            title = "评论和@",
            emoji = "💬",
            backgroundColor = Color(0xFF2E8B57)
        ) { onCommentAtClicked() }
    }
}

@Composable
private fun FunctionIcon(
    title: String,
    emoji: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(backgroundColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = Color.Black,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ConversationCard(
    conversation: ConversationItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(getAvatarColor(conversation.id))
        ) {
            if (conversation.isGroup) {
                // Group avatar with multiple faces
                Text(
                    text = "👥",
                    fontSize = 24.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Individual avatar
                Text(
                    text = getAvatarEmoji(conversation.id),
                    fontSize = 24.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (conversation.isGroup) {
                        conversation.groupName ?: "群聊"
                    } else {
                        conversation.user.nickname
                    },
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestamp(conversation.lastMessage.createdAt),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    
                    if (conversation.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = conversation.lastMessage.content.ifEmpty { "[图片]" },
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun getAvatarColor(conversationId: String): Color {
    val colorIndex = conversationId.hashCode().absoluteValue % 6
    return when (colorIndex) {
        0 -> Color(0xFF4A90E2) // Blue
        1 -> Color(0xFFF5A623) // Orange  
        2 -> Color(0xFF7ED321) // Green
        3 -> Color(0xFFD0021B) // Red
        4 -> Color(0xFF9013FE) // Purple
        5 -> Color(0xFF50E3C2) // Teal
        else -> Color(0xFF4A90E2)
    }
}

private fun getAvatarEmoji(conversationId: String): String {
    val emojiIndex = conversationId.hashCode().absoluteValue % 8
    return when (emojiIndex) {
        0 -> "🔔"
        1 -> "💬"
        2 -> "📢"
        3 -> "👨‍🏫"
        4 -> "🏃"
        5 -> "🌟"
        6 -> "📚"
        7 -> "😊"
        else -> "😊"
    }
}

private fun formatTimestamp(date: Date?): String {
    if (date == null) return ""
    
    val now = Date()
    val diffInMillis = now.time - date.time
    val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)
    
    return when {
        diffInDays == 0L -> {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            format.format(date)
        }
        diffInDays == 1L -> "昨天"
        diffInDays < 7L -> {
            val format = SimpleDateFormat("EEEE", Locale.CHINESE)
            format.format(date)
        }
        else -> {
            val format = SimpleDateFormat("MM-dd", Locale.getDefault())
            format.format(date)
        }
    }
}