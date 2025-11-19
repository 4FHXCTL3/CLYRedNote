package com.example.test05.utils

import android.content.Context
import com.example.CLYRedNote.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * 浏览历史管理器
 * 负责记录和管理用户的浏览历史
 */
class BrowsingHistoryManager(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private var browsingStartTime: Date? = null
    private var currentNoteId: String? = null
    private var interactions: MutableList<BrowsingInteraction> = mutableListOf()
    
    companion object {
        private var browsingHistories: MutableList<BrowsingHistory> = mutableListOf()
        
        // 初始化示例数据
        init {
            browsingHistories.addAll(BrowsingHistoryData.getSampleBrowsingHistories())
        }
    }
    
    /**
     * 开始记录浏览会话
     */
    fun startBrowsingSession(
        noteId: String,
        sourceType: SourceType = SourceType.DIRECT,
        viewType: ViewType = ViewType.DETAIL
    ) {
        browsingStartTime = Date()
        currentNoteId = noteId
        interactions.clear()
        
        // 可以在这里记录进入事件
        recordInteraction(InteractionType.SCROLL, parameters = mapOf("action" to "enter"))
    }
    
    /**
     * 记录交互行为
     */
    fun recordInteraction(
        interactionType: InteractionType,
        targetId: String? = null,
        parameters: Map<String, Any> = emptyMap()
    ) {
        currentNoteId?.let { noteId ->
            val interaction = BrowsingInteraction(
                id = "interaction_${System.currentTimeMillis()}",
                interactionType = interactionType,
                timestamp = Date(),
                targetId = targetId,
                parameters = parameters
            )
            interactions.add(interaction)
        }
    }
    
    /**
     * 结束浏览会话并保存历史记录
     */
    fun endBrowsingSession(
        note: Note?,
        exitType: ExitType = ExitType.NORMAL,
        readingProgress: Float = 0.0f
    ) {
        browsingStartTime?.let { startTime ->
            currentNoteId?.let { noteId ->
                note?.let { noteData ->
                    val browsingDuration = Date().time - startTime.time
                    val isCompleteRead = readingProgress >= 0.8f // 80%以上认为是完整阅读
                    
                    val browsingHistory = BrowsingHistory(
                        id = "browse_${System.currentTimeMillis()}",
                        userId = "user_current", // 当前用户ID
                        noteId = noteId,
                        noteTitle = noteData.title,
                        noteAuthor = noteData.author,
                        browsedAt = startTime,
                        browsingDuration = browsingDuration,
                        viewType = ViewType.DETAIL,
                        sourceType = SourceType.DIRECT, // 可以根据实际来源设置
                        deviceInfo = getDeviceInfo(),
                        readingProgress = readingProgress,
                        interactions = interactions.toList(),
                        exitType = exitType,
                        noteType = noteData.type,
                        noteCoverImage = noteData.coverImage,
                        isCompleteRead = isCompleteRead
                    )
                    
                    // 保存浏览历史
                    saveBrowsingHistory(browsingHistory)
                }
            }
        }
        
        // 重置会话数据
        browsingStartTime = null
        currentNoteId = null
        interactions.clear()
    }
    
    /**
     * 保存浏览历史
     */
    private fun saveBrowsingHistory(browsingHistory: BrowsingHistory) {
        scope.launch {
            try {
                browsingHistories.add(0, browsingHistory) // 添加到列表开头
                
                // 限制历史记录数量，保留最近1000条
                if (browsingHistories.size > 1000) {
                    browsingHistories = browsingHistories.take(1000).toMutableList()
                }
                
                // 这里可以添加持久化存储逻辑，比如存储到数据库或文件
                println("浏览历史已保存: ${browsingHistory.noteTitle}")
            } catch (e: Exception) {
                println("保存浏览历史失败: ${e.message}")
            }
        }
    }
    
    /**
     * 获取设备信息
     */
    private fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceType = "smartphone",
            osVersion = android.os.Build.VERSION.RELEASE,
            appVersion = "1.0.0",
            screenResolution = "${context.resources.displayMetrics.widthPixels}x${context.resources.displayMetrics.heightPixels}",
            networkType = "WiFi" // 可以通过网络管理器获取实际网络类型
        )
    }
    
    /**
     * 获取用户的浏览历史列表
     */
    fun getBrowsingHistories(
        userId: String = "user_current",
        limit: Int = 50
    ): List<BrowsingHistory> {
        return browsingHistories
            .filter { it.userId == userId }
            .take(limit)
    }
    
    /**
     * 获取浏览统计信息
     */
    fun getBrowsingStatistics(userId: String = "user_current"): BrowsingStatistics {
        val userHistories = browsingHistories.filter { it.userId == userId }
        
        if (userHistories.isEmpty()) {
            return BrowsingStatistics(
                userId = userId,
                totalBrowsingTime = 0,
                totalViewCount = 0,
                averageBrowsingTime = 0,
                favoriteCategories = emptyList(),
                mostActiveHours = emptyList(),
                frequentAuthors = emptyList(),
                readingCompletionRate = 0.0f
            )
        }
        
        val totalTime = userHistories.sumOf { it.browsingDuration }
        val averageTime = totalTime / userHistories.size
        val completionRate = userHistories.count { it.isCompleteRead }.toFloat() / userHistories.size
        
        return BrowsingStatistics(
            userId = userId,
            totalBrowsingTime = totalTime,
            totalViewCount = userHistories.size,
            averageBrowsingTime = averageTime,
            favoriteCategories = emptyList(), // 可以根据浏览的笔记类型统计
            mostActiveHours = emptyList(), // 可以根据浏览时间统计活跃时段
            frequentAuthors = userHistories.map { it.noteAuthor }.distinctBy { it.id },
            readingCompletionRate = completionRate
        )
    }
    
    /**
     * 清除浏览历史
     */
    fun clearBrowsingHistory(userId: String = "user_current") {
        browsingHistories.removeAll { it.userId == userId }
    }
}