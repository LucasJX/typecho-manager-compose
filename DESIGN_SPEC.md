# Blogga V3 — 全局设计规范

> Material 3 Expressive 视觉规范 · 适用于所有页面开发
> 最后更新: 2026-06-12

---

## 1. 设计原则

| 原则 | 说明 |
|------|------|
| **渐变图标徽章** | 所有标题区/统计卡/操作按钮使用圆形渐变徽章，取代纯色 Icon |
| **大号数字** | 统计数据使用 headlineMedium 字号 + CountUp 动画 |
| **骨架屏** | 每个页面必须有对应 Skeleton，禁用纯 CircularProgressIndicator |
| **错开入场** | 区块使用 AnimatedVisibility stagger，列表使用 itemEnterAnimation |
| **DesignSystem 优先** | 所有间距/圆角/颜色必须引用 DesignSystem 常量，禁止魔法数字 |

---

## 2. 间距系统

只允许 4/8/16/24/32/48dp，禁止随机值。

```
DesignSystem.Spacing
├── ExtraSmall   = 4dp    // 图标与文字间隙
├── Small        = 8dp    // 紧凑间距
├── Medium       = 16dp   // 标准间距（卡片内边距、页面水平边距）
├── Large        = 24dp   // 区域间距（区块标题间距）
├── ExtraLarge   = 32dp   // 页面边距 / Hero 区域
└── XXLarge      = 48dp   // 大块留白

语义别名:
├── CardPadding     = 16dp   // 卡片内边距
├── CardGap         = 8dp    // 卡片间距
├── SectionGap      = 24dp   // 区块间距
└── PageHorizontal  = 16dp   // 页面水平边距
```

---

## 3. 圆角系统

```
DesignSystem.Corner
├── Button     = 20dp    // 按钮、Chip、FAB
├── Card       = 24dp    // 卡片、Hero
├── StatBar    = 24dp    // 统计条
├── Input      = 16dp    // 输入框
├── Medium     = 16dp    // 列表项容器
└── Thumbnail  = 12dp    // 缩略图
```

M3 Shapes 对应:
- `extraSmall` = 4dp（最小）
- `small` = 20dp（Chip、小按钮）
- `medium` = 24dp（卡片、统计条）
- `large` = 28dp（大容器）

---

## 4. 配色方案

### 4.1 品牌色

| Token | 色值 | 用途 |
|-------|------|------|
| Primary | `#5B6EFF` | 品牌主色、选中态、渐变起点 |
| Secondary | `#5CC8FF` | 辅助色、次级按钮 |
| Tertiary | `#7C5CCC` | 点缀色、渐变终点 |
| Background | `#F6F7FB` | 页面背景 |
| Surface | `#FFFFFF` | 卡片背景 |

### 4.2 语义色

| Token | 色值 | 用途 |
|-------|------|------|
| Success | `#4CAF50` | 已发布状态、成功提示 |
| Warning | `#FFB020` | 草稿状态、警告提示 |
| Error | `#FF5252` | 错误状态、删除操作 |

### 4.3 分类色

| 分类 | 色值 | 图标徽章渐变 |
|------|------|-------------|
| 生活杂谈 | `#66BB6A` | → `#81C784` |
| 技术 | `#42A5F5` | → `#64B5F6` |
| AI | `#AB47BC` | → `#CE93D8` |
| 工具 | `#FF7043` | → `#FF8A65` |
| 旅行 | `#26C6DA` | → `#4DD0E1` |

### 4.4 操作按钮渐变色

| 操作 | 渐变 |
|------|------|
| 写文章 | `#5B6EFF → #7C8FFF` (蓝) |
| AI 助手 | `#9B65D6 → #B388FF` (紫) |
| 草稿箱 | `#FF9800 → #FFB74D` (橙) |
| 上传附件 | `#4CAF50 → #81C784` (绿) |

---

## 5. 字体系统

```
Typography Token    Size  Weight      用途
─────────────────────────────────────────────
displayLarge        48sp  Bold        首页博客名
displayMedium       40sp  Bold        (保留)
displaySmall        36sp  Bold        (保留)

headlineLarge       36sp  SemiBold    Hero 标题
headlineMedium      28sp  SemiBold    统计数字、欢迎语
headlineSmall       24sp  SemiBold    区块标题

titleLarge          24sp  Medium      (保留)
titleMedium         20sp  Medium      页面标题
titleSmall          16sp  Medium      文章卡片标题

bodyLarge           16sp  Normal      正文
bodyMedium          14sp  Normal      描述文字
bodySmall           12sp  Normal      辅助文字

labelLarge          14sp  Medium      按钮文字、Chip
labelMedium         12sp  Medium      标签、统计说明
labelSmall          11sp  Medium      角标、时间戳
```

---

## 6. 渐变图标徽章系统

V3 核心视觉模式。每个页面标题区、统计卡、操作按钮必须使用。

### 6.1 尺寸规格

| 场景 | 尺寸 | 图标大小 | 代码 |
|------|------|---------|------|
| 标题区大徽章 | 56dp | 28dp | `DesignSystem.GradientBadge.TitleSize` |
| 统计卡/操作按钮 | 40dp | 20dp | `DesignSystem.GradientBadge.StatCardSize` |
| 设置项/列表项 | 36dp | 18dp | `DesignSystem.GradientBadge.ListItemSize` |

### 6.2 渐变方向

默认：Primary → Tertiary，从左上到右下（`Brush.linearGradient`）

```kotlin
// 使用方式
Brush.linearGradient(
    colors = DesignSystem.GradientBadge.defaultBrush()
)
```

### 6.3 实现模板

```kotlin
@Composable
fun GradientBadge(
    icon: ImageVector,
    size: Dp = DesignSystem.GradientBadge.TitleSize,
    iconSize: Dp = DesignSystem.GradientBadge.TitleIconSize,
    colors: List<Color> = DesignSystem.GradientBadge.defaultBrush(),
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(colors)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = Color.White,
        )
    }
}
```

---

## 7. 页面布局模板

### 7.1 标准页面结构

每个页面遵循统一布局：

```
Scaffold {
    LazyColumn {
        // 1. 标题区
        item { PageTitleSection() }

        // 2. 数据概览（统计条 / 统计卡）
        item { StatsSection() }

        // 3. 主内容区
        items { ContentItems() }
    }
}
```

### 7.2 标题区规范

```
┌─────────────────────────────────┐
│  ◉ 标题文字                      │  ← 渐变圆形徽章 + headlineSmall
│     副标题描述                    │  ← bodyMedium + onSurfaceVariant
└─────────────────────────────────┘
```

- 徽章: 56dp 圆形，Primary→Tertiary 渐变
- 标题: headlineSmall (24sp, SemiBold)
- 副标题: bodyMedium (14sp, Normal, onSurfaceVariant)
- 标题与副标题间距: 8dp (TitleGap)

### 7.3 数据统计条规范

```
┌─────────────────────────────────────┐
│     128          12           3     │  ← headlineMedium 数字
│   文章数        草稿数       附件数   │  ← labelMedium 标签
└─────────────────────────────────────┘
```

- 高度: 72dp (StatBarHeight)
- 背景: surfaceContainerHighest
- 圆角: 24dp
- 数字: headlineMedium + CountUp 动画
- 标签: labelMedium + onSurfaceVariant

### 7.4 搜索栏 + 筛选规范

```
┌─────────────────────────────────┐
│  🔍 搜索...                      │  ← DockedSearchBar 56dp
└─────────────────────────────────┘
[全部] [图片] [视频] [文档]          ← FilterChipRow 36dp
```

- 搜索栏: DockedSearchBar，高度 56dp，圆角 24dp
- 筛选标签: FilterChip，高度 36dp，圆角 20dp
- 标签间距: 8dp

---

## 8. 动效系统

### 8.1 入场动画

#### 区块级（AnimatedVisibility + MutableTransitionState）

用于页面头部、统计条等独立区块：

```kotlin
val visibleState = remember {
    MutableTransitionState(false).apply { targetState = true }
}

AnimatedVisibility(
    visibleState = visibleState,
    enter = fadeIn(tween(500)) + slideInVertically(
        animationSpec = tween(500),
        initialOffsetY = { 40 }
    ),
) {
    // 区块内容
}
```

- 时长: 500ms
- 区块间延迟: 100ms
- 上滑偏移: 40dp

#### 列表项级（itemEnterAnimation）

用于 LazyColumn items 的交错入场：

```kotlin
itemsIndexed(drafts) { index, draft ->
    DraftCard(draft)
        .itemEnterAnimation(index)
}
```

- 时长: 300ms
- 项间延迟: 50ms
- 上滑偏移: 40dp

### 8.2 交互动效

| 动效 | 参数 | 代码 |
|------|------|------|
| 卡片点击缩放 | 0.97, 100ms | `DesignSystem.Animation.CardPressScale` |
| 数字滚动 | 800ms | `DesignSystem.Animation.NumberScrollDuration` |
| 图片 CrossFade | 300ms | `DesignSystem.Animation.CrossfadeDuration` |
| FAB Morph | spring damping 0.6 | `DesignSystem.Animation.FabSpringDamping` |
| 页面切换 | 300ms | `DesignSystem.Animation.PageTransitionDuration` |

### 8.3 骨架屏闪烁

```kotlin
// 自动应用闪烁效果
Box(modifier = Modifier.skeletonShimmer()) { ... }

// 参数
ShimmerMinAlpha = 0.3f
ShimmerMaxAlpha = 0.7f
ShimmerDuration = 1000ms (循环)
```

---

## 9. 骨架屏规范

每个页面必须有对应骨架屏，命名规则: `{Page}Skeleton`

| 页面 | 骨架屏 | 关键占位 |
|------|--------|---------|
| 首页 | `HomeSkeleton` | 问候语 + 4格统计卡 + Hero + 时间线 + 2×2快捷操作 |
| 文章列表 | `PostsSkeleton` | 搜索栏 + FilterChips + 文章卡片×5 |
| 创作中心 | `CreatorSkeleton` | 标题 + 2×2按钮 + 草稿列表×3 |
| 我的 | `ProfileSkeleton` | 标题 + 用户卡 + 3格统计 + 热力图 + 设置组 |
| 素材库 | `AttachmentsSkeleton` | 标题 + 统计行 + 搜索栏 + 2列网格×6 |

骨架屏必须使用 `DesignSystem` 的间距和尺寸常量，与实际页面布局一致。

---

## 10. 组件规范

### 10.1 ArticleCard（文章卡片）

- 固定高度: 100dp
- 圆角: 24dp (Card)
- 布局: 封面(68dp) | 标题(2行) + 分类 + 浏览/评论
- 封面圆角: 12dp (Thumbnail)
- 状态标签: 右上角半透明黑底 + 彩色圆点
  - 已发布: 绿色圆点
  - 草稿: 黄色圆点
  - 私密: 灰色圆点
- 点击缩放: 0.97 (CardPressScale)
- 选中态: primaryContainer 30% + primary 边框 2dp

### 10.2 StatBar（统计条）

- 高度: 72dp
- 背景: surfaceContainerHighest
- 圆角: 24dp
- 内边距: 水平 24dp
- 数字: headlineMedium + CountUp 动画 (800ms)
- 标签: labelMedium + onSurfaceVariant

### 10.3 FilterChipRow（筛选标签行）

- 标签高度: 36dp
- 圆角: 20dp
- 间距: 8dp
- 水平内边距: 24dp (Large)
- 选中态: primaryContainer 背景 + onPrimaryContainer 文字
- 默认态: surfaceVariant 背景 + onSurfaceVariant 文字

### 10.4 MorphingFab（变形 FAB）

- 展开态: ExtendedFloatingActionButton（图标 + 文字）
- 收缩态: SmallFloatingActionButton（仅图标）
- 圆角: 20dp (Fab)
- 弹簧: dampingRatio = LowBouncy, stiffness = Medium

### 10.5 BottomNavigationBar

- 高度: 80dp
- 阴影: 0dp (无阴影，使用 surfaceContainer 背景区分)

---

## 11. 页面实现清单

开发新页面时必须包含：

- [ ] 引用 `DesignSystem` 常量（间距/圆角/颜色/尺寸）
- [ ] 渐变图标徽章标题区
- [ ] 统计条/统计卡（如有数据展示需求）
- [ ] 骨架屏 `{Page}Skeleton`
- [ ] 入场动画（区块级 stagger + 列表项级 itemEnterAnimation）
- [ ] PullToRefreshBox 下拉刷新（如有列表数据）
- [ ] 空状态处理
- [ ] 错误状态处理

---

## 12. 代码规范

### 12.1 文件结构

```
ui/
├── designsystem/
│   └── DesignSystem.kt          // 全局常量
├── components/
│   └── v3/
│       ├── Animations.kt        // 动效工具
│       ├── ArticleCard.kt       // 文章卡片
│       ├── CollapsingTitle.kt   // 折叠标题
│       ├── FilterChipRow.kt     // 筛选标签
│       ├── Skeletons.kt         // 所有骨架屏
│       └── StatBar.kt           // 统计条
├── theme/
│   ├── Color.kt                 // 色板
│   ├── Shape.kt                 // 形状
│   ├── Theme.kt                 // 主题
│   └── Type.kt                  // 字体
└── {page}/
    ├── {Page}Screen.kt          // 页面 Composable
    └── {Page}ViewModel.kt       // ViewModel
```

### 12.2 命名约定

| 类型 | 规则 | 示例 |
|------|------|------|
| 页面 | `{Page}Screen` | `HomeScreen`, `PostsScreen` |
| ViewModel | `{Page}ViewModel` | `HomeViewModel` |
| 骨架屏 | `{Page}Skeleton` | `HomeSkeleton`, `PostsSkeleton` |
| 组件 | PascalCase | `ArticleCard`, `StatBar` |
| 常量 | PascalCase | `DesignSystem.Spacing.Large` |

### 12.3 禁止事项

- ❌ 硬编码 dp/sp 值（必须用 DesignSystem 常量）
- ❌ 硬编码颜色值（必须用 DesignSystem 或 MaterialTheme）
- ❌ 使用 `CircularProgressIndicator` 作为页面加载态（必须用骨架屏）
- ❌ 页面无入场动画
- ❌ 标题区使用纯色 Icon（必须用渐变徽章）
- ❌ 随机间距值（只允许 4/8/16/24/32/48dp）

---

## 附录: DesignSystem.kt 速查

```
DesignSystem
├── Spacing          // 间距 4/8/16/24/32/48
├── Corner           // 圆角 12/16/20/24
├── BrandColors      // 品牌色 Primary/Secondary/Tertiary
├── SemanticColors   // 语义色 Success/Warning/Error
├── CategoryColors   // 分类色 Life/Tech/AI/Tools/Travel
├── Elevation        // 阴影 Card=2dp/Floating=4dp
├── Component        // 组件尺寸 NavBar/SearchBar/Chip/StatBar...
├── Typography       // 字体 Display/Headline/Title/Body/Label
├── Animation        // 动效参数
├── GradientBadge    // 渐变图标徽章 [NEW]
│   ├── TitleSize/StatCardSize/ListItemSize
│   ├── defaultBrush() → [Primary, Tertiary]
│   └── ActionColors.Write/AI/Draft/Upload
├── Entrance         // 入场动画参数 [NEW]
│   ├── Section*     // 区块级 500ms/100ms delay
│   └── Item*        // 列表项级 300ms/50ms delay
├── Skeleton         // 骨架屏参数 [NEW]
│   ├── Shimmer*Alpha/Duration
│   └── placeholderColor()
├── PageTemplate     // 页面布局模板 [NEW]
│   ├── TitleBadgeSize/TitleGap/SectionSpacing
│   ├── StatCardMinHeight/Columns
│   └── SearchBarHeight/FilterChipHeight
└── Constants        // 其他常量
```
