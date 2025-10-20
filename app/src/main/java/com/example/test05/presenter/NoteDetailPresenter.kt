package com.example.test05.presenter

import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.Comment
import com.example.CLYRedNote.model.User
import com.example.test05.ui.tabs.notedetail.NoteDetailContract
import com.example.test05.utils.JsonDataLoader
import kotlinx.coroutines.*
import java.util.Date

class NoteDetailPresenter(
    private val dataLoader: JsonDataLoader
) : NoteDetailContract.Presenter {
    private var view: NoteDetailContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var currentNote: Note? = null
    private var comments: List<Comment> = emptyList()
    private var users: List<User> = emptyList()
    private var follows: List<String> = emptyList() // Following user IDs
    
    override fun attachView(view: NoteDetailContract.View) {
        this.view = view
        loadUserData()
    }

    override fun detachView() {
        view = null
        presenterScope.cancel()
    }

    private fun loadUserData() {
        presenterScope.launch {
            try {
                users = dataLoader.loadUsers()
                follows = dataLoader.loadFollows().filter { it.followerId == "user_current" }.map { it.followingId }
            } catch (e: Exception) {
                view?.showError("Failed to load user data: ${e.message}")
            }
        }
    }

    override fun loadNoteDetail(noteId: String) {
        view?.showLoading(true)
        presenterScope.launch {
            try {
                val notes = dataLoader.loadNotes()
                val note = notes.find { it.id == noteId }
                if (note != null) {
                    currentNote = note
                    view?.showNote(currentNote!!)
                    loadComments(noteId)
                } else {
                    view?.showError("Note not found")
                }
            } catch (e: Exception) {
                view?.showError("Failed to load note: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun loadComments(noteId: String) {
        presenterScope.launch {
            try {
                val allComments = dataLoader.loadComments()
                val noteComments = allComments.filter { it.noteId == noteId }
                comments = noteComments
                view?.showComments(comments)
            } catch (e: Exception) {
                view?.showError("Failed to load comments: ${e.message}")
            }
        }
    }

    override fun onLikeClicked(noteId: String) {
        currentNote?.let { note ->
            val newLikeStatus = !note.isLiked
            val newLikeCount = if (newLikeStatus) note.likeCount + 1 else note.likeCount - 1
            
            currentNote = note.copy(
                isLiked = newLikeStatus,
                likeCount = newLikeCount
            )
            
            view?.updateLikeStatus(newLikeStatus, newLikeCount)
        }
    }

    override fun onCollectClicked(noteId: String) {
        currentNote?.let { note ->
            val newCollectStatus = !note.isCollected
            val newCollectCount = if (newCollectStatus) note.collectCount + 1 else note.collectCount - 1
            
            currentNote = note.copy(
                isCollected = newCollectStatus,
                collectCount = newCollectCount
            )
            
            view?.updateCollectStatus(newCollectStatus, newCollectCount)
        }
    }

    override fun onShareClicked(noteId: String) {
        // Share functionality - could open share dialog
        currentNote?.let { note ->
            val newShareCount = note.shareCount + 1
            currentNote = note.copy(shareCount = newShareCount)
        }
    }

    override fun onFollowClicked(authorId: String) {
        val isCurrentlyFollowing = follows.contains(authorId)
        val newFollowStatus = !isCurrentlyFollowing
        
        follows = if (newFollowStatus) {
            follows + authorId
        } else {
            follows - authorId
        }
        
        view?.updateFollowStatus(newFollowStatus)
    }

    override fun onCommentLikeClicked(commentId: String) {
        val comment = comments.find { it.id == commentId }
        comment?.let {
            val newLikeStatus = !it.isLiked
            val newLikeCount = if (newLikeStatus) it.likeCount + 1 else it.likeCount - 1
            
            comments = comments.map { c ->
                if (c.id == commentId) {
                    c.copy(isLiked = newLikeStatus, likeCount = newLikeCount)
                } else {
                    c
                }
            }
            
            view?.showCommentLiked(commentId, newLikeStatus, newLikeCount)
        }
    }

    override fun onAddComment(noteId: String, content: String) {
        if (content.isBlank()) return
        
        val currentUser = users.find { it.id == "user_current" }
        if (currentUser != null) {
            val newComment = Comment(
                id = "comment_${System.currentTimeMillis()}",
                content = content,
                author = currentUser,
                noteId = noteId,
                createdAt = Date()
            )
            
            comments = comments + newComment
            view?.showCommentAdded(newComment)
            
            // Update note comment count
            currentNote?.let { note ->
                currentNote = note.copy(commentCount = note.commentCount + 1)
            }
        }
    }

    override fun onReplyComment(commentId: String, content: String, replyToUserId: String?) {
        if (content.isBlank()) return
        
        val currentUser = users.find { it.id == "user_current" }
        val parentComment = comments.find { it.id == commentId }
        
        if (currentUser != null && parentComment != null) {
            val replyToUser = replyToUserId?.let { userId -> users.find { it.id == userId } }
            
            val newReply = Comment(
                id = "comment_${System.currentTimeMillis()}",
                content = content,
                author = currentUser,
                noteId = parentComment.noteId,
                parentCommentId = commentId,
                replyToUserId = replyToUserId,
                replyToUsername = replyToUser?.nickname,
                createdAt = Date()
            )
            
            comments = comments + newReply
            view?.showCommentAdded(newReply)
        }
    }

    override fun onBackClicked() {
        // Handle back navigation - will be implemented in the screen
    }
}