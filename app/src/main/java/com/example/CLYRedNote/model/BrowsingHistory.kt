package com.example.CLYRedNote.model

import java.util.Date

/**
 * 浏览历史数据模型
 * 记录用户浏览笔记的详细信息
 */
data class BrowsingHistory(
    val id: String,                          // 浏览记录唯一ID
    val userId: String,                      // 浏览用户ID
    val noteId: String,                      // 被浏览笔记ID
    val noteTitle: String,                   // 笔记标题
    val noteAuthor: User,                    // 笔记作者信息
    val browsedAt: Date,                     // 浏览时间
    val browsingDuration: Long = 0,          // 浏览时长（毫秒）
    val viewType: ViewType = ViewType.DETAIL, // 浏览类型
    val sourceType: SourceType = SourceType.DIRECT, // 来源类型
    val deviceInfo: DeviceInfo? = null,      // 设备信息
    val readingProgress: Float = 0.0f,       // 阅读进度（0.0-1.0）
    val interactions: List<BrowsingInteraction> = emptyList(), // 交互行为
    val exitType: ExitType = ExitType.NORMAL, // 退出方式
    val noteType: NoteType = NoteType.TEXT,  // 笔记类型
    val noteCoverImage: String? = null,      // 笔记封面图
    val isCompleteRead: Boolean = false      // 是否完整阅读
)

/**
 * 浏览类型枚举
 */
enum class ViewType {
    DETAIL,      // 详情页浏览
    PREVIEW,     // 预览浏览
    SEARCH,      // 搜索结果浏览
    RECOMMENDATION // 推荐浏览
}

/**
 * 来源类型枚举
 */
enum class SourceType {
    DIRECT,         // 直接进入
    HOME_FEED,      // 首页信息流
    SEARCH_RESULT,  // 搜索结果
    USER_PROFILE,   // 用户主页
    RECOMMENDATION, // 推荐
    SHARE_LINK,     // 分享链接
    NOTIFICATION,   // 通知消息
    COLLECTION,     // 收藏夹
    FOLLOW_FEED     // 关注动态
}

/**
 * 退出方式枚举
 */
enum class ExitType {
    NORMAL,      // 正常返回
    SHARE,       // 分享后退出
    COLLECT,     // 收藏后退出
    LIKE,        // 点赞后退出
    COMMENT,     // 评论后退出
    FOLLOW,      // 关注后退出
    BACK_PRESS,  // 物理返回键
    TAB_SWITCH   // 切换Tab
}

/**
 * 设备信息
 */
data class DeviceInfo(
    val deviceType: String,     // 设备类型（手机/平板）
    val osVersion: String,      // 系统版本
    val appVersion: String,     // 应用版本
    val screenResolution: String, // 屏幕分辨率
    val networkType: String     // 网络类型（WiFi/4G/5G）
)

/**
 * 浏览交互行为
 */
data class BrowsingInteraction(
    val id: String,
    val interactionType: InteractionType,
    val timestamp: Date,
    val targetId: String? = null,      // 交互目标ID（如评论ID、用户ID等）
    val parameters: Map<String, Any> = emptyMap() // 交互参数
)

/**
 * 交互类型枚举
 */
enum class InteractionType {
    SCROLL,         // 滚动
    ZOOM,           // 缩放
    CLICK_IMAGE,    // 点击图片
    CLICK_TAG,      // 点击标签
    CLICK_AUTHOR,   // 点击作者
    CLICK_PRODUCT,  // 点击商品
    CLICK_COMMENT,  // 点击评论
    SHARE,          // 分享
    LIKE,           // 点赞
    COLLECT,        // 收藏
    COMMENT,        // 评论
    FOLLOW,         // 关注
    REPORT          // 举报
}

/**
 * 浏览历史统计信息
 */
data class BrowsingStatistics(
    val userId: String,
    val totalBrowsingTime: Long,              // 总浏览时长
    val totalViewCount: Int,                  // 总浏览次数
    val averageBrowsingTime: Long,            // 平均浏览时长
    val favoriteCategories: List<String>,     // 偏好分类
    val mostActiveHours: List<Int>,           // 最活跃时段
    val frequentAuthors: List<User>,          // 常看作者
    val readingCompletionRate: Float,         // 阅读完成率
    val lastUpdated: Date = Date()
)

/**
 * 浏览历史查询条件
 */
data class BrowsingHistoryFilter(
    val userId: String,
    val startDate: Date? = null,
    val endDate: Date? = null,
    val noteType: NoteType? = null,
    val viewType: ViewType? = null,
    val sourceType: SourceType? = null,
    val authorId: String? = null,
    val minDuration: Long? = null,
    val maxDuration: Long? = null,
    val isCompleteRead: Boolean? = null,
    val limit: Int = 50,
    val offset: Int = 0
)