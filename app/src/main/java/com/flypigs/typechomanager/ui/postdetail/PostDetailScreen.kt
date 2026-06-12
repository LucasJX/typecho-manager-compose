package com.flypigs.typechomanager.ui.postdetail

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import com.flypigs.typechomanager.ui.editor.MarkdownPreview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // 工具栏始终不透明（无封面图）
    val scrollState = rememberLazyListState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            } else if (post != null) {
                val p = post!!
                val isDraftOrPrivate = p.status == "draft" || p.status == "private"

                // 内容区域
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // 返回按钮 + 标题 + 元信息（与其他页面一致）
                    item(key = "article_info") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // 返回按钮 + 大标题（与更新日志/素材库一致）
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = DesignSystem.Spacing.Medium, bottom = DesignSystem.Spacing.ExtraSmall)
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

                            // 元信息
                            Row(
                                modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Large),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // 分类
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

                                // 发布时间
                                Text(
                                    text = formatTimestamp(p.created),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                // 状态标签
                                if (isDraftOrPrivate) {
                                    Text(
                                        text = if (p.status == "draft") "草稿" else "私密",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .clip(DesignSystem.Corner.Chip)
                                            .background(MaterialTheme.colorScheme.errorContainer)
                                            .padding(horizontal = DesignSystem.Spacing.Medium, vertical = DesignSystem.Spacing.Small),
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))

                            // 标签
                            if (p.tags.isNotEmpty()) {
                                Row(
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
                                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
                            }
                        }
                    }

                    // 文章内容（Markdown 渲染）
                    item(key = "content") {
                        MarkdownPreview(
                            markdown = p.text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignSystem.Spacing.Medium),
                        )
                    }

                    // 底部间距
                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(DesignSystem.Component.FabBottomPadding + DesignSystem.Spacing.Large))
                    }
                }

                // 底部悬浮栏（半透明）
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                )
                            )
                        )
                        .padding(
                            horizontal = DesignSystem.Spacing.Large,
                            vertical = DesignSystem.Spacing.Medium,
                        ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 左侧：阅读数 + 评论数
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Large),
                        ) {
                            // 阅读数
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.ExtraSmall),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = p.viewsCount.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            // 评论数
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.ExtraSmall),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Comment,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = p.commentCount.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        // 右侧按钮组
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // 草稿/私密文章显示发布按钮
                            if (isDraftOrPrivate) {
                                FilledTonalButton(
                                    onClick = { viewModel.publishPost() },
                                    enabled = !isUpdating,
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
                                        Text("发布")
                                    }
                                }
                            }

                            // 编辑按钮
                            FilledTonalButton(
                                onClick = { onEdit(cid) },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(DesignSystem.Spacing.ExtraSmall))
                                Text("编辑")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 工具函数
// ═══════════════════════════════════════════════════════
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
