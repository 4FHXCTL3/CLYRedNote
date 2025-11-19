package com.example.test05.ui.tabs.me

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import java.io.IOException
import com.example.CLYRedNote.model.Collection
import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.User
import com.example.CLYRedNote.model.NoteVisibility
import com.example.test05.presenter.MeTabPresenter
import com.example.test05.utils.JsonDataLoader
import com.example.test05.ui.tabs.notedetail.NoteDetailScreen

// Mock data classes
data class MockNoteItemWithDetails(
    val title: String,
    val folder: String,
    val author: String,
    val likes: String,
    val publishDate: String,
    val imagePath: String
)

data class MockCollectionNoteItem(
    val title: String,
    val folder: String,
    val likes: String,
    val timeAgo: String,
    val imagePath: String,
    val noteId: String
)

data class MockLikedNoteItem(
    val title: String,
    val content: String,
    val author: String,
    val likes: String,
    val imagePath: String,
    val noteId: String
)

@Composable
fun MeTabScreen(
    refreshKey: Int = 0,
    onFollowingClicked: () -> Unit = {},
    onFansClicked: () -> Unit = {},
    onProfileEditClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
    onNoteDetailStateChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val dataStorage = remember { com.example.test05.utils.DataStorage(context) }
    val presenter = remember { MeTabPresenter(dataLoader, dataStorage) }
    
    var currentUser by remember { mutableStateOf<User?>(null) }
    var followingCount by remember { mutableIntStateOf(0) }
    var followerCount by remember { mutableIntStateOf(0) }
    var likesAndCollections by remember { mutableIntStateOf(0) }
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var collections by remember { mutableStateOf<List<Collection>>(emptyList()) }
    var likedNotes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showNoteDetail by remember { mutableStateOf(false) }
    var currentNoteId by remember { mutableStateOf<String?>(null) }

    val view = object : MeTabContract.View {
        override fun showUserProfile(user: User) {
            currentUser = user
        }

        override fun showUserStats(newFollowingCount: Int, newFollowerCount: Int, newLikesAndCollections: Int) {
            followingCount = newFollowingCount
            followerCount = newFollowerCount
            likesAndCollections = newLikesAndCollections
        }

        override fun showNotes(newNotes: List<Note>) {
            notes = newNotes
        }

        override fun showCollections(newCollections: List<Collection>) {
            collections = newCollections
        }

        override fun showLikedNotes(newNotes: List<Note>) {
            likedNotes = newNotes
        }

        override fun showLoading(newIsLoading: Boolean) {
            isLoading = newIsLoading
        }

        override fun showError(message: String) {
            errorMessage = message
        }
    }

    LaunchedEffect(Unit) {
        presenter.attachView(view)
        presenter.loadUserData()
        presenter.loadUserNotes()
    }

    // Refresh when refreshKey changes (e.g., returning from ProfileEdit)
    LaunchedEffect(refreshKey) {
        if (refreshKey > 0) {
            presenter.refreshUserData()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            presenter.detachView()
        }
    }

    LaunchedEffect(showNoteDetail) {
        onNoteDetailStateChanged(showNoteDetail)
    }
    
    if (showNoteDetail && currentNoteId != null) {
        NoteDetailScreen(
            noteId = currentNoteId!!,
            onBackClicked = {
                showNoteDetail = false
                currentNoteId = null
                onNoteDetailStateChanged(false)
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
        // Top Section with User Profile
        currentUser?.let { user ->
            UserProfileSection(
                user = user,
                onEditProfile = onProfileEditClicked,
                onSettings = onSettingsClicked
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Section
        UserStatsSection(
            followingCount = followingCount,
            followerCount = followerCount,
            likesAndCollections = likesAndCollections,
            onFollowingClicked = onFollowingClicked,
            onFansClicked = onFansClicked,
            onEditProfileClicked = onProfileEditClicked,
            onSettings = onSettingsClicked
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action Cards Section
        ActionCardsSection()

        Spacer(modifier = Modifier.height(16.dp))

        // Notes Tabs Section
        NotesTabsSection(
            selectedTab = selectedTab,
            collectionsCount = collections.size,
            onTabSelected = { tabIndex ->
                selectedTab = tabIndex
                when (tabIndex) {
                    0 -> presenter.onNotesTabSelected()
                    1 -> presenter.onCollectionsTabSelected()
                    2 -> presenter.onLikedTabSelected()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

            // Content based on selected tab
            when (selectedTab) {
                0 -> NotesContent(
                    notes = notes, 
                    selectedTab = selectedTab,
                    onNoteClicked = { noteId ->
                        currentNoteId = noteId
                        showNoteDetail = true
                    }
                )
                1 -> CollectionsContent(
                    collections = collections, 
                    selectedTab = selectedTab,
                    onNoteClicked = { noteId ->
                        currentNoteId = noteId
                        showNoteDetail = true
                    }
                )
                2 -> LikedNotesContent(
                    notes = likedNotes, 
                    selectedTab = selectedTab,
                    onNoteClicked = { noteId ->
                        currentNoteId = noteId
                        showNoteDetail = true
                    }
                )
            }
        }
    }
}

@Composable
private fun UserProfileSection(
    user: User,
    onEditProfile: () -> Unit,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Avatar
        Box(
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Avatar",
                tint = Color.Gray,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
            
            // Plus icon overlay
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Edit",
                tint = Color.Black,
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.Yellow, CircleShape)
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // User Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.nickname,
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Dropdown",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Text(
                text = "小红书号: ${user.id}",
                color = Color.Gray,
                fontSize = 12.sp
            )
            
            Text(
                text = "IP属地: 湖北",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        // Action Buttons
        Column {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = Color.Black,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { }
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Bio and gender
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "点击这里，填写简介",
            color = Color.Gray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "♀",
            color = androidx.compose.ui.graphics.Color.Magenta,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun UserStatsSection(
    followingCount: Int,
    followerCount: Int,
    likesAndCollections: Int,
    onFollowingClicked: () -> Unit = {},
    onFansClicked: () -> Unit = {},
    onEditProfileClicked: () -> Unit = {},
    onSettings: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Stats
        Row(modifier = Modifier.weight(1f)) {
            StatItem(
                count = followingCount, 
                label = "关注",
                onClick = onFollowingClicked
            )
            Spacer(modifier = Modifier.width(32.dp))
            StatItem(
                count = followerCount, 
                label = "粉丝",
                onClick = onFansClicked
            )
            Spacer(modifier = Modifier.width(32.dp))
            StatItem(count = likesAndCollections, label = "获赞与收藏")
        }

        // Action Buttons
        Row {
            Button(
                onClick = onEditProfileClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                modifier = Modifier.height(40.dp)
            ) {
                Text("编辑资料", color = Color.Black, fontSize = 12.sp)
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onSettings,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Transparent, CircleShape)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    count: Int, 
    label: String,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        Text(
            text = count.toString(),
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ActionCardsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActionCard(
            title = "创作灵感",
            subtitle = "学创作技灵感",
            modifier = Modifier.weight(1f)
        )
        ActionCard(
            title = "群聊",
            subtitle = "查看详情",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun NotesTabsSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    collectionsCount: Int = 0
) {
    val tabs = listOf("笔记", "收藏", "赞过")
    
    Row(modifier = Modifier.fillMaxWidth()) {
        tabs.forEachIndexed { index, tab ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tab,
                    color = if (selectedTab == index) Color.Black else Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                )
                if (selectedTab == index) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(2.dp)
                            .background(Color.Red)
                    )
                }
            }
        }
        
        // Search icon
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color.Gray,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterVertically)
        )
    }
}

@Composable
private fun NotesContent(notes: List<Note>, selectedTab: Int, onNoteClicked: (String) -> Unit = {}) {
    // Sub-category tabs for Notes section
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("公开", "私密", "合集")
    
    // Filter notes based on visibility
    val publicNotes = notes.filter { it.visibility == NoteVisibility.PUBLIC }
    val privateNotes = notes.filter { 
        it.visibility == NoteVisibility.PRIVATE || 
        it.visibility == NoteVisibility.FRIENDS_ONLY || 
        it.visibility == NoteVisibility.SPECIFIC_FRIENDS 
    }
    
    Column {
        // Sub tabs row - aligned to left
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            subTabs.forEachIndexed { index, tab ->
                val count = when (index) {
                    0 -> publicNotes.size
                    1 -> privateNotes.size
                    else -> 0
                }
                Text(
                    text = "$tab $count",
                    color = if (selectedSubTab == index) Color.Red else Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clickable { selectedSubTab = index }
                )
            }
        }
        
        // Notes grid based on selected tab
        when (selectedSubTab) {
            0 -> {
                // Public notes
                if (publicNotes.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(publicNotes.size) { index ->
                            NoteGridItem(note = publicNotes[index], isPrivate = false, onNoteClicked = onNoteClicked)
                        }
                    }
                } else {
                    EmptyGridContent(text = "还没有发布的公开笔记")
                }
            }
            1 -> {
                // Private notes
                if (privateNotes.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(privateNotes.size) { index ->
                            NoteGridItem(note = privateNotes[index], isPrivate = true, onNoteClicked = onNoteClicked)
                        }
                    }
                } else {
                    EmptyGridContent(text = "还没有发布的私密笔记")
                }
            }
            2 -> {
                // Collections - keep empty for now
                EmptyGridContent(text = "还没有创建的合集")
            }
        }
    }
}

@Composable
private fun CollectionsContent(collections: List<Collection>, selectedTab: Int, onNoteClicked: (String) -> Unit = {}) {
    // Sub-category tabs for Collections section
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("笔记", "专辑")
    
    Column {
        // Statistics row - aligned to left like in screenshot
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "笔记 213",
                color = if (selectedSubTab == 0) Color.Red else Color.Gray,
                fontSize = 14.sp,
                fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clickable { selectedSubTab = 0 }
            )
            Text(
                text = "专辑 21",
                color = if (selectedSubTab == 1) Color.Red else Color.Gray,
                fontSize = 14.sp,
                fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clickable { selectedSubTab = 1 }
            )
        }
        
        // Collections grid
        if (selectedSubTab == 0) {
            // Create mock collection notes using real note IDs
            val mockCollectionNotes = listOf(
                MockCollectionNoteItem("夏日穿搭分享", "穿搭日记", "3.1万", "1天前", "image/scenery1.jpg", "note_001"),
                MockCollectionNoteItem("精致妆容教程", "美妆心得", "2.8万", "3天前", "image/scenery2.jpg", "note_002"),
                MockCollectionNoteItem("旅行打卡记录", "足迹", "1.9万", "1周前", "image/scenery3.jpg", "note_003"),
                MockCollectionNoteItem("美食探店推荐", "吃货日常", "4.2万", "2周前", "image/scenery4.jpg", "note_004"),
                MockCollectionNoteItem("家居装饰灵感", "温馨小窝", "1.5万", "3周前", "image/scenery5.jpg", "note_005"),
                MockCollectionNoteItem("健身打卡记录", "自律生活", "2.1万", "1个月前", "image/scenery6.jpg", "note_006"),
                MockCollectionNoteItem("摄影作品分享", "光影世界", "3.7万", "2个月前", "image/scenery1.jpg", "note_007"),
                MockCollectionNoteItem("读书笔记整理", "精神食粮", "1.2万", "3个月前", "image/scenery2.jpg", "note_008"),
                MockCollectionNoteItem("宠物日常记录", "毛孩子", "5.1万", "4个月前", "image/scenery3.jpg", "note_009")
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(mockCollectionNotes.size) { index ->
                    CollectionNoteGridItem(
                        note = mockCollectionNotes[index],
                        onNoteClicked = onNoteClicked
                    )
                }
            }
        } else {
            EmptyGridContent(text = "还没有收藏的专辑")
        }
    }
}

@Composable
private fun LikedNotesContent(notes: List<Note>, selectedTab: Int, onNoteClicked: (String) -> Unit = {}) {
    // Create mock liked notes
    val mockLikedNotes = listOf(
        MockLikedNoteItem("2025新年快乐", "急救跑者就位｜带你体验长沙马拉松半马赛道", "肉未来", "207", "image/scenery1.jpg", "note_010"),
        MockLikedNoteItem("我们在操场撞到人之后", "运动的时候也要美美哒", "狮山长跑队", "1.5万", "image/scenery2.jpg", "note_011"),
        MockLikedNoteItem("夏日清爽妆容教程", "超详细步骤分享", "美妆达人小王", "5.2万", "image/scenery3.jpg", "note_012"),
        MockLikedNoteItem("周末探店新发现", "这家咖啡厅太有格调了", "探店小分队", "3.8万", "image/scenery4.jpg", "note_013"),
        MockLikedNoteItem("家居改造前后对比", "小空间大变身", "装修小能手", "2.1万", "image/scenery5.jpg", "note_014"),
        MockLikedNoteItem("健身打卡第100天", "坚持的力量", "健身小白", "4.3万", "image/scenery6.jpg", "note_015"),
        MockLikedNoteItem("旅行vlog分享", "三天两夜重庆游", "旅行记录者", "6.7万", "image/scenery1.jpg", "note_016"),
        MockLikedNoteItem("读书分享推荐", "这本书改变了我", "书虫小姐", "1.9万", "image/scenery2.jpg", "note_017"),
        MockLikedNoteItem("宠物日常萌照", "我家橘猫的搞笑日常", "铲屎官日记", "8.1万", "image/scenery3.jpg", "note_018")
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(mockLikedNotes.size) { index ->
            LikedNoteGridItem(
                note = mockLikedNotes[index],
                onNoteClicked = onNoteClicked
            )
        }
    }
}

@Composable
private fun NoteItem(note: Note) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = note.title,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = note.content,
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun EmptyContentPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📷",
            fontSize = 80.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "分享你的大学出游日常",
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
        ) {
            Text("去发布", color = Color.Black)
        }
    }
}


@Composable
private fun CollectionNoteItem(collection: Collection) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    
    // Load image from assets
    LaunchedEffect(collection.note.coverImage) {
        try {
            val imagePath = collection.note.coverImage ?: collection.note.images.firstOrNull()
            if (!imagePath.isNullOrEmpty()) {
                val inputStream = context.assets.open(imagePath)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                imageBitmap = bitmap?.asImageBitmap()
            }
        } catch (e: Exception) {
            // Image loading failed, will show fallback
            imageBitmap = null
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Note image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(if (imageBitmap == null) getCollectionImageColor(collection.folderName ?: "默认") else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = "Note Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback to emoji with color background
                    Text(
                        text = getCollectionEmoji(collection.folderName ?: "默认"),
                        fontSize = 40.sp,
                        color = Color.White
                    )
                }
            }
            
            // Note title
            Text(
                text = collection.note.title,
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

private fun getCollectionImageColor(folderName: String): Color {
    return when (folderName) {
        "科技" -> Color(0xFF2196F3) // Blue
        "穿搭" -> Color(0xFFE91E63) // Pink
        "美妆" -> Color(0xFFFF5722) // Deep Orange
        "旅行" -> Color(0xFF4CAF50) // Green
        "家居" -> Color(0xFF795548) // Brown
        "健身" -> Color(0xFFFF9800) // Orange
        "摄影" -> Color(0xFF9C27B0) // Purple
        "阅读" -> Color(0xFF607D8B) // Blue Grey
        "宠物" -> Color(0xFFFFEB3B) // Yellow
        else -> Color(0xFF9E9E9E) // Grey
    }
}

private fun getCollectionEmoji(folderName: String): String {
    return when (folderName) {
        "科技" -> "💻"
        "穿搭" -> "👗"
        "美妆" -> "💄"
        "旅行" -> "🌍"
        "家居" -> "🏠"
        "健身" -> "💪"
        "摄影" -> "📸"
        "阅读" -> "📚"
        "宠物" -> "🐱"
        else -> "📝"
    }
}

// Helper function to format like count
private fun formatLikeCount(count: Int): String {
    return when {
        count >= 10000 -> "${(count / 10000.0).let { if (it == it.toInt().toDouble()) it.toInt() else String.format("%.1f", it) }}万"
        count >= 1000 -> "${(count / 1000.0).let { if (it == it.toInt().toDouble()) it.toInt() else String.format("%.1f", it) }}k"
        else -> count.toString()
    }
}

@Composable
private fun NoteGridItem(note: Note, isPrivate: Boolean, onNoteClicked: (String) -> Unit = {}) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    
    // Load image from assets
    LaunchedEffect(note.coverImage ?: note.images.firstOrNull()) {
        try {
            val imagePath = note.coverImage ?: note.images.firstOrNull()
            if (!imagePath.isNullOrEmpty()) {
                val inputStream = context.assets.open(imagePath)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                imageBitmap = bitmap?.asImageBitmap()
            }
        } catch (e: Exception) {
            imageBitmap = null
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable { onNoteClicked(note.id) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Image section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = "Note Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📷",
                            fontSize = 32.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                // Privacy indicator for private notes
                if (isPrivate) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when (note.visibility) {
                                NoteVisibility.PRIVATE -> "仅自己可见"
                                NoteVisibility.FRIENDS_ONLY -> "仅好友可见" 
                                NoteVisibility.SPECIFIC_FRIENDS -> "部分好友可见"
                                else -> "私密"
                            },
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            
            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = note.title,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = note.content.ifBlank { "分享生活点滴" },
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Author",
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = note.author.nickname,
                            color = Color.Gray,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = Color.Red,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatLikeCount(note.likeCount),
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivateNoteDetailItem(note: MockNoteItemWithDetails) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    
    // Load image from assets
    LaunchedEffect(note.imagePath) {
        try {
            val inputStream = context.assets.open(note.imagePath)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            imageBitmap = bitmap?.asImageBitmap()
        } catch (e: Exception) {
            imageBitmap = null
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Image section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = "Private Note Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📷",
                            fontSize = 32.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                // Privacy indicator at bottom left
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "仅自己可见",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
            
            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = note.title,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Author",
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = note.author,
                            color = Color.Gray,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = Color.Red,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = note.likes,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = note.publishDate,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun CollectionNoteGridItem(note: MockCollectionNoteItem, onNoteClicked: (String) -> Unit = {}) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    
    // Load image from assets
    LaunchedEffect(note.imagePath) {
        try {
            val inputStream = context.assets.open(note.imagePath)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            imageBitmap = bitmap?.asImageBitmap()
        } catch (e: Exception) {
            imageBitmap = null
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable { 
                onNoteClicked(note.noteId)
            },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Image section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = "Collection Note Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📷",
                            fontSize = 32.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = note.title,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Author",
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = note.folder,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = Color.Red,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = note.likes,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = note.timeAgo,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun LikedNoteGridItem(note: MockLikedNoteItem, onNoteClicked: (String) -> Unit = {}) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    
    // Load image from assets
    LaunchedEffect(note.imagePath) {
        try {
            val inputStream = context.assets.open(note.imagePath)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            imageBitmap = bitmap?.asImageBitmap()
        } catch (e: Exception) {
            imageBitmap = null
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable { 
                onNoteClicked(note.noteId)
            },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Image section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = "Liked Note Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📷",
                            fontSize = 32.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = note.title,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = note.content,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Author",
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = note.author,
                            color = Color.Gray,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = Color.Red,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = note.likes,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyGridContent(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📷",
                fontSize = 48.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}