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
 * Collapsing title — reserved for future use.
 * Currently unused on home screen (replaced by GreetingSection).
 */
@Composable
fun CollapsingTitle(
    scrollProgress: Float,
    blogName: String = "",
    modifier: Modifier = Modifier,
) {
    if (blogName.isBlank()) return

    val fontSize by animateFloatAsState(
        targetValue = if (scrollProgress < 0.5f) {
            DesignSystem.Constants.HomeTitleInitialSize.value
        } else {
            DesignSystem.Constants.HomeTitleCollapsedSize.value
        },
        animationSpec = tween(durationMillis = 300),
        label = "titleFontSize"
    )

    val alpha by animateFloatAsState(
        targetValue = if (scrollProgress < 0.5f) 1f else DesignSystem.Constants.HomeTitleCollapsedAlpha,
        animationSpec = tween(durationMillis = 300),
        label = "titleAlpha"
    )

    val color = if (scrollProgress < 0.5f) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSystem.Spacing.Large, vertical = DesignSystem.Spacing.ExtraSmall)
    ) {
        Text(
            text = blogName,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = fontSize.sp,
            ),
            color = color,
            modifier = Modifier
                .alpha(alpha)
                .graphicsLayer {
                    translationY = scrollProgress * 20f
                },
        )
    }
}
