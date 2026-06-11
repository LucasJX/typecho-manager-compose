package com.flypigs.typechomanager.ui.components.v3

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flypigs.typechomanager.ui.designsystem.DesignSystem

/**
 * 统计条 - 高度 72dp，背景 surfaceContainerHighest，圆角 24dp
 * 横向均分显示统计数据，数字从 0 滚动到目标值
 */
@Composable
fun StatBar(
    stats: List<StatItem>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(DesignSystem.Component.StatBarHeight)
            .clip(DesignSystem.Corner.StatBar)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = DesignSystem.Spacing.Large),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        stats.forEach { stat ->
            StatColumn(
                value = stat.value,
                label = stat.label,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatColumn(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    // 数字滚动动画：从 0 滚动到目标值
    var targetValue by remember { mutableIntStateOf(0) }
    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(
            durationMillis = DesignSystem.Animation.NumberScrollDuration,
            easing = FastOutSlowInEasing,
        ),
        label = "statValue"
    )

    LaunchedEffect(value) {
        targetValue = value
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = animatedValue.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 统计项数据
 */
data class StatItem(
    val value: Int,
    val label: String,
)
