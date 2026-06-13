package com.flypigs.typechomanager.ui.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import com.flypigs.typechomanager.data.model.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // 入场动画
    val enterState = remember { MutableTransitionState(false) }
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && uiState.totalPosts >= 0) {
            enterState.targetState = true
        }
    }

    Scaffold { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = DesignSystem.BrandColors.Primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = DesignSystem.Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.SectionGap),
        ) {
            // ─── 1. 标题栏 ───
            item(key = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = DesignSystem.Spacing.Large,
                            end = DesignSystem.Spacing.Large,
                            top = DesignSystem.Spacing.Large,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.Small))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        DesignSystem.BrandColors.Primary,
                                        DesignSystem.BrandColors.Tertiary,
                                    )
                                ),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueryStats,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.White,
                        )
                    }
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.Medium))
                    Column {
                        Text(
                            text = "数据统计",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "博客运营全景",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // ─── 2. 核心指标卡片（2×2） ───
            item(key = "overview") {
                AnimatedVisibility(
                    visibleState = enterState,
                    enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration)) +
                        slideInVertically(
                            tween(DesignSystem.Entrance.SectionDuration),
                            initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                        ) {
                            StatsOverviewCard(
                                label = "已发布",
                                value = uiState.publishedCount.toString(),
                                icon = Icons.Default.Visibility,
                                gradient = Brush.linearGradient(
                                    colors = listOf(
                                        DesignSystem.BrandColors.Primary,
                                        DesignSystem.BrandColors.Primary.copy(alpha = 0.7f),
                                    )
                                ),
                                modifier = Modifier.weight(1f),
                            )
                            StatsOverviewCard(
                                label = "草稿箱",
                                value = uiState.draftCount.toString(),
                                icon = Icons.Default.Edit,
                                gradient = Brush.linearGradient(
                                    colors = listOf(
                                        DesignSystem.SemanticColors.Warning,
                                        DesignSystem.SemanticColors.Warning.copy(alpha = 0.7f),
                                    )
                                ),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                        ) {
                            StatsOverviewCard(
                                label = "分类",
                                value = uiState.categoryCount.toString(),
                                icon = Icons.Default.Category,
                                gradient = Brush.linearGradient(
                                    colors = listOf(
                                        DesignSystem.SemanticColors.Success,
                                        DesignSystem.SemanticColors.Success.copy(alpha = 0.7f),
                                    )
                                ),
                                modifier = Modifier.weight(1f),
                            )
                            StatsOverviewCard(
                                label = "附件",
                                value = uiState.attachmentCount.toString(),
                                icon = Icons.Default.AttachFile,
                                gradient = Brush.linearGradient(
                                    colors = listOf(
                                        DesignSystem.BrandColors.Tertiary,
                                        DesignSystem.BrandColors.Tertiary.copy(alpha = 0.7f),
                                    )
                                ),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            // ─── 3. 创作洞察（连续发文 + 月均篇数 + 浏览量 + 评论） ───
            item(key = "insights") {
                AnimatedVisibility(
                    visibleState = enterState,
                    enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay)) +
                        slideInVertically(
                            tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay),
                            initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                        ) {
                            InsightCard(
                                icon = Icons.Default.LocalFireDepartment,
                                label = "最长连续",
                                value = "${uiState.longestStreak} 天",
                                color = DesignSystem.SemanticColors.Warning,
                                modifier = Modifier.weight(1f),
                            )
                            InsightCard(
                                icon = Icons.Default.TrendingUp,
                                label = "月均篇数",
                                value = String.format("%.1f", uiState.avgPostsPerMonth),
                                color = DesignSystem.BrandColors.Primary,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                        ) {
                            InsightCard(
                                icon = Icons.Default.Visibility,
                                label = "总浏览量",
                                value = formatLargeNumber(uiState.totalViews),
                                color = DesignSystem.SemanticColors.Success,
                                modifier = Modifier.weight(1f),
                            )
                            InsightCard(
                                icon = Icons.Default.AutoAwesome,
                                label = "总评论",
                                value = uiState.totalComments.toString(),
                                color = DesignSystem.BrandColors.Tertiary,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            // ─── 4. 月度发文趋势（柱状图） ───
            if (uiState.monthlyPosts.isNotEmpty()) {
                item(key = "chart_header") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 2)) +
                            slideInVertically(
                                tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 2),
                                initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                            ),
                    ) {
                        SectionHeader(
                            icon = Icons.Default.TrendingUp,
                            title = "发文趋势",
                            subtitle = "最近 12 个月",
                        )
                    }
                }
                item(key = "chart") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 3)) +
                            slideInVertically(
                                tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 3),
                                initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                            ),
                    ) {
                        MonthlyBarChart(
                            data = uiState.monthlyPosts,
                            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                        )
                    }
                }
            }

            // ─── 5. 分类分布 ───
            if (uiState.categoryStats.isNotEmpty()) {
                item(key = "category_header") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 4)) +
                            slideInVertically(
                                tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 4),
                                initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                            ),
                    ) {
                        SectionHeader(
                            icon = Icons.Default.Category,
                            title = "分类分布",
                            subtitle = "共 ${uiState.categoryCount} 个分类",
                        )
                    }
                }
                itemsIndexed(
                    items = uiState.categoryStats.take(8),
                    key = { _, item -> "cat_${item.name}" },
                ) { index, category ->
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 5 + index * 60)) +
                            slideInVertically(
                                tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 5 + index * 60),
                                initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                            ),
                    ) {
                        CategoryBar(
                            name = category.name,
                            count = category.count,
                            maxCount = uiState.categoryStats.maxOfOrNull { it.count } ?: 1,
                            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                        )
                    }
                }
            }

            // ─── 6. 最近发布 ───
            if (uiState.recentPosts.isNotEmpty()) {
                item(key = "recent_header") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 6)) +
                            slideInVertically(
                                tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 6),
                                initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                            ),
                    ) {
                        SectionHeader(
                            icon = Icons.Default.AutoAwesome,
                            title = "最近发布",
                            subtitle = "最新 ${uiState.recentPosts.size} 篇",
                        )
                    }
                }
                itemsIndexed(
                    items = uiState.recentPosts,
                    key = { _, post -> "recent_${post.cid}" },
                ) { index, post ->
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 7 + index * 60)) +
                            slideInVertically(
                                tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 7 + index * 60),
                                initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                            ),
                    ) {
                        RecentPostItem(
                            title = post.title,
                            date = formatTimestamp(post.created),
                            status = post.status,
                            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                        )
                    }
                }
            }

            // 底部留白
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 子组件
// ═══════════════════════════════════════════════════════════════

@Composable
private fun StatsOverviewCard(
    label: String,
    value: String,
    icon: ImageVector,
    gradient: Brush,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(88.dp),
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignSystem.Elevation.Card),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = DesignSystem.Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(gradient, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = DesignSystem.Typography.Title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = DesignSystem.Typography.Label),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InsightCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignSystem.Elevation.Card),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = color,
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = DesignSystem.Typography.Label),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = DesignSystem.Spacing.Large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            DesignSystem.BrandColors.Primary,
                            DesignSystem.BrandColors.Tertiary,
                        )
                    ),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White,
            )
        }
        Spacer(modifier = Modifier.width(DesignSystem.Spacing.Medium))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MonthlyBarChart(
    data: List<MonthlyPosts>,
    modifier: Modifier = Modifier,
) {
    val maxCount = data.maxOfOrNull { it.count } ?: 1
    val primaryColor = DesignSystem.BrandColors.Primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignSystem.Elevation.Card),
    ) {
        Column(
            modifier = Modifier.padding(DesignSystem.Spacing.Medium),
        ) {
            // 柱状图
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            ) {
                val barCount = data.size
                val totalWidth = size.width
                val barWidth = totalWidth / barCount * 0.6f
                val gap = totalWidth / barCount * 0.4f
                val maxHeight = size.height - 30f

                data.forEachIndexed { index, item ->
                    val x = index * (barWidth + gap) + gap / 2
                    val barHeight = if (maxCount > 0) {
                        (item.count.toFloat() / maxCount) * maxHeight
                    } else 0f
                    val y = size.height - barHeight - 20f

                    // 背景柱
                    drawRoundRect(
                        color = surfaceVariant,
                        topLeft = Offset(x, 20f),
                        size = Size(barWidth, maxHeight),
                        cornerRadius = CornerRadius(6f, 6f),
                    )

                    // 数据柱（渐变）
                    if (item.count > 0) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor,
                                    primaryColor.copy(alpha = 0.6f),
                                ),
                                startY = y,
                                endY = size.height - 20f,
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6f, 6f),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Small))

            // 月份标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                data.forEach { item ->
                    Text(
                        text = item.month,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBar(
    name: String,
    count: Int,
    maxCount: Int,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (maxCount > 0) count.toFloat() / maxCount else 0f,
        animationSpec = tween(800),
        label = "category_progress",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "$count 篇",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DesignSystem.BrandColors.Primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = DesignSystem.BrandColors.Primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun RecentPostItem(
    title: String,
    date: String,
    status: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
        ) {
            // 状态指示器
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (status == "publish") {
                            DesignSystem.SemanticColors.Success
                        } else {
                            DesignSystem.SemanticColors.Warning
                        },
                        CircleShape,
                    ),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // 状态标签
            Text(
                text = if (status == "publish") "已发布" else "草稿",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = if (status == "publish") {
                    DesignSystem.SemanticColors.Success
                } else {
                    DesignSystem.SemanticColors.Warning
                },
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA)
    return sdf.format(java.util.Date(timestamp * 1000L))
}

private fun formatLargeNumber(num: Int): String {
    return when {
        num >= 10000 -> String.format("%.1f万", num / 10000.0)
        num >= 1000 -> String.format("%.1fk", num / 1000.0)
        else -> num.toString()
    }
}
