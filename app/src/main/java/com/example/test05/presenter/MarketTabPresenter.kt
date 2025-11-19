package com.example.test05.presenter

import com.example.test05.ui.tabs.market.MarketTabContract
import com.example.test05.utils.JsonDataLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MarketTabPresenter(
    private val dataLoader: JsonDataLoader
) : MarketTabContract.Presenter {
    
    private var view: MarketTabContract.View? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun attachView(view: MarketTabContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun loadProducts() {
        scope.launch {
            view?.showLoading(true)
            try {
                val products = withContext(Dispatchers.IO) {
                    dataLoader.loadProducts()
                }
                view?.showProducts(products)
            } catch (e: Exception) {
                view?.showError("Failed to load products: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun loadProductsByCategory(category: String) {
        scope.launch {
            try {
                val allProducts = withContext(Dispatchers.IO) {
                    dataLoader.loadProducts()
                }
                
                // Filter products by category
                val filteredProducts = if (category == "推荐") {
                    allProducts // Show all for recommendation
                } else {
                    allProducts.filter { product ->
                        product.category.contains(category, ignoreCase = true) ||
                        product.tags.any { tag -> tag.contains(category, ignoreCase = true) }
                    }
                }
                
                view?.showProducts(if (filteredProducts.isEmpty()) allProducts else filteredProducts)
            } catch (e: Exception) {
                view?.showError("Failed to load products by category: ${e.message}")
            }
        }
    }

    override fun searchProducts(query: String) {
        scope.launch {
            try {
                val allProducts = withContext(Dispatchers.IO) {
                    dataLoader.loadProducts()
                }
                
                // Filter products by search query
                val searchResults = if (query.isEmpty()) {
                    allProducts
                } else {
                    allProducts.filter { product ->
                        product.name.contains(query, ignoreCase = true) ||
                        product.description.contains(query, ignoreCase = true) ||
                        product.brand?.contains(query, ignoreCase = true) == true ||
                        product.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                    }
                }
                
                view?.updateSearchResults(searchResults)
            } catch (e: Exception) {
                view?.showError("Failed to search products: ${e.message}")
            }
        }
    }

    override fun onProductClicked(productId: String) {
        // TODO: Navigate to product detail screen
    }

    override fun onCategorySelected(category: String) {
        loadProductsByCategory(category)
    }
}