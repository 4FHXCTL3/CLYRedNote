package com.example.test05.ui.tabs.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import com.example.CLYRedNote.model.Note
import com.example.test05.presenter.SearchTabPresenter
import com.example.test05.utils.JsonDataLoader

@Composable
fun SearchTabScreen(
    onBackClicked: () -> Unit = {},
    onNoteClicked: (String) -> Unit = {},
    onSearchPerformed: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val presenter = remember { SearchTabPresenter(dataLoader) }
    
    var searchText by remember { mutableStateOf("") }
    var searchHistory by remember { mutableStateOf<List<String>>(emptyList()) }
    var recommendedSearches by remember { mutableStateOf<List<String>>(emptyList()) }
    var hotTopics by remember { mutableStateOf<List<HotTopic>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<Note>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSearchResults by remember { mutableStateOf(false) }

    val view = object : SearchTabContract.View {
        override fun showSearchHistory(history: List<String>) {
            searchHistory = history
        }

        override fun showRecommendedSearches(recommendations: List<String>) {
            recommendedSearches = recommendations
        }

        override fun showHotTopics(topics: List<HotTopic>) {
            hotTopics = topics
        }

        override fun showSearchResults(notes: List<Note>) {
            searchResults = notes
            showSearchResults = notes.isNotEmpty()
        }

        override fun showLoading(loading: Boolean) {
            isLoading = loading
        }

        override fun showError(message: String) {
            errorMessage = message
        }

        override fun updateSearchText(text: String) {
            searchText = text
        }

        override fun clearSearchResults() {
            searchResults = emptyList()
            showSearchResults = false
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
        // Search Bar
        SearchBar(
            searchText = searchText,
            onSearchTextChange = { 
                searchText = it
                presenter.onSearchTextChanged(it)
            },
            onSearchClicked = { 
                presenter.onSearchClicked(searchText)
                if (searchText.isNotEmpty()) {
                    onSearchPerformed(searchText)
                }
            },
            onBackClicked = onBackClicked
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
        }

        if (showSearchResults) {
            // Search Results
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(searchResults) { note ->
                    SearchResultCard(
                        note = note,
                        onClick = { onNoteClicked(note.id) }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Search History Section
                if (searchHistory.isNotEmpty()) {
                    item {
                        SearchHistorySection(
                            history = searchHistory,
                            onHistoryClicked = { presenter.onHistoryItemClicked(it) },
                            onClearClicked = { presenter.clearHistory() }
                        )
                    }
                }

                // Recommended Searches Section
                item {
                    RecommendedSearchesSection(
                        recommendations = recommendedSearches,
                        onRecommendationClicked = { presenter.onRecommendationClicked(it) }
                    )
                }

                // Hot Topics Section
                item {
                    HotTopicsSection(
                        hotTopics = hotTopics,
                        onTopicClicked = { presenter.onHotTopicClicked(it) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onBackClicked: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }
        
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            placeholder = { 
                Text(
                    "搜索笔记、用户",
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
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color.Red
            ),
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { 
                    onSearchClicked()
                    keyboardController?.hide()
                }
            ),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        TextButton(
            onClick = { 
                onSearchClicked()
                keyboardController?.hide()
            }
        ) {
            Text(
                text = "搜索",
                color = Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SearchHistorySection(
    history: List<String>,
    onHistoryClicked: (String) -> Unit,
    onClearClicked: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "搜索历史",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            TextButton(onClick = onClearClicked) {
                Text(
                    text = "清空",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history) { item ->
                HistoryChip(
                    text = item,
                    onClick = { onHistoryClicked(item) }
                )
            }
        }
    }
}

@Composable
private fun HistoryChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun RecommendedSearchesSection(
    recommendations: List<String>,
    onRecommendationClicked: (String) -> Unit
) {
    Column {
        Text(
            text = "猜你想搜",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recommendations) { item ->
                RecommendationChip(
                    text = item,
                    onClick = { onRecommendationClicked(item) }
                )
            }
        }
    }
}

@Composable
private fun RecommendationChip(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = Color.Red,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun HotTopicsSection(
    hotTopics: List<HotTopic>,
    onTopicClicked: (HotTopic) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "小红书热点",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "🔥",
                fontSize = 18.sp
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        hotTopics.forEachIndexed { index, topic ->
            HotTopicItem(
                topic = topic,
                rank = index + 1,
                onClick = { onTopicClicked(topic) }
            )
            if (index < hotTopics.size - 1) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HotTopicItem(
    topic: HotTopic,
    rank: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank number
        Text(
            text = rank.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (rank <= 3) Color.Red else Color.Gray,
            modifier = Modifier.width(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Title
        Text(
            text = topic.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // View count
        Text(
            text = "${formatCount(topic.viewCount)}",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun SearchResultCard(
    note: Note,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Note Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📷",
                            fontSize = 32.sp
                        )
                    }
                }
            }
            
            // Note Info
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(
                    text = note.title,
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.author.nickname,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint = if (note.isLiked) Color.Red else Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatCount(note.likeCount.toLong()),
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatCount(count: Long): String {
    return when {
        count >= 10000 -> "${count / 10000}万"
        count >= 1000 -> "${count / 1000}k"
        else -> count.toString()
    }
}