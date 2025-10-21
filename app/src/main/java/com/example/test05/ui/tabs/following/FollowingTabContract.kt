package com.example.test05.ui.tabs.following

import com.example.CLYRedNote.model.Follow
import com.example.CLYRedNote.model.User

interface FollowingTabContract {
    interface View {
        fun showFollowList(follows: List<FollowingUser>)
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun updateFollowStatus(userId: String, isFollowed: Boolean)
        fun showUnfollowSuccess(userId: String)
        fun updateSearchResults(users: List<FollowingUser>)
        fun updateFollowingCount(count: Int)
        fun showTabContent(tabType: FollowingTabType, users: List<FollowingUser>)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadFollowingData()
        fun onTabSelected(tabType: FollowingTabType)
        fun onCategorySelected(category: String)
        fun onSortOptionSelected(sortType: String)
        fun onSearchTextChanged(text: String)
        fun onUnfollowClicked(userId: String)
        fun onUserClicked(userId: String)
        fun onMoreClicked(userId: String)
        fun onBackClicked()
    }
}

enum class FollowingTabType {
    MUTUAL,     // 互相关注
    FOLLOWING,  // 关注
    FOLLOWERS,  // 粉丝
    RECOMMENDED // 推荐
}

data class FollowingUser(
    val user: User,
    val isFollowing: Boolean,
    val isMutual: Boolean,
    val unreadNoteCount: Int = 0,
    val description: String? = null,
    val isBusinessAccount: Boolean = false,
    val followedAt: String? = null
)