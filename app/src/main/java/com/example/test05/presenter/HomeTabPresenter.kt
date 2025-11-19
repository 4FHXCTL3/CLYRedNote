package com.example.test05.presenter

import com.example.test05.ui.tabs.home.HomeTabContract
import com.example.test05.ui.tabs.home.HomeTabType
import com.example.test05.utils.JsonDataLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeTabPresenter(
    private val dataLoader: JsonDataLoader
) : HomeTabContract.Presenter {
    
    private var view: HomeTabContract.View? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun attachView(view: HomeTabContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun loadNotes(tabType: HomeTabType) {
        scope.launch {
            view?.showLoading(true)
            try {
                val allNotes = withContext(Dispatchers.IO) {
                    dataLoader.loadNotes()
                }
                
                // Filter notes based on tab type
                val filteredNotes = when (tabType) {
                    HomeTabType.FOLLOWING -> {
                        // Show notes from followed users
                        val follows = dataLoader.loadFollows().filter { it.followerId == "user_current" }
                        val followingIds = follows.map { it.followingId }
                        allNotes.filter { note -> followingIds.contains(note.author.id) }
                    }
                    HomeTabType.DISCOVER -> {
                        // Show all public notes
                        allNotes.filter { it.visibility.name == "PUBLIC" }
                    }
                    HomeTabType.LOCAL -> {
                        // For now, show notes with location info
                        allNotes.filter { it.location != null }
                    }
                }
                
                view?.showNotes(filteredNotes)
            } catch (e: Exception) {
                view?.showError("Failed to load notes: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun loadNotesByCategory(category: String) {
        scope.launch {
            try {
                val allNotes = withContext(Dispatchers.IO) {
                    dataLoader.loadNotes()
                }
                
                // Filter notes by category/tags
                val filteredNotes = allNotes.filter { note ->
                    note.tags.any { tag -> 
                        tag.contains(category, ignoreCase = true) 
                    } || note.topics.any { topic ->
                        topic.contains(category, ignoreCase = true)
                    }
                }
                
                view?.showNotes(if (filteredNotes.isEmpty()) allNotes else filteredNotes)
            } catch (e: Exception) {
                view?.showError("Failed to load notes by category: ${e.message}")
            }
        }
    }

    override fun onNoteLiked(noteId: String) {
        scope.launch {
            try {
                // In a real app, this would update the backend
                // For now, just simulate the like action
                view?.updateNoteStatus(noteId, true, 0) // This would be the new like count
            } catch (e: Exception) {
                view?.showError("Failed to like note: ${e.message}")
            }
        }
    }

    override fun onNoteClicked(noteId: String) {
        // TODO: Navigate to note detail screen
    }

    override fun onTabSelected(tabType: HomeTabType) {
        loadNotes(tabType)
    }

    override fun onCategorySelected(category: String) {
        loadNotesByCategory(category)
    }
}