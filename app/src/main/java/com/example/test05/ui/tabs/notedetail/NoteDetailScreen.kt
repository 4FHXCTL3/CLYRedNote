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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.ui.text.style.TextAlign
import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.Comment
import com.example.CLYRedNote.model.InteractionType
import com.example.CLYRedNote.model.ExitType
import com.example.CLYRedNote.model.SourceType
import com.example.test05.presenter.NoteDetailPresenter
import com.example.test05.utils.JsonDataLoader
import com.example.test05.utils.BrowsingHistoryManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NoteDetailScreen(
    noteId: String,
    onBackClicked: () -> Unit,
    sourceType: SourceType = SourceType.DIRECT
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val dataStorage = remember { com.example.test05.utils.DataStorage(context) }
    val presenter = remember { NoteDetailPresenter(dataLoader, dataStorage) }
    val browsingHistoryManager = remember { BrowsingHistoryManager(context) }
    
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
    var showShareBottomSheet by remember { mutableStateOf(false) }

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
        presenter.setSourceType(sourceType)  // 设置来源类型
        presenter.loadNoteDetail(noteId)

        // 开始记录浏览历史
        browsingHistoryManager.startBrowsingSession(noteId)
    }

    DisposableEffect(Unit) {
        onDispose {
            presenter.detachView()
            
            // 结束浏览会话并保存历史记录
            browsingHistoryManager.endBrowsingSession(
                note = note,
                exitType = ExitType.NORMAL,
                readingProgress = 0.8f // 假设阅读了80%，实际可以根据滚动位置计算
            )
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
                            onBackClicked = {
                                browsingHistoryManager.endBrowsingSession(
                                    note = note,
                                    exitType = ExitType.BACK_PRESS,
                                    readingProgress = 0.8f
                                )
                                onBackClicked()
                            },
                            onFollowClicked = { 
                                presenter.onFollowClicked(noteData.author.id)
                                browsingHistoryManager.recordInteraction(
                                    InteractionType.FOLLOW,
                                    targetId = noteData.author.id,
                                    parameters = mapOf("isFollowed" to !isFollowing)
                                )
                            },
                            onShareClicked = { 
                                showShareBottomSheet = true
                                browsingHistoryManager.recordInteraction(
                                    InteractionType.SHARE,
                                    parameters = mapOf("action" to "openShareDialog")
                                )
                            }
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
                                title = noteData.title,
                                content = noteData.content,
                                topics = noteData.topics,
                                tags = noteData.tags,
                                createdAt = noteData.createdAt,
                                location = noteData.location?.name ?: "湖北"
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        // Comments Section Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "共 ${comments.size} 条评论",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "More options",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
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
                    browsingHistoryManager.recordInteraction(
                        InteractionType.COMMENT,
                        parameters = mapOf("commentText" to commentText)
                    )
                },
                isLiked = isLiked,
                isCollected = isCollected,
                likeCount = likeCount,
                commentCount = noteData.commentCount,
                onLikeClicked = { 
                    presenter.onLikeClicked(noteData.id)
                    browsingHistoryManager.recordInteraction(
                        InteractionType.LIKE,
                        parameters = mapOf("isLiked" to !isLiked)
                    )
                },
                onCollectClicked = { 
                    presenter.onCollectClicked(noteData.id)
                    browsingHistoryManager.recordInteraction(
                        InteractionType.COLLECT,
                        parameters = mapOf("isCollected" to !isCollected)
                    )
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

    // Share Bottom Sheet
    if (showShareBottomSheet) {
        ShareBottomSheet(
            onDismiss = { showShareBottomSheet = false },
            onShareClicked = {
                // Handle share action - save share record
                presenter.onShareClicked(noteId)
                showShareBottomSheet = false
            },
            onDislikeClicked = {
                // Handle dislike action - could be sent to presenter
                presenter.onDislikeClicked(noteId)
                browsingHistoryManager.recordInteraction(
                    InteractionType.REPORT,
                    parameters = mapOf("action" to "dislike")
                )
            }
        )
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
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Author Name
        Text(
            text = note.author.nickname,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        
        // Follow Button - styled like reference
        OutlinedButton(
            onClick = onFollowClicked,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isFollowing) Color.Gray.copy(alpha = 0.1f) else Color.White,
                contentColor = if (isFollowing) Color.Gray else Color.Red
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, 
                if (isFollowing) Color.Gray else Color.Red
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.height(36.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isFollowing) "已关注" else "关注",
                fontSize = 14.sp
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
    title: String,
    content: String,
    topics: List<String>,
    tags: List<String>,
    createdAt: Date,
    location: String
) {
    Column {
        // Note Title (if different from content)
        if (title.isNotEmpty() && title != content) {
            Text(
                text = title,
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 26.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Note Content
        Text(
            text = content,
            color = Color.Black,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Topic Tags
        if (topics.isNotEmpty() || tags.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(topics) { topic ->
                    TopicTag(text = topic)
                }
                items(tags) { tag ->
                    TopicTag(text = "#$tag")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Time and Location Info
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "今天 ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(createdAt)}",
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = location,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun TopicTag(text: String) {
    Text(
        text = text,
        color = Color(0xFF0066CC),
        fontSize = 14.sp,
        modifier = Modifier.clickable { /* Handle topic click */ }
    )
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
    onSendClicked: () -> Unit,
    isLiked: Boolean,
    isCollected: Boolean,
    likeCount: Int,
    commentCount: Int,
    onLikeClicked: () -> Unit,
    onCollectClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // Bottom action bar from screenshot
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Comment input field with send button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        Color.Gray.copy(alpha = 0.1f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = onCommentTextChanged,
                    placeholder = { Text("说点什么...", color = Color.Gray, fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 1,
                    singleLine = true
                )
                
                if (commentText.isNotBlank()) {
                    IconButton(
                        onClick = onSendClicked,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "发送评论",
                            tint = Color(0xFFFF6B9D),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Like button with count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onLikeClicked() }
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = likeCount.toString(),
                    color = if (isLiked) Color.Red else Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Collect button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onCollectClicked() }
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Collect",
                    tint = if (isCollected) Color(0xFFFFD700) else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "收藏",
                    color = if (isCollected) Color(0xFFFFD700) else Color.Gray,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Comment button with count
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Comment",
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = commentCount.toString(),
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ShareBottomSheet(
    onDismiss: () -> Unit,
    onShareClicked: () -> Unit = {},
    onDislikeClicked: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White,
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(16.dp)
                .clickable(enabled = false) { }
        ) {
            // Title and close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "分享至",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Recent contacts row
            RecentContactsRow(onContactClicked = onShareClicked)

            Spacer(modifier = Modifier.height(24.dp))

            // Social platforms row
            SocialPlatformsRow(onPlatformClicked = onShareClicked)

            Spacer(modifier = Modifier.height(24.dp))

            // Other options row
            OtherOptionsRow(
                onShareClicked = onShareClicked,
                onDislikeClicked = onDislikeClicked
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RecentContactsRow(onContactClicked: () -> Unit = {}) {
    Column {
        Text(
            text = "最近联系人",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(5) { index ->
                ShareContactItem(
                    name = when (index) {
                        0 -> "张雨萱"
                        1 -> "李思凡"
                        2 -> "王浩然"
                        3 -> "陈梦琪"
                        4 -> "刘振宇"
                        else -> "联系人${index + 1}"
                    },
                    avatarImage = when (index) {
                        0 -> "image/scenery1.jpg"
                        1 -> "image/scenery2.jpg"
                        2 -> "image/scenery3.jpg"
                        3 -> "image/scenery4.jpg"
                        4 -> "image/scenery5.jpg"
                        else -> null
                    },
                    isOnline = index < 3,
                    onClick = onContactClicked
                )
            }
        }
    }
}

@Composable
private fun ShareContactItem(
    name: String,
    avatarImage: String? = null,
    isOnline: Boolean = false,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    
    // Load image from assets
    LaunchedEffect(avatarImage) {
        if (!avatarImage.isNullOrEmpty()) {
            try {
                val inputStream = context.assets.open(avatarImage)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                imageBitmap = bitmap?.asImageBitmap()
            } catch (e: Exception) {
                imageBitmap = null
            }
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = name,
                    tint = Color.Gray,
                    modifier = Modifier.size(56.dp)
                )
            }
            
            if (isOnline) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.Green, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = name,
            fontSize = 12.sp,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(64.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SocialPlatformsRow(onPlatformClicked: () -> Unit = {}) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        items(listOf(
            ShareOption("私信好友", Icons.Default.Send, Color.Red),
            ShareOption("微信好友", Icons.Default.Email, Color.Green),
            ShareOption("朋友圈", Icons.Default.Person, Color.Green),
            ShareOption("QQ好友", Icons.Default.Person, Color.Blue),
            ShareOption("QQ空间", Icons.Default.Star, Color.Yellow)
        )) { option ->
            ShareOptionItem(
                option = option,
                onClick = onPlatformClicked
            )
        }
    }
}

@Composable
private fun OtherOptionsRow(
    onShareClicked: () -> Unit = {},
    onDislikeClicked: () -> Unit = {}
) {
    var isDisliked by remember { mutableStateOf(false) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        items(listOf(
            ShareOption("创群分享", Icons.Default.Person, Color.Gray),
            ShareOption("生成分享图", Icons.Default.Add, Color.Gray),
            ShareOption("复制链接", Icons.Default.Share, Color.Gray),
            ShareOption("为ta加热", Icons.Default.Favorite, Color.Gray),
            ShareOption("不喜欢", Icons.Default.Close, if (isDisliked) Color.Red else Color.Gray)
        )) { option ->
            ShareOptionItem(
                option = option,
                onClick = {
                    if (option.title == "不喜欢") {
                        isDisliked = !isDisliked
                        onDislikeClicked()
                    } else {
                        // All other options trigger share action
                        onShareClicked()
                    }
                }
            )
        }
    }
}

@Composable
private fun ShareOptionItem(
    option: ShareOption,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(option.backgroundColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.title,
                tint = option.backgroundColor,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = option.title,
            fontSize = 12.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

data class ShareOption(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val backgroundColor: Color
)