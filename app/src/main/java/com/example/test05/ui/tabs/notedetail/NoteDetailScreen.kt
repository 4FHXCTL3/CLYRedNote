package com.example.test05.ui.tabs.notedetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import android.graphics.BitmapFactory
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.Comment
import com.example.test05.presenter.NoteDetailPresenter
import com.example.test05.utils.JsonDataLoader
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NoteDetailScreen(
    noteId: String,
    onBackClicked: () -> Unit
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val presenter = remember { NoteDetailPresenter(dataLoader) }
    
    var note by remember { mutableStateOf<Note?>(null) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLiked by remember { mutableStateOf(false) }
    var isCollected by remember { mutableStateOf(false) }
    var isFollowing by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(0) }
    var collectCount by remember { mutableIntStateOf(0) }
    var commentText by remember { mutableStateOf("") }

    val view = object : NoteDetailContract.View {
        override fun showNote(noteData: Note) {
            note = noteData
            isLiked = noteData.isLiked
            isCollected = noteData.isCollected
            likeCount = noteData.likeCount
            collectCount = noteData.collectCount
        }

        override fun showComments(commentList: List<Comment>) {
            comments = commentList
        }

        override fun showLoading(loading: Boolean) {
            isLoading = loading
        }

        override fun showError(message: String) {
            errorMessage = message
        }

        override fun updateLikeStatus(liked: Boolean, count: Int) {
            isLiked = liked
            likeCount = count
        }

        override fun updateCollectStatus(collected: Boolean, count: Int) {
            isCollected = collected
            collectCount = count
        }

        override fun updateFollowStatus(following: Boolean) {
            isFollowing = following
        }

        override fun showCommentAdded(comment: Comment) {
            comments = comments + comment
            commentText = ""
        }

        override fun showCommentLiked(commentId: String, liked: Boolean, count: Int) {
            comments = comments.map { comment ->
                if (comment.id == commentId) {
                    comment.copy(isLiked = liked, likeCount = count)
                } else {
                    comment
                }
            }
        }
    }

    LaunchedEffect(noteId) {
        presenter.attachView(view)
        presenter.loadNoteDetail(noteId)
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
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else {
            note?.let { noteData ->
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        // Top Bar with Author Info
                        TopBarWithAuthor(
                            note = noteData,
                            isFollowing = isFollowing,
                            onBackClicked = onBackClicked,
                            onFollowClicked = { presenter.onFollowClicked(noteData.author.id) },
                            onShareClicked = { presenter.onShareClicked(noteData.id) }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        // Note Images - always show images
                        val imagesToShow = if (noteData.images.isNotEmpty()) {
                            noteData.images
                        } else {
                            // If no images, use default assets
                            listOf("image/scenery1.jpg", "image/scenery2.jpg", "image/scenery3.jpg")
                        }
                        NoteImageGallery(images = imagesToShow)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        // Note Content
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            NoteContent(
                                content = noteData.content,
                                createdAt = noteData.createdAt,
                                location = noteData.location?.name ?: "湖北"
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        // Action Buttons
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            ActionButtons(
                                isLiked = isLiked,
                                isCollected = isCollected,
                                likeCount = likeCount,
                                collectCount = collectCount,
                                commentCount = noteData.commentCount,
                                shareCount = noteData.shareCount,
                                onLikeClicked = { presenter.onLikeClicked(noteData.id) },
                                onCollectClicked = { presenter.onCollectClicked(noteData.id) },
                                onCommentClicked = { /* Scroll to comments */ },
                                onShareClicked = { presenter.onShareClicked(noteData.id) }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        // Comments Section Header
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = "评论 ${comments.size}",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Comments List
                    items(comments) { comment ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            CommentItem(
                                comment = comment,
                                onLikeClicked = { presenter.onCommentLikeClicked(comment.id) },
                                onReplyClicked = { /* Handle reply */ }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // Comment Input
        note?.let { noteData ->
            CommentInputBar(
                commentText = commentText,
                onCommentTextChanged = { commentText = it },
                onSendClicked = { 
                    presenter.onAddComment(noteData.id, commentText)
                }
            )
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
private fun TopBarWithAuthor(
    note: Note,
    isFollowing: Boolean,
    onBackClicked: () -> Unit,
    onFollowClicked: () -> Unit,
    onShareClicked: () -> Unit
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
        
        // Author Avatar
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar",
            tint = Color.Gray,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Author Name
        Text(
            text = note.author.nickname,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        
        // Follow Button
        Button(
            onClick = onFollowClicked,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFollowing) Color.Gray else Color.Red
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isFollowing) "已关注" else "关注",
                color = Color.White,
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Share Button
        IconButton(
            onClick = onShareClicked,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


@Composable
private fun NoteImageGallery(images: List<String>) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    
    Column {
        // Image Pager - Full width
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f) // 4:3 aspect ratio like in screenshot
        ) { page ->
            NoteImage(
                imageName = images[page],
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Page Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(images.size) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (index == pagerState.currentPage) Color.Red else Color.Gray.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
                if (index < images.size - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
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
        modifier = modifier
            .background(Color.Gray.copy(alpha = 0.1f)),
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
                fontSize = 48.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun NoteContent(
    content: String,
    createdAt: Date,
    location: String
) {
    Column {
        Text(
            text = content,
            color = Color.Black,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(createdAt),
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "IP属地：$location",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ActionButtons(
    isLiked: Boolean,
    isCollected: Boolean,
    likeCount: Int,
    collectCount: Int,
    commentCount: Int,
    shareCount: Int,
    onLikeClicked: () -> Unit,
    onCollectClicked: () -> Unit,
    onCommentClicked: () -> Unit,
    onShareClicked: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionButton(
            icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            text = likeCount.toString(),
            tint = if (isLiked) Color.Red else Color.Gray,
            onClick = onLikeClicked
        )
        
        ActionButton(
            icon = Icons.Default.Email,
            text = commentCount.toString(),
            tint = Color.Gray,
            onClick = onCommentClicked
        )
        
        ActionButton(
            icon = if (isCollected) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            text = collectCount.toString(),
            tint = if (isCollected) Color(0xFFFF9800) else Color.Gray,
            onClick = onCollectClicked
        )
        
        ActionButton(
            icon = Icons.Default.Share,
            text = shareCount.toString(),
            tint = Color.Gray,
            onClick = onShareClicked
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            color = tint,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    onLikeClicked: () -> Unit,
    onReplyClicked: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Comment Author Avatar
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Avatar",
                tint = Color.Gray,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Comment Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = comment.author.nickname,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = comment.content,
                    color = Color.Black,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(comment.createdAt),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = "IP属地：湖北",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onLikeClicked() }
                    ) {
                        Icon(
                            imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (comment.isLiked) Color.Red else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        if (comment.likeCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = comment.likeCount.toString(),
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = "回复",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { onReplyClicked() }
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentInputBar(
    commentText: String,
    onCommentTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = commentText,
            onValueChange = onCommentTextChanged,
            placeholder = { Text("说点什么...", color = Color.Gray, fontSize = 14.sp) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(20.dp),
            maxLines = 3
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Button(
            onClick = onSendClicked,
            enabled = commentText.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = "发送",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}