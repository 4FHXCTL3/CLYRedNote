package com.example.test05.ui.tabs.bloggerdetail

import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.User

interface BloggerDetailContract {
    interface View {
        fun showBloggerInfo(user: User)
        fun showBloggerNotes(notes: List<Note>)
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun updateFollowStatus(isFollowing: Boolean)
        fun showFollowSuccess()
        fun showUnfollowSuccess()
        fun navigateToMessage(userId: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadBloggerDetail(userId: String)
        fun onFollowClicked()
        fun onUnfollowClicked()
        fun onMessageClicked()
        fun onGroupChatClicked()
        fun onEvaluateClicked()
        fun onNoteClicked(noteId: String)
        fun onSearchNotes(query: String)
        fun onFilterByCategory(category: String)
        fun onBackClicked()
    }
}