package com.example.test05.ui.tabs.fan

import com.example.CLYRedNote.model.User

interface FanTabContract {
    interface View {
        fun showFanList(fans: List<FanUser>)
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun updateFollowStatus(userId: String, isFollowed: Boolean)
        fun showFollowSuccess(userId: String)
        fun updateFanCount(count: Int)
        fun showTabContent(tabType: FanTabType, users: List<FanUser>)
        fun showRecommendedUsers(users: List<FanUser>)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadFanData()
        fun onTabSelected(tabType: FanTabType)
        fun onFollowBackClicked(userId: String)
        fun onUserClicked(userId: String)
        fun onBackClicked()
    }
}

enum class FanTabType {
    MUTUAL,     // 互相关注
    FOLLOWING,  // 关注
    FANS,       // 粉丝
    RECOMMENDED // 推荐
}

data class FanUser(
    val user: User,
    val isFollowingBack: Boolean, // 是否已回关
    val isMutual: Boolean,
    val noteCount: Int = 0,
    val fanCount: Int = 0,
    val followedMeAt: String? = null
)