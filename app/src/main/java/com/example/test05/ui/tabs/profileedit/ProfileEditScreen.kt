package com.example.test05.ui.tabs.profileedit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CLYRedNote.model.User
import com.example.test05.presenter.ProfileEditPresenter
import com.example.test05.utils.JsonDataLoader

@Composable
fun ProfileEditScreen(
    onBackPressed: () -> Unit = {}
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val presenter = remember { ProfileEditPresenter(dataLoader) }
    
    var currentUser by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val view = object : ProfileEditContract.View {
        override fun showUserProfile(user: User) {
            currentUser = user
        }
        
        override fun showLoading(loading: Boolean) {
            isLoading = loading
        }
        
        override fun showError(message: String) {
            errorMessage = message
        }
        
        override fun showSuccess(message: String) {
            // TODO: Show success snackbar
        }
        
        override fun navigateBack() {
            onBackPressed()
        }
    }
    
    LaunchedEffect(Unit) {
        presenter.attachView(view)
        presenter.loadUserProfile()
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
        // Top Navigation Bar
        ProfileEditTopBar(
            onBackPressed = { presenter.onBackPressed() },
            onPreviewClicked = { presenter.onPreviewClicked() }
        )
        
        // Content
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            currentUser?.let { user ->
                ProfileEditContent(
                    user = user,
                    presenter = presenter
                )
            }
        }
    }
}

@Composable
private fun ProfileEditTopBar(
    onBackPressed: () -> Unit,
    onPreviewClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackPressed) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "编辑资料",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        TextButton(onClick = onPreviewClicked) {
            Text(
                text = "预览",
                color = Color.Black,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun ProfileEditContent(
    user: User,
    presenter: ProfileEditPresenter
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Avatar Section
            ProfileAvatarSection(
                user = user,
                onAvatarEditClicked = { presenter.onAvatarEditClicked() }
            )
        }
        
        item {
            // Edit Items
            ProfileEditItems(
                user = user,
                presenter = presenter
            )
        }
    }
}

@Composable
private fun ProfileAvatarSection(
    user: User,
    onAvatarEditClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Box {
            // Avatar
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "头像",
                tint = Color.Gray,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
            
            // Camera icon overlay
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.Black, CircleShape)
                    .align(Alignment.BottomEnd)
                    .clickable { onAvatarEditClicked() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "编辑头像",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileEditItems(
    user: User,
    presenter: ProfileEditPresenter
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        ProfileEditItem(
            label = "名字",
            value = user.nickname,
            onClick = { presenter.onNameEditClicked() }
        )
        
        ProfileEditItem(
            label = "小红书号",
            value = user.id,
            onClick = { presenter.onRedBookIdEditClicked() }
        )
        
        ProfileEditItem(
            label = "背景图",
            value = "",
            hasImage = true,
            onClick = { presenter.onBackgroundEditClicked() }
        )
        
        ProfileEditItem(
            label = "简介",
            value = "介绍一下自己",
            isPlaceholder = true,
            onClick = { presenter.onBioEditClicked() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ProfileEditItem(
            label = "性别",
            value = "女",
            onClick = { presenter.onGenderEditClicked() }
        )
        
        ProfileEditItem(
            label = "生日",
            value = "",
            isPlaceholder = true,
            onClick = { presenter.onBirthdayEditClicked() }
        )
        
        ProfileEditItem(
            label = "地区",
            value = "选择所在的地区",
            isPlaceholder = true,
            onClick = { presenter.onLocationEditClicked() }
        )
        
        ProfileEditItem(
            label = "职业",
            value = "选择职业",
            isPlaceholder = true,
            onClick = { presenter.onProfessionEditClicked() }
        )
        
        ProfileEditItem(
            label = "学校",
            value = "选择学校",
            isPlaceholder = true,
            onClick = { presenter.onSchoolEditClicked() }
        )
        
        ProfileEditItem(
            label = "原创信息",
            value = "暂未完成原创认证",
            isPlaceholder = true,
            onClick = { presenter.onOriginalInfoEditClicked() }
        )
    }
}

@Composable
private fun ProfileEditItem(
    label: String,
    value: String,
    hasImage: Boolean = false,
    isPlaceholder: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.width(80.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            if (hasImage) {
                // Background image thumbnail
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "背景图",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = if (isPlaceholder) Color.Gray else Color.Black,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "编辑",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}