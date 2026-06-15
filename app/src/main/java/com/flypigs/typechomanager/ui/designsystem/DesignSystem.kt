package com.flypigs.typechomanager.ui.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design System v3 — Blogga 移动博客工作台
 * Material 3 Expressive 视觉规范
 *
 * 全局规范（所有页面必须遵守）：
 * - 间距只允许 8/16/24/32，禁止随机值
 * - 圆角：按钮 20dp，卡片 24dp，输入框 16dp
 * - 字体：Display 36, Headline 28, Title 20, Body 16, Label 12
 * - 动效：页面切换 300ms，卡片点击 scale 0.97
 */
object DesignSystem {

    // ═══════════════════════════════════════════════════════
    // 间距系统（只允许 4/8/16/24/32/48）
    // ═══════════════════════════════════════════════════════
    object Spacing {
        val ExtraSmall = 4.dp      // 最小间距（图标与文字间隙）
        val Small = 8.dp           // 紧凑间距
        val Medium = 16.dp         // 标准间距
        val Large = 24.dp          // 区域间距
        val ExtraLarge = 32.dp     // 页面边距 / Hero 区域
        val XXLarge = 48.dp        // 大块留白
        val CardPadding = 16.dp    // 卡片内边距
        val CardGap = 8.dp         // 卡片间距
        val SectionGap = 24.dp     // 区块间距
        val PageHorizontal = 16.dp // 页面水平边距
        val PageHeaderHorizontal = 20.dp // PageHeader 水平边距
        val PageHeaderVertical = 12.dp   // PageHeader 垂直边距
        val TitleSubtitleGap = 2.dp      // 标题与副标题间距
    }

    // ═══════════════════════════════════════════════════════
    // 圆角系统（按钮 20dp，卡片 24dp，输入框 16dp）
    // ═══════════════════════════════════════════════════════
    object Corner {
        // 按钮类
        val Button = RoundedCornerShape(20.dp)
        val Chip = RoundedCornerShape(20.dp)
        val Fab = RoundedCornerShape(20.dp)

        // 卡片类
        val Card = RoundedCornerShape(24.dp)
        val StatBar = RoundedCornerShape(24.dp)
        val Hero = RoundedCornerShape(24.dp)

        // 输入框类
        val Input = RoundedCornerShape(16.dp)

        // 中等容器（列表项）
        val Medium = RoundedCornerShape(16.dp)

        // 缩略图
        val Thumbnail = RoundedCornerShape(12.dp)
    }

    // ═══════════════════════════════════════════════════════
    // 品牌色（@Composable，自动适配暗色模式）
    // ═══════════════════════════════════════════════════════
    object BrandColors {
        val Primary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
        val Secondary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondary
        val Tertiary: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiary
        val Background: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
        val Surface: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface
    }

    // 语义色（@Composable，自动适配暗色模式）
    object SemanticColors {
        val Success: Color @Composable @ReadOnlyComposable get() =
            if (isSystemInDarkTheme()) Color(0xFF81C784) else Color(0xFF4CAF50)
        val Warning: Color @Composable @ReadOnlyComposable get() =
            if (isSystemInDarkTheme()) Color(0xFFFFD54F) else Color(0xFFFFB020)
        val Error: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.error
    }

    // 分类专属色（亮色/暗色自适应）
    object CategoryColors {
        val Life: Color @Composable @ReadOnlyComposable get() =
            if (isSystemInDarkTheme()) Color(0xFF81C784) else Color(0xFF66BB6A)
        val Tech: Color @Composable @ReadOnlyComposable get() =
            if (isSystemInDarkTheme()) Color(0xFF90CAF9) else Color(0xFF42A5F5)
        val AI: Color @Composable @ReadOnlyComposable get() =
            if (isSystemInDarkTheme()) Color(0xFFCE93D8) else Color(0xFFAB47BC)
        val Tools: Color @Composable @ReadOnlyComposable get() =
            if (isSystemInDarkTheme()) Color(0xFFFFAB91) else Color(0xFFFF7043)
        val Travel: Color @Composable @ReadOnlyComposable get() =
            if (isSystemInDarkTheme()) Color(0xFF80DEEA) else Color(0xFF26C6DA)
    }

    // ═══════════════════════════════════════════════════════
    // 阴影
    // ═══════════════════════════════════════════════════════
    object Elevation {
        val Card = 2.dp
        val Floating = 4.dp
        val Navigation = 0.dp
        val Flat = 0.dp
        val Subtle = 1.dp
    }

    // ═══════════════════════════════════════════════════════
    // 彩色竖条（设置项/统计行左侧装饰）
    // ═══════════════════════════════════════════════════════
    object AccentBar {
        val Width = 3.dp
        val Height = 28.dp
        val Corner = RoundedCornerShape(2.dp)
    }

    // ═══════════════════════════════════════════════════════
    // 热力图网格
    // ═══════════════════════════════════════════════════════
    object Heatmap {
        val CellSize = 12.dp
        val CellCorner = RoundedCornerShape(2.dp)
        val CellGap = 2.dp
        val LegendCellSize = 10.dp
        val LegendCellPadding = 1.dp
    }

    // ═══════════════════════════════════════════════════════
    // 文章状态徽章（卡片右上角半透明标签）
    // ═══════════════════════════════════════════════════════
    object StatusBadge {
        val Corner = RoundedCornerShape(6.dp)
        val HorizontalPadding = 5.dp
        val VerticalPadding = 2.dp
        val DotSize = 5.dp
        val DotGap = 3.dp
        val FontSize = 9.sp
    }

    // ═══════════════════════════════════════════════════════
    // 组件尺寸
    // ═══════════════════════════════════════════════════════
    object Component {
        // NavigationBar
        val NavBarHeight = 80.dp

        // 搜索栏
        val SearchBarHeight = 56.dp

        // FilterChip
        val ChipHeight = 36.dp
        val ChipGap = 8.dp

        // StatBar
        val StatBarHeight = 72.dp

        // 文章卡片
        val CardThumbnailSize = 80.dp

        // FAB
        val FabBottomPadding = 88.dp

        // 附件网格
        val AttachmentGridColumns = 2
        val AttachmentGridIconSize = 48.dp
        val AttachmentOverlayHeight = 72.dp
        val AttachmentPreviewHeight = 300.dp
        val GalleryNavIconSize = 36.dp

        // Hero 卡片
        val HeroHeight = 200.dp

        // 热力图
        val HeatmapHeight = 120.dp

        // 首页统计卡
        val StatCardHeight = 96.dp

        // 首页横滑文章卡
        val ArticleCarouselWidth = 200.dp
        val ArticleCarouselHeight = 220.dp

        // 快速操作按钮
        val QuickActionHeight = 80.dp

        // 头像
        val ProfileAvatarSize = 72.dp

        // 状态指示点
        val StatusDotSize = 8.dp

        // 进度条
        val ProgressHeight = 8.dp
        val ProgressCorner = RoundedCornerShape(4.dp)

        // 空状态
        val EmptyStateIconSize = 64.dp

        // IconButton
        val IconButtonSize = 40.dp

        // 草稿列表项
        val DraftItemHeight = 64.dp

        // 区块标题高度
        val SectionTitleHeight = 160.dp

        // 骨架屏 FilterChip 占位
        val ChipSkeletonWidth = 72.dp
        val ChipSkeletonHeight = 32.dp

        // 统计数值字号
        val StatValueFontSize = 22.sp
        val StatLabelFontSize = 9.sp
    }

    // ═══════════════════════════════════════════════════════
    // 排版系统（基础 + 阅读器专用）
    // ═══════════════════════════════════════════════════════

    /** 基础字号（向后兼容） */
    object Typography {
        val Display = 36.sp
        val Headline = 28.sp
        val Title = 20.sp
        val Body = 16.sp
        val Label = 12.sp
    }

    /**
     * 阅读器排版系统 — 文章内容专用
     *
     * 参考规范 (.specs/task1-typography.md)：
     * - 正文 16sp / 行高 1.8-2.0 (长文阅读最佳)
     * - H1 22-24sp bold, H2 20sp bold, H3 18sp w600
     * - 段落间距 12-16px
     * - letter-spacing: 0.2
     * - 代码 14sp / monospace
     */
    object ReaderTypography {
        // ── 正文 ──
        val BodySize = 16.sp
        val BodyLineHeight = 29.sp           // 16 * 1.81 ≈ 29（spec: 1.8~2.0）
        const val BodyLetterSpacing = 0.02f  // spec: 0.2 (Android sp = 0.02em)

        // ── 标题（spec: H1 22-24sp, H2 20sp, H3 18sp）──
        val H1Size = 24.sp
        val H1LineHeight = 32.sp             // 24 * 1.33 ≈ 32
        const val H1LetterSpacing = -0.01f   // 大标题收紧

        val H2Size = 20.sp
        val H2LineHeight = 28.sp             // 20 * 1.4 = 28
        const val H2LetterSpacing = -0.01f

        val H3Size = 18.sp
        val H3LineHeight = 26.sp             // 18 * 1.44 ≈ 26
        const val H3LetterSpacing = 0f

        val H4Size = 17.sp
        val H4LineHeight = 24.sp
        const val H4LetterSpacing = 0f

        val H5Size = 16.sp
        val H5LineHeight = 24.sp
        const val H5LetterSpacing = 0.01f

        val H6Size = 14.sp
        val H6LineHeight = 20.sp
        const val H6LetterSpacing = 0.01f

        // Markwon headingTextSizeMultipliers (相对 textSize 的倍数)
        // Index 0=H1 ... Index 5=H6
        val HeadingMultipliers = floatArrayOf(
            H1Size.value / BodySize.value,   // 1.50
            H2Size.value / BodySize.value,   // 1.25
            H3Size.value / BodySize.value,   // 1.125
            H4Size.value / BodySize.value,   // 1.0625
            H5Size.value / BodySize.value,   // 1.0
            H6Size.value / BodySize.value,   // 0.875
        )

        // ── 段落间距（spec: 12-16px 上下）──
        val ParagraphSpacing = 14.sp         // 上下各 7sp

        // ── 代码 ──
        val CodeSize = 14.sp
        val CodeLineHeight = 22.sp
        const val CodeLetterSpacing = 0f

        // ── 引用 ──
        val BlockquoteSize = 15.sp
        val BlockquoteLineHeight = 24.sp
        const val BlockquoteLetterSpacing = 0.01f

        // ── 说明文字 ──
        val CaptionSize = 13.sp
        val CaptionLineHeight = 18.sp
        const val CaptionLetterSpacing = 0.04f
    }

    // ═══════════════════════════════════════════════════════
    // 动效
    // ═══════════════════════════════════════════════════════
    object Animation {
        val CardPressScale = 0.97f          // 卡片点击缩放
        val CardPressDuration = 100         // 卡片点击时长 ms
        val PageTransitionDuration = 300    // 页面切换 300ms
        val NumberScrollDuration = 800      // 数字 CountUp
        val CrossfadeDuration = 300         // 图片 CrossFade
        val ShimmerDuration = 2000          // 骨架屏微光
        val FabSpringDamping = 0.6f         // FAB Morph 弹簧阻尼
        val FabSpringStiffness = 400f       // FAB Morph 弹簧刚度
    }

    // ═══════════════════════════════════════════════════════
    // 渐变图标徽章系统（V3 核心视觉模式）
    // ═══════════════════════════════════════════════════════
    object GradientBadge {
        // 标题区大徽章
        val TitleSize = 56.dp
        val TitleIconSize = 28.dp

        // 统计卡/操作按钮徽章
        val StatCardSize = 40.dp
        val StatCardIconSize = 20.dp

        // 设置项/列表项小徽章
        val ListItemSize = 36.dp
        val ListItemIconSize = 18.dp

        // 渐变方向：Primary → Tertiary（左上→右下）
        @Composable
        fun defaultBrush() = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
        )

        // FAB/操作按钮独立渐变色（暗色自适应）
        object ActionColors {
            val Write: List<Color> @Composable @ReadOnlyComposable get() =
                if (isSystemInDarkTheme()) listOf(Color(0xFF8B9AFF), Color(0xFF9EAAFF))
                else listOf(Color(0xFF5B6EFF), Color(0xFF7C8FFF))
            val AI: List<Color> @Composable @ReadOnlyComposable get() =
                if (isSystemInDarkTheme()) listOf(Color(0xFFB48AE8), Color(0xFFCAAAFF))
                else listOf(Color(0xFF9B65D6), Color(0xFFB388FF))
            val Draft: List<Color> @Composable @ReadOnlyComposable get() =
                if (isSystemInDarkTheme()) listOf(Color(0xFFFFB74D), Color(0xFFFFCC80))
                else listOf(Color(0xFFFF9800), Color(0xFFFFB74D))
            val Upload: List<Color> @Composable @ReadOnlyComposable get() =
                if (isSystemInDarkTheme()) listOf(Color(0xFF81C784), Color(0xFFA5D6A7))
                else listOf(Color(0xFF4CAF50), Color(0xFF81C784))
        }
    }

    // ═══════════════════════════════════════════════════════
    // 入场动画系统（V3 核心动效模式）
    // ═══════════════════════════════════════════════════════
    object Entrance {
        // 区块级入场（AnimatedVisibility + MutableTransitionState）
        val SectionDuration = 500           // 区块淡入时长 ms
        val SectionDelay = 100              // 区块间错开延迟 ms
        val SectionSlideOffset = 40         // 上滑偏移量 dp

        // 列表项级入场（itemEnterAnimation）
        val ItemDuration = 300              // 列表项淡入时长 ms
        val ItemDelay = 50                  // 列表项间错开延迟 ms
        val ItemSlideOffset = 40f           // 上滑偏移量 dp
    }

    // ═══════════════════════════════════════════════════════
    // 骨架屏系统
    // ═══════════════════════════════════════════════════════
    object Skeleton {
        val ShimmerMinAlpha = 0.3f          // 闪烁最低透明度
        val ShimmerMaxAlpha = 0.7f          // 闪烁最高透明度
        val ShimmerDuration = 1000          // 闪烁周期 ms
        @Composable
        fun placeholderColor() = MaterialTheme.colorScheme.surfaceVariant
    }

    // ═══════════════════════════════════════════════════════
    // 页面布局模板（V3 标准页面结构）
    // ═══════════════════════════════════════════════════════
    object PageTemplate {
        // 标题区高度范围
        val TitleBadgeSize = 56.dp          // 标题图标徽章
        val TitleGap = 8.dp                 // 标题与副标题间距
        val SectionSpacing = 24.dp          // 区块间距

        // 数据统计卡
        val StatCardMinHeight = 88.dp       // 统计卡最小高度
        val StatCardColumns = 4             // 首页统计卡列数
        val StatCardColumnsProfile = 3      // 个人页统计卡列数

        // 搜索栏 + 筛选
        val SearchBarHeight = 56.dp         // 搜索栏高度
        val FilterChipHeight = 36.dp        // 筛选标签高度
    }

    // ═══════════════════════════════════════════════════════
    // 其他常量
    // ═══════════════════════════════════════════════════════
    object Constants {
        val HomeTitleInitialSize = 48.sp
        val HomeTitleCollapsedSize = 24.sp
        val HomeTitleCollapsedAlpha = 0.6f
        val AttachmentPreviewContentPadding = 80.dp
        const val DateFormatPattern = "yyyy-MM-dd HH:mm:ss"
        const val RelativeTimeFormat = "relative"
    }
}
