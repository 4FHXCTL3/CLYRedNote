package com.example.test05.ui.tabs.settings

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test05.presenter.SettingsPresenter

@Composable
fun SettingsScreen(
    onBackClicked: () -> Unit = {},
    onAccountSecurityClicked: () -> Unit = {}
) {
    val presenter = remember { SettingsPresenter() }
    
    var settingsItems by remember { mutableStateOf<List<SettingsItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val view = object : SettingsContract.View {
        override fun showSettingsItems(items: List<SettingsItem>) {
            settingsItems = items
        }

        override fun showLoading(loading: Boolean) {
            isLoading = loading
        }

        override fun showError(message: String) {
            errorMessage = message
        }

        override fun navigateToAccountSecurity() {
            onAccountSecurityClicked()
        }
    }

    LaunchedEffect(Unit) {
        presenter.attachView(view)
        presenter.loadSettingsItems()
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
            // Settings Items List
            SettingsItemsList(
                items = settingsItems,
                onItemClick = { presenter.onSettingsItemClicked(it) }
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
            text = "设置",
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
private fun SettingsItemsList(
    items: List<SettingsItem>,
    onItemClick: (SettingsItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(items) { item ->
            SettingsItemRow(
                item = item,
                isLast = item == items.lastOrNull(),
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
private fun SettingsItemRow(
    item: SettingsItem,
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
            // Icon
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconForName(item.icon),
                    contentDescription = item.title,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Title
            Text(
                text = item.title,
                color = Color.Black,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            
            // Right text (if any)
            item.rightText?.let { rightText ->
                Text(
                    text = rightText,
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
        
        // Divider
        if (!isLast) {
            HorizontalDivider(
                color = Color.Gray.copy(alpha = 0.2f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 56.dp)
            )
        }
    }
}

private fun getIconForName(iconName: String): ImageVector {
    return when (iconName) {
        "AccountCircle" -> Icons.Default.AccountCircle
        "Settings" -> Icons.Default.Settings
        "Notifications" -> Icons.Default.Notifications
        "Lock" -> Icons.Default.Lock
        "Storage" -> Icons.Default.Build
        "Tune" -> Icons.Default.Settings
        "LocationOn" -> Icons.Default.LocationOn
        "Widgets" -> Icons.Default.Add
        "Security" -> Icons.Default.Lock
        "Science" -> Icons.Default.Star
        "Support" -> Icons.Default.Phone
        "Info" -> Icons.Default.Info
        else -> Icons.Default.Settings
    }
}