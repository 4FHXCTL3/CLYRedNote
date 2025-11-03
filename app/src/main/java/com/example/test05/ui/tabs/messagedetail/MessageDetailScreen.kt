package com.example.test05.ui.tabs.messagedetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import com.example.CLYRedNote.model.Message
import com.example.CLYRedNote.model.MessageType
import com.example.CLYRedNote.model.User
import com.example.test05.presenter.MessageDetailPresenter
import com.example.test05.utils.JsonDataLoader
import com.example.test05.utils.MessageStorage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageDetailScreen(
    userId: String,
    conversationId: String = "conv_user_current_$userId",
    onBackClicked: () -> Unit
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val messageStorage = remember { MessageStorage(context) }
    val presenter = remember { MessageDetailPresenter(dataLoader, messageStorage) }
    
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var chatUser by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var messageInput by remember { mutableStateOf("") }
    
    val listState = rememberLazyListState()

    val view = object : MessageDetailContract.View {
        override fun showMessages(newMessages: List<Message>) {
            messages = newMessages
        }

        override fun showMessageSent(message: Message) {
            messages = messages + message
            messageInput = ""
        }

        override fun showLoading(loading: Boolean) {
            isLoading = loading
        }

        override fun showError(message: String) {
            errorMessage = message
        }

        override fun scrollToBottom() {
            // Scroll to bottom will be handled by LaunchedEffect
        }

        override fun updateUserInfo(user: User) {
            chatUser = user
        }
    }

    LaunchedEffect(Unit) {
        presenter.attachView(view)
        presenter.loadUserInfo(userId)
        presenter.loadMessages(conversationId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
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
        // Top Bar
        TopBarWithUserInfo(
            user = chatUser,
            onBackClicked = {
                presenter.onBackClicked()
                onBackClicked()
            },
            onMoreOptionsClicked = { presenter.onMoreOptionsClicked() }
        )

        // Messages Area
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                if (message.type == MessageType.SYSTEM) {
                    SystemMessageItem(message = message)
                } else {
                    MessageItem(
                        message = message,
                        isCurrentUser = message.sender.id == "user_current"
                    )
                }
            }
        }

        // Emoji Recommendation Bar
        EmojiRecommendationBar(
            onEmojiClicked = { emoji ->
                presenter.sendEmojiMessage(emoji, userId)
            }
        )

        // Bottom Input Area
        MessageInputArea(
            messageInput = messageInput,
            onMessageInputChanged = { messageInput = it },
            onSendClicked = {
                if (messageInput.isNotBlank()) {
                    presenter.sendTextMessage(messageInput, userId)
                }
            },
            onVoiceClicked = { /* TODO: Voice input */ },
            onEmojiClicked = { /* TODO: Emoji picker */ },
            onMoreClicked = { /* TODO: More options */ }
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Red)
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
private fun TopBarWithUserInfo(
    user: User?,
    onBackClicked: () -> Unit,
    onMoreOptionsClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(
            onClick = onBackClicked,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // User Avatar
        if (user != null) {
            UserAvatar(
                avatarName = user.avatar,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // User Name
            Text(
                text = user.nickname,
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // More Options Button
        IconButton(
            onClick = onMoreOptionsClicked,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SystemMessageItem(
    message: Message
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message.content,
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun MessageItem(
    message: Message,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isCurrentUser) {
            // Other user's avatar
            UserAvatar(
                avatarName = message.sender.avatar,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Message Bubble
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentUser) Color(0xFF007AFF) else Color(0xFFF0F0F0)
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 16.dp
            )
        ) {
            when (message.type) {
                MessageType.TEXT -> {
                    Text(
                        text = message.content,
                        color = if (isCurrentUser) Color.White else Color.Black,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                MessageType.EMOJI -> {
                    Text(
                        text = message.emoji ?: message.content,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                MessageType.IMAGE -> {
                    ImageMessage(imagePath = message.images.firstOrNull())
                }
                else -> {
                    Text(
                        text = message.content,
                        color = if (isCurrentUser) Color.White else Color.Black,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        if (isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // Current user's avatar
            UserAvatar(
                avatarName = message.sender.avatar,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun ImageMessage(imagePath: String?) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(imagePath) {
        if (!imagePath.isNullOrEmpty()) {
            try {
                val inputStream = context.assets.open(imagePath)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                imageBitmap = bitmap?.asImageBitmap()
            } catch (e: Exception) {
                imageBitmap = null
            }
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = "Image Message",
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("📷", fontSize = 40.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun EmojiRecommendationBar(
    onEmojiClicked: (String) -> Unit
) {
    val emojis = listOf("hello", "谢谢宝", "呢", "在干嘛", "真漂亮")
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(emojis) { emoji ->
            Card(
                modifier = Modifier.clickable { onEmojiClicked(emoji) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = emoji,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun MessageInputArea(
    messageInput: String,
    onMessageInputChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onVoiceClicked: () -> Unit,
    onEmojiClicked: () -> Unit,
    onMoreClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Voice Input Button
        IconButton(
            onClick = onVoiceClicked,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Voice",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        // Message Input Field
        OutlinedTextField(
            value = messageInput,
            onValueChange = onMessageInputChanged,
            placeholder = { Text("发消息...", color = Color.Gray) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Gray
            ),
            shape = RoundedCornerShape(20.dp),
            maxLines = 3
        )

        // Emoji Button
        IconButton(
            onClick = onEmojiClicked,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "Emoji",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        // More Options Button
        IconButton(
            onClick = if (messageInput.isNotBlank()) onSendClicked else onMoreClicked,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = if (messageInput.isNotBlank()) Icons.Default.Send else Icons.Default.Add,
                contentDescription = if (messageInput.isNotBlank()) "Send" else "More",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun UserAvatar(
    avatarName: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap = remember(avatarName) {
        try {
            if (avatarName != null) {
                val inputStream = context.assets.open(avatarName)
                BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } ?: run {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default Avatar",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}