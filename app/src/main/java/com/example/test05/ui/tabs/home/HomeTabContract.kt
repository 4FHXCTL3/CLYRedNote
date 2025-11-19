package com.example.test05.ui.tabs.home

import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.User

interface HomeTabContract {
    interface View {
        fun showNotes(notes: List<Note>)
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun updateNoteStatus(noteId: String, isLiked: Boolean, likeCount: Int)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadNotes(tabType: HomeTabType)
        fun loadNotesByCategory(category: String)
        fun onNoteLiked(noteId: String)
        fun onNoteClicked(noteId: String)
        fun onTabSelected(tabType: HomeTabType)
        fun onCategorySelected(category: String)
    }
}

enum class HomeTabType {
    FOLLOWING,      // 关注
    DISCOVER,       // 发现
    LOCAL           // 同城
}

enum class CategoryType {
    RECOMMEND,      // 推荐
    VIDEO,          // 视频
    LIVE,           // 直播
    SHORT_DRAMA,    // 短剧
    FASHION,        // 穿搭
    STUDY           // 学习
}