package com.example.CLYRedNote.model

import java.util.Date

data class Comment(
    val id: String,
    val content: String,
    val author: User,
    val noteId: String,
    val parentCommentId: String? = null,
    val replyToUserId: String? = null,
    val replyToUsername: String? = null,
    val likeCount: Int = 0,
    val replyCount: Int = 0,
    val isLiked: Boolean = false,
    val images: List<String> = emptyList(),
    val status: CommentStatus = CommentStatus.NORMAL,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val replies: List<Comment> = emptyList(),
    val isAuthorReply: Boolean = false,
    val isPinned: Boolean = false
)

enum class CommentStatus {
    NORMAL,
    DELETED,
    HIDDEN,
    REVIEWING
}

data class CommentReply(
    val comment: Comment,
    val targetComment: Comment
)