package com.example.test05.ui.tabs.commentat

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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import com.example.test05.presenter.CommentAtTabPresenter
import com.example.test05.utils.JsonDataLoader
import com.example.test05.utils.DataStorage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CommentAtTabScreen(
    onBackClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val dataStorage = remember { DataStorage(context) }
    val presenter = remember { CommentAtTabPresenter(dataLoader, dataStorage) }
    
    var commentNotifications by remember { mutableStateOf<List<CommentNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var replyTexts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val view = object : CommentAtTabContract.View {
        override fun showCommentNotifications(notifications: List<CommentNotification>) {
            commentNotifications = notifications
        }

        override fun showLoading(loading: Boolean) {
            isLoading = loading
        }

        override fun showError(message: String) {
            errorMessage = message
        }

        override fun updateCommentLikeStatus(commentId: String, isLiked: Boolean, likeCount: Int) {
            commentNotifications = commentNotifications.map { notification ->
                if (notification.comment.id == commentId) {
                    notification.copy(
                        comment = notification.comment.copy(
                            isLiked = isLiked,
                            likeCount = likeCount
                        ),
                        isLiked = isLiked
                    )
                } else {
                    notification
                }
            }
        }

        override fun showReplySuccess() {
            // Clear the reply text after successful reply
        }
        
        override fun updateReplyVisibility(commentId: String, showInput: Boolean) {
            commentNotifications = commentNotifications.map { notification ->
                if (notification.comment.id == commentId) {
                    notification.copy(showReplyInput = showInput)
                } else {
                    notification
                }
            }
        }
        
        override fun addReplyToComment(commentId: String, replyText: String) {
            commentNotifications = commentNotifications.map { notification ->
                if (notification.comment.id == commentId) {
                    notification.copy(
                        replies = notification.replies + replyText,
                        showReplyInput = false
                    )
                } else {
                    notification
                }
            }
            // Clear the reply text
            replyTexts = replyTexts - commentId
        }
    }

    LaunchedEffect(Unit) {
        presenter.attachView(view)
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
        TopBar(onBackClicked = onBackClicked)
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(commentNotifications) { notification ->
                    CommentNotificationItem(
                        notification = notification,
                        replyText = replyTexts[notification.comment.id] ?: "",
                        onReplyTextChanged = { text ->
                            replyTexts = replyTexts + (notification.comment.id to text)
                        },
                        onLikeClicked = { presenter.onCommentLikeClicked(notification.comment.id) },
                        onReplyClicked = { presenter.onReplyClicked(notification.comment.id) },
                        onReplySubmitted = { replyText ->
                            if (replyText.isNotBlank()) {
                                presenter.onReplySubmitted(notification.comment.id, replyText)
                            }
                        }
                    )
                }
                
                // Add "THE END" footer if there are notifications
                if (commentNotifications.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "- THE END -",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        }
                    }
                }
            }
        }

        errorMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
            ) {
                Text(
                    text = message,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    onBackClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClicked,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }
        
        Text(
            text = "收到的评论和@",
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        
        // Empty space for balance
        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun CommentNotificationItem(
    notification: CommentNotification,
    replyText: String,
    onReplyTextChanged: (String) -> Unit,
    onLikeClicked: () -> Unit,
    onReplyClicked: () -> Unit,
    onReplySubmitted: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // User avatar
            UserAvatar(
                avatarName = notification.comment.author.avatar,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // User name with author badge and time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.comment.author.nickname,
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // Check if this is the note author
                    if (notification.originalNote.author.id == notification.comment.author.id) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "作者",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(
                                    Color.Gray.copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        text = formatCommentTime(notification.comment.createdAt),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // "回复了你的评论" text
                Text(
                    text = "回复了你的评论",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Comment content
                Text(
                    text = notification.comment.content,
                    color = Color.Black,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Note content preview in gray box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.Gray.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = notification.originalNote.content,
                        color = Color.Gray,
                        fontSize = 13.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Like button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onLikeClicked() }
                    ) {
                        Icon(
                            imageVector = if (notification.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (notification.isLiked) Color.Red else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "赞",
                            color = if (notification.isLiked) Color.Red else Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                    
                    // Reply button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onReplyClicked() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Reply",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "回复",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Reply input (if visible)
                if (notification.showReplyInput) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = onReplyTextChanged,
                            placeholder = { Text("回复评论...", color = Color.Gray, fontSize = 14.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Gray,
                                unfocusedBorderColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onReplySubmitted(replyText) },
                            enabled = replyText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("发送", fontSize = 14.sp)
                        }
                    }
                }
                
                // Display replies
                if (notification.replies.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    notification.replies.forEach { reply ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Gray.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "你回复：$reply",
                                color = Color.Black,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Note thumbnail on the right
            NoteImage(
                imageName = notification.originalNote.images.firstOrNull() ?: "image/scenery1.jpg",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
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

@Composable
private fun NoteImage(
    imageName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap = remember(imageName) {
        try {
            val inputStream = context.assets.open(imageName)
            BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = modifier.background(Color.Gray.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "Note Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } ?: run {
            Text(
                text = "📷",
                fontSize = 24.sp,
                color = Color.Gray
            )
        }
    }
}

private fun formatCommentTime(date: Date): String {
    val now = Date()
    val diff = now.time - date.time
    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)
    
    return when {
        minutes < 60 -> "${minutes}分钟前"
        hours < 24 -> "${hours}小时前"
        days < 7 -> "${days}天前"
        else -> SimpleDateFormat("MM-dd", Locale.getDefault()).format(date)
    }
}