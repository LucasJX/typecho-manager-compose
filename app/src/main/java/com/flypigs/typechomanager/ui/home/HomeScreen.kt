package com.flypigs.typechomanager.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.flypigs.typechomanager.ui.components.v3.HomeSkeleton
import com.flypigs.typechomanager.ui.components.v3.MorphingFab
import com.flypigs.typechomanager.ui.components.v3.rememberCountUpState
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import com.flypigs.typechomanager.util.extractFirstImageUrl
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ═══════════════════════════════════════════════════════════════
// HomeScreen — Blogga V3 首页
//
// 布局 (LazyColumn):
//   1. 标题区 — 渐变图标徽章 + "首页" + 问候语 + 博客名
//   2. 数据概览（4格紧凑横向行）
//   3. 最新文章 HorizontalPager（5篇轮播 + 圆点指示器）
//   4. 最近动态（渐变徽章标题 + 时间线 ×5）
//   5. 快捷操作（2×2 网格）
// ═══════════════════════════════════════════════════════════════

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

    // FAB 扩展态：滚动到第 2 项以后收缩为小 FAB
    val fabExtended by remember {
        derivedStateOf { listState.firstVisibleItemIndex < 2 }
    }

    // 记灵感 BottomSheet
    var showInspirationSheet by remember { mutableStateOf(false) }
    var inspirationText by remember { mutableStateOf("") }

    // 入场动画状态
    val enterState = remember { MutableTransitionState(false) }

    // 最新文章（取前 5 篇作为 HorizontalPager 页面）
    val recentPosts = remember(uiState.allPosts) {
        uiState.allPosts.sortedByDescending { it.created }.take(5)
    }

    // 最近动态（排除 Hero 已展示的文章，避免图片重复）
    val heroCids = remember(recentPosts) { recentPosts.map { it.cid }.toSet() }
    val recentActivity = remember(uiState.allPosts, heroCids) {
        uiState.allPosts
            .sortedByDescending { it.created }
            .filter { it.cid !in heroCids }
            .take(5)
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

        // 数据加载完成后触发入场动画
        LaunchedEffect(uiState.allPosts) {
            if (uiState.allPosts.isNotEmpty() && !enterState.currentState) {
                enterState.targetState = true
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                MorphingFab(
                    extended = fabExtended,
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
                // ─── 1. 问候语 + 博客名（入场动画）───
                item(key = "greeting") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration)) +
                            slideInVertically(
                                tween(DesignSystem.Entrance.SectionDuration),
                                initialOffsetY = { -DesignSystem.Entrance.SectionSlideOffset },
                            ),
                    ) {
                        GreetingSection(
                            userName = uiState.userName,
                            blogName = uiState.blogName,
                            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                        )
                    }
                }

                // ─── 2. 数据概览（入场动画 + 延迟）───
                item(key = "stats") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay)) +
                            slideInVertically(
                                tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay),
                                initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                            ),
                    ) {
                        StatsRow(
                            publishedCount = uiState.allPosts.count { it.status == "publish" },
                            draftCount = uiState.draftCount,
                            categoryCount = uiState.categories.size,
                            attachmentCount = uiState.attachmentCount,
                            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                        )
                    }
                }

                // ─── 3. 最新文章 HorizontalPager（入场动画 + 延迟）───
                if (recentPosts.isNotEmpty()) {
                    item(key = "hero_pager") {
                        AnimatedVisibility(
                            visibleState = enterState,
                            enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 2)) +
                                slideInVertically(
                                    tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 2),
                                    initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                                ),
                        ) {
                            ArticleHeroPager(
                                posts = recentPosts,
                                onPostClick = { cid -> onPostClick(cid) },
                                modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                            )
                        }
                    }
                }

                // ─── 4. 最近动态（标题 + 时间线，入场动画 + 延迟）───
                if (recentActivity.isNotEmpty()) {
                    item(key = "activity_header") {
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
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // 渐变圆形图标徽章
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    DesignSystem.SemanticColors.Success,
                                                    DesignSystem.SemanticColors.Success.copy(alpha = 0.7f),
                                                )
                                            ),
                                            CircleShape,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Update,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color.White,
                                    )
                                }
                                Spacer(modifier = Modifier.width(DesignSystem.Spacing.Medium))
                                Text(
                                    text = "最近动态",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                    items(
                        items = recentActivity,
                        key = { post -> "activity_${post.cid}" },
                    ) { post ->
                        ActivityTimelineItem(
                            post = post,
                            onClick = { onPostClick(post.cid) },
                            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                        )
                    }
                }

                // ─── 5. 快捷操作（2×2 网格，入场动画 + 延迟）───
                item(key = "quick_actions") {
                    AnimatedVisibility(
                        visibleState = enterState,
                        enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 4)) +
                            slideInVertically(
                                tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay * 4),
                                initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                            ),
                    ) {
                        QuickActionsGrid(
                            onWriteClick = onWriteClick,
                            onNewDraftClick = onNewDraftClick,
                            onUploadImageClick = onUploadImageClick,
                            onViewStatsClick = onViewStatsClick,
                            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                        )
                    }
                }

                // 底部留白
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
                }
            }
        }

        // 记灵感 FAB（右下角）
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

    // ─── 记灵感 BottomSheet ───
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

// ═══════════════════════════════════════════════════════════════
// 1. Greeting — 问候语 + 博客名（紧凑布局：badge+标题行，问候语在下方）
// ═══════════════════════════════════════════════════════════════
@Composable
private fun GreetingSection(
    userName: String,
    blogName: String,
    modifier: Modifier = Modifier,
) {
    val greeting = remember { getGreeting() }

    Column(modifier = modifier) {
        // 主行：badge + 标题 + 副标题 + 右侧按钮（与其他页面一致）
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 渐变圆形图标徽章（与我的/素材库一致）
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
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White,
                )
            }

            Spacer(modifier = Modifier.width(DesignSystem.Spacing.Medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "首页",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "$greeting 👋 $userName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 右上角按钮（与 PostsScreen/AttachmentsScreen 一致）
            IconButton(onClick = { /* TODO: 刷新 */ }) {
                Icon(
                    imageVector = Icons.Default.Update,
                    contentDescription = "刷新",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // 博客名（如有）
        if (blogName.isNotBlank()) {
            Text(
                text = blogName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = DesignSystem.Typography.Body, // 16sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = DesignSystem.Spacing.Small),
            )
        }
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

// ═══════════════════════════════════════════════════════════════
// 2. Stats Row — 4 格紧凑横向，渐变色图标徽章 + CountUp 数字
// ═══════════════════════════════════════════════════════════════
@Composable
private fun StatsRow(
    publishedCount: Int,
    draftCount: Int,
    categoryCount: Int,
    attachmentCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
    ) {
        StatItem(
            label = "文章",
            value = publishedCount,
            icon = Icons.Default.Visibility,
            gradient = Brush.linearGradient(
                colors = listOf(
                    DesignSystem.BrandColors.Primary,
                    DesignSystem.BrandColors.Secondary,
                )
            ),
            modifier = Modifier.weight(1f),
        )
        StatItem(
            label = "草稿",
            value = draftCount,
            icon = Icons.Default.VisibilityOff,
            gradient = Brush.linearGradient(
                colors = listOf(
                    DesignSystem.SemanticColors.Warning,
                    DesignSystem.SemanticColors.Warning.copy(alpha = 0.7f),
                )
            ),
            modifier = Modifier.weight(1f),
        )
        StatItem(
            label = "分类",
            value = categoryCount,
            icon = Icons.Default.QueryStats,
            gradient = Brush.linearGradient(
                colors = listOf(
                    DesignSystem.SemanticColors.Success,
                    DesignSystem.SemanticColors.Success.copy(alpha = 0.7f),
                )
            ),
            modifier = Modifier.weight(1f),
        )
        StatItem(
            label = "附件",
            value = attachmentCount,
            icon = Icons.Default.Image,
            gradient = Brush.linearGradient(
                colors = listOf(
                    DesignSystem.BrandColors.Tertiary,
                    DesignSystem.BrandColors.Primary,
                )
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: Int,
    icon: ImageVector,
    gradient: Brush,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(DesignSystem.Component.StatCardHeight), // 96dp
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
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
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
            // 数字 + 标签
            Column {
                Text(
                    text = rememberCountUpState(value).toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = DesignSystem.Typography.Headline, // 28sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = DesignSystem.Typography.Label, // 12sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 3. Article Hero Pager — 最新文章 HorizontalPager + 页面指示器
// ═══════════════════════════════════════════════════════════════
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ArticleHeroPager(
    posts: List<Post>,
    onPostClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { posts.size })

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = DesignSystem.Spacing.Small,
        ) { page ->
            ArticleHeroCard(
                post = posts[page],
                onClick = { onPostClick(posts[page].cid) },
            )
        }

        // 页面指示器（圆点）
        if (posts.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = DesignSystem.Spacing.Small),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(posts.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = DesignSystem.Spacing.ExtraSmall)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    DesignSystem.BrandColors.Primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                }
                            ),
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 3b. Article Hero Card — 单篇文章大卡片，封面铺满 + 毛玻璃底栏
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ArticleHeroCard(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coverUrl = remember(post) {
        post.cover.takeIf { it.isNotEmpty() } ?: extractFirstImageUrl(post.text)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(DesignSystem.Component.HeroHeight) // 200dp
            .clickable(onClick = onClick),
        shape = DesignSystem.Corner.Hero,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 封面图或渐变占位
            if (coverUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(coverUrl)
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
                                    DesignSystem.BrandColors.Primary.copy(alpha = 0.2f),
                                    DesignSystem.BrandColors.Secondary.copy(alpha = 0.1f),
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
                                Color.Black.copy(alpha = 0.55f),
                            ),
                            startY = 120f,
                        )
                    ),
            )

            // 底部信息栏：标题 + 时间
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(DesignSystem.Spacing.Large),
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = DesignSystem.Typography.Title, // 20sp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatRelativeTime(post.created),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = DesignSystem.Typography.Label, // 12sp
                    ),
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = DesignSystem.Spacing.ExtraSmall),
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 4. Activity Timeline — 最近动态时间线
//    ● 发布文章 2小时前 《标题》
//    ● 草稿更新 5小时前 《标题》
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ActivityTimelineItem(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val relativeTime = remember(post.created) {
        formatRelativeTime(post.created)
    }
    val actionLabel = remember(post.status) {
        when (post.status) {
            "publish" -> "发布文章"
            "draft" -> "更新草稿"
            "private" -> "私密文章"
            else -> "编辑文章"
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = DesignSystem.Spacing.Small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
    ) {
        // 圆点
        Text(
            text = "●",
            fontSize = 8.sp,
            color = DesignSystem.BrandColors.Primary,
        )
        // 操作标签
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = DesignSystem.Typography.Body,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        // 相对时间
        Text(
            text = relativeTime,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = DesignSystem.Typography.Label,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // 文章标题
        Text(
            text = "《${post.title}》",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = DesignSystem.Typography.Body,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// 5. Quick Actions Grid — 2×2 快捷操作网格（与创作页风格一致）
// ═══════════════════════════════════════════════════════════════
@Composable
private fun QuickActionsGrid(
    onWriteClick: () -> Unit,
    onNewDraftClick: () -> Unit,
    onUploadImageClick: () -> Unit,
    onViewStatsClick: () -> Unit,
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
            QuickActionButton(
                label = "写文章",
                icon = Icons.Default.Edit,
                onClick = onWriteClick,
                modifier = Modifier.weight(1f),
            )
            QuickActionButton(
                label = "新建草稿",
                icon = Icons.AutoMirrored.Filled.NoteAdd,
                onClick = onNewDraftClick,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
        ) {
            QuickActionButton(
                label = "上传图片",
                icon = Icons.Default.Image,
                onClick = onUploadImageClick,
                modifier = Modifier.weight(1f),
            )
            QuickActionButton(
                label = "查看统计",
                icon = Icons.Default.BarChart,
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
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(DesignSystem.Component.QuickActionHeight), // 80dp
        shape = DesignSystem.Corner.Button,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = DesignSystem.Typography.Label,
                ),
                modifier = Modifier.padding(top = DesignSystem.Spacing.ExtraSmall),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 工具函数
// ═══════════════════════════════════════════════════════════════
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
