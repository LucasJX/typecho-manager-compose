package com.flypigs.typechomanager.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.flypigs.typechomanager.data.model.Post
import com.flypigs.typechomanager.ui.components.v3.CollapsingTitle
import com.flypigs.typechomanager.ui.components.v3.HomeSkeleton
import com.flypigs.typechomanager.ui.components.v3.MorphingFab
import com.flypigs.typechomanager.ui.components.v3.rememberCountUpState
import com.flypigs.typechomanager.ui.components.v3.itemEnterAnimation
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPostClick: (Int) -> Unit,
    onWriteClick: () -> Unit,
    onUploadImageClick: () -> Unit,
    onNewDraftClick: () -> Unit,
    onViewStatsClick: () -> Unit,
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

    // 最近发布的文章（用于动态时间线，取最近 5 篇）
    val recentActivity = remember(uiState.allPosts) {
        uiState.allPosts.sortedByDescending { it.created }.take(5)
    }

    // 横滑文章（取前 5 篇有封面的）
    val carouselPosts = remember(uiState.allPosts) {
        uiState.allPosts.sortedByDescending { it.created }.take(5)
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
                // FAB Morph：滚动时从 Extended 变形为 Small FAB
                MorphingFab(
                    extended = fabVisible,
                    onClick = onWriteClick,
                )
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
                // 2. Greeting
                // ═══════════════════════════════════════════
                item(key = "greeting") {
                    GreetingSection(
                        userName = uiState.userName,
                        postCount = uiState.allPosts.size,
                        allPosts = uiState.allPosts,
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                    )
                }

                // ═══════════════════════════════════════════
                // 3. 2×2 统计卡片
                // ═══════════════════════════════════════════
                item(key = "stats_grid") {
                    StatsGrid(
                        publishedCount = uiState.allPosts.count { it.status == "publish" },
                        draftCount = uiState.draftCount,
                        categoryCount = uiState.categories.size,
                        attachmentCount = uiState.attachmentCount,
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                    )
                }

                // ═══════════════════════════════════════════
                // 4. 最近文章（横滑）
                // ═══════════════════════════════════════════
                item(key = "carousel") {
                    RecentArticlesCarousel(
                        posts = carouselPosts,
                        onPostClick = onPostClick,
                    )
                }

                // ═══════════════════════════════════════════
                // 5. 最近动态时间线
                // ═══════════════════════════════════════════
                item(key = "timeline_header") {
                    Text(
                        text = "最近动态",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                    )
                }

                itemsIndexed(
                    items = recentActivity,
                    key = { _, post -> "activity_${post.cid}" },
                ) { index, post ->
                    ActivityTimelineItem(
                        post = post,
                        onClick = { onPostClick(post.cid) },
                        modifier = Modifier.itemEnterAnimation(index),
                    )
                }

                // ═══════════════════════════════════════════
                // 6. 快速操作
                // ═══════════════════════════════════════════
                item(key = "quick_actions") {
                    QuickActionsRow(
                        onWriteClick = onWriteClick,
                        onUploadImageClick = onUploadImageClick,
                        onNewDraftClick = onNewDraftClick,
                        onViewStatsClick = onViewStatsClick,
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
        // 7. 灵感浮窗 Chip（吸附在右下角）
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
                    imageVector = Icons.Default.Lightbulb,
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
// Greeting
// ═══════════════════════════════════════════════════════
@Composable
private fun GreetingSection(
    userName: String,
    postCount: Int,
    allPosts: List<Post>,
    modifier: Modifier = Modifier,
) {
    val greeting = remember { getGreeting() }
    val latestUpdate = remember(allPosts) {
        if (allPosts.isEmpty()) "暂无更新"
        else formatRelativeTime(allPosts.maxOf { it.created })
    }

    Column(modifier = modifier) {
        Text(
            text = "$greeting，$userName 👋",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "今天有 $postCount 篇文章，最近更新 $latestUpdate",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

private fun getGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 6..11 -> "早上好"
        in 12..13 -> "中午好"
        in 14..17 -> "下午好"
        in 18..22 -> "晚上好"
        else -> "夜深了"
    }
}

// ═══════════════════════════════════════════════════════
// 2×2 统计卡片
// ═══════════════════════════════════════════════════════
@Composable
private fun StatsGrid(
    publishedCount: Int,
    draftCount: Int,
    categoryCount: Int,
    attachmentCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
        ) {
            StatCard(
                label = "文章",
                value = publishedCount,
                icon = Icons.Default.Visibility,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "分类",
                value = categoryCount,
                icon = Icons.Default.QueryStats,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
        ) {
            StatCard(
                label = "附件",
                value = attachmentCount,
                icon = Icons.Default.Image,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "草稿",
                value = draftCount,
                icon = Icons.Default.VisibilityOff,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(DesignSystem.Component.StatCardHeight),
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignSystem.Elevation.Card),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSystem.Spacing.Large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = rememberCountUpState(value).toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 最近文章（横滑）
// ═══════════════════════════════════════════════════════
@Composable
private fun RecentArticlesCarousel(
    posts: List<Post>,
    onPostClick: (Int) -> Unit,
) {
    Column {
        Text(
            text = "最近文章",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
        )

        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = DesignSystem.Spacing.Large),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
        ) {
            posts.forEach { post ->
                ArticleCarouselCard(
                    post = post,
                    onClick = { onPostClick(post.cid) },
                )
            }
        }
    }
}

@Composable
private fun ArticleCarouselCard(
    post: Post,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .size(
                width = DesignSystem.Component.ArticleCarouselWidth,
                height = DesignSystem.Component.ArticleCarouselHeight,
            )
            .clickable(onClick = onClick),
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 封面图或渐变占位
            if (!post.cover.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.cover)
                        .crossfade(DesignSystem.Animation.CrossfadeDuration)
                        .build(),
                    contentDescription = post.title,
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
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
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
                                Color.Black.copy(alpha = 0.5f),
                            ),
                            startY = 150f,
                        )
                    ),
            )

            // 底部信息
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(DesignSystem.Spacing.Medium),
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatRelativeTime(post.created),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 动态时间线项（时间轴样式）
// ═══════════════════════════════════════════════════════
@Composable
private fun ActivityTimelineItem(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formattedTime = remember(post.created) {
        formatExactTime(post.created)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = DesignSystem.Spacing.Large,
                vertical = DesignSystem.Spacing.Small,
            ),
        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
    ) {
        // 时间
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp),
        )

        // 时间轴竖线
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
        }

        // 动态描述
        Text(
            text = "发布《${post.title}》",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ═══════════════════════════════════════════════════════
// 快速操作行
// ═══════════════════════════════════════════════════════
@Composable
private fun QuickActionsRow(
    onWriteClick: () -> Unit,
    onUploadImageClick: () -> Unit,
    onNewDraftClick: () -> Unit,
    onViewStatsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "快速操作",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = DesignSystem.Spacing.Medium),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
        ) {
            QuickActionButton(
                label = "写文章",
                icon = Icons.Default.Edit,
                onClick = onWriteClick,
                modifier = Modifier.weight(1f),
            )
            QuickActionButton(
                label = "上传图片",
                icon = Icons.Default.Image,
                onClick = onUploadImageClick,
                modifier = Modifier.weight(1f),
            )
            QuickActionButton(
                label = "新建草稿",
                icon = Icons.AutoMirrored.Filled.NoteAdd,
                onClick = onNewDraftClick,
                modifier = Modifier.weight(1f),
            )
            QuickActionButton(
                label = "查看统计",
                icon = Icons.Default.QueryStats,
                onClick = onViewStatsClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .height(DesignSystem.Component.QuickActionHeight)
            .clickable(onClick = onClick),
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.outlineVariant,
                    MaterialTheme.colorScheme.outlineVariant,
                )
            )
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSystem.Spacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp),
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

private fun formatExactTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}
