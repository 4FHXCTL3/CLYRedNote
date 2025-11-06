package com.example.test05.presenter

import android.content.Context
import com.example.test05.ui.tabs.profileedit.ProfileEditContract
import com.example.test05.utils.DataStorage
import com.example.test05.utils.JsonDataLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileEditPresenter(
    private val dataLoader: JsonDataLoader,
    private val context: Context
) : ProfileEditContract.Presenter {
    
    private var view: ProfileEditContract.View? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun attachView(view: ProfileEditContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun loadUserProfile() {
        scope.launch {
            view?.showLoading(true)
            try {
                val currentUser = withContext(Dispatchers.IO) {
                    dataLoader.getCurrentUser()
                }
                
                currentUser?.let { user ->
                    view?.showUserProfile(user)
                } ?: run {
                    view?.showError("获取用户信息失败")
                }
            } catch (e: Exception) {
                view?.showError("加载用户信息失败: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun updateProfile(user: com.example.CLYRedNote.model.User) {
        scope.launch {
            view?.showLoading(true)
            try {
                // Update the user in the data loader (in-memory cache)
                dataLoader.updateUser(user)

                // Save to persistent storage
                val dataStorage = DataStorage(context)
                dataStorage.updateUser(user)

                view?.showSuccess("资料保存成功")

                // Navigate back after successful update
                view?.navigateBack()
            } catch (e: Exception) {
                view?.showError("保存失败: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun onBackPressed() {
        view?.navigateBack()
    }

    override fun onPreviewClicked() {
        // TODO: Navigate to profile preview
    }

    override fun onAvatarEditClicked() {
        // TODO: Open avatar edit dialog
    }

    override fun onNameEditClicked() {
        // TODO: Open name edit dialog
    }

    override fun onRedBookIdEditClicked() {
        // TODO: Open red book ID edit dialog
    }

    override fun onBackgroundEditClicked() {
        // TODO: Open background edit dialog
    }

    override fun onBioEditClicked() {
        // TODO: Open bio edit dialog
    }

    override fun onGenderEditClicked() {
        // TODO: Open gender selection dialog
    }

    override fun onBirthdayEditClicked() {
        // TODO: Open birthday picker dialog
    }

    override fun onLocationEditClicked() {
        // TODO: Open location picker dialog
    }

    override fun onProfessionEditClicked() {
        // TODO: Open profession picker dialog
    }

    override fun onSchoolEditClicked() {
        // TODO: Open school picker dialog
    }

    override fun onOriginalInfoEditClicked() {
        // TODO: Open original info edit dialog
    }
}