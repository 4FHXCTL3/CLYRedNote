# CLYRedNote - 小红书克隆应用

基于 Android Compose 的小红书克隆应用，用于 GUI Agent 测试。

## 项目结构

### 数据模型 (Model)
位置: `app/src/main/java/com/example/CLYRedNote/model/`

- **User.kt** - 用户相关数据结构
- **Note.kt** - 笔记相关数据结构  
- **Comment.kt** - 评论数据结构
- **Message.kt** - 消息和通知数据结构
- **Product.kt** - 商品相关数据结构
- **Shopping.kt** - 购物车和订单数据结构
- **Follow.kt** - 关注关系数据结构
- **Collection.kt** - 收藏和点赞数据结构
- **Search.kt** - 搜索相关数据结构

### 初始数据 (Assets)
位置: `app/src/main/assets/data/`

- **users.json** - 用户数据，包含6个测试用户
- **notes.json** - 笔记数据，包含不同类型的笔记内容
- **comments.json** - 评论数据，包含笔记评论和回复
- **follows.json** - 关注关系数据
- **products.json** - 商品数据，包含运动鞋、美妆、服装等
- **messages.json** - 消息数据，包含文本、表情、笔记分享等
- **collections.json** - 收藏数据
- **shopping_cart.json** - 购物车数据
- **orders.json** - 订单数据
- **topics.json** - 话题数据

### 界面开发 (UI)
位置: `app/src/main/java/com/example/test05/`

#### 已完成功能

1. **HomeTab页面** (首页)
   - 顶部导航栏：关注、发现、同城三个选项卡，支持切换
   - 分类筛选行：推荐、视频、直播、短剧、穿搭、学习
   - 笔记瀑布流：双列网格布局，支持垂直滚动
   - 笔记卡片：封面图、标题、作者信息、点赞数和点赞功能
   - 视频笔记标识：显示播放按钮
   - 图片加载：使用真实资源图片替代emoji占位符
   - 数据加载：根据选项卡和分类筛选不同笔记内容

2. **MarketTab页面** (市集)
   - 搜索栏：支持文本输入和实时搜索功能
   - 分类选项卡：推荐、1年1度、运动、穿搭、美护、家居、生活
   - 功能图标网格：市集直播、买手臻选、新品首发、超级满减等
   - 商品瀑布流：双列网格布局，支持垂直滚动
   - 商品卡片：商品图片、名称、价格、原价、销量信息
   - 数据加载：根据分类和搜索条件筛选商品
   - 图片加载：使用真实资源图片替代系统图标

3. **MessagesTab页面** (消息)
   - 顶部标题栏：居中显示"消息"标题，右侧搜索和添加图标
   - 功能图标行：赞和收藏、新增关注、评论和@三个快捷入口
   - 对话列表：显示系统消息、客服消息、活动消息、群聊对话等
   - 对话项目：头像、名称、最后消息内容、时间戳、未读标识
   - 时间格式：今日显示时间，昨天显示"昨天"，本周显示星期，更早显示日期
   - 未读消息：红点标识未读对话

4. **PostTab页面** (发布)
   - 顶部标题栏：左侧关闭按钮(×)，右侧"下一步"发布按钮
   - 图片上传区域：支持点击添加图片，显示相机图标占位符
   - 文本编辑区域："写想法"标题，支持多行文本输入
   - 内容输入框：占位符文本"说点什么或提个问题..."
   - 底部功能选项："写长文"功能入口，支持千字以上内容
   - 交互功能：发布验证、成功提示、返回首页导航
   - 浅蓝色背景设计，符合编辑界面风格

5. **MeTab页面** (我的页面)
   - 用户资料展示区：头像、昵称、小红书号、IP属地
   - 统计数据：关注数、粉丝数、获赞与收藏数
   - 功能按钮：编辑资料、设置
   - 创作灵感和群聊卡片
   - 笔记分类选项卡：笔记、收藏、赞过
   - 底部分享区域

6. **主导航**
   - 底部导航栏：首页、市集、发布(+)、消息、我
   - 当前所有五个Tab均已完全实现：HomeTab、MarketTab、PostTab、MessagesTab、MeTab
   - 默认启动显示首页

#### 技术架构

- **MVP 架构模式**
  - View: HomeTabScreen.kt, MarketTabScreen.kt, PostTabScreen.kt, MessagesTabScreen.kt, MeTabScreen.kt (UI层)
  - Presenter: HomeTabPresenter.kt, MarketTabPresenter.kt, PostTabPresenter.kt, MessagesTabPresenter.kt, MeTabPresenter.kt (业务逻辑层)
  - Contract: HomeTabContract.kt, MarketTabContract.kt, PostTabContract.kt, MessagesTabContract.kt, MeTabContract.kt (接口定义)

- **数据加载**
  - JsonDataLoader.kt: 加载 assets 中的 JSON 数据
  - 支持用户、笔记、商品、消息、收藏、关注等数据的解析

#### UI 特点

- 深色主题风格，符合小红书夜间模式
- 使用 Material Design 3 组件
- 响应式布局，适配不同屏幕尺寸
- 图标使用 Material Icons 和 Emoji 表情
- 符合原版小红书的 UI 结构和交互逻辑

## 构建状态

✅ **构建成功** - 2025年10月19日

- 所有 Kotlin 代码编译通过
- 依赖项正确配置 (包括 Gson 用于 JSON 解析)
- UI 渲染正常
- MVP 架构完整实现
- 所有五个主要Tab功能完整：HomeTab、MarketTab、PostTab、MessagesTab、MeTab

## 待开发功能

*所有主要功能已完成开发*

## 开发说明

- 项目使用 Android Gradle Plugin 8.6.0
- 目标 SDK: 33, 编译 SDK: 36
- 最低支持 SDK: 33
- 使用 Jetpack Compose UI 框架
- 遵循 MVP 架构模式，确保代码可维护性

## 数据说明

所有测试数据均为虚拟数据，用于 AI Agent 测试。当前用户为 "CLY"，ID 为 "user_current"。数据包含了测试任务所需的各种场景，如关注关系、笔记内容、商品信息等。