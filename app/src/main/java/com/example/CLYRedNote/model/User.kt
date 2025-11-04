package com.example.CLYRedNote.model

import java.util.Date

data class User(
    val id: String,
    val username: String,
    val nickname: String,
    val avatar: String? = null,
    val bio: String? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val noteCount: Int = 0,
    val isFollowed: Boolean = false,
    val isVerified: Boolean = false,
    val level: Int = 1,
    val password: String? = null,
    val createdAt: Date = Date(),
    val lastActiveAt: Date? = null
)

data class UserProfile(
    val user: User,
    val settings: UserSettings,
    val statistics: UserStatistics
)

data class UserSettings(
    val isDarkMode: Boolean = false,
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val privacySettings: PrivacySettings = PrivacySettings(),
    val accountSecurity: AccountSecurity = AccountSecurity()
)

data class NotificationSettings(
    val likeNotification: Boolean = true,
    val commentNotification: Boolean = true,
    val followNotification: Boolean = true,
    val messageNotification: Boolean = true,
    val systemNotification: Boolean = true,
    val marketingNotification: Boolean = false,
    val doNotDisturbEnabled: Boolean = false,
    val doNotDisturbStart: String? = null,
    val doNotDisturbEnd: String? = null
)

data class PrivacySettings(
    val allowStrangerViewFollowList: Boolean = true,
    val allowStrangerMessage: Boolean = true,
    val showOnlineStatus: Boolean = true,
    val showLocationInfo: Boolean = false
)

data class AccountSecurity(
    val loginDevices: List<LoginDevice> = emptyList(),
    val lastPasswordChange: Date? = null,
    val twoFactorEnabled: Boolean = false
)

data class LoginDevice(
    val deviceId: String,
    val deviceName: String,
    val deviceType: String,
    val location: String? = null,
    val lastLoginTime: Date,
    val isCurrentDevice: Boolean = false
)

data class UserStatistics(
    val totalLikes: Int = 0,
    val totalViews: Int = 0,
    val totalComments: Int = 0,
    val totalShares: Int = 0,
    val totalCollections: Int = 0
)