package com.example.test05.presenter

import com.example.CLYRedNote.model.Comment
import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.User
import com.example.test05.ui.tabs.commentat.CommentAtTabContract
import com.example.test05.ui.tabs.commentat.CommentNotification
import com.example.test05.utils.JsonDataLoader
import com.example.test05.utils.DataStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class CommentAtTabPresenter(
    private val dataLoader: JsonDataLoader,
    private val dataStorage: DataStorage
) : CommentAtTabContract.Presenter {

    private var view: CommentAtTabContract.View? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private val likedComments = mutableSetOf<String>()
    private val commentReplies = mutableMapOf<String, MutableList<String>>()
    private var currentUser: User? = null
    
    override fun attachView(view: CommentAtTabContract.View) {
        this.view = view
        scope.launch {
            try {
                // Load current user
                val users = withContext(Dispatchers.IO) {
                    dataLoader.loadUsers()
                }
                currentUser = users.find { it.id == "user_current" }
            } catch (e: Exception) {
                // Continue even if user loading fails
            }
            loadCommentNotifications()
        }
    }
    
    override fun detachView() {
        this.view = null
    }
    
    override fun loadCommentNotifications() {
        view?.showLoading(true)
        
        scope.launch {
            try {
                val notifications = withContext(Dispatchers.IO) {
                    generateCommentNotifications()
                }
                view?.showCommentNotifications(notifications)
            } catch (e: Exception) {
                view?.showError("加载评论通知失败: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }
    
    override fun onCommentLikeClicked(commentId: String) {
        scope.launch {
            try {
                // Toggle like status
                val isCurrentlyLiked = likedComments.contains(commentId)
                val newLikeStatus = !isCurrentlyLiked
                val newLikeCount = if (newLikeStatus) 1 else 0
                
                if (newLikeStatus) {
                    likedComments.add(commentId)
                } else {
                    likedComments.remove(commentId)
                }
                
                view?.updateCommentLikeStatus(commentId, newLikeStatus, newLikeCount)
            } catch (e: Exception) {
                view?.showError("点赞失败: ${e.message}")
            }
        }
    }
    
    override fun onReplyClicked(commentId: String) {
        scope.launch {
            try {
                // Toggle reply input visibility
                view?.updateReplyVisibility(commentId, true)
            } catch (e: Exception) {
                view?.showError("显示回复框失败: ${e.message}")
            }
        }
    }
    
    override fun onReplySubmitted(commentId: String, replyText: String) {
        scope.launch {
            try {
                // Get the current user
                val user = currentUser
                if (user == null) {
                    view?.showError("无法获取当前用户信息")
                    return@launch
                }

                // Find the original comment to get noteId
                val comments = withContext(Dispatchers.IO) {
                    dataLoader.loadComments()
                }
                val originalComment = comments.find { it.id == commentId }
                if (originalComment == null) {
                    view?.showError("找不到原评论")
                    return@launch
                }

                // Create a new reply comment
                val replyComment = Comment(
                    id = "comment_${System.currentTimeMillis()}",
                    content = replyText,
                    author = user,
                    noteId = originalComment.noteId,
                    parentCommentId = commentId,
                    replyToUserId = originalComment.author.id,
                    replyToUsername = originalComment.author.nickname,
                    likeCount = 0,
                    replyCount = 0,
                    isLiked = false,
                    images = emptyList(),
                    createdAt = Date(),
                    updatedAt = Date(),
                    replies = emptyList(),
                    isAuthorReply = false,
                    isPinned = false
                )

                // Save the reply comment to storage
                withContext(Dispatchers.IO) {
                    dataStorage.saveComment(replyComment)
                    dataLoader.saveComment(replyComment) // Add to cache
                }

                // Add reply to the comment (for UI display)
                commentReplies.getOrPut(commentId) { mutableListOf() }.add(replyText)
                view?.addReplyToComment(commentId, replyText)
                view?.updateReplyVisibility(commentId, false)
                view?.showReplySuccess()
            } catch (e: Exception) {
                view?.showError("回复失败: ${e.message}")
            }
        }
    }
    
    override fun refreshCommentNotifications() {
        loadCommentNotifications()
    }
    
    private suspend fun generateCommentNotifications(): List<CommentNotification> {
        val comments = dataLoader.loadComments()
        val notes = dataLoader.loadNotes()
        val users = dataLoader.loadUsers()
        val userMap = users.associateBy { it.id }
        val noteMap = notes.associateBy { it.id }
        
        // Get current user's notes to find comments on them
        val currentUser = userMap["user_current"]
        val currentUserNotes = notes.filter { it.author.id == "user_current" }
        val currentUserNoteIds = currentUserNotes.map { it.id }
        
        // Find comments on current user's notes from others
        val commentsOnMyNotes = comments.filter { comment ->
            comment.noteId in currentUserNoteIds && comment.author.id != "user_current"
        }
        
        return commentsOnMyNotes.mapNotNull { comment ->
            val note = noteMap[comment.noteId]
            val author = userMap[comment.author.id]
            if (note != null && author != null) {
                CommentNotification(
                    comment = comment.copy(author = author, isLiked = likedComments.contains(comment.id)),
                    originalNote = note,
                    isLiked = likedComments.contains(comment.id),
                    replies = commentReplies[comment.id] ?: emptyList(),
                    showReplyInput = false
                )
            } else null
        }.sortedByDescending { it.comment.createdAt }
    }
}