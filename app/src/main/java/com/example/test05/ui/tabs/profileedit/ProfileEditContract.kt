package com.example.test05.ui.tabs.profileedit

import com.example.CLYRedNote.model.User

interface ProfileEditContract {
    interface View {
        fun showUserProfile(user: User)
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun showSuccess(message: String)
        fun navigateBack()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadUserProfile()
        fun updateProfile(user: User)
        fun onBackPressed()
        fun onPreviewClicked()
        fun onAvatarEditClicked()
        fun onNameEditClicked()
        fun onRedBookIdEditClicked()
        fun onBackgroundEditClicked()
        fun onBioEditClicked()
        fun onGenderEditClicked()
        fun onBirthdayEditClicked()
        fun onLocationEditClicked()
        fun onProfessionEditClicked()
        fun onSchoolEditClicked()
        fun onOriginalInfoEditClicked()
    }
}