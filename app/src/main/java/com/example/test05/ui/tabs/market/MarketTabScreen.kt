package com.example.test05.ui.tabs.market

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import com.example.CLYRedNote.model.Product
import com.example.test05.presenter.MarketTabPresenter
import com.example.test05.utils.JsonDataLoader
import kotlin.math.absoluteValue

@Composable
fun MarketTabScreen(
    onCartClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val presenter = remember { MarketTabPresenter(dataLoader) }
    
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("推荐") }

    val view = object : MarketTabContract.View {
        override fun showProducts(productList: List<Product>) {
            products = productList
        }

        override fun showLoading(loading: Boolean) {
            isLoading = loading
        }

        override fun showError(message: String) {
            errorMessage = message
        }

        override fun updateSearchResults(productList: List<Product>) {
            products = productList
        }
    }

    LaunchedEffect(Unit) {
        presenter.attachView(view)
        presenter.loadProducts()
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
                presenter.searchProducts(it)
            },
            onCartClicked = onCartClicked
        )

        // Category Tabs
        CategoryTabs(
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                selectedCategory = category
                presenter.onCategorySelected(category)
            }
        )

        // Market Icons Grid
        MarketIconsGrid()

        // Products Grid
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else {
            ProductsGrid(
                products = products,
                onProductClicked = { productId -> presenter.onProductClicked(productId) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onCartClicked: () -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            placeholder = { 
                Text(
                    "无预购吉他",
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
                .weight(1f)
                .height(48.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = "搜索",
            color = Color.Black,
            fontSize = 16.sp,
            modifier = Modifier.clickable { keyboardController?.hide() }
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        IconButton(onClick = onCartClicked) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Cart",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun CategoryTabs(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("推荐", "1年1度", "运动", "穿搭", "美护", "家居", "生活")
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
            color = if (isSelected) Color.Red else Color.Black,
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
private fun MarketIconsGrid() {
    val iconItems = listOf(
        listOf(
            MarketIconItem("市集直播", "📹", Color(0xFFE91E63)),
            MarketIconItem("买手臻选", "🛍️", Color(0xFF2196F3)),
            MarketIconItem("新品首发", "🎉", Color(0xFFFF9800)),
            MarketIconItem("超级满减", "💰", Color(0xFF4CAF50)),
            MarketIconItem("宠粉清单", "❤️", Color(0xFFE91E63))
        ),
        listOf(
            MarketIconItem("我的订单", "📋", Color(0xFF9C27B0)),
            MarketIconItem("购物车", "🛒", Color(0xFFFF9800)),
            MarketIconItem("优惠券", "🎫", Color(0xFFFF5722)),
            MarketIconItem("客服消息", "💬", Color(0xFF00BCD4)),
            MarketIconItem("商品足迹", "👣", Color(0xFF795548))
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        iconItems.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { item ->
                    MarketIconCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun MarketIconCard(item: MarketIconItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(item.color, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.emoji,
                fontSize = 24.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.title,
            color = Color.Black,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun ProductsGrid(
    products: List<Product>,
    onProductClicked: (String) -> Unit
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
        items(products.size) { index ->
            val product = products[index]
            ProductCard(
                product = product,
                index = index,
                onClick = { onProductClicked(product.id) }
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    index: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                val context = LocalContext.current
                val imageName = product.thumbnailImage ?: product.images.firstOrNull()
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
                        contentDescription = "Product Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback to colored background with emoji
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(getProductFallbackColor(product, index)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getProductFallbackEmoji(product, index),
                            fontSize = 48.sp
                        )
                    }
                }
            }
            
            // Product Info
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Product Name
                Text(
                    text = product.name,
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Price and Sales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Price
                    Column {
                        Text(
                            text = "¥${product.price}",
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (product.originalPrice != null && product.originalPrice > product.price) {
                            Text(
                                text = "¥${product.originalPrice}",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        }
                    }
                    
                    // Sales
                    Text(
                        text = "${formatSalesCount(product.salesCount)}已售",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

private fun getProductFallbackColor(product: Product, index: Int): Color {
    val colorIndex = (product.id.hashCode().absoluteValue + index) % 6
    return when (colorIndex) {
        0 -> Color(0xFF2196F3) // Blue
        1 -> Color(0xFF4CAF50) // Green
        2 -> Color(0xFFFF9800) // Orange
        3 -> Color(0xFFE91E63) // Pink
        4 -> Color(0xFF9C27B0) // Purple
        5 -> Color(0xFFFF5722) // Deep Orange
        else -> Color(0xFF2196F3)
    }
}

private fun getProductFallbackEmoji(product: Product, index: Int): String {
    val emojiIndex = (product.id.hashCode().absoluteValue + index) % 8
    return when (emojiIndex) {
        0 -> "🛍️"
        1 -> "👟"
        2 -> "💄"
        3 -> "👗"
        4 -> "🎧"
        5 -> "💡"
        6 -> "🧴"
        7 -> "🏃"
        else -> "🛍️"
    }
}

private fun formatSalesCount(count: Int): String {
    return when {
        count >= 10000 -> "${count / 10000}万"
        count >= 1000 -> "${count / 1000}k"
        else -> count.toString()
    }
}