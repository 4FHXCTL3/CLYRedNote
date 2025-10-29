package com.example.test05.presenter

import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.NoteType
import com.example.CLYRedNote.model.NoteVisibility
import com.example.test05.ui.tabs.postnext.PostNextContract
import com.example.test05.ui.tabs.postnext.PostData
import com.example.test05.ui.tabs.postnext.PostPrivacy
import com.example.test05.utils.JsonDataLoader
import kotlinx.coroutines.*
import java.util.*

class PostNextPresenter(
    private val dataLoader: JsonDataLoader
) : PostNextContract.Presenter {
    
    private var view: PostNextContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var currentPostData = PostData()
    private var currentTitle = ""
    private var currentContent = ""
    private val currentTags = mutableListOf<String>()
    private val currentTopics = mutableListOf<String>()
    private var currentPrivacy = PostPrivacy.PRIVATE
    private var currentLocation = ""
    
    override fun attachView(view: PostNextContract.View) {
        this.view = view
    }
    
    override fun detachView() {
        view = null
        presenterScope.cancel()
    }
    
    override fun initWithPostData(postData: PostData) {
        currentPostData = postData
        currentTitle = postData.title
        currentContent = postData.content
        currentTags.clear()
        currentTags.addAll(postData.tags)
        currentTopics.clear()
        currentTopics.addAll(postData.topics)
        currentPrivacy = postData.privacy
        currentLocation = postData.location
    }
    
    override fun onTitleChanged(title: String) {
        currentTitle = title
    }
    
    override fun onContentChanged(content: String) {
        currentContent = content
    }
    
    override fun onTagAdded(tag: String) {
        if (!currentTags.contains(tag)) {
            currentTags.add(tag)
        }
    }
    
    override fun onTagRemoved(tag: String) {
        currentTags.remove(tag)
    }
    
    override fun onTopicAdded(topic: String) {
        if (!currentTopics.contains(topic)) {
            currentTopics.add(topic)
        }
    }
    
    override fun onLocationClicked() {
        // TODO: Implement location selection
    }
    
    override fun onPrivacyClicked() {
        view?.showPrivacySelector(currentPrivacy)
    }
    
    override fun onPrivacySelected(privacy: PostPrivacy) {
        currentPrivacy = privacy
        view?.hidePrivacySelector()
        view?.updatePrivacy(privacy)
    }
    
    override fun onAddImageClicked() {
        // TODO: Implement image selection
    }
    
    override fun onImageRemoved(imageIndex: Int) {
        // TODO: Implement image removal
    }
    
    override fun onMentionUserClicked() {
        // TODO: Implement user mention
    }
    
    override fun onVoteClicked() {
        // TODO: Implement vote creation
    }
    
    override fun onBackClicked() {
        view?.navigateBack()
    }
    
    override fun onPreviewClicked() {
        // TODO: Implement preview
    }
    
    override fun onSettingsClicked() {
        // TODO: Implement settings
    }
    
    override fun onSaveDraftClicked() {
        presenterScope.launch {
            try {
                view?.showLoading(true)
                // TODO: Save draft logic
                view?.showSuccess("草稿已保存")
            } catch (e: Exception) {
                view?.showError("保存草稿失败: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }
    
    override fun onPublishClicked() {
        presenterScope.launch {
            try {
                view?.showLoading(true)
                
                if (currentTitle.isBlank() && currentContent.isBlank()) {
                    view?.showError("请添加标题或内容")
                    return@launch
                }
                
                // Map PostPrivacy to NoteVisibility
                val noteVisibility = when (currentPrivacy) {
                    PostPrivacy.PUBLIC -> NoteVisibility.PUBLIC
                    PostPrivacy.PRIVATE -> NoteVisibility.PRIVATE
                    PostPrivacy.FRIENDS_ONLY -> NoteVisibility.FRIENDS_ONLY
                    PostPrivacy.FOLLOWERS_ONLY -> NoteVisibility.FRIENDS_ONLY
                    PostPrivacy.MUTUAL_FRIENDS -> NoteVisibility.SPECIFIC_FRIENDS
                }
                
                // Create new note
                val newNote = Note(
                    id = "note_${System.currentTimeMillis()}",
                    title = currentTitle.ifBlank { 
                        if (currentContent.isNotBlank()) {
                            currentContent.take(20) + "..."
                        } else {
                            "无标题笔记"
                        }
                    },
                    content = currentContent,
                    type = if (currentPostData.images.isNotEmpty()) NoteType.IMAGE else NoteType.TEXT,
                    author = dataLoader.getCurrentUser() ?: throw Exception("用户信息不存在"),
                    coverImage = currentPostData.images.firstOrNull(),
                    images = currentPostData.images,
                    tags = currentTags.toList(),
                    topics = currentTopics.toList(),
                    visibility = noteVisibility,
                    createdAt = Date(),
                    publishedAt = Date()
                )
                
                // Save note to JSON data
                dataLoader.saveNote(newNote)
                view?.showSuccess("笔记发布成功")
                // Navigate to NoteDetail instead of back
                view?.navigateToNoteDetail(newNote.id)
                
            } catch (e: Exception) {
                view?.showError("发布失败: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }
}