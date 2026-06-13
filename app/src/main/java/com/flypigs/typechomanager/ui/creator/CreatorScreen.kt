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
import androidx.compose.material.icons.filled.TextFields
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.flypigs.typechomanager.data.model.Post
import com.flypigs.typechomanager.ui.components.v3.CreatorSkeleton
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ═══════════════════════════════════════════════════════════════
// CreatorScreen — Blogga V3 创作中心
//
// 布局 (LazyColumn):
//   1. 标题区 — "创作中心" + "今天写点什么？" + 渐变图标
//   2. 创作统计条（总字数 + 草稿数）
//   3. 操作按钮区 — 2×2 网格（渐变图标徽章）
//   4. 最近草稿（标题 + 草稿卡片 ×5）
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorScreen(
    onWriteArticle: () -> Unit = {},
    onNewDraft: () -> Unit = {},
    onAIClick: () -> Unit = {},
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
                // ═══════════════════════════════════════════
                // 1. 标题区 — "创作中心" + 渐变图标徽章（与其他页面统一）
                // ═══════════════════════════════════════════
                item(key = "header") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(500)) + slideInVertically(
                            tween(500),
                            initialOffsetY = { -it / 2 },
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(
                                    start = DesignSystem.Spacing.Large,
                                    top = DesignSystem.Spacing.Large,
                                    end = DesignSystem.Spacing.Large,
                                ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                    ) {
                        // 渐变圆形图标徽章（56dp，与其他页面统一）
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            DesignSystem.BrandColors.Primary,
                                            DesignSystem.BrandColors.Tertiary,
                                        )
                                    )
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

                // ═══════════════════════════════════════════
                // 2. 创作统计条（总字数 + 草稿数）
                // ═══════════════════════════════════════════
                item(key = "creator_stats") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(
                            tween(500, delayMillis = 100),
                            initialOffsetY = { it / 4 },
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignSystem.Spacing.Large)
                                .clip(DesignSystem.Corner.StatBar)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .padding(horizontal = DesignSystem.Spacing.Large, vertical = DesignSystem.Spacing.Medium),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CreatorStatItem(
                            icon = Icons.Default.TextFields,
                            value = formatWordCount(uiState.totalWordCount),
                            label = "总字数",
                        )
                        CreatorStatItem(
                            icon = Icons.Default.Description,
                            value = uiState.recentDrafts.size.toString(),
                            label = "草稿",
                        )
                    }
                    }
                }

                // ═══════════════════════════════════════════
                // 3. 操作按钮区 — 2×2 网格（渐变图标徽章）
                // ═══════════════════════════════════════════
                item(key = "action_grid") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(
                            tween(500, delayMillis = 200),
                            initialOffsetY = { it / 4 },
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
                                BigActionButton(
                                    label = "写文章",
                                    icon = Icons.Default.Create,
                                    gradient = Brush.linearGradient(
                                        colors = listOf(
                                            DesignSystem.BrandColors.Primary,
                                            DesignSystem.BrandColors.Secondary,
                                        )
                                    ),
                                    onClick = onWriteArticle,
                                    modifier = Modifier
                                        .weight(1f),
                                )
                                BigActionButton(
                                    label = "AI辅助",
                                    icon = Icons.Default.AutoAwesome,
                                    gradient = Brush.linearGradient(
                                        colors = listOf(
                                            DesignSystem.BrandColors.Tertiary,
                                            DesignSystem.BrandColors.Primary,
                                        )
                                    ),
                                    onClick = onAIClick,
                                    modifier = Modifier
                                        .weight(1f),
                                )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                        ) {
                            BigActionButton(
                                label = "新建草稿",
                                icon = Icons.Default.Description,
                                gradient = Brush.linearGradient(
                                    colors = listOf(
                                        DesignSystem.SemanticColors.Warning,
                                        DesignSystem.SemanticColors.Warning.copy(alpha = 0.7f),
                                    )
                                ),
                                onClick = onNewDraft,
                                modifier = Modifier
                                    .weight(1f),
                            )
                            BigActionButton(
                                label = "上传图片",
                                icon = if (uiState.isUploading) null else Icons.Default.CloudUpload,
                                gradient = Brush.linearGradient(
                                    colors = listOf(
                                        DesignSystem.SemanticColors.Success,
                                        DesignSystem.SemanticColors.Success.copy(alpha = 0.7f),
                                    )
                                ),
                                onClick = { if (!uiState.isUploading) imagePickerLauncher.launch("image/*") },
                                modifier = Modifier
                                    .weight(1f),
                                isLoading = uiState.isUploading,
                            )
                        }
                    }
                    }
                }

                // ═══════════════════════════════════════════
                // 4. 最近草稿
                // ═══════════════════════════════════════════
                if (uiState.recentDrafts.isNotEmpty()) {
                    item(key = "drafts_header") {
                        AnimatedVisibility(
                            visibleState = enterState,
                            enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(
                                tween(500, delayMillis = 300),
                                initialOffsetY = { it / 4 },
                            ),
                        ) {
                            Text(
                                text = "最近草稿",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = DesignSystem.Typography.Title,
                                ),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .padding(horizontal = DesignSystem.Spacing.Large),
                            )
                        }
                    }

                    itemsIndexed(
                        items = uiState.recentDrafts,
                        key = { _, draft -> "draft_${draft.cid}" },
                    ) { index, draft ->
                        AnimatedVisibility(
                            visibleState = enterState,
                            enter = fadeIn(tween(500, delayMillis = 400 + index * 100)) + slideInVertically(
                                tween(500, delayMillis = 400 + index * 100),
                                initialOffsetY = { it / 4 },
                            ),
                        ) {
                            DraftListItem(
                                draft = draft,
                                onClick = { onDraftClick(draft.cid) },
                                modifier = Modifier
                                    .padding(horizontal = DesignSystem.Spacing.Large),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 统计项组件
// ═══════════════════════════════════════════════════════════════
@Composable
private fun CreatorStatItem(
    icon: ImageVector,
    value: String,
    label: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.ExtraSmall),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.ExtraSmall),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// 大按钮 — 渐变图标徽章 + 按压缩放
// ═══════════════════════════════════════════════════════════════
@Composable
private fun BigActionButton(
    label: String,
    icon: ImageVector?,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(DesignSystem.Component.QuickActionHeight),
        shape = DesignSystem.Corner.Button,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    strokeWidth = 2.dp,
                )
            } else if (icon != null) {
                // 渐变圆形图标徽章
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(gradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White,
                    )
                }
            }
            Text(
                text = if (isLoading) "上传中..." else label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = DesignSystem.Typography.Label,
                ),
                modifier = Modifier.padding(top = DesignSystem.Spacing.ExtraSmall),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 草稿列表项 — 紧凑卡片 + 字数 + 箭头
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
