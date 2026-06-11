package com.flypigs.typechomanager.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.flypigs.typechomanager.data.model.Post
import com.flypigs.typechomanager.ui.components.v3.ArticleCard
import com.flypigs.typechomanager.ui.components.v3.CollapsingTitle
import com.flypigs.typechomanager.ui.components.v3.HomeSkeleton
import com.flypigs.typechomanager.ui.components.v3.StatBar
import com.flypigs.typechomanager.ui.components.v3.StatItem
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPostClick: (Int) -> Unit,
    onWriteClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 计算滚动进度（0f = 完全展开, 1f = 完全折叠）
    val scrollProgress by remember {
        derivedStateOf {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val firstVisibleOffset = listState.firstVisibleItemScrollOffset
            if (firstVisibleIndex == 0) {
                (firstVisibleOffset / 200f).coerceIn(0f, 1f)
            } else {
                1f
            }
        }
    }

    // FAB 可见性
    val fabVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex < 2
        }
    }

    // 灵感浮窗
    var showInspirationSheet by remember { mutableStateOf(false) }
    var inspirationText by remember { mutableStateOf("") }

    // 最近发布的文章（用于动态时间线）
    val recentPosts = remember(uiState.allPosts) {
        uiState.allPosts.sortedByDescending { it.created }.take(3)
    }

    // 统计数据
    val publishedCount = remember(uiState.allPosts) {
        uiState.allPosts.count { it.status == "publish" }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() },
    ) {
        // 骨架屏
        if (uiState.isLoading && uiState.allPosts.isEmpty()) {
            HomeSkeleton()
            return@PullToRefreshBox
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                // FAB：滚动隐藏时收缩为小 FAB 再淡出
                AnimatedVisibility(
                    visible = fabVisible,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                ) {
                    ExtendedFloatingActionButton(
                        onClick = onWriteClick,
                        icon = { Icon(Icons.Default.Edit, "写文章") },
                        text = { Text("写文章") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = DesignSystem.Corner.Fab,
                    )
                }
            },
        ) { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = DesignSystem.Component.FabBottomPadding),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.SectionGap),
            ) {
                // ═══════════════════════════════════════════
                // 1. 可折叠大标题
                // ═══════════════════════════════════════════
                item(key = "collapsing_title") {
                    CollapsingTitle(scrollProgress = scrollProgress, blogName = uiState.blogName)
                }

                // ═══════════════════════════════════════════
                // 2. 博客实时预览卡片（Hero 卡）
                // ═══════════════════════════════════════════
                item(key = "hero_card") {
                    val latestPost = uiState.allPosts.firstOrNull()
                    HeroPreviewCard(
                        post = latestPost,
                        blogName = uiState.blogName.ifEmpty { "Blogga" },
                        onClick = { latestPost?.let { onPostClick(it.cid) } },
                    )
                }

                // ═══════════════════════════════════════════
                // 3. 今日统计条
                // ═══════════════════════════════════════════
                item(key = "stat_bar") {
                    StatBar(
                        stats = listOf(
                            StatItem(value = publishedCount, label = "已发布"),
                            StatItem(value = uiState.allPosts.size, label = "总文章"),
                            StatItem(value = uiState.categories.size, label = "分类"),
                            StatItem(value = uiState.attachmentCount, label = "附件"),
                        ),
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                    )
                }

                // ═══════════════════════════════════════════
                // 4. 最近动态时间线
                // ═══════════════════════════════════════════
                item(key = "activity_header") {
                    Text(
                        text = "最近动态",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                    )
                }

                items(
                    items = recentPosts,
                    key = { "activity_${it.cid}" },
                ) { post ->
                    ActivityTimelineItem(
                        post = post,
                        onClick = { onPostClick(post.cid) },
                    )
                }

                // ═══════════════════════════════════════════
                // 5. 最近文章（管理列表）
                // ═══════════════════════════════════════════
                item(key = "recent_header") {
                    Text(
                        text = "最近文章",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                    )
                }

                items(
                    items = uiState.posts.take(5),
                    key = { "post_${it.cid}" },
                ) { post ->
                    ArticleCard(
                        post = post,
                        onClick = { onPostClick(post.cid) },
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                    )
                }

                // 底部间距
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
                }
            }
        }

        // ═══════════════════════════════════════════
        // 6. 灵感浮窗 Chip（吸附在右下角）
        // ═══════════════════════════════════════════
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd,
        ) {
            FloatingActionButton(
                onClick = { showInspirationSheet = true },
                modifier = Modifier
                    .padding(
                        end = DesignSystem.Spacing.Large,
                        bottom = DesignSystem.Component.FabBottomPadding + DesignSystem.Spacing.Large,
                    )
                    .size(48.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = "记灵感",
                )
            }
        }
    }

    // 灵感浮窗底部弹窗
    if (showInspirationSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showInspirationSheet = false
                inspirationText = ""
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignSystem.Spacing.Large),
            ) {
                Text(
                    text = "记录灵感",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.Medium),
                )

                TextField(
                    value = inspirationText,
                    onValueChange = { inspirationText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("写下你的想法...") },
                    minLines = 3,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = DesignSystem.Spacing.Medium),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = {
                            showInspirationSheet = false
                            inspirationText = ""
                        },
                    ) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.Small))
                    TextButton(
                        onClick = {
                            // TODO: 保存灵感为草稿
                            scope.launch {
                                snackbarHostState.showSnackbar("灵感已保存为草稿")
                            }
                            showInspirationSheet = false
                            inspirationText = ""
                        },
                        enabled = inspirationText.isNotBlank(),
                    ) {
                        Text("保存草稿")
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// Hero 预览卡片
// ═══════════════════════════════════════════════════════
@Composable
private fun HeroPreviewCard(
    post: Post?,
    blogName: String = "Blogga",
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(DesignSystem.Component.HeroHeight)
            .padding(horizontal = DesignSystem.Spacing.Large)
            .clickable(onClick = onClick),
        shape = DesignSystem.Corner.Hero,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 背景图或占位
            if (post?.cover != null && post.cover.isNotEmpty()) {
                AsyncImage(
                    model = post.cover,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                )
                            )
                        ),
                )
            }

            // 底部渐变遮罩
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f),
                            ),
                            startY = 100f,
                        )
                    ),
            )

            // 底部信息
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(DesignSystem.Spacing.Large),
            ) {
                Text(
                    text = blogName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                )
                if (post != null) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 动态时间线项
// ═══════════════════════════════════════════════════════
@Composable
private fun ActivityTimelineItem(
    post: Post,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = DesignSystem.Spacing.Large,
                vertical = DesignSystem.Spacing.Small,
            ),
        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
    ) {
        // 时间轴小圆点
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "你发布了《${post.title}》",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatRelativeTime(post.created),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 工具函数
// ═══════════════════════════════════════════════════════
private fun formatRelativeTime(date: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - date

    return when {
        diff < 60 * 1000 -> "刚刚"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前"
        else -> SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(date))
    }
}
