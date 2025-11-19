package com.example.test05.ui.tabs.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.SourceType
import com.example.test05.presenter.HomeTabPresenter
import com.example.test05.utils.JsonDataLoader
import com.example.test05.ui.tabs.notedetail.NoteDetailScreen
import kotlin.math.absoluteValue

@Composable
fun HomeTabScreen(
    onSearchClicked: () -> Unit = {},
    onNoteDetailStateChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val presenter = remember { HomeTabPresenter(dataLoader) }
    
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(HomeTabType.DISCOVER) }
    var selectedCategory by remember { mutableStateOf("推荐") }
    var showNoteDetail by remember { mutableStateOf(false) }
    var currentNoteId by remember { mutableStateOf<String?>(null) }

    val view = object : HomeTabContract.View {
        override fun showNotes(noteList: List<Note>) {
            notes = noteList
        }

        override fun showLoading(loading: Boolean) {
            isLoading = loading
        }

        override fun showError(message: String) {
            errorMessage = message
        }

        override fun updateNoteStatus(noteId: String, isLiked: Boolean, likeCount: Int) {
            notes = notes.map { note ->
                if (note.id == noteId) {
                    note.copy(isLiked = isLiked, likeCount = likeCount)
                } else note
            }
        }
    }

    LaunchedEffect(Unit) {
        presenter.attachView(view)
        presenter.loadNotes(HomeTabType.DISCOVER)
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
            sourceType = SourceType.HOME_FEED,  // 标记来源为首页信息流
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
        ) {
        // Top Navigation Bar
        TopNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { tabType ->
                selectedTab = tabType
                presenter.onTabSelected(tabType)
            },
            onSearchClicked = onSearchClicked
        )

        // Category Filter Row
        CategoryFilterRow(
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                selectedCategory = category
                presenter.onCategorySelected(category)
            }
        )

        // Notes Grid
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else {
            NotesGrid(
                notes = notes,
                onNoteClicked = { noteId -> 
                    currentNoteId = noteId
                    showNoteDetail = true
                },
                onNoteLiked = { noteId -> presenter.onNoteLiked(noteId) }
            )
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
private fun TopNavigationBar(
    selectedTab: HomeTabType,
    onTabSelected: (HomeTabType) -> Unit,
    onSearchClicked: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Menu Icon
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )

        // Tab Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            TabItem(
                text = "关注",
                isSelected = selectedTab == HomeTabType.FOLLOWING,
                onClick = { onTabSelected(HomeTabType.FOLLOWING) }
            )
            TabItem(
                text = "发现",
                isSelected = selectedTab == HomeTabType.DISCOVER,
                onClick = { onTabSelected(HomeTabType.DISCOVER) }
            )
            TabItem(
                text = "同城",
                isSelected = selectedTab == HomeTabType.LOCAL,
                onClick = { onTabSelected(HomeTabType.LOCAL) }
            )
        }

        // Search Icon
        IconButton(onClick = onSearchClicked) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
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
private fun CategoryFilterRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("推荐", "视频", "直播", "短剧", "穿搭", "学习")
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { category ->
            CategoryChip(
                text = category,
                isSelected = category == selectedCategory,
                onClick = { onCategorySelected(category) }
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
        color = if (isSelected) Color.Black else Color.Gray,
        fontSize = 14.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun NotesGrid(
    notes: List<Note>,
    onNoteClicked: (String) -> Unit,
    onNoteLiked: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(notes.size) { index ->
            val note = notes[index]
            NoteCard(
                note = note,
                index = index,
                onClick = { onNoteClicked(note.id) },
                onLikeClick = { onNoteLiked(note.id) }
            )
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    index: Int,
    onClick: () -> Unit,
    onLikeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Cover Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                val context = LocalContext.current
                val imageName = note.coverImage ?: note.images.firstOrNull()
                val bitmap = remember(imageName) {
                    try {
                        if (imageName != null) {
                            val inputStream = context.assets.open(imageName)
                            BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Note Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback to colored background with emoji
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(getNoteImageColor(note, index)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getNoteImageEmoji(note, index),
                            fontSize = 60.sp
                        )
                    }
                }
                
                // Video play button for video notes
                if (note.type.name == "VIDEO") {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = Color.White,
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .padding(8.dp)
                    )
                }
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Title
                Text(
                    text = note.title,
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Author and Like
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Author
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Author",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = note.author.nickname,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Like button and count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onLikeClick() }
                    ) {
                        Icon(
                            imageVector = if (note.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (note.isLiked) Color.Red else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatLikeCount(note.likeCount),
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatLikeCount(count: Int): String {
    return when {
        count >= 10000 -> "${count / 10000}万"
        count >= 1000 -> "${count / 1000}k"
        else -> count.toString()
    }
}


private fun getNoteImageColor(note: Note, index: Int): Color {
    // Use different colors based on note ID or index to simulate different images
    val colorIndex = (note.id.hashCode().absoluteValue + index) % 6
    return when (colorIndex) {
        0 -> Color(0xFF4CAF50) // Green - nature scenery
        1 -> Color(0xFF2196F3) // Blue - sky/water scenery
        2 -> Color(0xFFFF9800) // Orange - sunset scenery
        3 -> Color(0xFF9C27B0) // Purple - urban scenery
        4 -> Color(0xFFFF5722) // Red - autumn scenery
        5 -> Color(0xFF607D8B) // Blue Grey - mountain scenery
        else -> Color(0xFF4CAF50)
    }
}

private fun getNoteImageEmoji(note: Note, index: Int): String {
    // Use different emojis based on note content to simulate different images
    val emojiIndex = (note.id.hashCode().absoluteValue + index) % 8
    return when (emojiIndex) {
        0 -> "🌸" // Flowers
        1 -> "🏔️" // Mountains
        2 -> "🌅" // Sunrise
        3 -> "🌊" // Waves
        4 -> "🌳" // Trees
        5 -> "🌙" // Moon
        6 -> "☀️" // Sun
        7 -> "⭐" // Stars
        else -> "🌸"
    }
}