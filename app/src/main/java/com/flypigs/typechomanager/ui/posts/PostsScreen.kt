package com.flypigs.typechomanager.ui.posts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    // 删除确认（单篇/批量）
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteCid by remember { mutableStateOf<Int?>(null) } // 左滑触发的单篇删除

    // FAB 可见性
    val fabVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex < 2
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
    ) {
        // 骨架屏
        if (uiState.isLoading && uiState.posts.isEmpty()) {
            PostsSkeleton()
            return@PullToRefreshBox
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                    // 普通搜索栏
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
                    ) {}
                }

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

                // ═══════════════════════════════════════════
                // 顶部标题行：文章 / 共 N 篇
                // ═══════════════════════════════════════════
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignSystem.Spacing.Large),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "文章 / 共 ${filteredPosts.size} 篇",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Small))

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
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = !isMultiSelectMode,
                            enableDismissFromEndToStart = !isMultiSelectMode,
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
                                isSelected = isMultiSelectMode && post.cid in selectedPosts,
                                onLongClick = {
                                    if (!isMultiSelectMode) {
                                        isMultiSelectMode = true
                                        selectedPosts = setOf(post.cid)
                                    }
                                },
                            )
                        }
                    }

                    // 底部间距（批量模式时留更多空间给操作栏）
                    item(key = "bottom_spacer") {
                        val bottomPadding = if (isMultiSelectMode) {
                            DesignSystem.Component.FabBottomPadding + 48.dp
                        } else {
                            DesignSystem.Component.FabBottomPadding
                        }
                        Spacer(modifier = Modifier.height(bottomPadding))
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
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = tint,
        )
    }
}
