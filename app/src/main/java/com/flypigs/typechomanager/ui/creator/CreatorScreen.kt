package com.flypigs.typechomanager.ui.creator

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flypigs.typechomanager.data.model.Post
import com.flypigs.typechomanager.ui.components.v3.CreatorSkeleton
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.shape.RoundedCornerShape

// ═══════════════════════════════════════════════════════════════
// CreatorScreen — Blogga V3 创作中心（重设计）
//
// 布局 (LazyColumn):
//   1. 标题区 — "创作中心" + 渐变图标徽章
//   2. 英雄卡 — 渐变背景大卡片 + 创作灵感 CTA
//   3. 统计行 — 已发布 / 草稿 / 总字数（3列卡片）
//   4. 快捷操作 — 写文章 / 新建草稿 / 上传图片（3列）
//   5. 最近草稿（标题 + 草稿卡片 ×5）
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorScreen(
    onWriteArticle: () -> Unit = {},
    onNewDraft: () -> Unit = {},
    onDraftClick: (Int) -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: CreatorViewModel = hiltViewModel(),
) {
    BackHandler(onBack = onBack)
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadImage(it) }
    }

    // 显示上传结果
    LaunchedEffect(uiState.uploadResult, uiState.error) {
        val message = uiState.uploadResult ?: uiState.error
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearResult()
        }
    }

    // 入场动画状态
    val enterState = remember { MutableTransitionState(false) }
    LaunchedEffect(uiState.isLoadingDrafts) {
        if (!uiState.isLoadingDrafts) {
            enterState.targetState = true
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() },
    ) {
        // 骨架屏
        if (uiState.isLoadingDrafts && uiState.recentDrafts.isEmpty()) {
            CreatorSkeleton()
            return@PullToRefreshBox
        }

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = DesignSystem.Spacing.Large),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.SectionGap),
            ) {
                // ─── 1. 标题区 ───
                item(key = "header") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration)) +
                            slideInVertically(
                                tween(DesignSystem.Entrance.SectionDuration),
                                initialOffsetY = { -DesignSystem.Entrance.SectionSlideOffset },
                            ),
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                start = DesignSystem.Spacing.Large,
                                top = DesignSystem.Spacing.Large,
                                end = DesignSystem.Spacing.Large,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
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
                                    imageVector = Icons.Default.Create,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = Color.White,
                                )
                            }
                            Column {
                                Text(
                                    text = "创作中心",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "今天写点什么？",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // ─── 2. 英雄卡 — 渐变背景 CTA ───
                item(key = "hero") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay)) +
                            slideInVertically(
                                tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay),
                                initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                            ),
                    ) {
                        HeroCreateCard(
                            onWriteClick = onWriteArticle,
                            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                        )
                    }
                }

                // ─── 3. 统计行 — 3 列卡片 ───
                item(key = "stats") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 2)) +
                            slideInVertically(
                                tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 2),
                                initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
                        ) {
                            CreatorStatCard(
                                icon = Icons.Default.Visibility,
                                value = uiState.publishedCount.toString(),
                                label = "已发布",
                                accentColor = DesignSystem.BrandColors.Primary,
                            )
                            CreatorStatCard(
                                icon = Icons.Default.Description,
                                value = uiState.recentDrafts.size.toString(),
                                label = "草稿",
                                accentColor = DesignSystem.SemanticColors.Warning,
                            )
                            CreatorStatCard(
                                icon = Icons.Default.TextFields,
                                value = formatWordCount(uiState.totalWordCount),
                                label = "总字数",
                                accentColor = DesignSystem.SemanticColors.Success,
                            )
                        }
                    }
                }

                // ─── 4. 快捷操作 — 3 列 ───
                item(key = "actions") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 3)) +
                            slideInVertically(
                                tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 3),
                                initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                            ),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                        ) {
                            QuickActionCard(
                                label = "写文章",
                                icon = Icons.Default.Create,
                                gradient = Brush.linearGradient(
                                    colors = listOf(
                                        DesignSystem.BrandColors.Primary,
                                        DesignSystem.BrandColors.Secondary,
                                    )
                                ),
                                onClick = onWriteArticle,
                                modifier = Modifier.weight(1f),
                            )
                            QuickActionCard(
                                label = "新草稿",
                                icon = Icons.Default.Description,
                                gradient = Brush.linearGradient(
                                    colors = listOf(
                                        DesignSystem.SemanticColors.Warning,
                                        DesignSystem.SemanticColors.Warning.copy(alpha = 0.7f),
                                    )
                                ),
                                onClick = onNewDraft,
                                modifier = Modifier.weight(1f),
                            )
                            QuickActionCard(
                                label = "传图片",
                                icon = if (uiState.isUploading) null else Icons.Default.CloudUpload,
                                gradient = Brush.linearGradient(
                                    colors = listOf(
                                        DesignSystem.SemanticColors.Success,
                                        DesignSystem.SemanticColors.Success.copy(alpha = 0.7f),
                                    )
                                ),
                                onClick = { if (!uiState.isUploading) imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                isLoading = uiState.isUploading,
                            )
                        }
                    }
                }

                // ─── 5. 最近草稿 ───
                if (uiState.recentDrafts.isNotEmpty()) {
                    item(key = "drafts_header") {
                        AnimatedVisibility(
                            visibleState = enterState,
                            enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 4)) +
                                slideInVertically(
                                    tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 4),
                                    initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                                ),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
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
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color.White,
                                    )
                                }
                                Spacer(modifier = Modifier.width(DesignSystem.Spacing.Medium))
                                Text(
                                    text = "最近草稿",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    itemsIndexed(
                        items = uiState.recentDrafts,
                        key = { _, draft -> "draft_${draft.cid}" },
                    ) { index, draft ->
                        AnimatedVisibility(
                            visibleState = enterState,
                            enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 5 + index * 60)) +
                                slideInVertically(
                                    tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 5 + index * 60),
                                    initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                                ),
                        ) {
                            DraftListItem(
                                draft = draft,
                                onClick = { onDraftClick(draft.cid) },
                                modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 英雄卡 — 渐变背景 + CTA
// ═══════════════════════════════════════════════════════════════
@Composable
private fun HeroCreateCard(
    onWriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onWriteClick,
        modifier = modifier.fillMaxWidth(),
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            DesignSystem.BrandColors.Primary,
                            DesignSystem.BrandColors.Tertiary,
                        )
                    )
                )
                .padding(DesignSystem.Spacing.Large),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "✨ 开始创作",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "把灵感变成文字",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 统计卡片（与首页/素材库统一风格）
// ═══════════════════════════════════════════════════════════════
@Composable
private fun CreatorStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = DesignSystem.Corner.Medium,
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.06f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignSystem.Spacing.Medium, vertical = DesignSystem.Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 左侧彩色竖条
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(28.dp)
                    .background(accentColor, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.Small))
            // 图标
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = accentColor,
            )
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.Small))
            // 标签
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
            // 数字
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 快捷操作卡片
// ═══════════════════════════════════════════════════════════════
@Composable
private fun QuickActionCard(
    label: String,
    icon: ImageVector?,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignSystem.Elevation.Card),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSystem.Spacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = DesignSystem.BrandColors.Primary,
                    strokeWidth = 2.dp,
                )
            } else if (icon != null) {
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
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isLoading) "上传中..." else label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = DesignSystem.Typography.Label,
                ),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 草稿列表项
// ═══════════════════════════════════════════════════════════════
@Composable
private fun DraftListItem(
    draft: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = DesignSystem.Corner.Medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.Medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = draft.title.ifEmpty { "无标题草稿" },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = DesignSystem.Typography.Body,
                    ),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatDraftTime(draft.modified),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = DesignSystem.Typography.Label,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${draft.text.length}字",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = DesignSystem.Typography.Label,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 工具函数
// ═══════════════════════════════════════════════════════════════
private fun formatDraftTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "刚刚编辑"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前编辑"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前编辑"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前编辑"
        else -> SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun formatWordCount(count: Int): String {
    return when {
        count >= 10_000 -> String.format("%.1fw", count / 10_000.0)
        count >= 1_000 -> String.format("%.1fk", count / 1_000.0)
        else -> count.toString()
    }
}
