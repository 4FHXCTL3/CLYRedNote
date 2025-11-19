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
    val presenter = remember { ProfileEditPresenter(dataLoader, context) }
    
    var originalUser by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Editable states
    var nickname by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var hasChanges by remember { mutableStateOf(false) }
    
    val view = object : ProfileEditContract.View {
        override fun showUserProfile(user: User) {
            originalUser = user
            nickname = user.nickname
            bio = user.bio ?: ""
            gender = "女" // Default value
            birthday = ""
            location = ""
            profession = ""
            school = ""
            hasChanges = false
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
            onSaveClicked = {
                if (hasChanges) {
                    originalUser?.let { user ->
                        val updatedUser = user.copy(
                            nickname = nickname,
                            bio = bio.takeIf { it.isNotBlank() }
                        )
                        presenter.updateProfile(updatedUser)
                        hasChanges = false
                    }
                }
            },
            hasChanges = hasChanges
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
            originalUser?.let { user ->
                ProfileEditContent(
                    originalUser = user,
                    nickname = nickname,
                    bio = bio,
                    gender = gender,
                    birthday = birthday,
                    location = location,
                    profession = profession,
                    school = school,
                    onNicknameChange = { 
                        nickname = it
                        hasChanges = true
                    },
                    onBioChange = { 
                        bio = it
                        hasChanges = true
                    },
                    onGenderChange = { 
                        gender = it
                        hasChanges = true
                    },
                    onBirthdayChange = { 
                        birthday = it
                        hasChanges = true
                    },
                    onLocationChange = { 
                        location = it
                        hasChanges = true
                    },
                    onProfessionChange = { 
                        profession = it
                        hasChanges = true
                    },
                    onSchoolChange = { 
                        school = it
                        hasChanges = true
                    },
                    presenter = presenter
                )
            }
        }
    }
}

@Composable
private fun ProfileEditTopBar(
    onBackPressed: () -> Unit,
    onSaveClicked: () -> Unit,
    hasChanges: Boolean = false
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
        
        TextButton(
            onClick = onSaveClicked,
            enabled = hasChanges
        ) {
            Text(
                text = "保存",
                color = if (hasChanges) Color.Red else Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ProfileEditContent(
    originalUser: User,
    nickname: String,
    bio: String,
    gender: String,
    birthday: String,
    location: String,
    profession: String,
    school: String,
    onNicknameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onBirthdayChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onProfessionChange: (String) -> Unit,
    onSchoolChange: (String) -> Unit,
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
                user = originalUser,
                onAvatarEditClicked = { presenter.onAvatarEditClicked() }
            )
        }
        
        item {
            // Edit Items
            ProfileEditItems(
                originalUser = originalUser,
                nickname = nickname,
                bio = bio,
                gender = gender,
                birthday = birthday,
                location = location,
                profession = profession,
                school = school,
                onNicknameChange = onNicknameChange,
                onBioChange = onBioChange,
                onGenderChange = onGenderChange,
                onBirthdayChange = onBirthdayChange,
                onLocationChange = onLocationChange,
                onProfessionChange = onProfessionChange,
                onSchoolChange = onSchoolChange
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
    originalUser: User,
    nickname: String,
    bio: String,
    gender: String,
    birthday: String,
    location: String,
    profession: String,
    school: String,
    onNicknameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onBirthdayChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onProfessionChange: (String) -> Unit,
    onSchoolChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        EditableProfileItem(
            label = "名字",
            value = nickname,
            onValueChange = onNicknameChange,
            placeholder = "请输入名字"
        )
        
        ProfileEditItem(
            label = "小红书号",
            value = originalUser.id,
            isReadOnly = true
        )
        
        ProfileEditItem(
            label = "背景图",
            value = "",
            hasImage = true
        )
        
        EditableProfileItem(
            label = "简介",
            value = bio,
            onValueChange = onBioChange,
            placeholder = "介绍一下自己",
            maxLines = 3
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SelectableProfileItem(
            label = "性别",
            value = gender,
            options = listOf("男", "女"),
            onValueChange = onGenderChange
        )
        
        EditableProfileItem(
            label = "生日",
            value = birthday,
            onValueChange = onBirthdayChange,
            placeholder = "选择生日"
        )
        
        EditableProfileItem(
            label = "地区",
            value = location,
            onValueChange = onLocationChange,
            placeholder = "选择所在的地区"
        )
        
        EditableProfileItem(
            label = "职业",
            value = profession,
            onValueChange = onProfessionChange,
            placeholder = "选择职业"
        )
        
        EditableProfileItem(
            label = "学校",
            value = school,
            onValueChange = onSchoolChange,
            placeholder = "选择学校"
        )
        
        ProfileEditItem(
            label = "原创信息",
            value = "暂未完成原创认证",
            isPlaceholder = true
        )
    }
}

@Composable
private fun ProfileEditItem(
    label: String,
    value: String,
    hasImage: Boolean = false,
    isPlaceholder: Boolean = false,
    isReadOnly: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it },
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
            
            if (!isReadOnly && onClick != null) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "编辑",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun EditableProfileItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    maxLines: Int = 1
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = if (maxLines > 1) Alignment.Top else Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.width(80.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Red,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                maxLines = maxLines,
                singleLine = maxLines == 1
            )
        }
    }
}

@Composable
private fun SelectableProfileItem(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
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
                
                Text(
                    text = value.ifEmpty { "选择${label}" },
                    fontSize = 16.sp,
                    color = if (value.isEmpty()) Color.Gray else Color.Black,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "选择",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            if (expanded) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onValueChange(option)
                                expanded = false
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Spacer(modifier = Modifier.width(96.dp))
                        Text(
                            text = option,
                            fontSize = 16.sp,
                            color = if (option == value) Color.Red else Color.Black,
                            fontWeight = if (option == value) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}