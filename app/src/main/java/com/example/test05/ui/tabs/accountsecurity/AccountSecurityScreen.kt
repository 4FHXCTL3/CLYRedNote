package com.example.test05.ui.tabs.accountsecurity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test05.presenter.AccountSecurityPresenter

@Composable
fun AccountSecurityScreen(
    onBackClicked: () -> Unit = {}
) {
    val presenter = remember { AccountSecurityPresenter() }
    
    var accountSecurityItems by remember { mutableStateOf<List<AccountSecurityItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val view = object : AccountSecurityContract.View {
        override fun showAccountSecurityItems(items: List<AccountSecurityItem>) {
            accountSecurityItems = items
        }

        override fun showLoading(loading: Boolean) {
            isLoading = loading
        }

        override fun showError(message: String) {
            errorMessage = message
        }
    }

    LaunchedEffect(Unit) {
        presenter.attachView(view)
        presenter.loadAccountSecurityItems()
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
        // Top Header
        TopHeader(
            onBackClicked = {
                presenter.onBackClicked()
                onBackClicked()
            }
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else {
            // Account Security Items List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(accountSecurityItems) { item ->
                    AccountSecurityItemRow(
                        item = item,
                        isLast = item == accountSecurityItems.lastOrNull(),
                        onClick = { presenter.onAccountSecurityItemClicked(item) }
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

@Composable
private fun TopHeader(
    onBackClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClicked,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "账号与安全",
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Empty space to balance the back button
        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun AccountSecurityItemRow(
    item: AccountSecurityItem,
    isLast: Boolean,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title
            Text(
                text = item.title,
                color = Color.Black,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            
            // Status and Arrow Section
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status text (if any)
                item.status?.let { status ->
                    Text(
                        text = status,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                // Arrow (if enabled)
                if (item.hasArrow) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Arrow",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        // Description (if any)
        item.description?.let { description ->
            Text(
                text = description,
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Divider
        if (!isLast) {
            HorizontalDivider(
                color = Color.Gray.copy(alpha = 0.2f),
                thickness = 0.5.dp
            )
        }
    }
}