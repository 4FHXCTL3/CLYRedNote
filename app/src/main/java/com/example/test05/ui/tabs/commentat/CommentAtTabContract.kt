package com.example.test05.ui.tabs.commentat

import com.example.CLYRedNote.model.Comment
import com.example.CLYRedNote.model.Note

data class CommentNotification(
    val comment: Comment,
    val originalNote: Note,
    val isLiked: Boolean = false
)

interface CommentAtTabContract {
    interface View {
        fun showCommentNotifications(notifications: List<CommentNotification>)
        fun showLoading(loading: Boolean)
        fun showError(message: String)
        fun updateCommentLikeStatus(commentId: String, isLiked: Boolean, likeCount: Int)
        fun showReplySuccess()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadCommentNotifications()
        fun onCommentLikeClicked(commentId: String)
        fun onReplyClicked(commentId: String, replyText: String)
        fun refreshCommentNotifications()
    }
}