package com.example.test05.ui.tabs.accountsecurity

data class AccountSecurityItem(
    val id: String,
    val title: String,
    val status: String? = null, // Status text like "未设置", "未绑定", "+86185****6098" etc.
    val description: String? = null, // Additional description text
    val hasArrow: Boolean = true
)

interface AccountSecurityContract {
    interface View {
        fun showAccountSecurityItems(items: List<AccountSecurityItem>)
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun showPasswordEditDialog(currentStatus: String)
        fun showSuccess(message: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadAccountSecurityItems()
        fun onAccountSecurityItemClicked(item: AccountSecurityItem)
        fun onPasswordUpdate(newPassword: String)
        fun onBackClicked()
    }
}