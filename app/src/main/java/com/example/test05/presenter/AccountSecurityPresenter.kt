package com.example.test05.presenter

import com.example.test05.ui.tabs.accountsecurity.AccountSecurityContract
import com.example.test05.ui.tabs.accountsecurity.AccountSecurityItem
import kotlinx.coroutines.*

class AccountSecurityPresenter : AccountSecurityContract.Presenter {
    
    private var view: AccountSecurityContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override fun attachView(view: AccountSecurityContract.View) {
        this.view = view
    }
    
    override fun detachView() {
        view = null
        presenterScope.cancel()
    }
    
    override fun loadAccountSecurityItems() {
        view?.showLoading(true)
        presenterScope.launch {
            try {
                val accountSecurityItems = createAccountSecurityItems()
                view?.showAccountSecurityItems(accountSecurityItems)
            } catch (e: Exception) {
                view?.showError("加载账号安全信息失败: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }
    
    private fun createAccountSecurityItems(): List<AccountSecurityItem> {
        return listOf(
            AccountSecurityItem(
                title = "手机号",
                status = "+86138****1234"
            ),
            AccountSecurityItem(
                title = "登录密码",
                status = "未设置"
            ),
            AccountSecurityItem(
                title = "微信账号",
                status = "未绑定"
            ),
            AccountSecurityItem(
                title = "微博账号",
                status = "未绑定"
            ),
            AccountSecurityItem(
                title = "QQ账号",
                status = "未绑定"
            ),
            AccountSecurityItem(
                title = "实名认证",
                status = "未认证"
            ),
            AccountSecurityItem(
                title = "官方认证",
                status = "未认证",
                description = "个人职业资质、机构、企业认证"
            ),
            AccountSecurityItem(
                title = "登录设备管理"
            ),
            AccountSecurityItem(
                title = "账号授权管理"
            ),
            AccountSecurityItem(
                title = "账号找回",
                description = "无法登录其他账号，通过该方式找回并登录"
            ),
            AccountSecurityItem(
                title = "专业号",
                status = "未升级"
            )
        )
    }
    
    override fun onAccountSecurityItemClicked(item: AccountSecurityItem) {
        // Handle account security item click - would navigate to detailed settings pages
        // For now, just placeholder functionality
    }
    
    override fun onBackClicked() {
        // Handle back navigation
    }
}