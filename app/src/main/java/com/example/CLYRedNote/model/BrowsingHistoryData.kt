package com.example.CLYRedNote.model

import java.util.Date
import java.util.Calendar

/**
 * 浏览历史示例数据
 * 包含3条具体的浏览记录数据
 */
object BrowsingHistoryData {
    
    // 示例用户数据
    private val sampleUser1 = User(
        id = "user_001",
        username = "beautylife_lisa",
        nickname = "Lisa美妆达人",
        avatar = "image/avatar1.jpg",
        bio = "分享美妆心得，让每个女孩都美美哒",
        followerCount = 15200,
        followingCount = 892,
        noteCount = 145,
        isVerified = true,
        level = 5
    )
    
    private val sampleUser2 = User(
        id = "user_002", 
        username = "foodie_zhang",
        nickname = "张大厨的美食日记",
        avatar = "image/avatar2.jpg",
        bio = "用心烹饪，分享人间烟火味",
        followerCount = 28900,
        followingCount = 456,
        noteCount = 328,
        isVerified = true,
        level = 6
    )
    
    private val sampleUser3 = User(
        id = "user_003",
        username = "travel_wanderer",
        nickname = "漫游者的足迹",
        avatar = "image/avatar3.jpg", 
        bio = "用脚步丈量世界，用镜头记录美好",
        followerCount = 42100,
        followingCount = 1230,
        noteCount = 567,
        isVerified = true,
        level = 7
    )
    
    // 当前用户（浏览者）
    private val currentUser = User(
        id = "user_current",
        username = "CLY",
        nickname = "CLY",
        followerCount = 128,
        followingCount = 89,
        noteCount = 23,
        level = 2
    )
    
    /**
     * 3条具体的浏览历史数据
     */
    fun getSampleBrowsingHistories(): List<BrowsingHistory> {
        val calendar = Calendar.getInstance()
        
        // 第一条记录：今天上午浏览美妆笔记
        calendar.add(Calendar.HOUR_OF_DAY, -3) // 3小时前
        val browsingTime1 = calendar.time
        
        val history1 = BrowsingHistory(
            id = "browse_001",
            userId = currentUser.id,
            noteId = "note_beauty_001",
            noteTitle = "夏日清透底妆教程｜让你的妆容持久一整天",
            noteAuthor = sampleUser1,
            browsedAt = browsingTime1,
            browsingDuration = 145000, // 2分25秒
            viewType = ViewType.DETAIL,
            sourceType = SourceType.HOME_FEED,
            deviceInfo = DeviceInfo(
                deviceType = "smartphone",
                osVersion = "Android 14",
                appVersion = "1.0.0",
                screenResolution = "1080x2400",
                networkType = "WiFi"
            ),
            readingProgress = 0.85f, // 阅读了85%
            interactions = listOf(
                BrowsingInteraction(
                    id = "interaction_001",
                    interactionType = InteractionType.SCROLL,
                    timestamp = Date(browsingTime1.time + 15000),
                    parameters = mapOf("scrollPosition" to 0.3)
                ),
                BrowsingInteraction(
                    id = "interaction_002", 
                    interactionType = InteractionType.CLICK_IMAGE,
                    timestamp = Date(browsingTime1.time + 45000),
                    targetId = "image_makeup_step3",
                    parameters = mapOf("imageIndex" to 3)
                ),
                BrowsingInteraction(
                    id = "interaction_003",
                    interactionType = InteractionType.LIKE,
                    timestamp = Date(browsingTime1.time + 120000),
                    parameters = mapOf("isLiked" to true)
                ),
                BrowsingInteraction(
                    id = "interaction_004",
                    interactionType = InteractionType.COLLECT,
                    timestamp = Date(browsingTime1.time + 140000),
                    parameters = mapOf("folderName" to "美妆教程")
                )
            ),
            exitType = ExitType.COLLECT,
            noteType = NoteType.IMAGE,
            noteCoverImage = "image/makeup_tutorial_cover.jpg",
            isCompleteRead = true
        )
        
        // 第二条记录：昨天下午浏览美食笔记
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 15)
        calendar.set(Calendar.MINUTE, 30)
        val browsingTime2 = calendar.time
        
        val history2 = BrowsingHistory(
            id = "browse_002",
            userId = currentUser.id,
            noteId = "note_food_001", 
            noteTitle = "麻辣香锅制作全攻略｜在家也能做出餐厅级别的味道",
            noteAuthor = sampleUser2,
            browsedAt = browsingTime2,
            browsingDuration = 89000, // 1分29秒
            viewType = ViewType.DETAIL,
            sourceType = SourceType.SEARCH_RESULT,
            deviceInfo = DeviceInfo(
                deviceType = "smartphone",
                osVersion = "Android 14", 
                appVersion = "1.0.0",
                screenResolution = "1080x2400",
                networkType = "4G"
            ),
            readingProgress = 0.45f, // 只阅读了45%
            interactions = listOf(
                BrowsingInteraction(
                    id = "interaction_005",
                    interactionType = InteractionType.SCROLL,
                    timestamp = Date(browsingTime2.time + 20000),
                    parameters = mapOf("scrollPosition" to 0.2)
                ),
                BrowsingInteraction(
                    id = "interaction_006",
                    interactionType = InteractionType.CLICK_AUTHOR,
                    timestamp = Date(browsingTime2.time + 35000),
                    targetId = sampleUser2.id,
                    parameters = mapOf("action" to "viewProfile")
                ),
                BrowsingInteraction(
                    id = "interaction_007",
                    interactionType = InteractionType.FOLLOW,
                    timestamp = Date(browsingTime2.time + 85000),
                    targetId = sampleUser2.id,
                    parameters = mapOf("isFollowed" to true)
                )
            ),
            exitType = ExitType.FOLLOW,
            noteType = NoteType.VIDEO,
            noteCoverImage = "image/spicy_hotpot_cover.jpg",
            isCompleteRead = false
        )
        
        // 第三条记录：3天前晚上浏览旅行笔记
        calendar.add(Calendar.DAY_OF_MONTH, -2) // 总共3天前
        calendar.set(Calendar.HOUR_OF_DAY, 21)
        calendar.set(Calendar.MINUTE, 15)
        val browsingTime3 = calendar.time
        
        val history3 = BrowsingHistory(
            id = "browse_003",
            userId = currentUser.id,
            noteId = "note_travel_001",
            noteTitle = "云南大理古城深度游｜小众景点打卡攻略",
            noteAuthor = sampleUser3,
            browsedAt = browsingTime3,
            browsingDuration = 312000, // 5分12秒
            viewType = ViewType.DETAIL,
            sourceType = SourceType.RECOMMENDATION,
            deviceInfo = DeviceInfo(
                deviceType = "smartphone",
                osVersion = "Android 14",
                appVersion = "1.0.0", 
                screenResolution = "1080x2400",
                networkType = "WiFi"
            ),
            readingProgress = 1.0f, // 完整阅读
            interactions = listOf(
                BrowsingInteraction(
                    id = "interaction_008",
                    interactionType = InteractionType.SCROLL,
                    timestamp = Date(browsingTime3.time + 25000),
                    parameters = mapOf("scrollPosition" to 0.1)
                ),
                BrowsingInteraction(
                    id = "interaction_009",
                    interactionType = InteractionType.CLICK_IMAGE,
                    timestamp = Date(browsingTime3.time + 68000),
                    targetId = "image_dali_sunset",
                    parameters = mapOf("imageIndex" to 5, "action" to "fullscreen")
                ),
                BrowsingInteraction(
                    id = "interaction_010",
                    interactionType = InteractionType.CLICK_TAG,
                    timestamp = Date(browsingTime3.time + 125000),
                    targetId = "tag_dali",
                    parameters = mapOf("tagName" to "大理旅游")
                ),
                BrowsingInteraction(
                    id = "interaction_011",
                    interactionType = InteractionType.COMMENT,
                    timestamp = Date(browsingTime3.time + 280000),
                    targetId = "comment_travel_001",
                    parameters = mapOf("commentText" to "太美了！已经列入我的旅行清单")
                ),
                BrowsingInteraction(
                    id = "interaction_012",
                    interactionType = InteractionType.SHARE,
                    timestamp = Date(browsingTime3.time + 305000),
                    parameters = mapOf("shareTarget" to "微信好友", "shareType" to "link")
                )
            ),
            exitType = ExitType.SHARE,
            noteType = NoteType.MIXED,
            noteCoverImage = "image/dali_ancient_city_cover.jpg",
            isCompleteRead = true
        )
        
        return listOf(history1, history2, history3)
    }
    
    /**
     * 获取示例浏览统计数据
     */
    fun getSampleBrowsingStatistics(): BrowsingStatistics {
        return BrowsingStatistics(
            userId = currentUser.id,
            totalBrowsingTime = 546000, // 9分6秒总浏览时长
            totalViewCount = 3,
            averageBrowsingTime = 182000, // 平均3分2秒
            favoriteCategories = listOf("美妆", "美食", "旅行", "穿搭"),
            mostActiveHours = listOf(9, 10, 15, 21, 22), // 最活跃的时段
            frequentAuthors = listOf(sampleUser1, sampleUser2, sampleUser3),
            readingCompletionRate = 0.77f, // 77%的阅读完成率
            lastUpdated = Date()
        )
    }
}