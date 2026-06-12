package com.flypigs.typechomanager.ui.components.v3

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.flypigs.typechomanager.ui.designsystem.DesignSystem

/**
 * 骨架屏闪烁动画 Modifier
 */
@Composable
fun Modifier.skeletonShimmer(): Modifier {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeletonAlpha"
    )
    return this.alpha(alpha)
}

/**
 * 骨架块 — 带闪烁的灰色占位
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .skeletonShimmer()
    )
}

// ═══════════════════════════════════════════════════════
// 文章卡片骨架
// ═══════════════════════════════════════════════════════
@Composable
fun ArticleCardSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DesignSystem.Spacing.Large),
        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
    ) {
        // 缩略图占位
        SkeletonBox(modifier = Modifier.size(DesignSystem.Component.CardThumbnailSize))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
        ) {
            // 标题
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.8f).height(18.dp))
            // 分类 + 日期
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp))
            // 摘要
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.6f).height(14.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════
// 首页骨架（标题 + 数据概览 + Hero + 时间线 + 快捷操作）
// ═══════════════════════════════════════════════════════
@Composable
fun HomeSkeleton() {
    LazyColumn(
        contentPadding = PaddingValues(bottom = DesignSystem.Component.FabBottomPadding),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.SectionGap),
    ) {
        // 问候语
        item {
            Column(modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large)) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(48.dp),
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Small))
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .height(16.dp),
                )
            }
        }
        // 数据概览 — 单行 4 格
        item {
            Row(
                modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
            ) {
                repeat(4) {
                    SkeletonBox(
                        modifier = Modifier
                            .weight(1f)
                            .height(DesignSystem.Component.StatCardHeight),
                    )
                }
            }
        }
        // Hero 卡片
        item {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DesignSystem.Component.HeroHeight)
                    .padding(horizontal = DesignSystem.Spacing.Large),
            )
        }
        // 最近动态标题 + 时间线 ×3
        item {
            Column(modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large)) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.25f)
                        .height(24.dp),
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))
                repeat(3) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
                        modifier = Modifier.padding(vertical = DesignSystem.Spacing.Small),
                    ) {
                        SkeletonBox(modifier = Modifier.size(8.dp))
                        SkeletonBox(modifier = Modifier.weight(1f).height(16.dp))
                    }
                }
            }
        }
        // 快捷操作 2×2
        item {
            Column(
                modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium)) {
                    SkeletonBox(modifier = Modifier.weight(1f).height(DesignSystem.Component.QuickActionHeight))
                    SkeletonBox(modifier = Modifier.weight(1f).height(DesignSystem.Component.QuickActionHeight))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium)) {
                    SkeletonBox(modifier = Modifier.weight(1f).height(DesignSystem.Component.QuickActionHeight))
                    SkeletonBox(modifier = Modifier.weight(1f).height(DesignSystem.Component.QuickActionHeight))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 文章列表骨架
// ═══════════════════════════════════════════════════════
@Composable
fun PostsSkeleton() {
    Column {
        // 搜索栏
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignSystem.Component.SearchBarHeight)
                .padding(horizontal = DesignSystem.Spacing.Large),
        )
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))
        // FilterChip 行
        Row(
            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
        ) {
            repeat(4) {
                SkeletonBox(modifier = Modifier.width(72.dp).height(32.dp))
            }
        }
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))
        // 文章卡片 ×5
        repeat(5) {
            ArticleCardSkeleton()
        }
    }
}

// ═══════════════════════════════════════════════════════
// 附件网格骨架
// ═══════════════════════════════════════════════════════
@Composable
fun AttachmentsSkeleton() {
    Column {
        // 标题
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(28.dp)
                .padding(horizontal = DesignSystem.Spacing.Large),
        )
        // 统计行
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(14.dp)
                .padding(horizontal = DesignSystem.Spacing.Large),
        )
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))
        // 搜索栏
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignSystem.Component.SearchBarHeight)
                .padding(horizontal = DesignSystem.Spacing.Large),
        )
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))
        // 2列网格 ×6
        repeat(3) {
            Row(
                modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
            ) {
                SkeletonBox(modifier = Modifier.weight(1f).height(160.dp))
                SkeletonBox(modifier = Modifier.weight(1f).height(160.dp))
            }
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))
        }
    }
}
