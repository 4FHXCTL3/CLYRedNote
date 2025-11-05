package com.example.test05.ui.tabs.following

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import com.example.test05.presenter.FollowingTabPresenter
import com.example.test05.utils.JsonDataLoader

@Composable
fun FollowingTabScreen(
    onBackClicked: () -> Unit = {},
    onUserClicked: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val dataStorage = remember { com.example.test05.utils.DataStorage(context) }
    val presenter = remember { FollowingTabPresenter(dataLoader, dataStorage) }
    
    var selectedTab by remember { mutableStateOf(FollowingTabType.FOLLOWING) }
    var searchText by remember { mutableStateOf("") }
    var followingUsers by remember { mutableStateOf<List<FollowingUser>>(emptyList()) }
    var followingCount by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf("全部") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val view = object : FollowingTabContract.View {
        override fun showFollowList(follows: List<FollowingUser>) {
            followingUsers = follows
        }

        override fun showLoading(loading: Boolean) {
            isLoading = loading
        }

        override fun showError(message: String) {
            errorMessage = message
        }

        override fun updateFollowStatus(userId: String, isFollowed: Boolean) {
            followingUsers = followingUsers.map { user ->
                if (user.user.id == userId) {
                    user.copy(isFollowing = isFollowed)
                } else {
                    user
                }
            }
        }

        override fun showUnfollowSuccess(userId: String) {
            // Show success message if needed
        }

        override fun updateSearchResults(users: List<FollowingUser>) {
            followingUsers = users
        }

        override fun updateFollowingCount(count: Int) {
            followingCount = count
        }

        override fun showTabContent(tabType: FollowingTabType, users: List<FollowingUser>) {
            followingUsers = users
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
        // Top Bar with Tabs in same row
        TopBarWithTabs(
            selectedTab = selectedTab,
            onBackClicked = onBackClicked,
            onTabSelected = { 
                selectedTab = it
                presenter.onTabSelected(it)
            }
        )
        
        // Search Bar
        SearchBar(
            searchText = searchText,
            onSearchTextChange = { 
                searchText = it
                presenter.onSearchTextChanged(it)
            }
        )
        
        // My Following Section (only show for FOLLOWING tab)
        if (selectedTab == FollowingTabType.FOLLOWING) {
            MyFollowingSection(
                followingCount = followingCount,
                selectedCategory = selectedCategory,
                onCategorySelected = { 
                    selectedCategory = it
                    presenter.onCategorySelected(it)
                },
                onSortClicked = { presenter.onSortOptionSelected("综合排序") }
            )
        }
        
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
            // Following List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(followingUsers) { followingUser ->
                    FollowingUserItem(
                        followingUser = followingUser,
                        onUnfollowClicked = { presenter.onUnfollowClicked(it) },
                        onUserClicked = { userId ->
                            presenter.onUserClicked(userId)
                            onUserClicked(userId)
                        },
                        onMoreClicked = { presenter.onMoreClicked(it) }
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
private fun TopBarWithTabs(
    selectedTab: FollowingTabType,
    onBackClicked: () -> Unit,
    onTabSelected: (FollowingTabType) -> Unit
) {
    val tabs = listOf(
        FollowingTabType.MUTUAL to "互相关注",
        FollowingTabType.FOLLOWING to "关注",
        FollowingTabType.FOLLOWERS to "粉丝",
        FollowingTabType.RECOMMENDED to "推荐"
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
                "搜索已关注的人",
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
        trailingIcon = {
            if (searchText.isNotEmpty()) {
                IconButton(onClick = { onSearchTextChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp)
    )
}

@Composable
private fun MyFollowingSection(
    followingCount: Int,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onSortClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Following count and sort
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "我的关注 $followingCount",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSortClicked() }
            ) {
                Text(
                    text = "综合排序",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Sort",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Category tabs
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CategoryChip(
                text = "全部",
                isSelected = selectedCategory == "全部",
                onClick = { onCategorySelected("全部") }
            )
            CategoryChip(
                text = "商家",
                isSelected = selectedCategory == "商家",
                onClick = { onCategorySelected("商家") }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = if (isSelected) Color.Red else Color.Gray,
        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
private fun FollowingUserItem(
    followingUser: FollowingUser,
    onUnfollowClicked: (String) -> Unit,
    onUserClicked: (String) -> Unit,
    onMoreClicked: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClicked(followingUser.user.id) },
        verticalAlignment = Alignment.Top
    ) {
        // Avatar
        UserAvatar(
            avatarName = followingUser.user.avatar,
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
                    text = followingUser.user.nickname,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                
                if (followingUser.user.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "✓",
                        fontSize = 12.sp,
                        color = Color.Blue
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = followingUser.description ?: "暂无简介",
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            if (followingUser.unreadNoteCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${followingUser.unreadNoteCount}条未看笔记",
                    fontSize = 11.sp,
                    color = Color.Red
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Action buttons
        Column {
            if (followingUser.isFollowing) {
                Button(
                    onClick = { onUnfollowClicked(followingUser.user.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color.Gray
                    ),
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
                    onClick = { /* Handle follow */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = "关注",
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            IconButton(
                onClick = { onMoreClicked(followingUser.user.id) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
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