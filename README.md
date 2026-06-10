# Typecho Manager — Compose

**会飞的肥猪猪** — Typecho 博客管理客户端 (Jetpack Compose)

## 技术栈

- **UI**: Jetpack Compose + Material 3 Expressive
- **架构**: MVVM + Hilt DI
- **网络**: OkHttp (XML-RPC + Companion API)
- **存储**: DataStore Preferences
- **图片**: Coil
- **序列化**: kotlinx.serialization

## 功能

- 📝 文章管理 (CRUD)
- 📎 附件管理 (上传/删除/复制URL)
- 🏷️ 分类筛选
- 🎨 Material 3 Expressive 动态色彩
- 🌙 亮色/暗色主题
- 📱 原生 Android 体验

## 构建

```bash
./gradlew assembleDebug
```

## 架构

```
app/src/main/java/com/flypigs/typechomanager/
├── data/
│   ├── local/      # ConfigDataStore
│   ├── model/      # Post, Attachment, BlogConfig, Category
│   ├── remote/     # XmlRpcClient, CompanionApiClient
│   └── repository/ # PostRepository, ConfigRepository
├── di/             # AppModule (Hilt)
├── ui/
│   ├── theme/      # M3 Expressive (Color, Type, Shape, Theme)
│   ├── navigation/ # NavGraph, Screen routes
│   ├── home/       # HomeScreen + ViewModel
│   ├── posts/      # PostsScreen + ViewModel
│   ├── editor/     # EditorScreen + ViewModel
│   ├── attachments/ # AttachmentsScreen + ViewModel
│   ├── settings/   # SettingsScreen + ViewModel
│   ├── setup/      # SetupScreen + ViewModel
│   └── components/ # BottomNavBar, shared widgets
└── TypechoApp.kt   # Application (HiltAndroidApp)
```

## License

MIT
