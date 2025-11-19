package com.example.test05.ui.tabs.notedetail

import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.Comment
import com.example.CLYRedNote.model.User

interface NoteDetailContract {
    interface View {
        fun showNote(note: Note)
        fun showComments(comments: List<Comment>)
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun updateLikeStatus(isLiked: Boolean, likeCount: Int)
        fun updateCollectStatus(isCollected: Boolean, collectCount: Int)
        fun updateFollowStatus(isFollowing: Boolean)
        fun showCommentAdded(comment: Comment)
        fun showCommentLiked(commentId: String, isLiked: Boolean, likeCount: Int)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadNoteDetail(noteId: String)
        fun loadComments(noteId: String)
        fun onLikeClicked(noteId: String)
        fun onCollectClicked(noteId: String)
        fun onShareClicked(noteId: String)
        fun onFollowClicked(authorId: String)
        fun onCommentLikeClicked(commentId: String)
        fun onAddComment(noteId: String, content: String)
        fun onReplyComment(commentId: String, content: String, replyToUserId: String?)
        fun onDislikeClicked(noteId: String)
        fun onBackClicked()
    }
}