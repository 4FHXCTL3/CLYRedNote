package com.example.test05.ui.tabs.bloggerdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.User
import com.example.test05.presenter.BloggerDetailPresenter
import com.example.test05.utils.JsonDataLoader
import com.example.test05.ui.tabs.notedetail.NoteDetailScreen
import com.example.test05.ui.tabs.messagedetail.MessageDetailScreen

@Composable
fun BloggerDetailScreen(
    userId: String,
    onBackClicked: () -> Unit = {},
    onNoteClicked: (String) -> Unit = {},
    onMessageClicked: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val dataStorage = remember { com.example.test05.utils.DataStorage(context) }
    val presenter = remember { BloggerDetailPresenter(dataLoader, dataStorage) }
    
    var blogger by remember { mutableStateOf<User?>(null) }
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isFollowing by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("全部") }
    var showNoteDetail by remember { mutableStateOf(false) }
    var currentNoteId by remember { mutableStateOf<String?>(null) }
    var showMessageDetail by remember { mutableStateOf(false) }
    var currentMessageUserId by remember { mutableStateOf<String?>(null) }

    val view = object : BloggerDetailContract.View {
        override fun showBloggerInfo(user: User) {
            blogger = user
        }

        override fun showBloggerNotes(notesList: List<Note>) {
            notes = notesList
        }

        override fun showLoading(loading: Boolean) {
            isLoading = loading
        }

        override fun showError(message: String) {
            errorMessage = message
        }

        override fun updateFollowStatus(following: Boolean) {
            isFollowing = following
        }

        override fun showFollowSuccess() {
            // Could show a snackbar or toast
        }

        override fun showUnfollowSuccess() {
            // Could show a snackbar or toast
        }

        override fun navigateToMessage(userId: String) {
            currentMessageUserId = userId
            showMessageDetail = true
        }
    }

    LaunchedEffect(userId) {
        presenter.attachView(view)
        presenter.loadBloggerDetail(userId)
    }

    DisposableEffect(Unit) {
        onDispose {
            presenter.detachView()
        }
    }

    if (showMessageDetail && currentMessageUserId != null) {
        MessageDetailScreen(
            userId = currentMessageUserId!!,
            onBackClicked = {
                showMessageDetail = false
                currentMessageUserId = null
            }
        )
    } else if (showNoteDetail && currentNoteId != null) {
        NoteDetailScreen(
            noteId = currentNoteId!!,
            onBackClicked = {
                showNoteDetail = false
                currentNoteId = null
            },
            sourceType = com.example.CLYRedNote.model.SourceType.USER_PROFILE
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
        // Top Bar
        TopBar(onBackClicked = onBackClicked)
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else {
            blogger?.let { user ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // User Info Section
                        UserInfoSection(
                            user = user,
                            isFollowing = isFollowing,
                            onFollowClicked = { 
                                if (isFollowing) {
                                    presenter.onUnfollowClicked()
                                } else {
                                    presenter.onFollowClicked()
                                }
                            },
                            onMessageClicked = { presenter.onMessageClicked() },
                            onGroupChatClicked = { presenter.onGroupChatClicked() },
                            onEvaluateClicked = { presenter.onEvaluateClicked() }
                        )
                    }
                    
                    item {
                        // Notes Section Header
                        NotesHeader(
                            searchText = searchText,
                            onSearchTextChange = { 
                                searchText = it
                                presenter.onSearchNotes(it)
                            },
                            selectedCategory = selectedCategory,
                            onCategorySelected = { 
                                selectedCategory = it
                                presenter.onFilterByCategory(it)
                            }
                        )
                    }
                    
                    item {
                        // Notes Grid
                        NotesGrid(
                            notes = notes,
                            onNoteClicked = { noteId ->
                                currentNoteId = noteId
                                showNoteDetail = true
                            }
                        )
                    }
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
}

@Composable
private fun TopBar(onBackClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        IconButton(onClick = { /* More options */ }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color.Black
            )
        }
    }
}

@Composable
private fun UserInfoSection(
    user: User,
    isFollowing: Boolean,
    onFollowClicked: () -> Unit,
    onMessageClicked: () -> Unit,
    onGroupChatClicked: () -> Unit,
    onEvaluateClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Avatar and basic info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            UserAvatar(
                avatarName = user.avatar,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.nickname,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = "小红书号: ${user.username}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                if (user.isVerified) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color.Blue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "已认证",
                            fontSize = 12.sp,
                            color = Color.Blue,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                
                Text(
                    text = "IP属地: 北京",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        // Bio
        user.bio?.let { bio ->
            Text(
                text = bio,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        
        // Tags
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            items(listOf("时尚", "穿搭", "生活", "分享")) { tag ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF5F5F5)
                ) {
                    Text(
                        text = tag,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
        
        // Stats and First Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stats Section - Left aligned, not centered
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatItem(label = "关注", count = user.followingCount)
                StatItem(label = "粉丝", count = user.followerCount)
                StatItem(label = "获赞与收藏", count = user.noteCount * 10)
            }
            
            // First Action Buttons - Right aligned
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onFollowClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowing) Color(0xFFF5F5F5) else Color.Red,
                        contentColor = if (isFollowing) Color.Gray else Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (isFollowing) "已关注" else "关注",
                        fontSize = 12.sp
                    )
                }
                
                OutlinedButton(
                    onClick = onMessageClicked,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "私信",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }
        
        // Large Action Buttons - Full width big boxes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                onClick = { onGroupChatClicked() },
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F8F8)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💬",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "群聊",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Card(
                onClick = { onEvaluateClicked() },
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F8F8)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⭐",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "评价",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, count: Int) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = count.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 1.dp)
        )
    }
}

@Composable
private fun NotesHeader(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Column {
        // Notes title and search icon in same row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "笔记",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            IconButton(
                onClick = { /* Search action */ },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        
        // Category Filter
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 12.dp)
        ) {
            items(listOf("全部", "图文", "视频")) { category ->
                CategoryChip(
                    text = category,
                    isSelected = selectedCategory == category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        placeholder = { 
            Text(
                "搜索笔记",
                color = Color.Gray,
                fontSize = 14.sp
            ) 
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Gray,
            unfocusedBorderColor = Color.Gray,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            cursorColor = Color.Black
        ),
        shape = RoundedCornerShape(20.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { keyboardController?.hide() }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    )
}

@Composable
private fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color.Red else Color(0xFFF5F5F5),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (isSelected) Color.White else Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun NotesGrid(
    notes: List<Note>,
    onNoteClicked: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(400.dp) // Set a fixed height
    ) {
        items(notes) { note ->
            NoteItem(
                note = note,
                onClicked = { onNoteClicked(note.id) }
            )
        }
    }
}

@Composable
private fun NoteItem(
    note: Note,
    onClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClicked() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Note Image
            if (note.images.isNotEmpty()) {
                NoteImage(
                    imageName = note.images.first(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.Gray.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📷",
                        fontSize = 40.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Note Info
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = note.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = Color.Red,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = note.likeCount.toString(),
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
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
                modifier = Modifier.size(40.dp)
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

    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = "Note Image",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(8.dp))
        )
    } ?: run {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📷",
                fontSize = 40.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}