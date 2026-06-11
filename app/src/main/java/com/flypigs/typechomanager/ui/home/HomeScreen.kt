package com.flypigs.typechomanager.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.flypigs.typechomanager.data.model.Post
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNewPost: () -> Unit = {},
    onPostClick: (Int) -> Unit = {},
    onNavigateToPosts: () -> Unit = {},
    onNavigateToAttachments: () -> Unit = {},
) {
    val viewModel = hiltViewModel<HomeViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val publishedPosts = uiState.allPosts.filter { it.status == "publish" }

    val listState = rememberLazyListState()
    var lastFirstVisibleIndex by remember { mutableIntStateOf(0) }
    val fabVisible = remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                fabVisible.value = index <= lastFirstVisibleIndex || index == 0
                lastFirstVisibleIndex = index
            }
    }

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible.value,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                ExtendedFloatingActionButton(
                    onClick = onNewPost,
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    text = { Text("写文章") },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = DesignSystem.Fab.BottomPadding)
            ) {
                // 欢迎信息
                item {
                    WelcomeSection(
                        userName = "管理员",
                        publishedCount = publishedPosts.size,
                        draftCount = uiState.allPosts.count { it.status == "draft" }
                    )
                }

                // 今日数据卡片
                item {
                    TodayStatsSection(
                        postCount = uiState.allPosts.size,
                        categoryCount = uiState.categories.size,
                        attachmentCount = uiState.attachmentCount,
                        onPostClick = onNavigateToPosts,
                        onCategoryClick = onNavigateToPosts,
                        onAttachmentClick = onNavigateToAttachments
                    )
                }

                // 快速操作
                item {
                    QuickActionsSection(
                        onNewPost = onNewPost,
                        onAttachments = onNavigateToAttachments
                    )
                }

                // 最近文章标题
                item {
                    SectionHeader(
                        title = "最近文章",
                        actionText = "查看全部",
                        onAction = onNavigateToPosts
                    )
                }

                // 最近文章列表
                if (uiState.isLoading && uiState.allPosts.isEmpty()) {
                    items(3) {
                        PostCardSkeleton()
                    }
                } else {
                    items(publishedPosts.take(5)) { post ->
                        PostCard(
                            post = post,
                            onClick = { onPostClick(post.cid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeSection(
    userName: String,
    publishedCount: Int,
    draftCount: Int
) {
    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "早上好"
        in 12..17 -> "下午好"
        else -> "晚上好"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSystem.Spacing.ExtraLarge, vertical = DesignSystem.Spacing.Large)
    ) {
        Text(
            text = "$greeting，$userName",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.ExtraSmall))
        Text(
            text = "已发布 $publishedCount 篇文章，$draftCount 篇草稿",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TodayStatsSection(
    postCount: Int,
    categoryCount: Int,
    attachmentCount: Int,
    onPostClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onAttachmentClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSystem.Spacing.ExtraLarge),
        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Description,
            label = "文章",
            value = postCount.toString(),
            color = MaterialTheme.colorScheme.primary,
            onClick = onPostClick
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Category,
            label = "分类",
            value = categoryCount.toString(),
            color = MaterialTheme.colorScheme.secondary,
            onClick = onCategoryClick
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Attachment,
            label = "附件",
            value = attachmentCount.toString(),
            color = MaterialTheme.colorScheme.tertiary,
            onClick = onAttachmentClick
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.clickable(onClick = onClick),
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.Large),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(DesignSystem.Card.IconSize)
                    .clip(DesignSystem.Card.IconCorner)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.Medium))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onNewPost: () -> Unit,
    onAttachments: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSystem.Spacing.ExtraLarge, vertical = DesignSystem.Spacing.Medium)
    ) {
        Text(
            text = "快速操作",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium)
        ) {
            ActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Edit,
                label = "写文章",
                description = "创建新内容",
                color = MaterialTheme.colorScheme.primary,
                onClick = onNewPost
            )
            ActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Attachment,
                label = "附件",
                description = "管理媒体文件",
                color = MaterialTheme.colorScheme.tertiary,
                onClick = onAttachments
            )
        }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.clickable(onClick = onClick),
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.Large)
        ) {
            Box(
                modifier = Modifier
                    .size(DesignSystem.Card.IconSize)
                    .clip(DesignSystem.Card.IconCorner)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSystem.Spacing.ExtraLarge, vertical = DesignSystem.Spacing.Medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PostCard(
    post: Post,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSystem.Spacing.ExtraLarge, vertical = DesignSystem.Spacing.ExtraSmall)
            .clickable(onClick = onClick),
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.Large)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.ExtraSmall))
                Text(
                    text = stripHtml(post.text).take(80),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Small))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestamp(post.created),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (post.categories.isNotEmpty()) {
                        Text(
                            text = " · ${post.categories.first()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (post.cover.isNotEmpty()) {
                Spacer(modifier = Modifier.width(DesignSystem.Spacing.Medium))
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.cover)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(DesignSystem.Card.ThumbnailSize)
                        .clip(DesignSystem.Card.ThumbnailCorner)
                )
            }
        }
    }
}

@Composable
private fun PostCardSkeleton() {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSystem.Spacing.ExtraLarge, vertical = DesignSystem.Spacing.ExtraSmall),
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.Large)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp)
                        .clip(DesignSystem.Corner.Small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Small))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(16.dp)
                        .clip(DesignSystem.Corner.Small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Small))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(14.dp)
                        .clip(DesignSystem.Corner.Small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.Medium))
            Box(
                modifier = Modifier
                    .size(DesignSystem.Card.ThumbnailSize)
                    .clip(DesignSystem.Card.ThumbnailCorner)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (_: Exception) { "" }
}

private fun stripHtml(html: String): String {
    return html
        .replace(Regex("<br\\s*/?>"), "\n")
        .replace(Regex("<[^>]+>"), "")
        .replace(Regex("&[a-zA-Z]+;"), " ")
        .replace(Regex("&#\\d+;"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}
