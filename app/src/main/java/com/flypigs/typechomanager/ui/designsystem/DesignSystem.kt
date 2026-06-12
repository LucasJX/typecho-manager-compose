package com.flypigs.typechomanager.ui.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
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
    // 间距系统（只允许 8/16/24/32）
    // ═══════════════════════════════════════════════════════
    object Spacing {
        val Small = 8.dp           // 紧凑间距
        val Medium = 16.dp         // 标准间距
        val Large = 24.dp          // 区域间距
        val ExtraLarge = 32.dp     // 页面边距 / Hero 区域
        val CardPadding = 16.dp    // 卡片内边距
        val CardGap = 8.dp         // 卡片间距
        val SectionGap = 24.dp     // 区块间距
        val PageHorizontal = 16.dp // 页面水平边距

        // 兼容旧值（逐步迁移）
        @Deprecated("Use Small (8dp)", ReplaceWith("Small"))
        val ExtraSmall = 4.dp
        @Deprecated("Use Large (24dp)", ReplaceWith("Large"))
        val XXLarge = 24.dp
        @Deprecated("Use ExtraLarge (32dp)", ReplaceWith("ExtraLarge"))
        val XXXLarge = 32.dp
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

        // 兼容旧值（逐步迁移）
        @Deprecated("Use Button", ReplaceWith("Button"))
        val SmallButton = Button
        @Deprecated("Use Card", ReplaceWith("Card"))
        val LargeContainer = Card
        @Deprecated("Use Card", ReplaceWith("Card"))
        val Thumbnail = RoundedCornerShape(24.dp)
        @Deprecated("Use Card", ReplaceWith("Card"))
        val ImageSmall = RoundedCornerShape(24.dp)
        @Deprecated("Use Button", ReplaceWith("Button"))
        val Small = RoundedCornerShape(20.dp)
        @Deprecated("Use Input", ReplaceWith("Input"))
        val Medium = RoundedCornerShape(16.dp)
        @Deprecated("Use Input", ReplaceWith("Input"))
        val Large = RoundedCornerShape(16.dp)
        @Deprecated("Use Button", ReplaceWith("Button"))
        val ExtraLarge = Button
    }

    // ═══════════════════════════════════════════════════════
    // 品牌色
    // ═══════════════════════════════════════════════════════
    object BrandColors {
        val Primary = Color(0xFF5B6EFF)       // 品牌主色
        val Secondary = Color(0xFF5CC8FF)     // 辅助色
        val Tertiary = Color(0xFFB388FF)      // 点缀色
        val Background = Color(0xFFF6F7FB)    // 页面背景
        val Surface = Color(0xFFFFFFFF)       // 卡片背景
    }

    // 语义色
    object SemanticColors {
        val Success = Color(0xFF4CAF50)
        val Warning = Color(0xFFFFB020)
        val Error = Color(0xFFFF5252)
    }

    // 分类专属色
    object CategoryColors {
        val Life = Color(0xFF66BB6A)
        val Tech = Color(0xFF42A5F5)
        val AI = Color(0xFFAB47BC)
        val Tools = Color(0xFFFF7043)
        val Travel = Color(0xFF26C6DA)
    }

    // ═══════════════════════════════════════════════════════
    // 阴影
    // ═══════════════════════════════════════════════════════
    object Elevation {
        val Card = 2.dp
        val Floating = 4.dp
        val Navigation = 0.dp
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
    }

    // ═══════════════════════════════════════════════════════
    // 排版（Display 36, Headline 28, Title 20, Body 16, Label 12）
    // ═══════════════════════════════════════════════════════
    object Typography {
        val Display = 36.sp
        val Headline = 28.sp
        val Title = 20.sp
        val Body = 16.sp
        val Label = 12.sp

        // 兼容旧 M3 命名（逐步迁移）
        @Deprecated("Use Display", ReplaceWith("Display"))
        val DisplayLarge = Display
        @Deprecated("Use Display", ReplaceWith("Display"))
        val DisplayMedium = Display
        @Deprecated("Use Display", ReplaceWith("Display"))
        val DisplaySmall = Display
        @Deprecated("Use Headline", ReplaceWith("Headline"))
        val HeadlineLarge = Headline
        @Deprecated("Use Headline", ReplaceWith("Headline"))
        val HeadlineMedium = Headline
        @Deprecated("Use Headline", ReplaceWith("Headline"))
        val HeadlineSmall = 24.sp
        @Deprecated("Use Title", ReplaceWith("Title"))
        val TitleLarge = Title
        @Deprecated("Use Title", ReplaceWith("Title"))
        val TitleMedium = Title
        @Deprecated("Use Body", ReplaceWith("Body"))
        val TitleSmall = Body
        @Deprecated("Use Body", ReplaceWith("Body"))
        val BodyLarge = Body
        @Deprecated("Use Body", ReplaceWith("Body"))
        val BodyMedium = 14.sp
        @Deprecated("Use Label", ReplaceWith("Label"))
        val BodySmall = Label
        @Deprecated("Use Body", ReplaceWith("Body"))
        val LabelLarge = 14.sp
        @Deprecated("Use Label", ReplaceWith("Label"))
        val LabelMedium = Label
        @Deprecated("Use Label", ReplaceWith("Label"))
        val LabelSmall = 11.sp
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
