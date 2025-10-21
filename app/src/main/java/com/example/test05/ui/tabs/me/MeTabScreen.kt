package com.example.test05.ui.tabs.me

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CLYRedNote.model.Collection
import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.User
import com.example.test05.presenter.MeTabPresenter
import com.example.test05.utils.JsonDataLoader

@Composable
fun MeTabScreen(
    onFollowingClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val presenter = remember { MeTabPresenter(dataLoader) }
    
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

    DisposableEffect(Unit) {
        onDispose {
            presenter.detachView()
        }
    }

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
                onEditProfile = { presenter.onEditProfileClicked() },
                onSettings = { presenter.onSettingsClicked() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Section
        UserStatsSection(
            followingCount = followingCount,
            followerCount = followerCount,
            likesAndCollections = likesAndCollections,
            onFollowingClicked = onFollowingClicked
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action Cards Section
        ActionCardsSection()

        Spacer(modifier = Modifier.height(16.dp))

        // Notes Tabs Section
        NotesTabsSection(
            selectedTab = selectedTab,
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
            0 -> NotesContent(notes)
            1 -> CollectionsContent(collections)
            2 -> LikedNotesContent(likedNotes)
        }

        // Bottom content section
        Spacer(modifier = Modifier.weight(1f))
        
        // Share section
        ShareSection()
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
    onFollowingClicked: () -> Unit = {}
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
            StatItem(count = followerCount, label = "粉丝")
            Spacer(modifier = Modifier.width(32.dp))
            StatItem(count = likesAndCollections, label = "获赞与收藏")
        }

        // Action Buttons
        Row {
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                modifier = Modifier.height(40.dp)
            ) {
                Text("编辑资料", color = Color.Black, fontSize = 12.sp)
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = { },
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
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("笔记", "收藏", "赞过")
    val tabCounts = listOf("公开 0", "私密 4", "合集 0")
    
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
                if (index < tabCounts.size) {
                    Text(
                        text = tabCounts[index],
                        color = Color.Gray,
                        fontSize = 12.sp
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
private fun NotesContent(notes: List<Note>) {
    if (notes.isEmpty()) {
        EmptyContentPlaceholder()
    } else {
        LazyColumn {
            items(notes) { note ->
                NoteItem(note = note)
            }
        }
    }
}

@Composable
private fun CollectionsContent(collections: List<Collection>) {
    if (collections.isEmpty()) {
        EmptyContentPlaceholder()
    } else {
        LazyColumn {
            items(collections) { collection ->
                Text(
                    text = collection.folderName ?: "Unknown",
                    color = Color.Black,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun LikedNotesContent(notes: List<Note>) {
    if (notes.isEmpty()) {
        EmptyContentPlaceholder()
    } else {
        LazyColumn {
            items(notes) { note ->
                NoteItem(note = note)
            }
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
private fun ShareSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📷",
            fontSize = 60.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "分享你的大学出游日常",
            color = Color.Gray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
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