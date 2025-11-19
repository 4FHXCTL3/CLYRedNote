package com.example.test05.presenter

import com.example.CLYRedNote.model.User
import com.example.test05.ui.tabs.following.FollowingTabContract
import com.example.test05.ui.tabs.following.FollowingTabType
import com.example.test05.ui.tabs.following.FollowingUser
import com.example.test05.utils.JsonDataLoader
import kotlinx.coroutines.*

class FollowingTabPresenter(
    private val dataLoader: JsonDataLoader,
    private val dataStorage: com.example.test05.utils.DataStorage
) : FollowingTabContract.Presenter {
    private var view: FollowingTabContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var allUsers: List<User> = emptyList()
    private var followingUsers: List<FollowingUser> = emptyList()
    private var followersUsers: List<FollowingUser> = emptyList()
    private var mutualUsers: List<FollowingUser> = emptyList()
    private var recommendedUsers: List<FollowingUser> = emptyList()
    private var currentTabType = FollowingTabType.FOLLOWING
    private var currentCategory = "全部"
    private var currentSortType = "综合排序"

    override fun attachView(view: FollowingTabContract.View) {
        this.view = view
        loadFollowingData()
    }

    override fun detachView() {
        view = null
        presenterScope.cancel()
    }

    override fun loadFollowingData() {
        view?.showLoading(true)
        presenterScope.launch {
            try {
                val users = dataLoader.loadUsers()
                val follows = dataLoader.loadFollows()
                val currentUser = dataLoader.getCurrentUser()
                
                allUsers = users
                
                // Create following users (people current user follows)
                followingUsers = follows
                    .filter { it.followerId == currentUser?.id }
                    .mapNotNull { follow ->
                        val user = users.find { it.id == follow.followingId }
                        user?.let {
                            FollowingUser(
                                user = it,
                                isFollowing = true,
                                isMutual = follow.isMutual,
                                unreadNoteCount = (0..5).random(),
                                description = generateUserDescription(it),
                                isBusinessAccount = it.isVerified,
                                followedAt = "2023-10-${(1..30).random()}"
                            )
                        }
                    }
                
                // Create followers (people who follow current user)
                followersUsers = follows
                    .filter { it.followingId == currentUser?.id }
                    .mapNotNull { follow ->
                        val user = users.find { it.id == follow.followerId }
                        user?.let {
                            FollowingUser(
                                user = it,
                                isFollowing = follow.isMutual,
                                isMutual = follow.isMutual,
                                unreadNoteCount = 0,
                                description = generateUserDescription(it),
                                isBusinessAccount = it.isVerified
                            )
                        }
                    }
                
                // Mutual follows
                mutualUsers = followingUsers.filter { it.isMutual }
                
                // Recommended users (users not followed yet)
                recommendedUsers = users
                    .filter { user -> 
                        user.id != currentUser?.id && 
                        followingUsers.none { it.user.id == user.id }
                    }
                    .take(10)
                    .map { user ->
                        FollowingUser(
                            user = user,
                            isFollowing = false,
                            isMutual = false,
                            unreadNoteCount = 0,
                            description = generateUserDescription(user),
                            isBusinessAccount = user.isVerified
                        )
                    }
                
                view?.updateFollowingCount(followingUsers.size)
                onTabSelected(currentTabType)
                
            } catch (e: Exception) {
                view?.showError("Failed to load following data: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun onTabSelected(tabType: FollowingTabType) {
        currentTabType = tabType
        val users = when (tabType) {
            FollowingTabType.MUTUAL -> mutualUsers
            FollowingTabType.FOLLOWING -> followingUsers
            FollowingTabType.FOLLOWERS -> followersUsers
            FollowingTabType.RECOMMENDED -> recommendedUsers
        }
        
        val filteredUsers = filterUsersByCategory(users, currentCategory)
        view?.showTabContent(tabType, filteredUsers)
    }

    override fun onCategorySelected(category: String) {
        currentCategory = category
        onTabSelected(currentTabType)
    }

    override fun onSortOptionSelected(sortType: String) {
        currentSortType = sortType
        // Apply sorting logic here if needed
        onTabSelected(currentTabType)
    }

    override fun onSearchTextChanged(text: String) {
        if (text.isEmpty()) {
            onTabSelected(currentTabType)
            return
        }
        
        val currentUsers = when (currentTabType) {
            FollowingTabType.MUTUAL -> mutualUsers
            FollowingTabType.FOLLOWING -> followingUsers
            FollowingTabType.FOLLOWERS -> followersUsers
            FollowingTabType.RECOMMENDED -> recommendedUsers
        }
        
        val searchResults = currentUsers.filter { followingUser ->
            followingUser.user.nickname.contains(text, ignoreCase = true) ||
            followingUser.user.username.contains(text, ignoreCase = true) ||
            followingUser.description?.contains(text, ignoreCase = true) == true
        }
        
        view?.updateSearchResults(searchResults)
    }

    override fun onUnfollowClicked(userId: String) {
        presenterScope.launch {
            try {
                // Update local data
                followingUsers = followingUsers.map { user ->
                    if (user.user.id == userId) {
                        user.copy(isFollowing = false)
                    } else {
                        user
                    }
                }

                view?.updateFollowStatus(userId, false)
                view?.showUnfollowSuccess(userId)
                view?.updateFollowingCount(followingUsers.count { it.isFollowing })

                // Remove follow record from data storage
                dataStorage.removeFollow("user_current", userId)

                // Refresh current tab
                onTabSelected(currentTabType)

            } catch (e: Exception) {
                view?.showError("Failed to unfollow user: ${e.message}")
            }
        }
    }

    override fun onUserClicked(userId: String) {
        // Handle user profile navigation
    }

    override fun onMoreClicked(userId: String) {
        // Handle more actions menu
    }

    override fun onBackClicked() {
        // Handle back navigation
    }

    private fun filterUsersByCategory(users: List<FollowingUser>, category: String): List<FollowingUser> {
        return when (category) {
            "商家" -> users.filter { it.isBusinessAccount }
            "全部" -> users
            else -> users
        }
    }

    private fun generateUserDescription(user: User): String {
        val descriptions = listOf(
            "${user.noteCount}篇笔记分享生活",
            "热爱生活，分享美好",
            "记录日常，分享快乐",
            "美食博主，探店达人",
            "旅行爱好者，世界探索者",
            "时尚达人，潮流引领者",
            "护肤美妆心得分享",
            "运动健身，健康生活",
            "读书写作，文艺青年",
            "摄影爱好者，光影记录",
            "宠物博主，萌宠日常",
            "数码科技，极客分享",
            "手工达人，创意无限",
            "音乐爱好者，旋律人生",
            "绘画艺术，色彩世界",
            "舞蹈老师，优雅身姿",
            "美食探店，味蕾体验",
            "家居设计，温馨小窝",
            "育儿经验，妈妈心得",
            "职场干货，成长分享",
            "学习达人，知识分享",
            "游戏主播，娱乐时光"
        )
        return descriptions.random()
    }
}