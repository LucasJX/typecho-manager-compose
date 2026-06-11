package com.flypigs.typechomanager.ui.posts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flypigs.typechomanager.data.model.Post
import com.flypigs.typechomanager.ui.components.v3.ArticleCard
import com.flypigs.typechomanager.ui.components.v3.FilterChipRow
import com.flypigs.typechomanager.ui.components.v3.FilterItem
import com.flypigs.typechomanager.ui.components.v3.StatBar
import com.flypigs.typechomanager.ui.components.v3.StatItem
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsScreen(
    onPostClick: (Int) -> Unit,
    onWriteClick: () -> Unit,
    viewModel: PostsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 搜索状态
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // 多选模式
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedPosts by remember { mutableStateOf(setOf<Int>()) }

    // 列表/网格模式
    var isListView by remember { mutableStateOf(true) }

    // 删除确认
    var showDeleteDialog by remember { mutableStateOf(false) }

    // FAB 可见性
    val fabVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex < 2
        }
    }

    // 统计数据
    val publishedCount = remember(uiState.posts) {
        uiState.posts.count { it.status == "publish" }
    }
    val draftCount = remember(uiState.posts) {
        uiState.posts.count { it.status == "draft" }
    }
    val privateCount = remember(uiState.posts) {
        uiState.posts.count { it.status == "private" }
    }

    // 筛选后的文章
    val filteredPosts = remember(uiState.posts, searchQuery, uiState.selectedStatus) {
        uiState.posts.filter { post ->
            val matchesSearch = searchQuery.isEmpty() ||
                post.title.contains(searchQuery, ignoreCase = true) ||
                post.content.contains(searchQuery, ignoreCase = true)

            val matchesStatus = uiState.selectedStatus == null ||
                post.status == uiState.selectedStatus

            matchesSearch && matchesStatus
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() },
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
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
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                // ═══════════════════════════════════════════
                // 顶部搜索栏
                // ═══════════════════════════════════════════
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { isSearchActive = false },
                    active = isSearchActive,
                    onActiveChange = { isSearchActive = it },
                    placeholder = { Text("搜索文章…") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    },
                    trailingIcon = {
                        // 列表/网格切换
                        IconButton(onClick = { isListView = !isListView }) {
                            Icon(
                                imageVector = if (isListView) Icons.Default.ViewModule else Icons.Default.ViewList,
                                contentDescription = if (isListView) "网格模式" else "列表模式",
                            )
                        }
                    },
                    colors = SearchBarDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignSystem.Spacing.Large)
                        .height(DesignSystem.Component.SearchBarHeight),
                    shape = DesignSystem.Corner.Input,
                ) {
                    // 搜索建议（可选）
                }

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

                // ═══════════════════════════════════════════
                // FilterChip 组
                // ═══════════════════════════════════════════
                FilterChipRow(
                    filters = listOf(
                        FilterItem(id = "all", label = "全部", count = uiState.posts.size),
                        FilterItem(id = "publish", label = "已发布", count = publishedCount),
                        FilterItem(id = "draft", label = "草稿", count = draftCount),
                        FilterItem(id = "private", label = "私密", count = privateCount),
                    ),
                    selectedFilter = uiState.selectedStatus ?: "all",
                    onFilterSelected = { status ->
                        viewModel.filterByStatus(if (status == "all") null else status)
                    },
                )

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

                // ═══════════════════════════════════════════
                // 文章列表
                // ═══════════════════════════════════════════
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        horizontal = DesignSystem.Spacing.Large,
                        vertical = DesignSystem.Spacing.Small,
                    ),
                    verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
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
                                        // 左滑删除
                                        showDeleteDialog = true
                                        false
                                    }
                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        // 右滑编辑
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
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = true,
                            enableDismissFromEndToStart = true,
                        ) {
                            ArticleCard(
                                post = post,
                                onClick = {
                                    if (isMultiSelectMode) {
                                        selectedPosts = if (post.cid in selectedPosts) {
                                            selectedPosts - post.cid
                                        } else {
                                            selectedPosts + post.cid
                                        }
                                    } else {
                                        onPostClick(post.cid)
                                    }
                                },
                            )
                        }
                    }

                    // 底部间距
                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(DesignSystem.Component.FabBottomPadding))
                    }
                }
            }

            // 批量操作栏（多选模式）
            AnimatedVisibility(
                visible = isMultiSelectMode && selectedPosts.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(DesignSystem.Spacing.Medium),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "已选择 ${selectedPosts.size} 篇",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Row {
                            TextButton(
                                onClick = {
                                    // TODO: 批量删除
                                    showDeleteDialog = true
                                },
                            ) {
                                Text("删除", color = MaterialTheme.colorScheme.error)
                            }
                            TextButton(
                                onClick = {
                                    isMultiSelectMode = false
                                    selectedPosts = emptySet()
                                },
                            ) {
                                Text("取消")
                            }
                        }
                    }
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = {
                Text(
                    if (selectedPosts.size > 1) {
                        "确定要删除 ${selectedPosts.size} 篇文章吗？此操作不可撤销。"
                    } else {
                        "确定要删除这篇文章吗？此操作不可撤销。"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedPosts.forEach { cid ->
                            viewModel.deletePost(cid)
                        }
                        scope.launch {
                            snackbarHostState.showSnackbar("已删除 ${selectedPosts.size} 篇文章")
                        }
                        isMultiSelectMode = false
                        selectedPosts = emptySet()
                        showDeleteDialog = false
                    },
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            },
        )
    }
}
