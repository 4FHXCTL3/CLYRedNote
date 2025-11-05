package com.example.test05.presenter

import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.SearchHistory
import com.example.test05.ui.tabs.searchdetail.SearchDetailContract
import com.example.test05.ui.tabs.searchdetail.SearchCategory
import com.example.test05.ui.tabs.searchdetail.SearchFilter
import com.example.test05.utils.JsonDataLoader
import com.example.test05.utils.DataStorage
import kotlinx.coroutines.*
import java.util.Date

class SearchDetailPresenter(
    private val dataLoader: JsonDataLoader,
    private val dataStorage: DataStorage
) : SearchDetailContract.Presenter {
    
    private var view: SearchDetailContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var currentQuery: String = ""
    private var currentCategory: SearchCategory = SearchCategory.ALL
    private var currentFilter: SearchFilter = SearchFilter.COMPREHENSIVE
    private var allNotes: List<Note> = emptyList()

    override fun attachView(view: SearchDetailContract.View) {
        this.view = view
        loadData()
    }

    override fun detachView() {
        this.view = null
        presenterScope.cancel()
    }

    private fun loadData() {
        presenterScope.launch {
            try {
                view?.showLoading(true)
                allNotes = withContext(Dispatchers.IO) {
                    dataLoader.loadNotes()
                }
                if (currentQuery.isNotEmpty()) {
                    performSearch()
                }
            } catch (e: Exception) {
                view?.showError("加载数据失败: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun loadSearchResults(query: String) {
        currentQuery = query
        view?.updateSearchText(query)
        performSearch()
    }

    override fun onSearchTextChanged(text: String) {
        currentQuery = text
        view?.updateSearchText(text)
    }

    override fun onSearchClicked(query: String) {
        currentQuery = query
        performSearch()
    }

    override fun onCategorySelected(category: String) {
        val searchCategory = SearchCategory.values().find { it.displayName == category }
        if (searchCategory != null) {
            currentCategory = searchCategory
            view?.updateSelectedCategory(category)
            performSearch()
        }
    }

    override fun onFilterSelected(filter: String) {
        val searchFilter = SearchFilter.values().find { it.displayName == filter }
        if (searchFilter != null) {
            currentFilter = searchFilter
            view?.updateSelectedFilter(filter)
            performSearch()
        }
    }

    private fun performSearch() {
        if (currentQuery.isBlank()) {
            view?.showSearchResults(emptyList())
            return
        }

        presenterScope.launch {
            try {
                view?.showLoading(true)

                val filteredNotes = withContext(Dispatchers.IO) {
                    filterNotes(allNotes, currentQuery, currentCategory, currentFilter)
                }

                view?.showSearchResults(filteredNotes)

                // Save search history
                saveSearchHistory(currentQuery, filteredNotes.size)
            } catch (e: Exception) {
                view?.showError("搜索失败: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    private fun saveSearchHistory(query: String, resultCount: Int) {
        presenterScope.launch {
            try {
                val searchHistory = SearchHistory(
                    id = "search_${System.currentTimeMillis()}",
                    userId = "user_current",
                    query = query,
                    searchedAt = Date(),
                    resultCount = resultCount
                )
                dataStorage.saveSearchHistory(searchHistory)
            } catch (e: Exception) {
                // Silent failure - don't interrupt search experience
                e.printStackTrace()
            }
        }
    }

    private fun filterNotes(
        notes: List<Note>,
        query: String,
        category: SearchCategory,
        filter: SearchFilter
    ): List<Note> {
        val queryLower = query.lowercase()
        
        var filteredNotes = notes.filter { note ->
            note.title.lowercase().contains(queryLower) ||
            note.content.lowercase().contains(queryLower) ||
            note.author.nickname.lowercase().contains(queryLower) ||
            note.tags.any { it.lowercase().contains(queryLower) }
        }

        // Apply category filter
        filteredNotes = when (category) {
            SearchCategory.ALL -> filteredNotes
            SearchCategory.USER -> filteredNotes // 实际实现中可能需要根据用户内容进一步过滤
            SearchCategory.PRODUCT -> filteredNotes.filter { note ->
                note.tags.any { tag -> tag.contains("商品") || tag.contains("购买") || tag.contains("推荐") }
            }
            SearchCategory.GROUP_CHAT -> filteredNotes // 群聊功能暂未实现
            SearchCategory.Q_AND_A -> filteredNotes.filter { note ->
                note.tags.any { tag -> tag.contains("问答") || tag.contains("求助") }
            }
        }

        // Apply filter
        filteredNotes = when (filter) {
            SearchFilter.COMPREHENSIVE -> filteredNotes.sortedByDescending { 
                it.likeCount * 0.5 + it.commentCount * 0.3 + it.shareCount * 0.2
            }
            SearchFilter.PURCHASABLE -> filteredNotes.filter { note ->
                note.tags.any { tag -> tag.contains("购买") || tag.contains("链接") || tag.contains("商品") }
            }
            SearchFilter.LATEST -> filteredNotes.sortedByDescending { it.publishedAt ?: it.createdAt }
            SearchFilter.PRACTICAL -> filteredNotes.filter { note ->
                note.tags.any { tag -> tag.contains("穿搭") || tag.contains("实用") || tag.contains("日常") }
            }
            SearchFilter.PLUS_SIZE -> filteredNotes.filter { note ->
                note.tags.any { tag -> tag.contains("微胖") || tag.contains("大码") || tag.contains("显瘦") }
            }
        }

        return filteredNotes
    }

    override fun onBackClicked() {
        // Navigation handled by the View/Activity
    }
}