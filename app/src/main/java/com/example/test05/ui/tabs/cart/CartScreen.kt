package com.example.test05.ui.tabs.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CLYRedNote.model.CartItem
import com.example.test05.presenter.CartPresenter
import com.example.test05.utils.JsonDataLoader
import android.graphics.BitmapFactory
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

@Composable
fun CartScreen(
    onBackClicked: () -> Unit
) {
    val context = LocalContext.current
    val dataLoader = remember { JsonDataLoader(context) }
    val presenter = remember { CartPresenter(dataLoader) }
    
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var totalAmount by remember { mutableStateOf(BigDecimal.ZERO) }
    var isSelectAll by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("全部") }

    val view = object : CartContract.View {
        override fun showCartItems(items: List<CartItem>) {
            cartItems = items
        }

        override fun showLoading(loading: Boolean) {
            isLoading = loading
        }

        override fun showError(message: String) {
            errorMessage = message
        }

        override fun updateTotalAmount(amount: BigDecimal) {
            totalAmount = amount
        }

        override fun updateSelectAll(selectAll: Boolean) {
            isSelectAll = selectAll
        }

        override fun showItemUpdated(item: CartItem) {
            cartItems = cartItems.map { if (it.id == item.id) item else it }
        }

        override fun showItemRemoved(itemId: String) {
            cartItems = cartItems.filter { it.id != itemId }
        }

        override fun showCheckoutSuccess() {
            // Show checkout success message
        }
    }

    LaunchedEffect(Unit) {
        presenter.attachView(view)
        presenter.loadCartItems()
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
        // Top Bar
        TopBar(
            onBackClicked = onBackClicked,
            onSearchClicked = { presenter.onSearchClicked() },
            onManageClicked = { presenter.onManageClicked() }
        )
        
        // Category Tabs
        CategoryTabs(
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                selectedCategory = category
                presenter.onCategorySelected(category)
            }
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else {
            // Cart Items List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Group items by store (using product category as store)
                val groupedItems = cartItems.groupBy { it.product.category }
                
                groupedItems.forEach { (store, storeItems) ->
                    item {
                        StoreHeader(storeName = store)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(storeItems) { item ->
                        CartItemCard(
                            item = item,
                            onSelectedChanged = { isSelected ->
                                presenter.onItemSelected(item.id, isSelected)
                            },
                            onQuantityChanged = { newQuantity ->
                                presenter.onQuantityChanged(item.id, newQuantity)
                            },
                            onSpecsChanged = { specs ->
                                presenter.onSpecsChanged(item.id, specs)
                            },
                            onRemoveClicked = {
                                presenter.onRemoveItem(item.id)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        // Bottom Checkout Bar
        BottomCheckoutBar(
            isSelectAll = isSelectAll,
            totalAmount = totalAmount,
            selectedCount = cartItems.count { it.isSelected },
            onSelectAllChanged = { presenter.onSelectAll(it) },
            onCheckoutClicked = { presenter.onCheckout() }
        )

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
private fun TopBar(
    onBackClicked: () -> Unit,
    onSearchClicked: () -> Unit,
    onManageClicked: () -> Unit
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
                contentDescription = "Back",
                tint = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Title Tabs
        Row {
            Text(
                text = "购物车",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .width(30.dp)
                    .height(2.dp)
                    .background(Color.Red)
                    .align(Alignment.Bottom)
            )
            
            Spacer(modifier = Modifier.width(32.dp))
            
            Text(
                text = "心愿单",
                color = Color.Gray,
                fontSize = 18.sp
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        IconButton(onClick = onSearchClicked) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Black
            )
        }
        
        Text(
            text = "管理",
            color = Color.Black,
            fontSize = 16.sp,
            modifier = Modifier.clickable { onManageClicked() }
        )
    }
}

@Composable
private fun CategoryTabs(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("全部", "1年1度", "降价")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        categories.forEach { category ->
            Text(
                text = category,
                color = if (category == selectedCategory) Color.Black else Color.Gray,
                fontSize = 16.sp,
                fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.clickable { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun StoreHeader(storeName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = "Store",
            tint = Color.Red,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "$storeName 旗舰店",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Arrow",
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onSelectedChanged: (Boolean) -> Unit,
    onQuantityChanged: (Int) -> Unit,
    onSpecsChanged: (Map<String, String>) -> Unit,
    onRemoveClicked: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Selection Checkbox
        Checkbox(
            checked = item.isSelected,
            onCheckedChange = onSelectedChanged,
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Red,
                uncheckedColor = Color.Gray
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Product Image
        ProductImage(
            imageName = item.product.images.firstOrNull() ?: item.product.thumbnailImage,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Product Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Title with badges
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.product.category == "1年1度") {
                    Text(
                        text = "1年1度",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(Color.Red, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                
                Text(
                    text = item.product.name,
                    color = Color.Black,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Specs Selector
            SpecsSelector(
                selectedSpecs = item.selectedSpecs,
                onSpecsChanged = onSpecsChanged
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Promotion tags
            Row {
                Text(
                    text = "限时6.9折",
                    color = Color.Red,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "6期免息",
                    color = Color.Black,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Price and Quantity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "¥${item.product.originalPrice}",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "预估优惠价 ",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "¥${item.product.price}",
                            color = Color.Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Quantity Selector
                QuantitySelector(
                    quantity = item.quantity,
                    onQuantityChanged = onQuantityChanged
                )
            }
        }
    }
}

@Composable
private fun ProductImage(
    imageName: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap = remember(imageName) {
        try {
            if (imageName != null) {
                val inputStream = context.assets.open(imageName)
                BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Gray.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "Product Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } ?: run {
            Text(
                text = "📷",
                fontSize = 24.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun SpecsSelector(
    selectedSpecs: Map<String, String>,
    onSpecsChanged: (Map<String, String>) -> Unit
) {
    val specsText = selectedSpecs.entries.joinToString(" ") { "${it.value}" }
    
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = specsText.ifEmpty { "选择规格" },
            color = Color.Gray,
            fontSize = 12.sp
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Dropdown",
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onQuantityChanged: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (quantity > 1) onQuantityChanged(quantity - 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Text(
                text = "-",
                color = if (quantity > 1) Color.Black else Color.Gray,
                fontSize = 16.sp
            )
        }
        
        Text(
            text = "×$quantity",
            color = Color.Black,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        IconButton(
            onClick = { onQuantityChanged(quantity + 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Text(
                text = "+",
                color = Color.Black,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun BottomCheckoutBar(
    isSelectAll: Boolean,
    totalAmount: BigDecimal,
    selectedCount: Int,
    onSelectAllChanged: (Boolean) -> Unit,
    onCheckoutClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Select All
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onSelectAllChanged(!isSelectAll) }
        ) {
            Checkbox(
                checked = isSelectAll,
                onCheckedChange = onSelectAllChanged,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.Red,
                    uncheckedColor = Color.Gray
                )
            )
            Text(
                text = "全选",
                color = Color.Black,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Total Amount
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "合计 ",
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Text(
                    text = "¥${NumberFormat.getNumberInstance(Locale.CHINA).format(totalAmount)}",
                    color = Color.Red,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Checkout Button
        Button(
            onClick = onCheckoutClicked,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(20.dp),
            enabled = selectedCount > 0
        ) {
            Text(
                text = "结算",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}