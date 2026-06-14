<p align="center">
  <img src="https://img.shields.io/badge/Version-v2.0.1-brightgreen?style=flat-square" alt="Version">
  <img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android" alt="Platform">
  <img src="https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=flat-square&logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/API-26+-orange?style=flat-square" alt="Min API">
</p>

<h1 align="center">Typecho Manager</h1>

<p align="center">
  <b>📱 随时随地管理你的 Typecho 博客</b><br>
  Material 3 Expressive · Jetpack Compose · 原生 Android 体验
</p>

---

## 📖 项目简介

Typecho Manager 是一款专为 [Typecho](https://typecho.org/) 博客打造的 Android 客户端，采用最新的 Material 3 Expressive 设计语言，基于 Jetpack Compose 构建。让你在手机上也能高效管理博客——写文章、传图片、看数据，随时随地。

## ✨ 核心功能

| 功能 | 说明 |
|------|------|
| 🏠 **首页仪表盘** | 博客概览、快速入口、最近文章横滑浏览 |
| 📝 **文章管理** | 列表浏览、搜索、分类筛选、状态筛选、批量操作 |
| ✏️ **创作与编辑** | Markdown 编辑器、草稿保存、发布/私密/草稿三种状态 |
| 🖼️ **素材库** | 图片/文件浏览、上传、管理，画廊式展示 |
| 📊 **数据统计** | 文章数、评论数、分类数、标签数一览 |
| 👤 **个人中心** | 账号信息、主题切换、外观设置 |
| 🎨 **动态主题** | Material 3 动态取色，跟随壁纸自动变换配色 |
| 🌙 **深色模式** | 自动跟随系统，也支持手动切换 |
| 📋 **更新日志** | 应用内查看版本更新记录 |

## 🏗️ 技术架构

- **语言** — Kotlin
- **UI 框架** — Jetpack Compose + Material 3 Expressive
- **架构模式** — MVVM + Repository Pattern
- **依赖注入** — Hilt
- **网络** — OkHttp + XML-RPC（Typecho 原生接口）
- **图片加载** — Coil（SingletonImageLoader）
- **本地存储** — DataStore
- **异步** — Kotlin Coroutines + Flow
- **序列化** — kotlinx.serialization

## 📦 安装使用

### 方式一：下载 APK（推荐）

1. 前往 [Releases](https://github.com/LucasJX/typecho-manager-compose/releases/latest) 下载最新 APK
2. 在 Android 设备上安装（需开启「允许安装未知来源应用」）
3. 打开应用，输入你的 Typecho 博客地址、用户名和密码

### 方式二：从源码构建

```bash
# 克隆仓库
git clone https://github.com/LucasJX/typecho-manager-compose.git

# 用 Android Studio 打开，同步 Gradle
# 构建并运行
```

**环境要求：**
- Android 8.0（API 26）及以上
- Typecho 博客已启用 XML-RPC 接口

## 📁 项目结构

```
app/src/main/java/com/flypigs/typechomanager/
├── data/            # 数据层 — API、数据库、DataStore
├── di/              # Hilt 依赖注入模块
├── domain/          # 业务逻辑 — Use Case、Repository 接口
├── ui/              # 界面层
│   ├── home/        # 首页仪表盘
│   ├── posts/       # 文章列表
│   ├── editor/      # 文章编辑器
│   ├── creator/     # 新建文章
│   ├── postdetail/  # 文章详情
│   ├── attachments/ # 素材库
│   ├── stats/       # 数据统计
│   ├── profile/     # 个人中心 / 设置
│   ├── setup/       # 登录配置
│   ├── changelog/   # 更新日志
│   ├── navigation/  # 路由导航
│   └── components/  # 共享 UI 组件
└── utils/           # 工具类与扩展函数
```

## 🤝 参与贡献

欢迎提交 Pull Request！

1. Fork 本仓库
2. 创建功能分支：`git checkout -b feature/你的功能`
3. 提交更改：`git commit -m 'feat: 添加某个功能'`
4. 推送分支：`git push origin feature/你的功能`
5. 发起 Pull Request

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 开源。

## 🙏 致谢

- [Typecho](https://typecho.org/) — 优雅的 PHP 博客平台
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — Android 现代 UI 工具包
- [Material 3](https://m3.material.io/) — Google 设计系统
