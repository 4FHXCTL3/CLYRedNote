package com.example.test05.ui.tabs.settings

data class SettingsItem(
    val title: String,
    val icon: String, // Will use emoji or material icon name
    val rightText: String? = null, // For items like "存储空间" showing "1.76 GB"
    val hasArrow: Boolean = true
)

interface SettingsContract {
    interface View {
        fun showSettingsItems(items: List<SettingsItem>)
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun navigateToAccountSecurity()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadSettingsItems()
        fun onSettingsItemClicked(item: SettingsItem)
        fun onBackClicked()
    }
}