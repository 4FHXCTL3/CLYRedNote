package com.example.test05.presenter

import com.example.test05.ui.tabs.settings.SettingsContract
import com.example.test05.ui.tabs.settings.SettingsItem
import kotlinx.coroutines.*

class SettingsPresenter : SettingsContract.Presenter {
    
    private var view: SettingsContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override fun attachView(view: SettingsContract.View) {
        this.view = view
    }
    
    override fun detachView() {
        view = null
        presenterScope.cancel()
    }
    
    override fun loadSettingsItems() {
        view?.showLoading(true)
        presenterScope.launch {
            try {
                val settingsItems = createSettingsItems()
                view?.showSettingsItems(settingsItems)
            } catch (e: Exception) {
                view?.showError("加载设置失败: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }
    
    private fun createSettingsItems(): List<SettingsItem> {
        return listOf(
            SettingsItem(
                title = "账号与安全",
                icon = "AccountCircle"
            ),
            SettingsItem(
                title = "通用设置", 
                icon = "Settings"
            ),
            SettingsItem(
                title = "通知设置",
                icon = "Notifications"
            ),
            SettingsItem(
                title = "隐私设置",
                icon = "Lock"
            ),
            SettingsItem(
                title = "存储空间",
                icon = "Storage",
                rightText = "1.76 GB"
            ),
            SettingsItem(
                title = "内容偏好调节",
                icon = "Tune"
            ),
            SettingsItem(
                title = "收货地址",
                icon = "LocationOn"
            ),
            SettingsItem(
                title = "添加小组件",
                icon = "Widgets"
            ),
            SettingsItem(
                title = "未成年人模式",
                icon = "Security",
                rightText = "未开启"
            ),
            SettingsItem(
                title = "新功能体验",
                icon = "Science"
            ),
            SettingsItem(
                title = "帮助与客服",
                icon = "Support"
            ),
            SettingsItem(
                title = "关于小红书",
                icon = "Info"
            )
        )
    }
    
    override fun onSettingsItemClicked(item: SettingsItem) {
        when (item.title) {
            "账号与安全" -> view?.navigateToAccountSecurity()
            else -> {
                // Handle other settings item clicks - would navigate to detailed settings pages
                // For now, just placeholder functionality
            }
        }
    }
    
    override fun onBackClicked() {
        // Handle back navigation
    }
}