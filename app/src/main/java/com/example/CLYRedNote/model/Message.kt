package com.example.CLYRedNote.model

import java.util.Date

data class Message(
    val id: String,
    val content: String,
    val type: MessageType,
    val sender: User,
    val receiver: User,
    val conversationId: String,
    val isRead: Boolean = false,
    val images: List<String> = emptyList(),
    val emoji: String? = null,
    val note: Note? = null,
    val createdAt: Date = Date(),
    val readAt: Date? = null
)

enum class MessageType {
    TEXT,
    IMAGE,
    EMOJI,
    NOTE_SHARE,
    SYSTEM
}

data class Conversation(
    val id: String,
    val participants: List<User>,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val isBlocked: Boolean = false,
    val isMuted: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

data class SystemNotification(
    val id: String,
    val title: String,
    val content: String,
    val type: NotificationType,
    val targetUserId: String,
    val relatedId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Date = Date(),
    val readAt: Date? = null
)

enum class NotificationType {
    LIKE,
    COMMENT,
    FOLLOW,
    SYSTEM,
    MARKETING,
    SECURITY
}