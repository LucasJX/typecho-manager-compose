package com.flypigs.typechomanager.ui.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Design System
 * 
 * 统一的设计规范，确保所有页面视觉语言一致
 */
object DesignSystem {
    
    // 圆角系统 - 8dp 网格
    object Corner {
        val Small = RoundedCornerShape(8.dp)
        val Medium = RoundedCornerShape(12.dp)
        val Large = RoundedCornerShape(16.dp)
        val ExtraLarge = RoundedCornerShape(20.dp)
        val Card = RoundedCornerShape(20.dp)  // 卡片统一圆角
    }
    
    // 间距系统 - 8dp 网格
    object Spacing {
        val ExtraSmall = 4.dp
        val Small = 8.dp
        val Medium = 12.dp
        val Large = 16.dp
        val ExtraLarge = 20.dp
        val XXLarge = 24.dp
        val XXXLarge = 32.dp
    }
    
    // 卡片尺寸
    object Card {
        val MinHeight = 80.dp
        val IconSize = 44.dp
        val IconCorner = RoundedCornerShape(12.dp)
        val ThumbnailSize = 80.dp
        val ThumbnailCorner = RoundedCornerShape(12.dp)
    }
    
    // 搜索框
    object Search {
        val Height = 56.dp
        val Corner = RoundedCornerShape(16.dp)
    }
    
    // FilterChip
    object Chip {
        val Height = 36.dp
        val Corner = RoundedCornerShape(20.dp)
    }
    
    // FAB
    object Fab {
        val Corner = RoundedCornerShape(16.dp)
        val BottomPadding = 88.dp
    }
    
    // NavigationBar
    object NavBar {
        val Height = 80.dp
        val HorizontalPadding = 16.dp
    }
    
    // 字体层级
    object Typography {
        val DisplayLarge = 48
        val DisplayMedium = 40
        val DisplaySmall = 36
        val HeadlineLarge = 32
        val HeadlineMedium = 28
        val HeadlineSmall = 24
        val TitleLarge = 22
        val TitleMedium = 16
        val TitleSmall = 14
        val BodyLarge = 16
        val BodyMedium = 14
        val BodySmall = 12
        val LabelLarge = 14
        val LabelMedium = 12
        val LabelSmall = 11
    }
    
    // 分类颜色
    object CategoryColors {
        val Default = 0xFF7C8CFF
        val Life = 0xFF4CAF50      // 生活杂谈 - 绿色
        val Tech = 0xFF2196F3      // 技术手机 - 蓝色
        val AI = 0xFF9C27B0        // AI - 紫色
        val Tools = 0xFFFF9800     // 工具 - 橙色
    }
}
