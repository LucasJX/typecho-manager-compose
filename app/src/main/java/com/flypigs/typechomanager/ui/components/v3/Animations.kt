package com.flypigs.typechomanager.ui.components.v3

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.flypigs.typechomanager.ui.designsystem.DesignSystem

/**
 * 卡片点击缩放效果 — Scale 1.0 → 0.97 (弹性)
 */
@Composable
fun Modifier.cardPressEffect(onClick: () -> Unit): Modifier {
    val scale = remember { Animatable(1f) }

    return this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    scale.animateTo(
                        targetValue = DesignSystem.Animation.CardPressScale,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        )
                    )
                    try {
                        awaitRelease()
                    } finally {
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                },
                onTap = { onClick() }
            )
        }
}

/**
 * CountUp 数字动画状态 — 从 0 到 targetValue
 */
@Composable
fun rememberCountUpState(targetValue: Int): Int {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatable.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(
                durationMillis = DesignSystem.Animation.NumberScrollDuration
            )
        )
    }

    return animatable.value.toInt()
}

/**
 * 列表项入场动画 — 渐现 + 从下方滑入
 * 用于 LazyColumn items 的 staggered 入场效果
 */
@Composable
fun Modifier.itemEnterAnimation(index: Int): Modifier {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(DesignSystem.Entrance.ItemSlideOffset) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * DesignSystem.Entrance.ItemDelay.toLong())
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = DesignSystem.Entrance.ItemDuration)
        )
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = DesignSystem.Entrance.ItemDuration)
        )
    }

    return this.graphicsLayer {
        this.alpha = alpha.value
        translationY = offsetY.value
    }
}

/**
 * FAB Morph 动效 — Extended FAB ↔ Small FAB 平滑变形
 *
 * Material 3 Morph 动画：
 * - 展开态：ExtendedFloatingActionButton（图标 + 文字）
 * - 收缩态：SmallFloatingActionButton（仅图标）
 * - 文字通过 expandHorizontally/shrinkHorizontally 进出
 * - 整体通过 AnimatedContent 平滑过渡
 */
@Composable
fun MorphingFab(
    extended: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = { Icon(Icons.Default.Edit, "写文章") },
    text: @Composable () -> Unit = { Text("写文章") },
) {
    AnimatedContent(
        targetState = extended,
        modifier = modifier,
        transitionSpec = {
            if (targetState) {
                // 收缩 → 展开：文字从右侧滑入，容器扩展
                (fadeIn(tween(200)) + expandHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    expandFrom = Alignment.Start
                )).togetherWith(
                    fadeOut(tween(100))
                ) using SizeTransform(clip = false)
            } else {
                // 展开 → 收缩：文字向右侧滑出，容器收缩
                (fadeIn(tween(200))).togetherWith(
                    fadeOut(tween(100)) + shrinkHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        shrinkTowards = Alignment.End
                    )
                ) using SizeTransform(clip = false)
            }
        },
        label = "fab_morph"
    ) { isExtended ->
        if (isExtended) {
            ExtendedFloatingActionButton(
                onClick = onClick,
                icon = icon,
                text = text,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = DesignSystem.Corner.Fab,
            )
        } else {
            SmallFloatingActionButton(
                onClick = onClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = DesignSystem.Corner.Fab,
            ) {
                icon()
            }
        }
    }
}
