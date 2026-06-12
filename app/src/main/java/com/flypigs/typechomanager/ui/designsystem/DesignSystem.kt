package com.flypigs.typechomanager.ui.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design System v3 — Blogga 移动博客工作台
 * Material 3 Expressive 视觉规范
 */
object DesignSystem {

    // ═══════════════════════════════════════════════════════
    // 间距系统（8dp 基准）
    // ═══════════════════════════════════════════════════════
    object Spacing {
        val ExtraSmall = 4.dp
        val Small = 8.dp
        val Medium = 12.dp
        val Large = 16.dp
        val ExtraLarge = 20.dp
        val XXLarge = 24.dp
        val XXXLarge = 32.dp
        val SectionGap = 24.dp
        val CardPadding = 16.dp
        val CardGap = 12.dp
        val PageHorizontal = 16.dp
    }

    // ═══════════════════════════════════════════════════════
    // 圆角系统
    // ═══════════════════════════════════════════════════════
    object Corner {
        // 小型组件（Chip、小按钮、输入框）
        val Chip = RoundedCornerShape(20.dp)
        val SmallButton = RoundedCornerShape(20.dp)
        val Input = RoundedCornerShape(20.dp)

        // 标准卡片
        val Card = RoundedCornerShape(24.dp)
        val StatBar = RoundedCornerShape(24.dp)

        // 大容器（Hero、阅读头图）
        val Hero = RoundedCornerShape(28.dp)
        val LargeContainer = RoundedCornerShape(28.dp)

        // FAB
        val Fab = RoundedCornerShape(20.dp)

        // 缩略图
        val Thumbnail = RoundedCornerShape(12.dp)
        val ImageSmall = RoundedCornerShape(12.dp)

        // 通用
        val Small = RoundedCornerShape(8.dp)
        val Medium = RoundedCornerShape(12.dp)
        val Large = RoundedCornerShape(16.dp)
        val ExtraLarge = RoundedCornerShape(20.dp)
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
        val StatCardHeight = 100.dp

        // 首页横滑文章卡
        val ArticleCarouselWidth = 200.dp
        val ArticleCarouselHeight = 220.dp

        // 快速操作按钮
        val QuickActionHeight = 80.dp
    }

    // ═══════════════════════════════════════════════════════
    // 排版
    // ═══════════════════════════════════════════════════════
    object Typography {
        val DisplayLarge = 48.sp
        val DisplayMedium = 40.sp
        val DisplaySmall = 36.sp
        val HeadlineLarge = 36.sp
        val HeadlineMedium = 28.sp
        val HeadlineSmall = 24.sp
        val TitleLarge = 24.sp
        val TitleMedium = 20.sp
        val TitleSmall = 16.sp
        val BodyLarge = 16.sp
        val BodyMedium = 14.sp
        val BodySmall = 12.sp
        val LabelLarge = 14.sp
        val LabelMedium = 12.sp
        val LabelSmall = 11.sp
    }

    // ═══════════════════════════════════════════════════════
    // 动效
    // ═══════════════════════════════════════════════════════
    object Animation {
        val CardPressScale = 0.97f
        val CardPressDuration = 100
        val NumberScrollDuration = 800
        val FabSpringDamping = 0.6f
        val FabSpringStiffness = 400f
        val NavigationTransitionDuration = 300
        val ShimmerDuration = 2000
        val CrossfadeDuration = 300
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
