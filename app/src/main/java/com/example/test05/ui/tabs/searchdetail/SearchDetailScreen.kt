package com.example.test05.ui.tabs.searchdetail

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
import com.example.CLYRedNote.model.SourceType
import com.example.test05.presenter.SearchDetailPresenter
import com.example.test05.utils.JsonDataLoader
import com.example.test05.ui.tabs.notedetail.NoteDetailScreen

@Composable
fun SearchDetailScreen(
    initialQuery: String = "",
    onBackClicked: () -> Unit = {},
    onNoteClicked: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val dataStorage = remember { com.example.test05.utils.DataStorage(context) }
    val presenter = remember { SearchDetailPresenter(dataLoader, dataStorage) }
    
    var searchText by remember { mutableStateOf(initialQuery) }
    var selectedCategory by remember { mutableStateOf(SearchCategory.ALL.displayName) }
    var selectedFilter by remember { mutableStateOf(SearchFilter.COMPREHENSIVE.displayName) }
    var searchResults by remember { mutableStateOf<List<Note>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showNoteDetail by remember { mutableStateOf(false) }
    var currentNoteId by remember { mutableStateOf<String?>(null) }

    val view = object : SearchDetailContract.View {
        override fun showSearchResults(notes: List<Note>) {
            searchResults = notes
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

        override fun updateSelectedCategory(category: String) {
            selectedCategory = category
        }

        override fun updateSelectedFilter(filter: String) {
            selectedFilter = filter
        }
    }

    LaunchedEffect(Unit) {
        presenter.attachView(view)
        if (initialQuery.isNotEmpty()) {
            presenter.loadSearchResults(initialQuery)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            presenter.detachView()
        }
    }

    if (showNoteDetail && currentNoteId != null) {
        NoteDetailScreen(
            noteId = currentNoteId!!,
            sourceType = SourceType.SEARCH_RESULT,  // 标记来源为搜索结果
            onBackClicked = {
                showNoteDetail = false
                currentNoteId = null
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
        // Search Bar
        SearchDetailBar(
            searchText = searchText,
            onSearchTextChange = { 
                searchText = it
                presenter.onSearchTextChanged(it)
            },
            onSearchClicked = { presenter.onSearchClicked(searchText) },
            onBackClicked = onBackClicked
        )

        // Category Tabs (全部、用户、商品、群聊、问一问)
        CategoryTabRow(
            categories = SearchCategory.values().map { it.displayName },
            selectedCategory = selectedCategory,
            onCategorySelected = { presenter.onCategorySelected(it) }
        )

        // Filter Tabs (综合、可购买、最新、实穿主义、微胖mm)
        FilterTabRow(
            filters = SearchFilter.values().map { it.displayName },
            selectedFilter = selectedFilter,
            onFilterSelected = { presenter.onFilterSelected(it) }
        )


        // Search Results
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
                        onClick = { 
                            currentNoteId = note.id
                            showNoteDetail = true
                        }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchDetailBar(
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
                    "秋冬穿搭",
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
private fun CategoryTabRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(categories) { category ->
            CategoryTab(
                text = category,
                isSelected = category == selectedCategory,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun CategoryTab(
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
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Black else Color.Gray,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(3.dp)
                    .background(Color.Red, RoundedCornerShape(1.5.dp))
            )
        } else {
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}

@Composable
private fun FilterTabRow(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(filters) { filter ->
            FilterChip(
                text = filter,
                isSelected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color.Red else Color(0xFFF5F5F5)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (isSelected) Color.White else Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
            .height(220.dp)
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
                    .height(140.dp)
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