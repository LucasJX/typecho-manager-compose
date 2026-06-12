package com.flypigs.typechomanager.ui.postdetail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import com.flypigs.typechomanager.ui.editor.MarkdownPreview
import com.flypigs.typechomanager.util.extractFirstImageUrl
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

    // 工具栏背景透明度
    val scrollState = rememberLazyListState()
    val toolbarAlpha by remember {
        derivedStateOf {
            val firstVisibleIndex = scrollState.firstVisibleItemIndex
            val firstVisibleOffset = scrollState.firstVisibleItemScrollOffset
            if (firstVisibleIndex == 0) {
                (firstVisibleOffset / 200f).coerceIn(0f, 1f)
            } else {
                1f
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            } else if (post != null) {
                val p = post!!
                val isDraftOrPrivate = p.status == "draft" || p.status == "private"
                val coverUrl = p.cover.takeIf { it.isNotEmpty() } ?: extractFirstImageUrl(p.text)

                // 内容区域
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // 头图
                    item(key = "header_image") {
                        if (coverUrl != null) {
                            AsyncImage(
                                model = coverUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                                MaterialTheme.colorScheme.surface,
                                            )
                                        )
                                    ),
                            )
                        }
                    }

                    // 文章信息
                    item(key = "article_info") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignSystem.Spacing.Large),
                        ) {
                            // 标题
                            Text(
                                text = p.title.ifBlank { "(无标题)" },
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

                            // 元信息
                            Row(
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
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
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
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
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
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }

                // 顶部透明工具栏（悬浮于内容上方）
                TopAppBar(
                    title = {
                        AnimatedVisibility(
                            visible = toolbarAlpha > 0.5f,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Text(
                                text = p.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = if (toolbarAlpha < 0.5f) Color.White else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = toolbarAlpha),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { alpha = 1f },
                )

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
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                                        Spacer(modifier = Modifier.width(4.dp))
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
                                Spacer(modifier = Modifier.width(4.dp))
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
