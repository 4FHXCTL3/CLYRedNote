package com.example.test05.ui.tabs.fan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.test05.presenter.FanTabPresenter
import com.example.test05.utils.JsonDataLoader

@Composable
fun FanTabScreen(
    initialTab: FanTabType = FanTabType.FANS,
    onBackClicked: () -> Unit = {},
    onUserClicked: (String) -> Unit = {},
    onNavigateToFollowing: () -> Unit = {}
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val dataStorage = remember { com.example.test05.utils.DataStorage(context) }
    val presenter = remember { FanTabPresenter(dataLoader, dataStorage) }
    
    var selectedTab by remember { mutableStateOf(initialTab) }
    var fanUsers by remember { mutableStateOf<List<FanUser>>(emptyList()) }
    var fanCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val view = object : FanTabContract.View {
        override fun showFanList(fans: List<FanUser>) {
            fanUsers = fans
        }

        override fun showLoading(loading: Boolean) {
            isLoading = loading
        }

        override fun showError(message: String) {
            errorMessage = message
        }

        override fun updateFollowStatus(userId: String, isFollowed: Boolean) {
            fanUsers = fanUsers.map { fanUser ->
                if (fanUser.user.id == userId) {
                    fanUser.copy(isFollowingBack = isFollowed)
                } else {
                    fanUser
                }
            }
        }

        override fun showFollowSuccess(userId: String) {
            // Could show a snackbar or toast
        }

        override fun updateFanCount(count: Int) {
            fanCount = count
        }

        override fun showTabContent(tabType: FanTabType, users: List<FanUser>) {
            fanUsers = users
        }

        override fun showRecommendedUsers(users: List<FanUser>) {
            // Could be used for recommendations section
        }
    }

    LaunchedEffect(Unit) {
        presenter.attachView(view)
        presenter.onTabSelected(initialTab)
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
        // Top Bar with Tabs in same row
        TopBarWithTabs(
            selectedTab = selectedTab,
            onBackClicked = onBackClicked,
            onTabSelected = { 
                selectedTab = it
                if (it == FanTabType.FOLLOWING) {
                    // Navigate to FollowingTab
                    onNavigateToFollowing()
                } else {
                    presenter.onTabSelected(it)
                }
            }
        )
        
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
            // Fan List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(fanUsers) { fanUser ->
                    FanUserItem(
                        fanUser = fanUser,
                        onFollowBackClicked = { presenter.onFollowBackClicked(it) },
                        onUserClicked = { userId ->
                            presenter.onUserClicked(userId)
                            onUserClicked(userId)
                        }
                    )
                }
                
                if (selectedTab == FanTabType.FANS && fanUsers.isNotEmpty()) {
                    item {
                        RecommendationSection()
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

@Composable
private fun TopBarWithTabs(
    selectedTab: FanTabType,
    onBackClicked: () -> Unit,
    onTabSelected: (FanTabType) -> Unit
) {
    val tabs = listOf(
        FanTabType.MUTUAL to "互相关注",
        FanTabType.FOLLOWING to "关注",
        FanTabType.FANS to "粉丝",
        FanTabType.RECOMMENDED to "推荐"
    )
    
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
        
        Spacer(modifier = Modifier.width(8.dp))
        
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(tabs) { (tabType, title) ->
                TabItem(
                    text = title,
                    isSelected = selectedTab == tabType,
                    onClick = { onTabSelected(tabType) }
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.Gray,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
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

@Composable
private fun FanUserItem(
    fanUser: FanUser,
    onFollowBackClicked: (String) -> Unit,
    onUserClicked: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClicked(fanUser.user.id) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        UserAvatar(
            avatarName = fanUser.user.avatar,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // User info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fanUser.user.nickname,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                
                if (fanUser.user.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "✓",
                        fontSize = 12.sp,
                        color = Color.Blue
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Stats row
            Row {
                Text(
                    text = "${fanUser.noteCount}篇笔记",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${fanUser.fanCount}粉丝",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Follow Back Button
        if (fanUser.isFollowingBack) {
            Button(
                onClick = { onFollowBackClicked(fanUser.user.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Gray
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(
                    text = "已关注",
                    fontSize = 12.sp
                )
            }
        } else {
            Button(
                onClick = { onFollowBackClicked(fanUser.user.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Red
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(
                    text = "回关",
                    fontSize = 12.sp
                )
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
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun RecommendationSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(1.dp)
                    .weight(1f)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
            
            Text(
                text = "你可能感兴趣的人",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Box(
                modifier = Modifier
                    .height(1.dp)
                    .weight(1f)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "💡 点击上方「推荐」查看更多推荐用户",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}