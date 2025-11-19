package com.example.CLYRedNote.model

import java.util.Date

data class SearchResult(
    val query: String,
    val notes: List<Note> = emptyList(),
    val users: List<User> = emptyList(),
    val products: List<Product> = emptyList(),
    val topics: List<Topic> = emptyList(),
    val totalCount: Int = 0,
    val hasMore: Boolean = false,
    val searchTime: Long = 0,
    val searchedAt: Date = Date()
)

data class SearchHistory(
    val id: String,
    val userId: String,
    val query: String,
    val searchedAt: Date = Date(),
    val resultCount: Int = 0
)

data class SearchSuggestion(
    val text: String,
    val type: SuggestionType,
    val hotLevel: Int = 0,
    val isRecommended: Boolean = false
)

enum class SuggestionType {
    KEYWORD,
    USER,
    TOPIC,
    BRAND
}

data class Topic(
    val id: String,
    val name: String,
    val description: String? = null,
    val participantCount: Int = 0,
    val noteCount: Int = 0,
    val viewCount: Int = 0,
    val isFollowing: Boolean = false,
    val isHot: Boolean = false,
    val tags: List<String> = emptyList(),
    val coverImage: String? = null,
    val createdAt: Date = Date()
)

data class HotTopic(
    val topic: Topic,
    val rank: Int,
    val growthRate: Double = 0.0,
    val isNew: Boolean = false,
    val updatedAt: Date = Date()
)

data class SearchFilter(
    val category: String? = null,
    val noteType: NoteType? = null,
    val timeRange: TimeRange? = null,
    val sortBy: SortType = SortType.RELEVANCE,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val brand: String? = null,
    val location: String? = null
)

enum class TimeRange {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    THIS_YEAR
}

enum class SortType {
    RELEVANCE,
    TIME_DESC,
    TIME_ASC,
    POPULARITY,
    PRICE_ASC,
    PRICE_DESC
}