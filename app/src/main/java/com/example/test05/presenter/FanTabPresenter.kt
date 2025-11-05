package com.example.test05.presenter

import com.example.CLYRedNote.model.Follow
import com.example.CLYRedNote.model.User
import com.example.test05.ui.tabs.fan.FanTabContract
import com.example.test05.ui.tabs.fan.FanTabType
import com.example.test05.ui.tabs.fan.FanUser
import com.example.test05.utils.JsonDataLoader
import kotlinx.coroutines.*
import java.util.Date

class FanTabPresenter(
    private val dataLoader: JsonDataLoader,
    private val dataStorage: com.example.test05.utils.DataStorage
) : FanTabContract.Presenter {
    private var view: FanTabContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var allUsers: List<User> = emptyList()
    private var fanUsers: List<FanUser> = emptyList()
    private var mutualUsers: List<FanUser> = emptyList()
    private var followingUsers: List<FanUser> = emptyList()
    private var recommendedUsers: List<FanUser> = emptyList()
    private var currentTabType = FanTabType.FANS

    override fun attachView(view: FanTabContract.View) {
        this.view = view
        loadFanData()
    }

    override fun detachView() {
        view = null
        presenterScope.cancel()
    }

    override fun loadFanData() {
        view?.showLoading(true)
        presenterScope.launch {
            try {
                val users = dataLoader.loadUsers()
                val follows = dataLoader.loadFollows()
                val currentUser = dataLoader.getCurrentUser()
                
                allUsers = users
                
                // Create fan users (people who follow current user)
                fanUsers = follows
                    .filter { it.followingId == currentUser?.id }
                    .mapNotNull { follow ->
                        val user = users.find { it.id == follow.followerId }
                        user?.let {
                            // Check if current user follows back
                            val isFollowingBack = follows.any { 
                                it.followerId == currentUser?.id && it.followingId == user.id 
                            }
                            
                            FanUser(
                                user = it,
                                isFollowingBack = isFollowingBack,
                                isMutual = follow.isMutual,
                                noteCount = it.noteCount,
                                fanCount = it.followerCount,
                                followedMeAt = "2023-10-${(1..30).random()}"
                            )
                        }
                    }
                
                // Create following users (people current user follows)
                followingUsers = follows
                    .filter { it.followerId == currentUser?.id }
                    .mapNotNull { follow ->
                        val user = users.find { it.id == follow.followingId }
                        user?.let {
                            FanUser(
                                user = it,
                                isFollowingBack = true,
                                isMutual = follow.isMutual,
                                noteCount = it.noteCount,
                                fanCount = it.followerCount
                            )
                        }
                    }
                
                // Mutual follows
                mutualUsers = fanUsers.filter { it.isMutual }
                
                // Recommended users (users not connected yet)
                recommendedUsers = users
                    .filter { user -> 
                        user.id != currentUser?.id && 
                        fanUsers.none { it.user.id == user.id } &&
                        followingUsers.none { it.user.id == user.id }
                    }
                    .take(5)
                    .map { user ->
                        FanUser(
                            user = user,
                            isFollowingBack = false,
                            isMutual = false,
                            noteCount = user.noteCount,
                            fanCount = user.followerCount
                        )
                    }
                
                view?.updateFanCount(fanUsers.size)
                onTabSelected(currentTabType)
                
            } catch (e: Exception) {
                view?.showError("Failed to load fan data: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun onTabSelected(tabType: FanTabType) {
        currentTabType = tabType
        val users = when (tabType) {
            FanTabType.MUTUAL -> mutualUsers
            FanTabType.FOLLOWING -> followingUsers
            FanTabType.FANS -> fanUsers
            FanTabType.RECOMMENDED -> recommendedUsers
        }
        
        view?.showTabContent(tabType, users)
    }

    override fun onFollowBackClicked(userId: String) {
        presenterScope.launch {
            try {
                // Update local data
                fanUsers = fanUsers.map { fanUser ->
                    if (fanUser.user.id == userId) {
                        fanUser.copy(isFollowingBack = !fanUser.isFollowingBack)
                    } else {
                        fanUser
                    }
                }

                val isNowFollowing = fanUsers.find { it.user.id == userId }?.isFollowingBack ?: false
                view?.updateFollowStatus(userId, isNowFollowing)
                if (isNowFollowing) {
                    view?.showFollowSuccess(userId)

                    // Save follow record to data storage
                    val currentUser = dataLoader.getCurrentUser()
                    val targetUser = allUsers.find { it.id == userId }
                    if (currentUser != null && targetUser != null) {
                        val follow = Follow(
                            id = "follow_${System.currentTimeMillis()}",
                            followerId = currentUser.id,
                            followingId = targetUser.id,
                            follower = currentUser,
                            following = targetUser,
                            followedAt = Date()
                        )
                        dataStorage.saveFollow(follow)
                    }
                }

                // Refresh current tab
                onTabSelected(currentTabType)

            } catch (e: Exception) {
                view?.showError("Failed to follow back user: ${e.message}")
            }
        }
    }

    override fun onUserClicked(userId: String) {
        // Handle user profile navigation
    }

    override fun onBackClicked() {
        // Handle back navigation
    }
}