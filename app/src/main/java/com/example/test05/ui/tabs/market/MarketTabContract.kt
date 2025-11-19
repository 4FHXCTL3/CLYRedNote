package com.example.test05.ui.tabs.market

import com.example.CLYRedNote.model.Product

interface MarketTabContract {
    interface View {
        fun showProducts(products: List<Product>)
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun updateSearchResults(products: List<Product>)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadProducts()
        fun loadProductsByCategory(category: String)
        fun searchProducts(query: String)
        fun onProductClicked(productId: String)
        fun onCategorySelected(category: String)
    }
}

data class MarketIconItem(
    val title: String,
    val emoji: String,
    val color: androidx.compose.ui.graphics.Color
)