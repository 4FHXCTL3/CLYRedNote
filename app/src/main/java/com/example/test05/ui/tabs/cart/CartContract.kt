package com.example.test05.ui.tabs.cart

import com.example.CLYRedNote.model.CartItem
import java.math.BigDecimal

interface CartContract {
    interface View {
        fun showCartItems(items: List<CartItem>)
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun updateTotalAmount(totalAmount: BigDecimal)
        fun updateSelectAll(isSelectAll: Boolean)
        fun showItemUpdated(item: CartItem)
        fun showItemRemoved(itemId: String)
        fun showCheckoutSuccess()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadCartItems()
        fun onItemSelected(itemId: String, isSelected: Boolean)
        fun onSelectAll(isSelectAll: Boolean)
        fun onQuantityChanged(itemId: String, newQuantity: Int)
        fun onSpecsChanged(itemId: String, specs: Map<String, String>)
        fun onRemoveItem(itemId: String)
        fun onCheckout()
        fun onBackClicked()
        fun onSearchClicked()
        fun onManageClicked()
        fun onCategorySelected(category: String)
    }
}