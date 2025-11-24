package com.example.test05.presenter

import com.example.CLYRedNote.model.Follow
import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.User
import com.example.test05.ui.tabs.bloggerdetail.BloggerDetailContract
import com.example.test05.utils.DataStorage
import com.example.test05.utils.JsonDataLoader
import kotlinx.coroutines.*
import java.util.Date

class BloggerDetailPresenter(
    private val dataLoader: JsonDataLoader,
    private val dataStorage: DataStorage
) : BloggerDetailContract.Presenter {
    private var view: BloggerDetailContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var currentUser: User? = null
    private var currentBlogger: User? = null
    private var allNotes: List<Note> = emptyList()
    private var filteredNotes: List<Note> = emptyList()
    private var isFollowing: Boolean = false

    override fun attachView(view: BloggerDetailContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
        presenterScope.cancel()
    }

    override fun loadBloggerDetail(userId: String) {
        view?.showLoading(true)
        presenterScope.launch {
            try {
                val users = dataLoader.loadUsers()
                val notes = dataLoader.loadNotes()
                val follows = dataLoader.loadFollows()
                val currentUserData = dataLoader.getCurrentUser()
                
                currentUser = currentUserData
                currentBlogger = users.find { it.id == userId }
                
                if (currentBlogger != null) {
                    // Check if current user is following this blogger
                    isFollowing = follows.any { 
                        it.followerId == currentUser?.id && it.followingId == userId 
                    }
                    
                    // Get blogger's notes and sort by createdAt descending (newest first)
                    allNotes = notes.filter { it.author.id == userId }
                        .sortedByDescending { it.createdAt }
                    filteredNotes = allNotes
                    
                    view?.showBloggerInfo(currentBlogger!!)
                    view?.showBloggerNotes(filteredNotes)
                    view?.updateFollowStatus(isFollowing)
                } else {
                    view?.showError("用户不存在")
                }
                
            } catch (e: Exception) {
                view?.showError("加载用户信息失败: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun onFollowClicked() {
        presenterScope.launch {
            try {
                // In a real app, this would make an API call
                // For now, we just update the local state
                isFollowing = true
                view?.updateFollowStatus(true)
                view?.showFollowSuccess()

                // Save follow record to data storage
                if (currentUser != null && currentBlogger != null) {
                    val follow = Follow(
                        id = "follow_${System.currentTimeMillis()}",
                        followerId = currentUser!!.id,
                        followingId = currentBlogger!!.id,
                        follower = currentUser!!,
                        following = currentBlogger!!,
                        followedAt = Date()
                    )
                    dataStorage.saveFollow(follow)
                }
            } catch (e: Exception) {
                view?.showError("关注失败: ${e.message}")
            }
        }
    }

    override fun onUnfollowClicked() {
        presenterScope.launch {
            try {
                // In a real app, this would make an API call
                // For now, we just update the local state
                isFollowing = false
                view?.updateFollowStatus(false)
                view?.showUnfollowSuccess()

                // Remove follow record from data storage
                if (currentUser != null && currentBlogger != null) {
                    dataStorage.removeFollow(currentUser!!.id, currentBlogger!!.id)
                }
            } catch (e: Exception) {
                view?.showError("取消关注失败: ${e.message}")
            }
        }
    }

    override fun onMessageClicked() {
        currentBlogger?.let { blogger ->
            view?.navigateToMessage(blogger.id)
        }
    }

    override fun onGroupChatClicked() {
        // Handle group chat action
    }

    override fun onEvaluateClicked() {
        // Handle evaluation action
    }

    override fun onNoteClicked(noteId: String) {
        // Handle note click - would normally navigate to note detail
    }

    override fun onSearchNotes(query: String) {
        if (query.isEmpty()) {
            filteredNotes = allNotes
        } else {
            filteredNotes = allNotes.filter { note ->
                note.title.contains(query, ignoreCase = true) ||
                note.content.contains(query, ignoreCase = true) ||
                note.tags.any { it.contains(query, ignoreCase = true) }
            }.sortedByDescending { it.createdAt }
        }
        view?.showBloggerNotes(filteredNotes)
    }

    override fun onFilterByCategory(category: String) {
        filteredNotes = when (category) {
            "全部" -> allNotes
            else -> allNotes.filter { note ->
                note.tags.contains(category) || note.topics.contains(category)
            }.sortedByDescending { it.createdAt }
        }
        view?.showBloggerNotes(filteredNotes)
    }

    override fun onBackClicked() {
        // Handle back navigation
    }
}