package com.example.test05.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test05.ui.tabs.me.MeTabScreen
import com.example.test05.ui.tabs.home.HomeTabScreen
import com.example.test05.ui.tabs.market.MarketTabScreen
import com.example.test05.ui.tabs.messages.MessagesTabScreen
import com.example.test05.ui.tabs.post.PostTabScreen
import com.example.test05.ui.tabs.notedetail.NoteDetailScreen
import com.example.test05.ui.tabs.cart.CartScreen
import com.example.test05.ui.tabs.search.SearchTabScreen
import com.example.test05.ui.tabs.following.FollowingTabScreen
import com.example.test05.ui.tabs.bloggerdetail.BloggerDetailScreen
import com.example.test05.ui.tabs.fan.FanTabScreen
import com.example.test05.ui.tabs.fan.FanTabType
import com.example.test05.ui.tabs.commentat.CommentAtTabScreen
import com.example.test05.ui.tabs.profileedit.ProfileEditScreen
import com.example.test05.ui.tabs.searchdetail.SearchDetailScreen
import com.example.test05.ui.tabs.postnext.PostNextScreen
import com.example.test05.ui.tabs.messagedetail.MessageDetailScreen
import com.example.test05.ui.tabs.settings.SettingsScreen
import com.example.test05.ui.tabs.accountsecurity.AccountSecurityScreen

@Composable
fun MainNavigation() {
    var selectedTab by remember { mutableIntStateOf(0) } // Start with HomeTab selected
    var currentNoteId by remember { mutableStateOf<String?>(null) }
    var showNoteDetail by remember { mutableStateOf(false) }
    var showCart by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showFollowing by remember { mutableStateOf(false) }
    var showBloggerDetail by remember { mutableStateOf(false) }
    var currentBloggerId by remember { mutableStateOf<String?>(null) }
    var showFanTab by remember { mutableStateOf(false) }
    var showCommentAt by remember { mutableStateOf(false) }
    var showProfileEdit by remember { mutableStateOf(false) }
    var showSearchDetail by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showPostNext by remember { mutableStateOf(false) }
    var postNextData by remember { mutableStateOf<com.example.test05.ui.tabs.postnext.PostData?>(null) }
    var showMessageDetail by remember { mutableStateOf(false) }
    var currentMessageUserId by remember { mutableStateOf<String?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var showAccountSecurity by remember { mutableStateOf(false) }
    var homeTabShowingNoteDetail by remember { mutableStateOf(false) }
    var meTabShowingNoteDetail by remember { mutableStateOf(false) }
    var meTabRefreshKey by remember { mutableIntStateOf(0) }
    
    // Navigation stack state to handle proper back navigation
    var fromSearchDetail by remember { mutableStateOf(false) }
    var fromSearchTab by remember { mutableStateOf(false) }
    var fromMessageDetail by remember { mutableStateOf(false) }
    var fromPostNext by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Content
        Box(modifier = Modifier.weight(1f)) {
            if (showPostNext && postNextData != null) {
                PostNextScreen(
                    postData = postNextData!!,
                    onBackClicked = {
                        showPostNext = false
                        postNextData = null
                        selectedTab = 2
                    },
                    onNavigateToNoteDetail = { noteId ->
                        showPostNext = false
                        postNextData = null
                        currentNoteId = noteId
                        fromPostNext = true
                        showNoteDetail = true
                    }
                )
            } else if (showSearchDetail) {
                SearchDetailScreen(
                    initialQuery = searchQuery,
                    onBackClicked = { 
                        showSearchDetail = false
                        searchQuery = ""
                        fromSearchDetail = false
                    }
                )
            } else if (showProfileEdit) {
                ProfileEditScreen(
                    onBackPressed = { 
                        showProfileEdit = false
                        meTabRefreshKey++ // Trigger MeTab refresh
                    }
                )
            } else if (showCommentAt) {
                CommentAtTabScreen(
                    onBackClicked = { 
                        showCommentAt = false
                    }
                )
            } else if (showBloggerDetail && currentBloggerId != null) {
                BloggerDetailScreen(
                    userId = currentBloggerId!!,
                    onBackClicked = { 
                        showBloggerDetail = false
                        currentBloggerId = null
                    }
                )
            } else if (showFanTab) {
                FanTabScreen(
                    initialTab = FanTabType.FANS,
                    onBackClicked = { 
                        showFanTab = false
                    },
                    onUserClicked = { userId ->
                        currentBloggerId = userId
                        showBloggerDetail = true
                    },
                    onNavigateToFollowing = {
                        showFanTab = false
                        showFollowing = true
                    }
                )
            } else if (showFollowing) {
                FollowingTabScreen(
                    onBackClicked = { 
                        showFollowing = false
                    },
                    onUserClicked = { userId ->
                        currentBloggerId = userId
                        showBloggerDetail = true
                    }
                )
            } else if (showSearch) {
                SearchTabScreen(
                    onBackClicked = { 
                        showSearch = false
                        fromSearchTab = false
                    },
                    onNoteClicked = { noteId ->
                        currentNoteId = noteId
                        fromSearchTab = true
                        showNoteDetail = true
                    },
                    onSearchPerformed = { query ->
                        searchQuery = query
                        showSearch = false
                        showSearchDetail = true
                    }
                )
            } else if (showCart) {
                CartScreen(
                    onBackClicked = { 
                        showCart = false
                    }
                )
            } else if (showAccountSecurity) {
                AccountSecurityScreen(
                    onBackClicked = {
                        showAccountSecurity = false
                        showSettings = true
                    }
                )
            } else if (showSettings) {
                SettingsScreen(
                    onBackClicked = {
                        showSettings = false
                    },
                    onAccountSecurityClicked = {
                        showSettings = false
                        showAccountSecurity = true
                    }
                )
            } else if (showMessageDetail && currentMessageUserId != null) {
                MessageDetailScreen(
                    userId = currentMessageUserId!!,
                    onBackClicked = {
                        showMessageDetail = false
                        currentMessageUserId = null
                    }
                )
            } else if (showNoteDetail && currentNoteId != null && fromSearchTab) {
                NoteDetailScreen(
                    noteId = currentNoteId!!,
                    onBackClicked = { 
                        showNoteDetail = false
                        currentNoteId = null
                        showSearch = true
                        fromSearchTab = false
                    }
                )
            } else if (showNoteDetail && currentNoteId != null && fromPostNext) {
                NoteDetailScreen(
                    noteId = currentNoteId!!,
                    onBackClicked = { 
                        showNoteDetail = false
                        currentNoteId = null
                        fromPostNext = false
                        selectedTab = 0 // Navigate back to home tab
                    }
                )
            } else {
                when (selectedTab) {
                    0 -> HomeTabScreen(
                        onSearchClicked = {
                            showSearch = true
                        },
                        onNoteDetailStateChanged = { isShowing ->
                            homeTabShowingNoteDetail = isShowing
                        }
                    )
                    1 -> MarketTabScreen(
                        onCartClicked = {
                            showCart = true
                        }
                    )
                    2 -> PostTabScreen(
                        onNavigateBack = { selectedTab = 0 },
                        onNavigateToPostNext = { title, content, images ->
                            postNextData = com.example.test05.ui.tabs.postnext.PostData(
                                title = title,
                                content = content,
                                images = images
                            )
                            showPostNext = true
                        }
                    )
                    3 -> MessagesTabScreen(
                        onCommentAtClicked = {
                            showCommentAt = true
                        },
                        onMessageClicked = { userId ->
                            currentMessageUserId = userId
                            showMessageDetail = true
                        }
                    )
                    4 -> MeTabScreen(
                        refreshKey = meTabRefreshKey,
                        onFollowingClicked = {
                            showFollowing = true
                        },
                        onFansClicked = {
                            showFanTab = true
                        },
                        onProfileEditClicked = {
                            showProfileEdit = true
                        },
                        onSettingsClicked = {
                            showSettings = true
                        },
                        onNoteDetailStateChanged = { isShowing ->
                            meTabShowingNoteDetail = isShowing
                        }
                    )
                }
            }
        }

        // Bottom Navigation (隐藏在某些全屏页面中)
        if (!showSearchDetail && !showNoteDetail && !showCart && !showSearch && 
            !showFollowing && !showBloggerDetail && !showFanTab && !showCommentAt && !showProfileEdit && !showPostNext && !showMessageDetail && !showSettings && !showAccountSecurity && 
            !homeTabShowingNoteDetail && !meTabShowingNoteDetail && selectedTab != 2) {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = Icons.Default.Home,
            label = "首页",
            isSelected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        BottomNavItem(
            icon = Icons.Default.ShoppingCart,
            label = "市集",
            isSelected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )
        
        // Special Post button with + icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Red, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = { onTabSelected(2) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Post",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        BottomNavItem(
            icon = Icons.Default.Email,
            label = "消息",
            isSelected = selectedTab == 3,
            onClick = { onTabSelected(3) }
        )
        BottomNavItem(
            icon = Icons.Default.Person,
            label = "我",
            isSelected = selectedTab == 4,
            onClick = { onTabSelected(4) }
        )
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.Black else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            color = if (isSelected) Color.Black else Color.Gray,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun HomeTabPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "首页 - 待开发",
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun MarketTabPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "市集 - 待开发",
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun PostTabPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "发布 - 待开发",
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun MessagesTabPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "消息 - 待开发",
            color = Color.White,
            fontSize = 18.sp
        )
    }
}