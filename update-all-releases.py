#!/usr/bin/env python3
"""批量更新 GitHub Release 更新日志"""

import subprocess
import json
import sys
import urllib.request

REPO = "LucasJX/typecho-manager-compose"

def get_token():
    """从 git credential store 读取 token"""
    try:
        result = subprocess.run(
            ["git", "credential", "fill"],
            input="protocol=https\nhost=github.com\n",
            capture_output=True, text=True
        )
        for line in result.stdout.split("\n"):
            if line.startswith("password="):
                return line.split("=", 1)[1]
    except:
        pass
    return None

def update_release(token, tag, body):
    """更新指定 tag 的 release body"""
    print(f"📝 更新 {tag} ...")
    
    # 获取 release ID
    req = urllib.request.Request(
        f"https://api.github.com/repos/{REPO}/releases/tags/{tag}",
        headers={
            "Authorization": f"token {token}",
            "Accept": "application/vnd.github+json"
        }
    )
    try:
        resp = urllib.request.urlopen(req)
        release_id = json.loads(resp.read()).get("id")
    except Exception as e:
        print(f"  ⚠️ 找不到 release {tag}: {e}")
        return
    
    # 更新 body
    data = json.dumps({"body": body}).encode()
    req = urllib.request.Request(
        f"https://api.github.com/repos/{REPO}/releases/{release_id}",
        data=data,
        method="PATCH",
        headers={
            "Authorization": f"token {token}",
            "Accept": "application/vnd.github+json",
            "Content-Type": "application/json"
        }
    )
    try:
        resp = urllib.request.urlopen(req)
        print(f"  ✅ {tag} 更新成功")
    except urllib.error.HTTPError as e:
        print(f"  ❌ {tag} 更新失败 (HTTP {e.code})")
        print(f"  {e.read().decode()}")

def main():
    token = sys.argv[1] if len(sys.argv) > 1 else get_token()
    if not token:
        print("❌ 需要 GitHub token")
        print("用法: python3 update-all-releases.py <token>")
        sys.exit(1)
    
    print(f"Token length: {len(token)}")
    
    releases = {
        "v1.2.1": """## 🎯 视觉一致性大修

本版本统一了所有页面的大标题/副标题布局，修复了弹出菜单跳动问题，新增了渐进式动画效果。

### 🐛 Bug Fixes

- `73be3c0` 修复文章/素材/标签页面标题被截断问题
- `0cb38fc` 统一数据/设置页面标题布局结构，对齐内容页
- `46295ec` 修复设置页面切换不流畅和阴影显示

### ✨ Features

- `775df89` 为大标题入场和弹出菜单添加渐进式动画
  - 所有页面 header 加入 fadeIn + slideInVertically 入场动画
  - DropDownMenu 使用 AnimatedVisibility 实现渐进式展开
  - 统一动画常量 `DesignSystem.Entrance`（350ms fade + 450ms slide）

### 🔧 Refactoring

- `2b40272` 清理冗余代码
- `3462509` 移除 ShadowSettings/BlurSettings/ReflectionSettings/PersonType 的 NestedScrollView 嵌套
- `614bc8f` 移除 CollapsibleHeader，改用 DesignSystem 标准组件
- `b2e72d6` 简化 AttachmentsScreen 布局
- `8b8ae9e` 统一 PostsScreen/AttachmentsScreen/TagsScreen 为 LazyColumn 内部 header 模式
- `494e5bd` 清理冗余导入

### 📐 统一规范

- **contentWindowInsets** — 所有 7 个页面统一 `WindowInsets(0,0,0,0)`
- **top padding** — 所有页面 header 顶部间距统一 24dp
- **header 布局** — 大标题 `headlineLarge` + 副标题 `bodyMedium`，位置/间距完全一致

### 📥 下载

[app-release.apk](https://github.com/LucasJX/typecho-manager-compose/releases/download/v1.2.1/app-release.apk)""",

        "v1.1.9": """## 🔧 批次 1 — 基础组件迁移

将 4 个基础组件从 Shadow 组件迁移到 DesignSystem 标准组件，跟随系统主题色。

### ✨ Features

- `e947b95` 统一 ShadowSettings 统计卡片为 DesignSystem 标准组件
- `541849d` 统一 BlurSettings 统计卡片为 DesignSystem 标准组件
- `f574fb4` 统一 ReflectionSettings 统计卡片为 DesignSystem 标准组件
- `381bb67` 统一 PersonType 统计卡片为 DesignSystem 标准组件

### 📐 DesignSystem 使用规范

- **SurfaceCard** — 卡片容器，跟随主题色
- **StatCard** — 统计卡片，`iconColor = DesignSystem.Colors.Primary`
- **SectionHeader** — 分区标题
- **PrimaryButton / SecondaryButton / DangerButton** — 按钮组件

### 📥 下载

[app-release.apk](https://github.com/LucasJX/typecho-manager-compose/releases/download/v1.1.9/app-release.apk)""",

        "v1.1.8": """## ✨ 文章页滚动优化

将文章页的大标题、搜索框、FilterChips 移入 LazyColumn 内部，随内容一起滚动，素材库同理重构。

### ✨ Features

- `5b51bd7` 文章页标题搜索和标签随列表滚动
  - 大标题 + 副标题 + 搜索框 + FilterChips 全部移入 LazyColumn
  - 素材库页同步重构

### 🔧 Refactoring

- `188ef8b` 修复媒体页面文件列表渲染（解决 merge 冲突残留）

### 📥 下载

[app-release.apk](https://github.com/LucasJX/typecho-manager-compose/releases/download/v1.1.8/typecho-manager-v1.1.8.apk)""",

        "v1.0.1": """## 🐛 Bug Fixes

修复 v1.0.0 首发版本的已知问题。

- 修复部分设备上文章列表加载卡顿的问题
- 修复素材上传偶发失败的错误处理
- 修复设置页面配置保存不生效的问题

### 📥 下载

[app-release.apk](https://github.com/LucasJX/typecho-manager-compose/releases/download/v1.0.1/app-release.apk)""",

        "v1.0.0": """## 🎉 首个正式发布版本

Typecho Manager — Material 3 Expressive 风格的 Typecho 博客管理客户端。

### ✨ 核心功能

- **文章管理** — 列表浏览、搜索、筛选、编辑、删除
- **素材库** — 媒体文件浏览与管理
- **标签管理** — 标签 CRUD
- **设置** — 站点配置、账号管理、外观设置

### 🎨 界面特性

- Material 3 Expressive 设计语言
- 动态取色（Dynamic Color）支持
- CustomTheme 主题系统，跟随系统深色/浅色模式
- 动效系统 — fade + slide 入场动画

### 🏗️ 技术栈

- Kotlin + Jetpack Compose
- Material 3 + CustomTheme
- Retrofit + OkHttp
- Coil 图片加载
- Hilt 依赖注入
- Navigation Compose

### 📥 下载

[app-release.apk](https://github.com/LucasJX/typecho-manager-compose/releases/download/v1.0.0/app-release.apk)"""
    }
    
    for tag, body in releases.items():
        update_release(token, tag, body)
    
    print("\n✅ 全部完成！")

if __name__ == "__main__":
    main()
