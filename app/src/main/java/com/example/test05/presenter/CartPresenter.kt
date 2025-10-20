package com.example.test05.presenter

import com.example.CLYRedNote.model.CartItem
import com.example.CLYRedNote.model.Product
import com.example.test05.ui.tabs.cart.CartContract
import com.example.test05.utils.JsonDataLoader
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.util.Date

class CartPresenter(
    private val dataLoader: JsonDataLoader
) : CartContract.Presenter {
    private var view: CartContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var cartItems: List<CartItem> = emptyList()
    private var products: List<Product> = emptyList()
    
    override fun attachView(view: CartContract.View) {
        this.view = view
        loadProducts()
    }

    override fun detachView() {
        view = null
        presenterScope.cancel()
    }

    private fun loadProducts() {
        presenterScope.launch {
            try {
                products = dataLoader.loadProducts()
                loadCartItems()
            } catch (e: Exception) {
                view?.showError("Failed to load products: ${e.message}")
            }
        }
    }

    override fun loadCartItems() {
        view?.showLoading(true)
        presenterScope.launch {
            try {
                val cartData = dataLoader.loadShoppingCart()
                
                // Attach product info to cart items
                cartItems = cartData.map { cartJson ->
                    val product = products.find { it.id == cartJson.productId }
                    if (product != null) {
                        CartItem(
                            id = cartJson.id,
                            product = product,
                            quantity = cartJson.quantity,
                            selectedSpecs = cartJson.selectedSpecs,
                            isSelected = cartJson.isSelected,
                            addedAt = cartJson.addedAt
                        )
                    } else {
                        null
                    }
                }.filterNotNull()
                
                view?.showCartItems(cartItems)
                updateTotalAmount()
                updateSelectAllStatus()
            } catch (e: Exception) {
                view?.showError("Failed to load cart items: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun onItemSelected(itemId: String, isSelected: Boolean) {
        cartItems = cartItems.map { item ->
            if (item.id == itemId) {
                item.copy(isSelected = isSelected)
            } else {
                item
            }
        }
        
        val updatedItem = cartItems.find { it.id == itemId }
        updatedItem?.let { view?.showItemUpdated(it) }
        
        updateTotalAmount()
        updateSelectAllStatus()
    }

    override fun onSelectAll(isSelectAll: Boolean) {
        cartItems = cartItems.map { item ->
            item.copy(isSelected = isSelectAll)
        }
        
        view?.showCartItems(cartItems)
        updateTotalAmount()
    }

    override fun onQuantityChanged(itemId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            onRemoveItem(itemId)
            return
        }
        
        cartItems = cartItems.map { item ->
            if (item.id == itemId) {
                item.copy(quantity = newQuantity)
            } else {
                item
            }
        }
        
        val updatedItem = cartItems.find { it.id == itemId }
        updatedItem?.let { view?.showItemUpdated(it) }
        
        updateTotalAmount()
    }

    override fun onSpecsChanged(itemId: String, specs: Map<String, String>) {
        cartItems = cartItems.map { item ->
            if (item.id == itemId) {
                item.copy(selectedSpecs = specs)
            } else {
                item
            }
        }
        
        val updatedItem = cartItems.find { it.id == itemId }
        updatedItem?.let { view?.showItemUpdated(it) }
    }

    override fun onRemoveItem(itemId: String) {
        cartItems = cartItems.filter { it.id != itemId }
        view?.showItemRemoved(itemId)
        view?.showCartItems(cartItems)
        updateTotalAmount()
        updateSelectAllStatus()
    }

    override fun onCheckout() {
        val selectedItems = cartItems.filter { it.isSelected }
        if (selectedItems.isEmpty()) {
            view?.showError("请选择要结算的商品")
            return
        }
        
        // 模拟结算成功
        view?.showCheckoutSuccess()
    }

    private fun updateTotalAmount() {
        val totalAmount = cartItems
            .filter { it.isSelected }
            .fold(BigDecimal.ZERO) { acc, item ->
                acc + (item.product.price.multiply(BigDecimal(item.quantity)))
            }
        
        view?.updateTotalAmount(totalAmount)
    }

    private fun updateSelectAllStatus() {
        val isSelectAll = cartItems.isNotEmpty() && cartItems.all { it.isSelected }
        view?.updateSelectAll(isSelectAll)
    }

    override fun onBackClicked() {
        // Handle back navigation
    }

    override fun onSearchClicked() {
        // Handle search action
    }

    override fun onManageClicked() {
        // Handle manage action
    }

    override fun onCategorySelected(category: String) {
        // Handle category selection
    }
}