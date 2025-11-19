package com.example.CLYRedNote.model

import java.util.Date

data class Follow(
    val id: String,
    val followerId: String,
    val followingId: String,
    val follower: User,
    val following: User,
    val followedAt: Date = Date(),
    val isMutual: Boolean = false,
    val isSpecialFollow: Boolean = false,
    val tags: List<String> = emptyList()
)

data class FollowList(
    val userId: String,
    val follows: List<Follow> = emptyList(),
    val totalCount: Int = 0,
    val lastUpdated: Date = Date()
)

data class FollowerList(
    val userId: String,
    val followers: List<Follow> = emptyList(),
    val totalCount: Int = 0,
    val lastUpdated: Date = Date()
)

data class FollowRecommendation(
    val user: User,
    val reason: String,
    val commonFollows: List<User> = emptyList(),
    val score: Double = 0.0,
    val isDismissed: Boolean = false
)