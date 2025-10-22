package com.example.test05.presenter

import com.example.CLYRedNote.model.Comment
import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.User
import com.example.test05.ui.tabs.commentat.CommentAtTabContract
import com.example.test05.ui.tabs.commentat.CommentNotification
import com.example.test05.utils.JsonDataLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class CommentAtTabPresenter(
    private val dataLoader: JsonDataLoader
) : CommentAtTabContract.Presenter {
    
    private var view: CommentAtTabContract.View? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    
    override fun attachView(view: CommentAtTabContract.View) {
        this.view = view
        loadCommentNotifications()
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
                // Simulate like/unlike toggle
                // In real app, this would update the backend
                view?.updateCommentLikeStatus(commentId, true, 1)
            } catch (e: Exception) {
                view?.showError("点赞失败: ${e.message}")
            }
        }
    }
    
    override fun onReplyClicked(commentId: String, replyText: String) {
        scope.launch {
            try {
                // Simulate reply
                // In real app, this would send the reply to backend
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
                    comment = comment.copy(author = author),
                    originalNote = note,
                    isLiked = false
                )
            } else null
        }.sortedByDescending { it.comment.createdAt }
    }
}