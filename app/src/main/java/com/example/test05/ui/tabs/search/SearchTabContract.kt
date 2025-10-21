package com.example.test05.ui.tabs.search

import com.example.CLYRedNote.model.Note

interface SearchTabContract {
    interface View {
        fun showSearchHistory(history: List<String>)
        fun showRecommendedSearches(recommendations: List<String>)
        fun showHotTopics(topics: List<HotTopic>)
        fun showSearchResults(notes: List<Note>)
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun updateSearchText(text: String)
        fun clearSearchResults()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadSearchData()
        fun onSearchTextChanged(text: String)
        fun onSearchClicked(query: String)
        fun onHistoryItemClicked(query: String)
        fun onRecommendationClicked(query: String)
        fun onHotTopicClicked(topic: HotTopic)
        fun onBackClicked()
        fun clearHistory()
        fun addToHistory(query: String)
    }
}

data class HotTopic(
    val id: String,
    val title: String,
    val description: String,
    val viewCount: Long,
    val discussionCount: Long,
    val category: String,
    val isHot: Boolean = false,
    val isNew: Boolean = false
)