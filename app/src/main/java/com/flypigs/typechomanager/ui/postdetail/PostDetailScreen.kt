package com.flypigs.typechomanager.ui.postdetail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import com.flypigs.typechomanager.ui.editor.MarkdownPreview
import com.flypigs.typechomanager.util.extractFirstImageUrl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ═══════════════════════════════════════════════════════════════
// PostDetailScreen — Blogga V3 文章详情页
//
// 布局 (LazyColumn):
//   1. 顶部栏（返回 + 标题 + 操作按钮）
//   2. 封面图 Hero 区域（如有）
//   3. 元信息区（分类 + 状态 + 时间）
//   4. 文章内容（Markdown 渲染）
//   5. 数据统计条（阅读数 + 评论数）
//   6. 底部操作栏（编辑 / 发布）
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    cid: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel(),
) {
    BackHandler(onBack = onBack)
    val post by viewModel.post.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberLazyListState()

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Animation enter state — animate content in once (no isLoading dependency)
    val enterState = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) {
        enterState.targetState = true
    }

    // 加载中骨架屏
    if (isLoading) {
        ArticleDetailSkeleton()
        return
    }

    val p = post ?: return
    val isDraftOrPrivate = p.status == "draft" || p.status == "private"
    val coverUrl = p.cover.takeIf { it.isNotEmpty() } ?: extractFirstImageUrl(p.text)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            // 编辑 FAB（MorphingFab 不适用，用标准 FAB）
            Box(
                modifier = Modifier
                    .padding(bottom = DesignSystem.Spacing.Large)
                    .navigationBarsPadding(),
            ) {
                FilledTonalButton(
                    onClick = { onEdit(cid) },
                    shape = DesignSystem.Corner.Button,
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.ExtraSmall))
                    Text("编辑文章")
                }
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // 内容区域
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Large),
            ) {
                // ─── 1. 顶部栏（返回 + 标题）───
                item(key = "header") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(500)) + slideInVertically(
                            tween(500, delayMillis = 0),
                            initialOffsetY = { -it / 2 },
                        ),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // 返回按钮 + 大标题
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = DesignSystem.Spacing.Large, bottom = DesignSystem.Spacing.Medium)
                                    .padding(horizontal = DesignSystem.Spacing.Large),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.title.ifBlank { "(无标题)" },
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

                            // 元信息行：分类 + 状态 + 时间
                            Row(
                                modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // 分类标签
                                if (p.categories.isNotEmpty()) {
                                    Text(
                                        text = p.categories.first(),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .clip(DesignSystem.Corner.Chip)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = DesignSystem.Spacing.Medium, vertical = DesignSystem.Spacing.Small),
                                    )
                                }

                                // 状态标签
                                if (isDraftOrPrivate) {
                                    val (statusLabel, statusColor) = when (p.status) {
                                        "draft" -> "草稿" to DesignSystem.SemanticColors.Warning
                                        "private" -> "私密" to MaterialTheme.colorScheme.onSurfaceVariant
                                        else -> "草稿" to DesignSystem.SemanticColors.Warning
                                    }
                                    Row(
                                        modifier = Modifier
                                            .clip(DesignSystem.Corner.Chip)
                                            .background(statusColor.copy(alpha = 0.12f))
                                            .padding(horizontal = DesignSystem.Spacing.Medium, vertical = DesignSystem.Spacing.Small),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(statusColor),
                                        )
                                        Text(
                                            text = statusLabel,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = statusColor,
                                        )
                                    }
                                }

                                // 发布时间
                                Text(
                                    text = formatTimestamp(p.created),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            // 标签
                            if (p.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))
                                Row(
                                    modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
                                ) {
                                    p.tags.take(5).forEach { tag ->
                                        Text(
                                            text = "#$tag",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.secondary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ─── 2. 封面图 Hero 区域（如有）───
                if (coverUrl != null) {
                    item(key = "cover") {
                        AnimatedVisibility(
                            visibleState = enterState,
                            enter = fadeIn(tween(500)) + slideInVertically(
                                tween(500, delayMillis = 100),
                                initialOffsetY = { it / 4 },
                            ),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(DesignSystem.Component.HeroHeight)
                                    .padding(horizontal = DesignSystem.Spacing.Large),
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(coverUrl)
                                        .crossfade(DesignSystem.Animation.CrossfadeDuration)
                                        .build(),
                                    contentDescription = p.title,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(DesignSystem.Corner.Hero),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
                    }
                }

                // ─── 3. 文章内容（Markdown 渲染）───
                item(key = "content") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(500)) + slideInVertically(
                            tween(500, delayMillis = 200),
                            initialOffsetY = { it / 2 },
                        ),
                    ) {
                        MarkdownPreview(
                            markdown = removeCoverImage(p.text, coverUrl),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignSystem.Spacing.Medium),
                        )
                    }
                }

                // ─── 4. 数据统计条（阅读数 + 评论数）───
                item(key = "stats") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(500)) + slideInVertically(
                            tween(500, delayMillis = 300),
                            initialOffsetY = { it / 2 },
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
                            // 阅读数
                            StatItem(
                                icon = Icons.Default.Visibility,
                                count = p.viewsCount,
                                label = "阅读",
                            )
                            // 评论数
                            StatItem(
                                icon = Icons.Default.Comment,
                                count = p.commentCount,
                                label = "评论",
                            )
                        }
                    }
                }

                // ─── 5. 底部操作区（草稿/私密文章显示发布按钮）───
                if (isDraftOrPrivate) {
                    item(key = "publish_action") {
                        AnimatedVisibility(
                            visibleState = enterState,
                            enter = fadeIn(tween(500)) + slideInVertically(
                                tween(500, delayMillis = 400),
                                initialOffsetY = { it / 2 },
                            ),
                        ) {
                            FilledTonalButton(
                                onClick = { viewModel.publishPost() },
                                enabled = !isUpdating,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = DesignSystem.Spacing.Large),
                                shape = DesignSystem.Corner.Button,
                            ) {
                                if (isUpdating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Publish,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.ExtraSmall))
                                    Text("发布文章")
                                }
                            }
                        }
                    }
                }
                // 底部间距
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(DesignSystem.Component.FabBottomPadding + DesignSystem.Spacing.Large))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 统计项组件
// ═══════════════════════════════════════════════════════════════
@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
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
                text = formatCount(count),
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
// 骨架屏
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ArticleDetailSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = DesignSystem.Spacing.Large),
    ) {
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
        // 返回按钮 + 标题
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.Medium))
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(28.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Small))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(16.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }
        }
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
        // 封面图占位
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignSystem.Component.HeroHeight)
                .clip(DesignSystem.Corner.Hero)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
        // 内容占位
        repeat(8) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (it % 3 == 0) 0.85f else 1f)
                    .height(16.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Small))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 工具函数
// ═══════════════════════════════════════════════════════════════

/**
 * 从 markdown 中移除包含指定 URL 的图片（避免与封面 Hero 重复）
 * 支持: ![alt](url), <img src="url">, 纯 URL
 */
private fun removeCoverImage(markdown: String, coverUrl: String?): String {
    if (coverUrl.isNullOrBlank()) return markdown
    val escaped = Regex.escape(coverUrl)
    // Try markdown image: ![...](...coverUrl...)
    val mdRegex = Regex("!\\[.*?\\]\\([^)]*" + escaped + "[^)]*\\)")
    mdRegex.find(markdown)?.let { match ->
        val before = markdown.substring(0, match.range.first)
        val after = markdown.substring(match.range.last + 1)
        return (before.trimEnd() + "\n" + after.trimStart()).trim()
    }
    // Try HTML img: <img ...coverUrl...>
    val htmlRegex = Regex("<img[^>]*" + escaped + "[^>]*>")
    htmlRegex.find(markdown)?.let { match ->
        val before = markdown.substring(0, match.range.first)
        val after = markdown.substring(match.range.last + 1)
        return (before.trimEnd() + "\n" + after.trimStart()).trim()
    }
    return markdown
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/** 格式化数字：1234 → 1.2k */
private fun formatCount(count: Int): String {
    return when {
        count >= 10_000 -> String.format("%.1fw", count / 10_000.0)
        count >= 1_000 -> String.format("%.1fk", count / 1_000.0)
        else -> count.toString()
    }
}
