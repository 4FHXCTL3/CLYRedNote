package com.example.test05.ui.tabs.me

import com.example.CLYRedNote.model.Collection
import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.User

interface MeTabContract {
    interface View {
        fun showUserProfile(user: User)
        fun showUserStats(followingCount: Int, followerCount: Int, likesAndCollections: Int)
        fun showNotes(notes: List<Note>)
        fun showCollections(collections: List<Collection>)
        fun showLikedNotes(notes: List<Note>)
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadUserData()
        fun loadUserNotes()
        fun loadUserCollections()
        fun loadLikedNotes()
        fun refreshUserData()
        fun onEditProfileClicked()
        fun onSettingsClicked()
        fun onNotesTabSelected()
        fun onCollectionsTabSelected()
        fun onLikedTabSelected()
    }
}