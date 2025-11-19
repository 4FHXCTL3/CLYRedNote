package com.example.CLYRedNote.model

import java.util.Date

data class Collection(
    val id: String,
    val userId: String,
    val noteId: String,
    val note: Note,
    val folderId: String? = null,
    val folderName: String? = null,
    val tags: List<String> = emptyList(),
    val notes: String? = null,
    val collectedAt: Date = Date()
)

data class CollectionFolder(
    val id: String,
    val name: String,
    val description: String? = null,
    val userId: String,
    val isDefault: Boolean = false,
    val noteCount: Int = 0,
    val visibility: FolderVisibility = FolderVisibility.PRIVATE,
    val coverImage: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class FolderVisibility {
    PRIVATE,
    PUBLIC,
    FRIENDS_ONLY
}

data class Like(
    val id: String,
    val userId: String,
    val targetId: String,
    val targetType: LikeTargetType,
    val likedAt: Date = Date()
)

enum class LikeTargetType {
    NOTE,
    COMMENT,
    PRODUCT_REVIEW
}

data class Share(
    val id: String,
    val userId: String,
    val noteId: String,
    val note: Note,
    val platform: SharePlatform,
    val message: String? = null,
    val sharedAt: Date = Date()
)

enum class SharePlatform {
    WECHAT,
    WEIBO,
    QQ,
    COPY_LINK,
    SYSTEM_SHARE,
    INTERNAL_MESSAGE
}

data class Dislike(
    val id: String,
    val userId: String,
    val noteId: String,
    val reason: DislikeReason? = null,
    val dislikedAt: Date = Date()
)

enum class DislikeReason {
    NOT_INTERESTED,
    SEEN_TOO_OFTEN,
    INAPPROPRIATE,
    MISLEADING,
    LOW_QUALITY,
    OTHER
}