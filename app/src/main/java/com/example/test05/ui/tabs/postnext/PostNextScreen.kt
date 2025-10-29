package com.example.test05.ui.tabs.postnext

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import android.graphics.BitmapFactory
import java.io.IOException
import com.example.test05.presenter.PostNextPresenter
import com.example.test05.utils.JsonDataLoader

@Composable
fun loadImageFromAssets(context: android.content.Context, imagePath: String): androidx.compose.ui.graphics.ImageBitmap? {
    return try {
        val inputStream = context.assets.open(imagePath)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        bitmap?.asImageBitmap()
    } catch (e: IOException) {
        null
    }
}

@Composable
fun PostNextScreen(
    postData: PostData = PostData(),
    onBackClicked: () -> Unit = {},
    onNavigateToNoteDetail: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val presenter = remember { PostNextPresenter(dataLoader) }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPrivacySelector by remember { mutableStateOf(false) }
    var currentPrivacy by remember { mutableStateOf(PostPrivacy.PRIVATE) }
    var title by remember { mutableStateOf(postData.title) }
    var content by remember { mutableStateOf(postData.content) }
    
    val view = object : PostNextContract.View {
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
            onBackClicked()
        }
        
        override fun showPrivacySelector(privacy: PostPrivacy) {
            currentPrivacy = privacy
            showPrivacySelector = true
        }
        
        override fun hidePrivacySelector() {
            showPrivacySelector = false
        }
        
        override fun updatePrivacy(privacy: PostPrivacy) {
            currentPrivacy = privacy
        }
        
        override fun navigateToNoteDetail(noteId: String) {
            onNavigateToNoteDetail(noteId)
        }
    }
    
    LaunchedEffect(Unit) {
        presenter.attachView(view)
        presenter.initWithPostData(postData)
    }
    
    DisposableEffect(Unit) {
        onDispose {
            presenter.detachView()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Top Bar
            PostNextTopBar(
                onBackClicked = { presenter.onBackClicked() },
                onPreviewClicked = { presenter.onPreviewClicked() }
            )
            
            // Content
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Image Gallery
                    PostImageGallery(
                        images = postData.images,
                        onAddImageClicked = { presenter.onAddImageClicked() },
                        onImageRemoved = { index -> presenter.onImageRemoved(index) }
                    )
                }
                
                item {
                    // Title Input
                    PostTitleInput(
                        title = title,
                        onTitleChanged = { 
                            title = it
                            presenter.onTitleChanged(it)
                        }
                    )
                }
                
                item {
                    // Content Display
                    PostContentDisplay(content = content)
                }
                
                item {
                    // Suggested Tags
                    PostSuggestedTags(
                        onTagClicked = { presenter.onTagAdded(it) }
                    )
                }
                
                item {
                    // Function Buttons
                    PostFunctionButtons(
                        onTopicClicked = { presenter.onTopicAdded("#话题") },
                        onMentionClicked = { presenter.onMentionUserClicked() },
                        onVoteClicked = { presenter.onVoteClicked() }
                    )
                }
                
                item {
                    // Settings Section
                    PostSettingsSection(
                        currentPrivacy = currentPrivacy,
                        onLocationClicked = { presenter.onLocationClicked() },
                        onPrivacyClicked = { presenter.onPrivacyClicked() }
                    )
                }
            }
            
            // Bottom Bar
            PostNextBottomBar(
                onSettingsClicked = { presenter.onSettingsClicked() },
                onSaveDraftClicked = { presenter.onSaveDraftClicked() },
                onPublishClicked = { presenter.onPublishClicked() }
            )
        }
        
        // Privacy Selector Dialog
        if (showPrivacySelector) {
            PostPrivacyDialog(
                currentPrivacy = currentPrivacy,
                onPrivacySelected = { presenter.onPrivacySelected(it) },
                onDismiss = { showPrivacySelector = false }
            )
        }
        
        // Loading Overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Red)
            }
        }
    }
}

@Composable
private fun PostNextTopBar(
    onBackClicked: () -> Unit,
    onPreviewClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = Color.Black
            )
        }
        
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
private fun PostImageGallery(
    images: List<String>,
    onAddImageClicked: () -> Unit,
    onImageRemoved: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(images.size) { index ->
            Card(
                modifier = Modifier
                    .size(100.dp)
                    .clickable { onImageRemoved(index) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                val context = LocalContext.current
                val imageBitmap = remember(images[index]) {
                    try {
                        val inputStream = context.assets.open(images[index])
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream.close()
                        bitmap?.asImageBitmap()
                    } catch (e: IOException) {
                        null
                    }
                }
                
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "笔记图片",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "图片",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier
                    .size(100.dp)
                    .clickable { onAddImageClicked() },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加图片",
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PostTitleInput(
    title: String,
    onTitleChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChanged,
        placeholder = {
            Text(
                text = "添加标题",
                color = Color.Gray,
                fontSize = 16.sp
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PostContentDisplay(content: String) {
    if (content.isNotEmpty()) {
        Text(
            text = content,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PostSuggestedTags(
    onTagClicked: (String) -> Unit
) {
    val suggestedTags = listOf("#ai", "#AI人工智能", "#挑战人工智能", "#人人都是创作者")
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(suggestedTags) { tag ->
            Card(
                modifier = Modifier.clickable { onTagClicked(tag) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun PostFunctionButtons(
    onTopicClicked: () -> Unit,
    onMentionClicked: () -> Unit,
    onVoteClicked: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        FunctionButton(
            icon = "#",
            text = "话题",
            onClick = onTopicClicked
        )
        
        FunctionButton(
            icon = "@",
            text = "用户",
            onClick = onMentionClicked
        )
        
        FunctionButton(
            icon = "📊",
            text = "投票",
            onClick = onVoteClicked
        )
    }
}

@Composable
private fun FunctionButton(
    icon: String,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 18.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun PostSettingsSection(
    currentPrivacy: PostPrivacy,
    onLocationClicked: () -> Unit,
    onPrivacyClicked: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Location
        SettingItem(
            icon = Icons.Default.LocationOn,
            text = "标记地点",
            onClick = onLocationClicked
        )
        
        // Privacy
        SettingItem(
            icon = Icons.Default.Lock,
            text = currentPrivacy.displayName,
            textColor = Color.Red,
            onClick = onPrivacyClicked,
            showArrow = true
        )
        
        // Add Component
        SettingItem(
            icon = Icons.Default.Add,
            text = "添加组件",
            subtitle = "可添加文件",
            onClick = { /* TODO */ },
            showArrow = true
        )
    }
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    subtitle: String? = null,
    textColor: Color = Color.Black,
    onClick: () -> Unit,
    showArrow: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (textColor == Color.Red) Color.Red else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = textColor
            )
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        
        if (showArrow) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "更多",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PostNextBottomBar(
    onSettingsClicked: () -> Unit,
    onSaveDraftClicked: () -> Unit,
    onPublishClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Settings
        Column(
            modifier = Modifier.clickable { onSettingsClicked() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "设置",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "设置",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.width(32.dp))
        
        // Save Draft
        Column(
            modifier = Modifier.clickable { onSaveDraftClicked() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "存草稿",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "存草稿",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Publish Button
        Button(
            onClick = onPublishClicked,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .height(48.dp)
                .width(120.dp)
        ) {
            Text(
                text = "发布笔记",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PostPrivacyDialog(
    currentPrivacy: PostPrivacy,
    onPrivacySelected: (PostPrivacy) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                PostPrivacy.values().forEach { privacy ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPrivacySelected(privacy) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (privacy) {
                                PostPrivacy.PUBLIC -> Icons.Default.Lock
                                PostPrivacy.FOLLOWERS_ONLY -> Icons.Default.Person
                                PostPrivacy.FRIENDS_ONLY -> Icons.Default.Person
                                PostPrivacy.MUTUAL_FRIENDS -> Icons.Default.Person
                                PostPrivacy.PRIVATE -> Icons.Default.Lock
                            },
                            contentDescription = privacy.displayName,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = privacy.displayName,
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (privacy == currentPrivacy) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "已选择",
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}