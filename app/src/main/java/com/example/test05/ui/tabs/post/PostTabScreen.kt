package com.example.test05.ui.tabs.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test05.presenter.PostTabPresenter
import com.example.test05.utils.JsonDataLoader

@Composable
fun PostTabScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val presenter = remember { PostTabPresenter(dataLoader) }
    
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var images by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val view = object : PostTabContract.View {
        override fun showTitle(titleText: String) {
            title = titleText
        }

        override fun showContent(contentText: String) {
            content = contentText
        }

        override fun showImages(imageList: List<String>) {
            images = imageList
        }

        override fun showLoading(loading: Boolean) {
            isLoading = loading
        }

        override fun showError(message: String) {
            errorMessage = message
        }

        override fun showSuccess(message: String) {
            successMessage = message
        }

        override fun navigateBack() {
            onNavigateBack()
        }

        override fun showImagePicker() {
            // Mock image selection
            val mockImage = "image/scenery${(1..6).random()}.jpg"
            presenter.onImageSelected(mockImage)
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

    // Clear messages after showing them
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            kotlinx.coroutines.delay(3000)
            errorMessage = null
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            kotlinx.coroutines.delay(2000)
            successMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F8FF)) // Light blue background like screenshot
    ) {
        // Top Header
        TopHeader(
            onCloseClicked = { presenter.onCloseClicked() },
            onNextStepClicked = { presenter.onNextStepClicked() },
            isLoading = isLoading
        )

        // Content Area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Image Upload Area
            ImageUploadArea(
                images = images,
                onAddImageClicked = { presenter.onAddImageClicked() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Text Input Area
            TextInputArea(
                title = title,
                content = content,
                onTitleChanged = { 
                    title = it
                    presenter.onTitleChanged(it)
                },
                onContentChanged = { 
                    content = it
                    presenter.onContentChanged(it)
                }
            )
        }

        // Bottom Options
        BottomOptions(
            onLongTextClicked = { presenter.onLongTextClicked() }
        )

        // Error/Success Messages
        errorMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
            ) {
                Text(
                    text = message,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        successMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Green.copy(alpha = 0.1f))
            ) {
                Text(
                    text = message,
                    color = Color.Green,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun TopHeader(
    onCloseClicked: () -> Unit,
    onNextStepClicked: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCloseClicked) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "关闭",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Button(
            onClick = onNextStepClicked,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6B9D),
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "下一步",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ImageUploadArea(
    images: List<String>,
    onAddImageClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (images.isEmpty()) {
            // Empty state with camera icon
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onAddImageClicked() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📷",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击添加图片",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            // Show selected images
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "已选择 ${images.size} 张图片",
                    color = Color.Black,
                    fontSize = 14.sp
                )
                // In a real app, you would display actual images here
            }
        }
    }
}

@Composable
private fun TextInputArea(
    title: String,
    content: String,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title Section
            Text(
                text = "写想法",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Content Input with placeholder
            Box {
                BasicTextField(
                    value = content,
                    onValueChange = onContentChanged,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black
                    ),
                    cursorBrush = SolidColor(Color.Red),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                )
                
                if (content.isEmpty()) {
                    Text(
                        text = "说点什么或提个问题...",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomOptions(
    onLongTextClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLongTextClicked() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "写长文",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "写长文",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = "支持千字以上，全屏阅读体验",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Text(
                text = "›",
                fontSize = 20.sp,
                color = Color.Gray
            )
        }
    }
}