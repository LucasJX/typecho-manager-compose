package com.flypigs.typechomanager.ui.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design System v3 - Material 3 Expressive
 * 面向 Blogga for Typecho 的完整设计规范
 */
object DesignSystem {

    // ═══════════════════════════════════════════════════════
    // 网格与间距（8dp 基准）
    // ═══════════════════════════════════════════════════════
    object Spacing {
        val ExtraSmall = 4.dp
        val Small = 8.dp       // 元素最小间距
        val Medium = 12.dp     // 卡片间隙
        val Large = 16.dp      // 页面水平边距、卡片内边距
        val ExtraLarge = 20.dp
        val XXLarge = 24.dp    // 区块间距
        val XXXLarge = 32.dp   // Hero 区大留白
        val SectionGap = 24.dp // 区块间距
        val CardPadding = 16.dp // 卡片内边距
        val CardGap = 12.dp    // 卡片间隙
        val PageHorizontal = 16.dp // 页面水平边距
    }

    // ═══════════════════════════════════════════════════════
    // 圆角系统
    // ═══════════════════════════════════════════════════════
    object Corner {
        // 小型组件（Chip、小按钮、输入框）
        val Chip = RoundedCornerShape(20.dp)
        val SmallButton = RoundedCornerShape(20.dp)
        val Input = RoundedCornerShape(20.dp)

        // 卡片、统计条
        val Card = RoundedCornerShape(24.dp)
        val StatBar = RoundedCornerShape(24.dp)

        // 大容器（Hero 卡片、阅读模式头图）
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
    // 品牌主色组（语义色，非硬编码）
    // ═══════════════════════════════════════════════════════
    object BrandColors {
        val Primary = Color(0xFF7C8CFF)
        val Secondary = Color(0xFF5CC8FF)
        val Tertiary = Color(0xFFB388FF)
    }

    // 语义色
    object SemanticColors {
        val Success = Color(0xFF4CAF50)
        val Warning = Color(0xFFFFB020)
        val Error = Color(0xFFFF5252)
    }

    // 分类专属色（用于标签）
    object CategoryColors {
        val Life = Color(0xFF66BB6A)        // 生活杂谈
        val Tech = Color(0xFF42A5F5)        // 技术
        val AI = Color(0xFFAB47BC)          // AI
        val Tools = Color(0xFFFF7043)       // 工具
        val Travel = Color(0xFF26C6DA)      // 旅行
    }

    // ═══════════════════════════════════════════════════════
    // 组件尺寸
    // ═══════════════════════════════════════════════════════
    object Component {
        // NavigationBar
        val NavBarHeight = 80.dp
        val NavBarHorizontalPadding = 16.dp

        // 搜索栏
        val SearchBarHeight = 56.dp
        val SearchBarCorner = Corner.Input

        // FilterChip
        val ChipHeight = 36.dp
        val ChipCorner = Corner.Chip
        val ChipGap = 8.dp

        // StatBar
        val StatBarHeight = 72.dp
        val StatBarCorner = Corner.StatBar

        // 文章卡片
        val CardThumbnailSize = 80.dp
        val CardThumbnailCorner = Corner.Thumbnail
        val CardCorner = Corner.Card

        // FAB
        val FabCorner = Corner.Fab
        val FabBottomPadding = 88.dp // 含导航栏

        // 附件网格
        val AttachmentGridColumns = 2
        val AttachmentItemCorner = Corner.Thumbnail

        // Hero 卡片
        val HeroHeight = 200.dp
        val HeroCorner = Corner.Hero

        // 热力图
        val HeatmapHeight = 120.dp
    }

    // ═══════════════════════════════════════════════════════
    // 排版层级（Expressive 强化版）
    // ═══════════════════════════════════════════════════════
    object Typography {
        // Display
        val DisplayLarge = 48.sp
        val DisplayMedium = 40.sp
        val DisplaySmall = 36.sp

        // Headline
        val HeadlineLarge = 36.sp
        val HeadlineMedium = 28.sp
        val HeadlineSmall = 24.sp

        // Title
        val TitleLarge = 24.sp
        val TitleMedium = 20.sp
        val TitleSmall = 16.sp

        // Body
        val BodyLarge = 16.sp
        val BodyMedium = 14.sp
        val BodySmall = 12.sp

        // Label
        val LabelLarge = 14.sp
        val LabelMedium = 12.sp
        val LabelSmall = 11.sp
    }

    // ═══════════════════════════════════════════════════════
    // 动效参数
    // ═══════════════════════════════════════════════════════
    object Animation {
        // 卡片点击缩放
        val CardPressScale = 0.97f
        val CardPressDuration = 100

        // 数字滚动
        val NumberScrollDuration = 800

        // FAB 动效
        val FabSpringDamping = 0.6f
        val FabSpringStiffness = 400f

        // 导航切换
        val NavigationTransitionDuration = 300

        // 灵感 Chip 微光
        val ShimmerDuration = 2000
    }

    // ═══════════════════════════════════════════════════════
    // 其他常量
    // ═══════════════════════════════════════════════════════
    object Constants {
        // 首页
        val HomeTitleInitialSize = 48.sp
        val HomeTitleCollapsedSize = 24.sp
        val HomeTitleCollapsedAlpha = 0.6f

        // 附件预览
        val AttachmentPreviewContentPadding = 80.dp

        // 日期格式
        const val DateFormatPattern = "yyyy-MM-dd HH:mm:ss"
        const val RelativeTimeFormat = "relative"
    }
}
