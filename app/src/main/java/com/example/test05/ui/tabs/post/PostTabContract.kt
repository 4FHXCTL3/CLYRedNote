package com.example.test05.ui.tabs.post

interface PostTabContract {
    interface View {
        fun showTitle(title: String)
        fun showContent(content: String)
        fun showImages(images: List<String>)
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun showSuccess(message: String)
        fun navigateBack()
        fun showImagePicker()
        fun navigateToPostNext(title: String, content: String, images: List<String>)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun onCloseClicked()
        fun onNextStepClicked()
        fun onTitleChanged(title: String)
        fun onContentChanged(content: String)
        fun onAddImageClicked()
        fun onImageSelected(imagePath: String)
        fun onLongTextClicked()
        fun publishNote()
    }
}