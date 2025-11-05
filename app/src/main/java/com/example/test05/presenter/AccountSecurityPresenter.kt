package com.example.test05.presenter

import com.example.test05.ui.tabs.accountsecurity.AccountSecurityContract
import com.example.test05.ui.tabs.accountsecurity.AccountSecurityItem
import com.example.test05.utils.DataStorage
import com.example.test05.utils.JsonDataLoader
import kotlinx.coroutines.*

class AccountSecurityPresenter(
    private val dataLoader: JsonDataLoader,
    private val dataStorage: DataStorage
) : AccountSecurityContract.Presenter {
    
    private var view: AccountSecurityContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var accountSecurityItems: MutableList<AccountSecurityItem> = mutableListOf()
    
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
        val items = listOf(
            AccountSecurityItem(
                id = "phone",
                title = "手机号",
                status = "+86138****1234"
            ),
            AccountSecurityItem(
                id = "password",
                title = "登录密码",
                status = "未设置"
            ),
            AccountSecurityItem(
                id = "wechat",
                title = "微信账号",
                status = "未绑定"
            ),
            AccountSecurityItem(
                id = "weibo",
                title = "微博账号",
                status = "未绑定"
            ),
            AccountSecurityItem(
                id = "qq",
                title = "QQ账号",
                status = "未绑定"
            ),
            AccountSecurityItem(
                id = "identity",
                title = "实名认证",
                status = "未认证"
            ),
            AccountSecurityItem(
                id = "official",
                title = "官方认证",
                status = "未认证",
                description = "个人职业资质、机构、企业认证"
            ),
            AccountSecurityItem(
                id = "devices",
                title = "登录设备管理"
            ),
            AccountSecurityItem(
                id = "authorization",
                title = "账号授权管理"
            ),
            AccountSecurityItem(
                id = "recovery",
                title = "账号找回",
                description = "无法登录其他账号，通过该方式找回并登录"
            ),
            AccountSecurityItem(
                id = "professional",
                title = "专业号",
                status = "未升级"
            )
        )
        accountSecurityItems = items.toMutableList()
        return items
    }
    
    override fun onAccountSecurityItemClicked(item: AccountSecurityItem) {
        when (item.id) {
            "password" -> {
                view?.showPasswordEditDialog(item.status ?: "未设置")
            }
            else -> {
                // Handle other account security items - placeholder for now
            }
        }
    }
    
    override fun onPasswordUpdate(newPassword: String) {
        presenterScope.launch {
            try {
                view?.showLoading(true)

                // Simulate password update delay
                delay(1000)

                // Get current user and update password
                val currentUser = dataLoader.getCurrentUser()
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(password = newPassword)
                    dataStorage.updateUser(updatedUser)
                }

                // Update the password item status
                val updatedItems = accountSecurityItems.map { item ->
                    if (item.id == "password") {
                        item.copy(status = "已设置")
                    } else {
                        item
                    }
                }
                accountSecurityItems = updatedItems.toMutableList()

                view?.showAccountSecurityItems(updatedItems)
                view?.showSuccess("密码设置成功")
            } catch (e: Exception) {
                view?.showError("密码设置失败: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }
    
    override fun onBackClicked() {
        // Handle back navigation
    }
}