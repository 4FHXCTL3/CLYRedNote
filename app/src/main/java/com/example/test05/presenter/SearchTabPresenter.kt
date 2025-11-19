package com.example.test05.presenter

import com.example.CLYRedNote.model.Note
import com.example.test05.ui.tabs.search.SearchTabContract
import com.example.test05.ui.tabs.search.HotTopic
import com.example.test05.utils.JsonDataLoader
import kotlinx.coroutines.*

class SearchTabPresenter(
    private val dataLoader: JsonDataLoader
) : SearchTabContract.Presenter {
    private var view: SearchTabContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var allNotes: List<Note> = emptyList()
    private var searchHistory: MutableList<String> = mutableListOf(
        "运动鞋推荐", "美妆教程", "穿搭分享", "减肥食谱"
    )
    private val recommendedSearches = listOf(
        "秋季穿搭", "护肤品种草", "运动健身", "美食制作", 
        "旅行攻略", "数码测评", "学习方法", "减肥瘦身"
    )
    private val hotTopics = listOf(
        HotTopic(
            id = "hot_001",
            title = "秋季穿搭分享",
            description = "分享你的秋季穿搭心得",
            viewCount = 1250000,
            discussionCount = 8900,
            category = "穿搭",
            isHot = true
        ),
        HotTopic(
            id = "hot_002", 
            title = "护肤品种草大会",
            description = "好用护肤品推荐",
            viewCount = 980000,
            discussionCount = 6780,
            category = "美妆",
            isHot = true
        ),
        HotTopic(
            id = "hot_003",
            title = "健身减肥打卡",
            description = "一起运动，一起变美",
            viewCount = 756000,
            discussionCount = 4560,
            category = "运动",
            isNew = true
        ),
        HotTopic(
            id = "hot_004",
            title = "美食制作教程",
            description = "在家做出餐厅级美食",
            viewCount = 623000,
            discussionCount = 3240,
            category = "美食"
        ),
        HotTopic(
            id = "hot_005",
            title = "数码好物分享",
            description = "实用数码产品推荐",
            viewCount = 445000,
            discussionCount = 2190,
            category = "数码"
        ),
        HotTopic(
            id = "hot_006",
            title = "旅行地推荐",
            description = "发现小众旅行地",
            viewCount = 387000,
            discussionCount = 1890,
            category = "旅行"
        )
    )

    override fun attachView(view: SearchTabContract.View) {
        this.view = view
        loadSearchData()
    }

    override fun detachView() {
        view = null
        presenterScope.cancel()
    }

    override fun loadSearchData() {
        view?.showLoading(true)
        presenterScope.launch {
            try {
                allNotes = dataLoader.loadNotes()
                
                view?.showSearchHistory(searchHistory)
                view?.showRecommendedSearches(recommendedSearches)
                view?.showHotTopics(hotTopics)
            } catch (e: Exception) {
                view?.showError("Failed to load search data: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun onSearchTextChanged(text: String) {
        if (text.isEmpty()) {
            view?.clearSearchResults()
            return
        }
        
        // Real-time search when user types
        presenterScope.launch {
            val searchResults = allNotes.filter { note ->
                note.title.contains(text, ignoreCase = true) ||
                note.content.contains(text, ignoreCase = true) ||
                note.tags.any { tag -> tag.contains(text, ignoreCase = true) }
            }.take(10) // Limit results for performance
            
            view?.showSearchResults(searchResults)
        }
    }

    override fun onSearchClicked(query: String) {
        if (query.isBlank()) return
        
        addToHistory(query)
        performSearch(query)
    }

    override fun onHistoryItemClicked(query: String) {
        view?.updateSearchText(query)
        performSearch(query)
    }

    override fun onRecommendationClicked(query: String) {
        view?.updateSearchText(query)
        addToHistory(query)
        performSearch(query)
    }

    override fun onHotTopicClicked(topic: HotTopic) {
        view?.updateSearchText(topic.title)
        addToHistory(topic.title)
        performSearch(topic.title)
    }

    override fun onBackClicked() {
        // Handle back navigation
    }

    override fun clearHistory() {
        searchHistory.clear()
        view?.showSearchHistory(searchHistory)
    }

    override fun addToHistory(query: String) {
        if (query.isBlank()) return
        
        // Remove if already exists to avoid duplicates
        searchHistory.remove(query)
        // Add to beginning of list
        searchHistory.add(0, query)
        // Keep only recent 10 items
        if (searchHistory.size > 10) {
            searchHistory = searchHistory.take(10).toMutableList()
        }
        
        view?.showSearchHistory(searchHistory)
    }

    private fun performSearch(query: String) {
        view?.showLoading(true)
        presenterScope.launch {
            try {
                val searchResults = allNotes.filter { note ->
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true) ||
                    note.tags.any { tag -> tag.contains(query, ignoreCase = true) } ||
                    note.topics.any { topic -> topic.contains(query, ignoreCase = true) }
                }
                
                view?.showSearchResults(searchResults)
            } catch (e: Exception) {
                view?.showError("Search failed: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }
}