package com.flypigs.typechomanager.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// M3 Expressive Shapes - Design System v3
// 圆角系统：20dp（Chip）→ 24dp（卡片）→ 28dp（大容器）

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // 保留最小
    small = RoundedCornerShape(20.dp),       // Chip、小按钮、输入框
    medium = RoundedCornerShape(24.dp),      // 卡片、统计条
    large = RoundedCornerShape(28.dp),       // 大容器（Hero、阅读模式）
    extraLarge = RoundedCornerShape(28.dp),  // 保持一致
)
