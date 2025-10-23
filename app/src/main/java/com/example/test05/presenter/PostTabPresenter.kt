package com.example.test05.presenter

import com.example.CLYRedNote.model.Note
import com.example.CLYRedNote.model.NoteType
import com.example.CLYRedNote.model.NoteVisibility
import com.example.test05.ui.tabs.post.PostTabContract
import com.example.test05.utils.JsonDataLoader
import kotlinx.coroutines.*

class PostTabPresenter(
    private val dataLoader: JsonDataLoader
) : PostTabContract.Presenter {
    
    private var view: PostTabContract.View? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var currentTitle: String = ""
    private var currentContent: String = ""
    private var currentImages: MutableList<String> = mutableListOf()
    
    override fun attachView(view: PostTabContract.View) {
        this.view = view
    }
    
    override fun detachView() {
        this.view = null
        scope.cancel()
    }
    
    override fun onCloseClicked() {
        view?.navigateBack()
    }
    
    override fun onNextStepClicked() {
        view?.navigateToPostNext(currentTitle, currentContent, currentImages)
    }
    
    override fun onTitleChanged(title: String) {
        currentTitle = title
    }
    
    override fun onContentChanged(content: String) {
        currentContent = content
    }
    
    override fun onAddImageClicked() {
        view?.showImagePicker()
    }
    
    override fun onImageSelected(imagePath: String) {
        currentImages.add(imagePath)
        view?.showImages(currentImages)
    }
    
    override fun onLongTextClicked() {
        // TODO: Switch to long text mode
        view?.showSuccess("切换到长文模式")
    }
    
    override fun publishNote() {
        if (!validateInput()) {
            return
        }
        
        view?.showLoading(true)
        
        scope.launch {
            try {
                // Simulate publishing
                delay(1000)
                
                val note = createNote()
                // In a real app, this would save to database or send to server
                
                view?.showSuccess("笔记发布成功")
                delay(500)
                view?.navigateBack()
            } catch (e: Exception) {
                view?.showError("发布失败: ${e.message}")
            } finally {
                view?.showLoading(false)
            }
        }
    }
    
    private fun validateInput(): Boolean {
        return when {
            currentTitle.isBlank() && currentContent.isBlank() -> {
                view?.showError("请输入标题或内容")
                false
            }
            else -> true
        }
    }
    
    private fun createNote(): Note {
        val currentUser = dataLoader.getCurrentUser()
        
        return Note(
            id = "note_${System.currentTimeMillis()}",
            title = currentTitle.ifBlank { "无标题" },
            content = currentContent,
            type = if (currentImages.isNotEmpty()) NoteType.IMAGE else NoteType.TEXT,
            author = currentUser ?: throw IllegalStateException("用户未登录"),
            images = currentImages.toList(),
            coverImage = currentImages.firstOrNull(),
            visibility = NoteVisibility.PUBLIC
        )
    }
}