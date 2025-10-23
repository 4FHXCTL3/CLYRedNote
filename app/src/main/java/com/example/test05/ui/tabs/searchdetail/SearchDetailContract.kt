package com.example.test05.ui.tabs.searchdetail

import com.example.CLYRedNote.model.Note

interface SearchDetailContract {
    interface View {
        fun showSearchResults(notes: List<Note>)
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun updateSearchText(text: String)
        fun updateSelectedCategory(category: String)
        fun updateSelectedFilter(filter: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadSearchResults(query: String)
        fun onSearchTextChanged(text: String)
        fun onSearchClicked(query: String)
        fun onCategorySelected(category: String)
        fun onFilterSelected(filter: String)
        fun onBackClicked()
    }
}

enum class SearchCategory(val displayName: String) {
    ALL("全部"),
    USER("用户"),
    PRODUCT("商品"),
    GROUP_CHAT("群聊"),
    Q_AND_A("问一问")
}

enum class SearchFilter(val displayName: String) {
    COMPREHENSIVE("综合"),
    PURCHASABLE("可购买"),
    LATEST("最新"),
    PRACTICAL("实穿主义"),
    PLUS_SIZE("微胖mm")
}