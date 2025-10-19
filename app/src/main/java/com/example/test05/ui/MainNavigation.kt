package com.example.test05.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test05.ui.tabs.me.MeTabScreen

@Composable
fun MainNavigation() {
    var selectedTab by remember { mutableIntStateOf(4) } // Start with MeTab selected

    Column(modifier = Modifier.fillMaxSize()) {
        // Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> HomeTabPlaceholder()
                1 -> MarketTabPlaceholder()
                2 -> PostTabPlaceholder()
                3 -> MessagesTabPlaceholder()
                4 -> MeTabScreen()
            }
        }

        // Bottom Navigation
        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}

@Composable
private fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = Icons.Default.Home,
            label = "首页",
            isSelected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        BottomNavItem(
            icon = Icons.Default.ShoppingCart,
            label = "市集",
            isSelected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )
        
        // Special Post button with + icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Red, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = { onTabSelected(2) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Post",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        BottomNavItem(
            icon = Icons.Default.Email,
            label = "消息",
            isSelected = selectedTab == 3,
            onClick = { onTabSelected(3) }
        )
        BottomNavItem(
            icon = Icons.Default.Person,
            label = "我",
            isSelected = selectedTab == 4,
            onClick = { onTabSelected(4) }
        )
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun HomeTabPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "首页 - 待开发",
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun MarketTabPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "市集 - 待开发",
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun PostTabPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "发布 - 待开发",
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun MessagesTabPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "消息 - 待开发",
            color = Color.White,
            fontSize = 18.sp
        )
    }
}