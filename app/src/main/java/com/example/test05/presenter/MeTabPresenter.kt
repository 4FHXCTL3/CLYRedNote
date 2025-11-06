package com.example.test05.presenter

import com.example.test05.ui.tabs.me.MeTabContract
import com.example.test05.utils.JsonDataLoader
import com.example.test05.utils.DataStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MeTabPresenter(
    private val dataLoader: JsonDataLoader,
    private val dataStorage: DataStorage
) : MeTabContract.Presenter {
    
    private var view: MeTabContract.View? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun attachView(view: MeTabContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun loadUserData() {
        scope.launch {
            view?.showLoading(true)
            try {
                val currentUser = withContext(Dispatchers.IO) {
                    dataLoader.getCurrentUser()
                }
                
                val follows = withContext(Dispatchers.IO) {
                    dataLoader.loadFollows()
                }

                currentUser?.let { user ->
                    view?.showUserProfile(user)
                    
                    val followingCount = follows.count { it.followerId == user.id }
                    val followerCount = follows.count { it.followingId == user.id }
                    val likesAndCollections = user.followerCount // Using followerCount as proxy for likes
                    
                    view?.showUserStats(followingCount, followerCount, likesAndCollections)
                }
            } catch (e: Exception) {
                view?.showError("Failed to load user data: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun loadUserNotes() {
        scope.launch {
            try {
                val notes = withContext(Dispatchers.IO) {
                    dataLoader.loadNotes().filter { it.author.id == "user_current" }
                }
                view?.showNotes(notes)
            } catch (e: Exception) {
                view?.showError("Failed to load notes: ${e.message}")
            }
        }
    }

    override fun loadUserCollections() {
        scope.launch {
            try {
                val collections = withContext(Dispatchers.IO) {
                    dataLoader.loadCollections().filter { it.userId == "user_current" }
                }
                view?.showCollections(collections)
            } catch (e: Exception) {
                view?.showError("Failed to load collections: ${e.message}")
            }
        }
    }

    override fun loadLikedNotes() {
        scope.launch {
            try {
                // Load likes from storage
                val likes = withContext(Dispatchers.IO) {
                    dataStorage.loadLikes()
                }

                // Filter likes for current user and type NOTE
                val userLikes = likes.filter {
                    it.userId == "user_current" && it.targetType.name == "NOTE"
                }

                // Load all notes
                val allNotes = withContext(Dispatchers.IO) {
                    dataLoader.loadNotes()
                }

                // Get liked notes and sort by like time (newest first)
                val likedNotes = userLikes
                    .sortedByDescending { it.likedAt }
                    .mapNotNull { like ->
                        allNotes.find { note -> note.id == like.targetId }
                    }

                view?.showLikedNotes(likedNotes)
            } catch (e: Exception) {
                view?.showError("Failed to load liked notes: ${e.message}")
            }
        }
    }

    override fun onEditProfileClicked() {
        // TODO: Navigate to edit profile screen
    }

    override fun onSettingsClicked() {
        // TODO: Navigate to settings screen
    }

    override fun onNotesTabSelected() {
        loadUserNotes()
    }

    override fun onCollectionsTabSelected() {
        loadUserCollections()
    }

    override fun onLikedTabSelected() {
        loadLikedNotes()
    }
    
    override fun refreshUserData() {
        loadUserData()
    }
}