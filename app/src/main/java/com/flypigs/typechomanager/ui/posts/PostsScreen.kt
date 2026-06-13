package com.flypigs.typechomanager.ui.posts

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flypigs.typechomanager.data.model.Post
import com.flypigs.typechomanager.ui.components.v3.ArticleCard
import com.flypigs.typechomanager.ui.components.v3.FilterChipRow
import com.flypigs.typechomanager.ui.components.v3.FilterItem
import com.flypigs.typechomanager.ui.components.v3.PostsSkeleton
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsScreen(
    onPostClick: (Int) -> Unit,
    onWriteClick: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: PostsViewModel = hiltViewModel(),
) {
    BackHandler(onBack = onBack)
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 搜索状态
    var searchQuery by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }

    // 多选模式
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedPosts by remember { mutableStateOf(setOf<Int>()) }

    // 列表/网格模式
    var isListView by remember { mutableStateOf(true) }

    // 删除确认（单篇/批量）
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteCid by remember { mutableStateOf<Int?>(null) } // 左滑触发的单篇删除

    // FAB 可见性
    val fabVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex < 2
        }
    }

    // 入场动画状态
    val enterState = remember { MutableTransitionState(false) }
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            enterState.targetState = true
        }
    }

    // 筛选后的文章
    val filteredPosts = remember(uiState.posts, searchQuery, uiState.selectedStatus) {
        uiState.posts.filter { post ->
            val matchesSearch = searchQuery.isEmpty() ||
                post.title.contains(searchQuery, ignoreCase = true) ||
                post.text.contains(searchQuery, ignoreCase = true)

            val matchesStatus = uiState.selectedStatus == null ||
                post.status == uiState.selectedStatus

            matchesSearch && matchesStatus
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                // 多选模式下隐藏 FAB
                if (!isMultiSelectMode) {
                    AnimatedVisibility(
                        visible = fabVisible,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut(),
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = onWriteClick,
                            icon = { Icon(Icons.Default.Add, "写文章") },
                            text = { Text("写文章") },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = DesignSystem.Corner.Fab,
                        )
                    }
                }
            },
        ) { paddingValues ->
            // 骨架屏
            if (uiState.isLoading && uiState.posts.isEmpty()) {
                PostsSkeleton()
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                // ═══════════════════════════════════════════
                // 多选模式顶栏 / 普通模式搜索栏
                // ═══════════════════════════════════════════
                    if (isMultiSelectMode) {
                        // 多选顶栏：已选 N 篇 + 关闭
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = DesignSystem.Spacing.Large, vertical = DesignSystem.Spacing.Medium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "已选 ${selectedPosts.size} 篇",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            IconButton(onClick = {
                                isMultiSelectMode = false
                                selectedPosts = emptySet()
                            }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "退出选择",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    } else {
                        // 大标题（与素材库/我的页一致：渐变图标徽章 + headlineMedium）
                        AnimatedVisibility(
                            visibleState = enterState,
                            enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration)) +
                                slideInVertically(
                                    tween(DesignSystem.Entrance.SectionDuration),
                                    initialOffsetY = { -DesignSystem.Entrance.SectionSlideOffset },
                                ),
                        ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = DesignSystem.Spacing.Large, bottom = DesignSystem.Spacing.ExtraSmall)
                                .padding(horizontal = DesignSystem.Spacing.Large),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // 渐变圆形图标徽章
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
                                    imageVector = Icons.AutoMirrored.Filled.ViewList,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = Color.White,
                                )
                            }
    
                            Spacer(modifier = Modifier.width(DesignSystem.Spacing.Medium))
    
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "文章",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "共 ${filteredPosts.size} 篇",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            // 视图切换按钮
                            IconButton(onClick = { isListView = !isListView }) {
                                Icon(
                                    imageVector = if (isListView) Icons.Default.ViewModule else Icons.AutoMirrored.Filled.ViewList,
                                    contentDescription = if (isListView) "网格模式" else "列表模式",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

                        // DockedSearchBar
                        DockedSearchBar(
                            inputField = {
                                SearchBarDefaults.InputField(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    onSearch = { searchActive = false },
                                    expanded = searchActive,
                                    onExpandedChange = { searchActive = it },
                                    placeholder = { Text("搜索文章…") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = "搜索")
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = "清除")
                                            }
                                        }
                                    },
                                )
                            },
                            expanded = searchActive,
                            onExpandedChange = { searchActive = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignSystem.Spacing.Large),
                            shape = DesignSystem.Corner.Input,
                            colors = SearchBarDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                        ) {
                            // 搜索建议（展开时显示匹配结果）
                            LazyColumn {
                                items(
                                    filteredPosts.take(5),
                                    key = { "search_${it.cid}" },
                                ) { post ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = DesignSystem.Spacing.Medium, vertical = DesignSystem.Spacing.Small),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
                                    ) {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = post.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    } // AnimatedVisibility (header + search)

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))

                AnimatedVisibility(
                    visibleState = enterState,
                    enter = fadeIn(tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay)) + slideInVertically(
                        tween(DesignSystem.Entrance.SectionDuration, delayMillis = DesignSystem.Entrance.SectionDelay),
                        initialOffsetY = { DesignSystem.Entrance.SectionSlideOffset },
                    ),
                ) {
                    // ═══════════════════════════════════════════
                    // FilterChip 组
                    // ═══════════════════════════════════════════
                    FilterChipRow(
                        filters = listOf(
                            FilterItem(id = "all", label = "全部", count = uiState.posts.size),
                            FilterItem(id = "publish", label = "已发布", count = 0),
                            FilterItem(id = "draft", label = "草稿", count = 0),
                            FilterItem(id = "private", label = "私密", count = 0),
                        ),
                        selectedFilter = uiState.selectedStatus ?: "all",
                        onFilterSelected = { status ->
                            viewModel.filterByStatus(if (status == "all") null else status)
                        },
                    )
                }

                AnimatedVisibility(
                    visibleState = enterState,
                    enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(
                        tween(500, delayMillis = 200),
                        initialOffsetY = { it / 4 },
                    ),
                ) {
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))
    
                    // ═══════════════════════════════════════════
                    // 文章列表 / 网格
                    // ═══════════════════════════════════════════
                    if (isListView) {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(
                                horizontal = DesignSystem.Spacing.Large,
                                vertical = DesignSystem.Spacing.Small,
                            ),
                            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
                            modifier = Modifier.weight(1f),
                        ) {
                            items(
                                items = filteredPosts,
                                key = { it.cid },
                            ) { post ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { dismissValue ->
                                        when (dismissValue) {
                                            SwipeToDismissBoxValue.EndToStart -> {
                                                pendingDeleteCid = post.cid
                                                showDeleteDialog = true
                                                false
                                            }
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                onPostClick(post.cid)
                                                false
                                            }
                                            else -> false
                                        }
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = {
                                        val direction = dismissState.dismissDirection
                                        val color = when (direction) {
                                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                                            SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                                            else -> Color.Transparent
                                        }
                                        val icon = when (direction) {
                                            SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                            SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                                            else -> Icons.Default.Edit
                                        }
                                        val alignment = when (direction) {
                                            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                            else -> Alignment.Center
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(DesignSystem.Corner.Card)
                                                .background(color)
                                                .padding(horizontal = DesignSystem.Spacing.Large),
                                            contentAlignment = alignment,
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp),
                                            )
                                        }
                                    },
                                    content = {
                                        ArticleCard(
                                            post = post,
                                            onClick = { onPostClick(post.cid) },
                                            onLongClick = {
                                                if (!isMultiSelectMode) {
                                                    isMultiSelectMode = true
                                                    selectedPosts = setOf(post.cid)
                                                }
                                            },
                                            isSelected = post.cid in selectedPosts,
                                        )
                                    },
                                    enableDismissFromStartToEnd = !isMultiSelectMode,
                                    enableDismissFromEndToStart = !isMultiSelectMode,
                                )
                            }
                            // 底部间距
                            item(key = "bottom_spacer") {
                                val bottomPadding = if (isMultiSelectMode) {
                                    DesignSystem.Component.FabBottomPadding + 48.dp
                                } else {
                                    DesignSystem.Component.FabBottomPadding
                                }
                                Spacer(modifier = Modifier.height(bottomPadding))
                            }
                        }
                    } else {
                        // 网格视图
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(
                                start = DesignSystem.Spacing.Large,
                                end = DesignSystem.Spacing.Large,
                                top = DesignSystem.Spacing.Small,
                                bottom = DesignSystem.Component.FabBottomPadding,
                            ),
                            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                            modifier = Modifier.weight(1f),
                        ) {
                            items(
                                items = filteredPosts,
                                key = { it.cid },
                            ) { post ->
                                PostGridItem(
                                    post = post,
                                    onClick = { onPostClick(post.cid) },
                                    onLongClick = {
                                        if (!isMultiSelectMode) {
                                            isMultiSelectMode = true
                                            selectedPosts = setOf(post.cid)
                                        }
                                    },
                                    isSelected = post.cid in selectedPosts,
                                )
                            }
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════
            // 底部批量操作栏
            // ═══════════════════════════════════════════
            AnimatedVisibility(
                visible = isMultiSelectMode && selectedPosts.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .navigationBarsPadding()
                        .padding(horizontal = DesignSystem.Spacing.Large, vertical = DesignSystem.Spacing.Medium),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 删除
                    BatchActionButton(
                        icon = Icons.Default.Delete,
                        label = "删除",
                        tint = MaterialTheme.colorScheme.error,
                        onClick = { showDeleteDialog = true },
                    )
                    // 移动分类
                    BatchActionButton(
                        icon = Icons.Default.FolderOpen,
                        label = "移动分类",
                        tint = MaterialTheme.colorScheme.primary,
                        onClick = {
                            // TODO: 弹出分类选择对话框
                            scope.launch {
                                snackbarHostState.showSnackbar("移动分类功能开发中")
                            }
                        },
                    )
                    // 设为草稿
                    BatchActionButton(
                        icon = Icons.Default.Edit,
                        label = "设为草稿",
                        tint = MaterialTheme.colorScheme.tertiary,
                        onClick = {
                            selectedPosts.forEach { cid -> viewModel.updatePostStatus(cid, "draft") }
                            scope.launch {
                                snackbarHostState.showSnackbar("已将 ${selectedPosts.size} 篇设为草稿")
                            }
                            isMultiSelectMode = false
                            selectedPosts = emptySet()
                        },
                    )
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        val hasSingle = pendingDeleteCid != null
        val hasBatch = selectedPosts.isNotEmpty()
        val deleteCount = if (hasSingle) 1 else selectedPosts.size

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                pendingDeleteCid = null
            },
            title = { Text("确认删除") },
            text = {
                Text("确定要删除 ${if (deleteCount > 1) "${deleteCount} 篇文章" else "这篇文章"}吗？此操作不可撤销。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (hasSingle) {
                            viewModel.deletePost(pendingDeleteCid!!)
                        }
                        if (hasBatch) {
                            selectedPosts.forEach { cid -> viewModel.deletePost(cid) }
                        }
                        val count = deleteCount
                        scope.launch {
                            snackbarHostState.showSnackbar("已删除 ${count} 篇文章")
                        }
                        isMultiSelectMode = false
                        selectedPosts = emptySet()
                        pendingDeleteCid = null
                        showDeleteDialog = false
                    },
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    pendingDeleteCid = null
                }) {
                    Text("取消")
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PostGridItem(
    post: Post,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .border(borderWidth, borderColor, DesignSystem.Corner.Card)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
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
            // 标题
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            // 底部信息
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // 分类
                if (post.categories.isNotEmpty()) {
                    Text(
                        text = post.categories.first(),
                        style = MaterialTheme.typography.labelSmall,
                        color = DesignSystem.BrandColors.Primary,
                    )
                }

                // 状态 + 日期
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (post.status == "publish") "已发布" else "草稿",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (post.status == "publish") {
                            DesignSystem.SemanticColors.Success
                        } else {
                            DesignSystem.SemanticColors.Warning
                        },
                    )
                    Text(
                        text = formatDate(post.created),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MM/dd", java.util.Locale.CHINA)
    return sdf.format(java.util.Date(timestamp * 1000L))
}

@Composable
private fun BatchActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
) {
    FilledTonalButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = tint,
        )
        Spacer(modifier = Modifier.width(DesignSystem.Spacing.Small))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = tint,
        )
    }
}
