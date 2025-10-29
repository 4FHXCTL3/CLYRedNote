package com.example.test05.ui.tabs.postnext

import com.example.CLYRedNote.model.Note

data class PostData(
    val images: List<String> = emptyList(),
    val title: String = "",
    val content: String = "",
    val tags: List<String> = emptyList(),
    val topics: List<String> = emptyList(),
    val location: String = "",
    val privacy: PostPrivacy = PostPrivacy.PRIVATE,
    val mentionedUsers: List<String> = emptyList()
)

enum class PostPrivacy(val displayName: String) {
    PUBLIC("公开可见"),
    FOLLOWERS_ONLY("不给谁看"),
    FRIENDS_ONLY("只给谁看"),
    MUTUAL_FRIENDS("仅互关好友可见"),
    PRIVATE("仅自己可见")
}

interface PostNextContract {
    interface View {
        fun showLoading(isLoading: Boolean)
        fun showError(message: String)
        fun showSuccess(message: String)
        fun navigateBack()
        fun showPrivacySelector(currentPrivacy: PostPrivacy)
        fun hidePrivacySelector()
        fun updatePrivacy(privacy: PostPrivacy)
        fun navigateToNoteDetail(noteId: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun initWithPostData(postData: PostData)
        fun onTitleChanged(title: String)
        fun onContentChanged(content: String)
        fun onTagAdded(tag: String)
        fun onTagRemoved(tag: String)
        fun onTopicAdded(topic: String)
        fun onLocationClicked()
        fun onPrivacyClicked()
        fun onPrivacySelected(privacy: PostPrivacy)
        fun onAddImageClicked()
        fun onImageRemoved(imageIndex: Int)
        fun onMentionUserClicked()
        fun onVoteClicked()
        fun onBackClicked()
        fun onPreviewClicked()
        fun onSettingsClicked()
        fun onSaveDraftClicked()
        fun onPublishClicked()
    }
}