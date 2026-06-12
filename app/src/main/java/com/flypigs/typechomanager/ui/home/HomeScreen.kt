package com.flypigs.typechomanager.ui.home

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.flypigs.typechomanager.ui.components.v3.itemEnterAnimation
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import com.flypigs.typechomanager.util.extractFirstImageUrl
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

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

    // FAB visibility
    val fabVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex < 2
        }
    }

    // Inspiration sheet state
    var showInspirationSheet by remember { mutableStateOf(false) }
    var inspirationText by remember { mutableStateOf("") }

    // Recent activity timeline (latest 5 posts)
    val recentActivity = remember(uiState.allPosts) {
        uiState.allPosts.sortedByDescending { it.created }.take(5)
    }

    // Carousel posts (first 5 posts)
    val carouselPosts = remember(uiState.allPosts) {
        uiState.allPosts.sortedByDescending { it.created }.take(5)
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() },
    ) {
        // Skeleton
        if (uiState.isLoading && uiState.allPosts.isEmpty()) {
            HomeSkeleton()
            return@PullToRefreshBox
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
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
                // 1. Greeting — HeadlineLarge (36sp)
                // ═══════════════════════════════════════════
                item(key = "greeting") {
                    GreetingSection(
                        userName = uiState.userName,
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                    )
                }

                // ═══════════════════════════════════════════
                // 2. 2×2 Stats Grid
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
                // 3. Recent Articles — HorizontalPager with Scale Parallax
                // ═══════════════════════════════════════════
                item(key = "carousel") {
                    RecentArticlesPager(
                        posts = carouselPosts,
                        onPostClick = onPostClick,
                    )
                }

                // ═══════════════════════════════════════════
                // 4. Recent Activity — Timeline (no header)
                // ═══════════════════════════════════════════
                itemsIndexed(
                    items = recentActivity,
                    key = { _, post -> "activity_${post.cid}" },
                ) { index, post ->
                    ActivityTimelineItem(
                        post = post,
                        onClick = { onPostClick(post.cid) },
                        modifier = Modifier
                            .padding(horizontal = DesignSystem.Spacing.Large)
                            .itemEnterAnimation(index),
                    )
                }

                // ═══════════════════════════════════════════
                // 5. Quick Actions (no header)
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

                // Bottom spacer
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
                }
            }
        }

        // Inspiration FAB (bottom-right corner)
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

    // Inspiration bottom sheet
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

// ═══════════════════════════════════════════════════════
// Greeting — "晚上好 👋 flypigs" (HeadlineLarge = 36sp)
// ═══════════════════════════════════════════════════════
@Composable
private fun GreetingSection(
    userName: String,
    modifier: Modifier = Modifier,
) {
    val greeting = remember { getGreeting() }

    Text(
        text = "$greeting 👋 $userName",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = DesignSystem.Typography.Display, // 36sp = HeadlineLarge
        ),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
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
// 2×2 Stats Grid — numbers at 36sp
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
        modifier = modifier.height(DesignSystem.Component.StatCardHeight), // 96dp
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
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = DesignSystem.Typography.Display, // 36sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = DesignSystem.Typography.Label,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = DesignSystem.BrandColors.Primary.copy(alpha = 0.6f),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// Recent Articles — HorizontalPager with Scale Parallax
// ═══════════════════════════════════════════════════════
@Composable
private fun RecentArticlesPager(
    posts: List<Post>,
    onPostClick: (Int) -> Unit,
) {
    if (posts.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { posts.size })

    Column {
        Text(
            text = "最近文章",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = DesignSystem.Typography.Title,
            ),
            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
        )

        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = DesignSystem.Spacing.Large),
            pageSpacing = DesignSystem.Spacing.Medium,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            ).absoluteValue

            // Scale parallax effect: center page = 1.0, side pages scale down
            val scale = 1f - (pageOffset * 0.1f).coerceIn(0f, 0.1f)
            val alpha = 1f - (pageOffset * 0.3f).coerceIn(0f, 0.3f)

            ArticlePagerCard(
                post = posts[page],
                onClick = { onPostClick(posts[page].cid) },
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ArticlePagerCard(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coverUrl = post.cover.takeIf { it.isNotEmpty() } ?: extractFirstImageUrl(post.text)
    Card(
        modifier = modifier
            .height(DesignSystem.Component.ArticleCarouselHeight)
            .clickable(onClick = onClick),
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Cover image or gradient placeholder
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
                                    DesignSystem.BrandColors.Primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                )
                            )
                        ),
                )
            }

            // Bottom gradient overlay
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

            // Bottom info: title + date
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(DesignSystem.Spacing.Medium),
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = DesignSystem.Typography.Title,
                    ),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatRelativeTime(post.created),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = DesignSystem.Typography.Label,
                    ),
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = DesignSystem.Spacing.ExtraSmall),
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// Activity Timeline — "● 发布文章 2小时前 《标题》"
// ═══════════════════════════════════════════════════════
@Composable
private fun ActivityTimelineItem(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val relativeTime = remember(post.created) {
        formatRelativeTime(post.created)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = DesignSystem.Spacing.Small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
    ) {
        // Bullet dot
        Text(
            text = "●",
            fontSize = 8.sp,
            color = DesignSystem.BrandColors.Primary,
        )
        // Action label
        Text(
            text = "发布文章",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = DesignSystem.Typography.Body,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        // Relative time
        Text(
            text = relativeTime,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = DesignSystem.Typography.Label,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // Article title in quotes
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

// ═══════════════════════════════════════════════════════
// Quick Actions Row (no section header)
// ═══════════════════════════════════════════════════════
@Composable
private fun QuickActionsRow(
    onWriteClick: () -> Unit,
    onUploadImageClick: () -> Unit,
    onNewDraftClick: () -> Unit,
    onViewStatsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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

// ═══════════════════════════════════════════════════════
// Utility functions
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
