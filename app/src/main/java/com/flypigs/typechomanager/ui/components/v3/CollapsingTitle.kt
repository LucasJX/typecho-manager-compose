package com.flypigs.typechomanager.ui.components.v3

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flypigs.typechomanager.ui.designsystem.DesignSystem

/**
 * 可折叠大标题 - 首页 "Blogga"
 * 初始 48sp，颜色 primary
 * 向上滚动时：字号缩小到 24sp，颜色变为 onSurface，透明度降低到 0.6
 */
@Composable
fun CollapsingTitle(
    scrollProgress: Float, // 0f = 完全展开, 1f = 完全折叠
    modifier: Modifier = Modifier,
) {
    // 字号动画：48sp → 24sp
    val fontSize by animateFloatAsState(
        targetValue = if (scrollProgress < 0.5f) {
            DesignSystem.Constants.HomeTitleInitialSize.value
        } else {
            DesignSystem.Constants.HomeTitleCollapsedSize.value
        },
        animationSpec = tween(durationMillis = 300),
        label = "titleFontSize"
    )

    // 透明度动画：1f → 0.6f
    val alpha by animateFloatAsState(
        targetValue = if (scrollProgress < 0.5f) 1f else DesignSystem.Constants.HomeTitleCollapsedAlpha,
        animationSpec = tween(durationMillis = 300),
        label = "titleAlpha"
    )

    // 颜色：primary → onSurface
    val color = if (scrollProgress < 0.5f) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSystem.Spacing.Large, vertical = DesignSystem.Spacing.Medium)
    ) {
        Text(
            text = "Blogga",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = fontSize.sp,
            ),
            color = color,
            modifier = Modifier
                .alpha(alpha)
                .graphicsLayer {
                    // 轻微视差效果
                    translationY = scrollProgress * 20f
                },
        )
    }
}
