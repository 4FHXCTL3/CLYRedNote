# 开发要求
## 任务介绍
我在做安卓开发，并研究如何利用AI来复刻公开的商业APP（比如饿了么、微信），但我的复刻无需100%相似，我只需要做到如下几点：
- 1、UI结构基本相似，图标做不到一模一样不要紧，类似即可。
- 2、不需要做出APP的所有页面，例如微信APP，我们只做聊天、通讯录、朋友圈这些典型页面就可以了，其他的页面，比如小程序、微信游戏，我们全部设置为空页面，只保留一个入口给后续开发接入，暂时不需要处理。
- 3、不需要实现动态，例如通讯录列表我们给他固定死（联系人就只有这些），饿了么的外卖商家我也给他固定死（就只有这些店铺）。
- 4、不需要数据库，但是需要保存重要的信号（这些信号我会指定），因为我要把构建的“虚拟APP”拿去给AI操作，例如我要它“给小明的朋友圈点赞”，为了check其任务有没有完成，我至少需要一个json来存储“所有人的朋友圈的点赞列表”。
- 5、不需要登陆，不需要联网，可以理解为我们这APP仅仅自己使用，哪怕UI上显示发送了一条消息给小明，小明也不可能真的收到这个信息。
- 6、开发过程，可以考虑截图给AI，让AI实现开发任务。
- 7、没有任何限制，对框架等的选取完全取决于开发者习惯
- 8、不需要通知权限等复杂的东西，至多需要下保存图片到本地、读取相册这样的权限

## 任务要求
1. 开发的时候可以采取逐步开发的策略，慢慢补充需求，慢慢新增数据字段等
2. 避免单个文件代码过长，**必须要遵循MVP模式**
3. 按照指定的页面结构、参考给定的截图作为UI设计指导
4. 严格按照已经定义好的数据结构`app\src\main\java\com\example\CLYRedNote\model`和已有的数据`app\src\main\assets\data`
5. 按照用户要求进行开发，除非用户指定，否则不要引入新的内容。
    - 你只需要参考我给你的截图，**不要脑补**截图中各个按钮点击后会达到的效果。
    - ***截图里面的信息（例如人的名字、人的头像）不必参考***，因为我们自己预设了已有的数据`app\src\main\assets\data`，图片参考app\src\main\assets\image
6. UI中，各个图标若找不到合适的，可以用emjoy表情代替。但优先考虑`androidx.compose.material.icons.Icons, androidx.compose.material.icons.filled.AccountCircle`等
7. 严禁执行删除文件等危险操作。
8. 每当你修改完代码，记得更新`README.md`文件，并且确定build正常了再交付给我。禁止修改所有开发要求文件.
9. 如果你需要图片支持，请给我反馈，告诉我需要什么图片。如果你使用了我给你的图片，记得把图片名改为一个可读的名字，便于后期维护。

## 页面树
```json
{
"MainActivity":[
	"HomeTab",//finished
	"MarketTab"，//finished
	"PostTab"，//finished
	"MessagesTab",//finished
	"MeTab"//finished
],
    
    "HomeTab":[
        "NoteDetail",//finished
        "SearchTab"//finished
    ],
    "Market":[
        "Cart"//finished
    ],
    "MeTab":[
        "FolllowingTab",//finished
        "FanTab",//finished
        "Collection",//finished
        "ProfileEdit",//finished
        "Settings"//NowToDo!!
    ],
    "MessagesTab":[
        "CommentAtTab"//finished
    ],
    "PostTab":[
        "PostNext"//finished
    ],
    	"FolllowingTab":[
            "BloggerDetail"//finished
        ],
    	"SearchTab":[
            "SearchDetail"//finished
        ],
    		"BloggerDetail":[
                "MessageDetail"//finished
            ]
}
```

## 可参考的截图
UIReference/Settings.jpg


## 你当前的任务
设计Settings页面，该页面的要素包括:
1. 页面上方是居中标题“设置”，左侧有返回箭头；接着是一系列设置选项，每个选项都有对应的图标和名称，右侧有箭头可进入详细设置页面，这些选项包括“账号与安全”“通用设置”“通知设置”“隐私设置”“存储空间”“内容偏好调节”“收货地址”“添加小组件”“未成年人模式”“新功能体验”“帮助与客服”“关于小红书”等，按顺序依次排列。
2. 该界面的进入路线“MeTab->编辑资料右边的设置图案”
3. 尽可能与参考截图一致