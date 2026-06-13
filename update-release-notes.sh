#!/bin/bash
# 更新 GitHub Release 日志
# 用法: ./update-release-notes.sh <github-token>
# 或先 gh auth login 然后直接运行

set -e
REPO="LucasJX/typecho-manager-compose"

if [ -n "$1" ]; then
    TOKEN="$1"
    AUTH="-H \"Authorization: token $TOKEN\""
else
    # 尝试 gh CLI
    if gh auth status &>/dev/null; then
        USE_GH=true
    else
        echo "请提供 GitHub token: $0 <token>"
        echo "或先运行: gh auth login"
        exit 1
    fi
fi

update_release() {
    local tag="$1"
    local body_file="$2"
    
    if [ "$USE_GH" = true ]; then
        gh release edit "$tag" --repo "$REPO" --notes-file "$body_file"
    else
        # Get release ID
        ID=$(curl -s -H "Authorization: token $TOKEN" \
            "https://api.github.com/repos/$REPO/releases/tags/$tag" | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))")
        
        if [ -z "$ID" ] || [ "$ID" = "None" ]; then
            echo "⚠️  Release $tag not found"
            return
        fi
        
        curl -s -X PATCH \
            -H "Authorization: token $TOKEN" \
            -H "Content-Type: application/json" \
            -d @"$body_file" \
            "https://api.github.com/repos/$REPO/releases/$ID" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'✅ {d.get(\"tag_name\",\"?\")} updated ({len(d.get(\"body\",\"\"))} chars)')"
    fi
}

echo "📝 更新 Release 日志..."

# v1.2.1
cat > /tmp/v1.2.1.json << 'JSONEOF'
{
"body": "## v1.2.1 (versionCode 22)\n\n### 🎬 入场动画统一\n- ✨ **PostsScreen 大标题** — `visible=条件` → `visibleState=enterState`，获得 fadeIn + slideInVertically 入场效果\n- ✨ **PostsScreen 网格模式大标题** — 同上，与设置页动画标准一致\n- ✨ **PostsScreen 网格文章区** — 硬编码 `tween(500)` → `DesignSystem.Entrance` 常量\n\n### 🎬 PostDetailScreen 动画规范化\n- 🔧 5 处硬编码 `tween(500)` / `{-it/2}` / `{it/4}` 全部替换为 `DesignSystem.Entrance.SectionDuration` / `SectionDelay` / `SectionSlideOffset`\n- 📐 入场延迟阶梯：Header(0ms) → Cover(100ms) → Content(200ms) → Stats(300ms) → Action(400ms)\n\n**Full Changelog**: https://github.com/LucasJX/typecho-manager-compose/compare/v1.1.9...v1.2.1"
}
JSONEOF

# v1.1.9
cat > /tmp/v1.1.9.json << 'JSONEOF'
{
"body": "## v1.1.9 (versionCode 20)\n\n### 📐 大标题高度统一\n- 🔧 **所有页面** 添加 `contentWindowInsets = WindowInsets(0, 0, 0, 0)`\n- 📐 修复页面: HomeScreen / PostsScreen / AttachmentsScreen / CreatorScreen / StatsScreen\n- ✅ PostDetailScreen / ProfileScreen 已有此配置，无需修改\n\n### 🔧 问题根因\n应用使用 `enableEdgeToEdge()` 绘制到状态栏后面。外层 Scaffold 提供系统栏 padding，内层 Scaffold 若不设 `contentWindowInsets(0,0,0,0)` 会再次注入状态栏高度，导致除设置页和文章详情页外所有页面大标题位置偏低。\n\n**Full Changelog**: https://github.com/LucasJX/typecho-manager-compose/compare/v1.1.8...v1.1.9"
}
JSONEOF

# v1.0.1
cat > /tmp/v1.0.1.json << 'JSONEOF'
{
"body": "## v1.0.1 (versionCode 2)\n\n### 🏠 首页修复\n- 🔄 恢复「最近动态」时间线区域\n- ✨ 大标题添加 fadeIn + slideInVertically 入场动画\n\n### 📝 文章页动画\n- ✨ PostsScreen 大标题/搜索框/FilterChips 添加入场动画\n- 📐 动画参数统一使用 `DesignSystem.Entrance` 常量\n\n### 🔐 登录页修复\n- 🔧 输入框去掉 `label` 参数，只保留 `placeholder`，防止文字重叠\n- 🔧 去掉 `height(56.dp)` 固定高度约束，让 OutlinedTextField 自适应\n\n### ✨ 动画统一\n- 📐 所有页面入场动画参数统一: SectionDuration=500ms / SectionDelay=100ms / SectionSlideOffset=40dp\n\n**Full Changelog**: https://github.com/LucasJX/typecho-manager-compose/compare/v1.0.0...v1.0.1"
}
JSONEOF

# v1.0.0
cat > /tmp/v1.0.0.json << 'JSONEOF'
{
"body": "## v1.0.0 — Blogga V3 首发版本 (versionCode 1)\n\n### 🎨 Design System V3\n- 全新设计规范：圆角(按钮20dp/卡片24dp/输入框16dp)、间距(8/16/24/32dp)、字体(Display36/Headline28/Title20/Body16/Label12)\n- 动效体系：页面切换300ms、卡片点击scale 0.97、入场动画fadeIn+slideIn\n- 暗色模式全面适配\n\n### 🔐 登录页\n- Logo 区域 260dp + Dynamic Gradient 背景\n- Material 3 风格输入框 + 渐变登录按钮\n\n### 🏠 首页重构\n- 渐变图标徽章 + 大标题 + 副标题 header\n- 横条统计卡片（已发布/草稿/分类/附件）\n- HorizontalPager 文章轮播（Scale + Parallax 效果）\n- 最近动态时间线\n\n### 📝 文章页重构\n- 统一 header 样式（渐变徽章 + 标题 + 副标题）\n- 搜索栏 + FilterChips 筛选\n- 列表/网格视图切换\n- SwipeToDismiss 手势操作\n\n### ✍️ 创作页重构\n- Markdown 编辑器 + 实时预览\n- 草稿/私密/发布三按钮底部栏\n- 分类选择 + 封面图设置\n\n### 📎 素材库重构\n- 统计卡片 + 筛选 Chips\n- 媒体网格展示\n\n### 👤 我的页面重构\n- 大头像 + 博客信息卡片\n- 写作热力图 + 账号信息\n- 更新日志（GitHub Releases API）\n- 退出登录功能\n\n### 📊 统计页\n- 核心指标卡片 + 创作洞察 + 分类分布\n\n**Full Changelog**: https://github.com/LucasJX/typecho-manager-compose/commits/v1.0.0"
}
JSONEOF

for tag in v1.0.0 v1.0.1 v1.1.9 v1.2.1; do
    update_release "$tag" "/tmp/$tag.json"
done

echo "✅ 全部更新完成"
