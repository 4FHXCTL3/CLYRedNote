package com.example.CLYRedNote.model

import java.util.Date

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val type: NoteType,
    val author: User,
    val coverImage: String? = null,
    val images: List<String> = emptyList(),
    val video: VideoInfo? = null,
    val tags: List<String> = emptyList(),
    val topics: List<String> = emptyList(),
    val location: Location? = null,
    val visibility: NoteVisibility = NoteVisibility.PUBLIC,
    val allowComment: Boolean = true,
    val allowShare: Boolean = true,
    val status: NoteStatus = NoteStatus.PUBLISHED,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val collectCount: Int = 0,
    val viewCount: Int = 0,
    val isLiked: Boolean = false,
    val isCollected: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val publishedAt: Date? = null,
    val relatedProducts: List<Product> = emptyList()
)

enum class NoteType {
    TEXT,
    IMAGE,
    VIDEO,
    MIXED
}

enum class NoteVisibility {
    PUBLIC,
    PRIVATE,
    FRIENDS_ONLY,
    SPECIFIC_FRIENDS
}

enum class NoteStatus {
    DRAFT,
    PUBLISHED,
    DELETED,
    HIDDEN,
    REVIEWING
}

data class VideoInfo(
    val url: String,
    val duration: Long,
    val width: Int,
    val height: Int,
    val size: Long,
    val thumbnail: String? = null,
    val format: String = "mp4"
)

data class Location(
    val name: String,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val cityCode: String? = null
)

data class NoteDraft(
    val id: String,
    val title: String,
    val content: String,
    val type: NoteType,
    val images: List<String> = emptyList(),
    val video: VideoInfo? = null,
    val tags: List<String> = emptyList(),
    val topics: List<String> = emptyList(),
    val location: Location? = null,
    val visibility: NoteVisibility = NoteVisibility.PUBLIC,
    val allowComment: Boolean = true,
    val allowShare: Boolean = true,
    val relatedProducts: List<Product> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)