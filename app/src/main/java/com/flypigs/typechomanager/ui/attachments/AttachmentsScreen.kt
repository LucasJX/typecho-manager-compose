package com.flypigs.typechomanager.ui.attachments

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.flypigs.typechomanager.data.model.Attachment
import com.flypigs.typechomanager.ui.components.v3.AttachmentsSkeleton
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.shape.RoundedCornerShape

/** 素材筛选类型 */
enum class FilterType(val label: String) {
    ALL("全部"), IMAGE("图片"), VIDEO("视频"), DOCUMENT("文档")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentsScreen(
    onBack: () -> Unit,
    viewModel: AttachmentsViewModel = hiltViewModel(),
) {
    BackHandler(onBack = onBack)
    val uiState by viewModel.uiState.collectAsState()
    val filteredAttachments by viewModel.filteredAttachments.collectAsState()
    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 全屏预览
    var showPreview by remember { mutableStateOf(false) }
    var previewIndex by remember { mutableStateOf(0) }

    // 删除确认
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Attachment?>(null) }

    // 长按菜单（底部弹出）
    var showContextMenu by remember { mutableStateOf(false) }
    var contextMenuTarget by remember { mutableStateOf<Attachment?>(null) }

    // 筛选类型
    var filterType by remember { mutableStateOf(FilterType.ALL) }

    // 入场动画状态
    val enterState = remember { MutableTransitionState(false) }
    LaunchedEffect(uiState.attachments) {
        if (uiState.attachments.isNotEmpty() && !enterState.currentState) {
            enterState.targetState = true
        }
    }

    // FAB 可见性
    val fabVisible by remember {
        derivedStateOf {
            if (uiState.viewMode == ViewMode.GRID) {
                gridState.firstVisibleItemIndex < 4
            } else {
                listState.firstVisibleItemIndex < 4
            }
        }
    }

    // 最近上传时间
    val recentUploadText = remember(uiState.attachments) {
        if (uiState.attachments.isNotEmpty()) {
            val mostRecent = uiState.attachments.maxOfOrNull { it.created } ?: 0L
            if (mostRecent > 0) formatRelativeTime(mostRecent) else ""
        } else ""
    }

    // 筛选后的数据
    val displayAttachments = remember(filteredAttachments, filterType) {
        when (filterType) {
            FilterType.ALL -> filteredAttachments
            FilterType.IMAGE -> filteredAttachments.filter { it.mime.startsWith("image/") }
            FilterType.VIDEO -> filteredAttachments.filter { it.mime.startsWith("video/") }
            FilterType.DOCUMENT -> filteredAttachments.filter {
                !it.mime.startsWith("image/") && !it.mime.startsWith("video/")
            }
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
                AnimatedVisibility(
                    visible = fabVisible,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { /* TODO: 上传附件 */ },
                        icon = { Icon(Icons.Default.Add, "上传附件") },
                        text = { Text("上传附件") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = DesignSystem.Corner.Fab,
                    )
                }
            },
        ) { paddingValues ->
            // 骨架屏
            if (uiState.isLoading && uiState.attachments.isEmpty()) {
                AttachmentsSkeleton()
                return@Scaffold
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = DesignSystem.Spacing.Large),
            ) {
                // ═══════════════════════════════════════════
                // 标题区：渐变图标徽章 + 标题 + 副标题
                // ═══════════════════════════════════════════
                AnimatedVisibility(
                    visibleState = enterState,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        tween(500),
                        initialOffsetY = { -it / 2 }
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = DesignSystem.Spacing.Large, bottom = DesignSystem.Spacing.Small),
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
                                imageVector = Icons.Default.Archive,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = Color.White,
                            )
                        }

                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.Medium))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "素材库",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "管理你的媒体资源",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // 视图切换按钮
                        IconButton(onClick = { viewModel.toggleViewMode() }) {
                            Icon(
                                imageVector = if (uiState.viewMode == ViewMode.GRID)
                                    Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                                contentDescription = if (uiState.viewMode == ViewMode.GRID) "列表视图" else "网格视图",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // ═══════════════════════════════════════════
                // 统计卡片：渐变图标徽章 + 大号数字 + 标签
                // ═══════════════════════════════════════════
                AnimatedVisibility(
                    visibleState = enterState,
                    enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(
                        tween(500, delayMillis = 100),
                        initialOffsetY = { it / 4 }
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
                    ) {
                        StatCard(
                            value = uiState.attachments.size.toString(),
                            label = "文件",
                            icon = Icons.Default.Archive,
                            accentColor = DesignSystem.BrandColors.Primary,
                        )
                        StatCard(
                            value = formatFileSize(uiState.totalSize),
                            label = "总大小",
                            icon = Icons.Default.InsertDriveFile,
                            accentColor = DesignSystem.BrandColors.Secondary,
                        )
                        if (recentUploadText.isNotEmpty()) {
                            StatCard(
                                value = recentUploadText,
                                label = "最近",
                                icon = Icons.Default.Image,
                                accentColor = DesignSystem.BrandColors.Tertiary,
                            )
                        }
                    }
                }

                // ═══════════════════════════════════════════
                // 筛选 Chips
                // ═══════════════════════════════════════════
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = DesignSystem.Spacing.Small),
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
                ) {
                    FilterType.entries.forEach { type ->
                        FilterChip(
                            selected = filterType == type,
                            onClick = { filterType = type },
                            label = { Text(type.label, style = MaterialTheme.typography.labelMedium) },
                            shape = DesignSystem.Corner.Chip,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                    }
                }

                // ═══════════════════════════════════════════
                // 错误提示
                // ═══════════════════════════════════════════
                if (uiState.error != null) {
                    Text(
                        text = "⚠ ${uiState.error}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = DesignSystem.Spacing.Small),
                    )
                }

                // ═══════════════════════════════════════════
                // 内容区 — 根据视图模式切换
                // ═══════════════════════════════════════════
                if (displayAttachments.isEmpty() && filterType != FilterType.ALL) {
                    // 筛选无结果
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            )
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))
                            Text(
                                text = "没有${filterType.label}类型的素材",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else if (uiState.viewMode == ViewMode.GRID) {
                    // ── 网格视图 ──
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(DesignSystem.Component.AttachmentGridColumns),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = DesignSystem.Spacing.Small,
                            bottom = DesignSystem.Component.FabBottomPadding,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                    ) {
                        items(
                            items = displayAttachments,
                            key = { it.cid },
                        ) { attachment ->
                            AttachmentGridItem(
                                attachment = attachment,
                                onClick = {
                                    previewIndex = displayAttachments.indexOf(attachment)
                                    showPreview = true
                                },
                                onLongClick = {
                                    contextMenuTarget = attachment
                                    showContextMenu = true
                                },
                            )
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
                        }
                    }
                } else {
                    // ── 列表视图 ──
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = DesignSystem.Spacing.Small,
                            bottom = DesignSystem.Component.FabBottomPadding,
                        ),
                        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
                    ) {
                        items(
                            items = displayAttachments,
                            key = { it.cid },
                        ) { attachment ->
                            AttachmentListItem(
                                attachment = attachment,
                                onClick = {
                                    previewIndex = displayAttachments.indexOf(attachment)
                                    showPreview = true
                                },
                                onLongClick = {
                                    contextMenuTarget = attachment
                                    showContextMenu = true
                                },
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
                        }
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // 全屏预览（支持左右滑动）
    // ═══════════════════════════════════════════════════════
    if (showPreview && displayAttachments.isNotEmpty()) {
        FullScreenPreview(
            attachments = displayAttachments,
            initialIndex = previewIndex,
            onDismiss = { showPreview = false },
            onDelete = { attachment ->
                deleteTarget = attachment
                showDeleteDialog = true
            },
        )
    }

    // ═══════════════════════════════════════════════════════
    // 长按上下文菜单（底部弹出）
    // ═══════════════════════════════════════════════════════
    if (showContextMenu && contextMenuTarget != null) {
        val sheetState = rememberModalBottomSheetState()
        val target = contextMenuTarget!!
        ModalBottomSheet(
            onDismissRequest = { showContextMenu = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = DesignSystem.Corner.Card,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignSystem.Spacing.Large)
                    .padding(bottom = DesignSystem.Spacing.ExtraLarge),
            ) {
                // 文件信息头
                Text(
                    text = target.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.ExtraSmall),
                )
                Text(
                    text = formatFileSize(target.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.Large),
                )

                // 复制链接
                DropdownMenuItem(
                    text = { Text("复制链接", style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        showContextMenu = false
                        viewModel.copyLink(target)
                        scope.launch {
                            snackbarHostState.showSnackbar("已复制链接")
                        }
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )

                // 下载
                DropdownMenuItem(
                    text = { Text("下载", style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        showContextMenu = false
                        viewModel.downloadFile(target)
                        scope.launch {
                            snackbarHostState.showSnackbar("正在下载 ${target.name}")
                        }
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )

                // 删除
                DropdownMenuItem(
                    text = {
                        Text(
                            "删除",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    onClick = {
                        showContextMenu = false
                        deleteTarget = target
                        showDeleteDialog = true
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                )
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog && deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除素材 \"${deleteTarget!!.name}\" 吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAttachment(deleteTarget!!.cid)
                        scope.launch {
                            snackbarHostState.showSnackbar("已删除素材 \"${deleteTarget!!.name}\"")
                        }
                        showDeleteDialog = false
                        deleteTarget = null
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

// ═══════════════════════════════════════════════════════
// 统计卡片 — 渐变图标徽章 + 大号数字
// ═══════════════════════════════════════════════════════
@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = DesignSystem.Corner.Medium,
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.06f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignSystem.Spacing.Medium, vertical = DesignSystem.Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 左侧彩色竖条
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(28.dp)
                    .background(accentColor, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.Small))
            // 图标
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = accentColor,
            )
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.Small))
            // 标签
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
            // 数字
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 获取文件类型图标
// ═══════════════════════════════════════════════════════
private fun getFileTypeIcon(mime: String): ImageVector {
    return when {
        mime.startsWith("video/") -> Icons.Default.VideoFile
        mime.startsWith("image/") -> Icons.Default.Image
        else -> Icons.Default.InsertDriveFile
    }
}

// ═══════════════════════════════════════════════════════
// 网格卡片 — 毛玻璃悬浮信息层
// ═══════════════════════════════════════════════════════
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AttachmentGridItem(
    attachment: Attachment,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(DesignSystem.Corner.Thumbnail)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        // 缩略图
        if (attachment.mime.startsWith("image/") && attachment.url.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(attachment.url)
                    .crossfade(DesignSystem.Animation.CrossfadeDuration)
                    .build(),
                contentDescription = attachment.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            // 非图片显示类型图标
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = getFileTypeIcon(attachment.mime),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Small))
                    Text(
                        text = attachment.type.split("/").last().uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // 毛玻璃悬浮信息层 — 底部渐变 + 文件名 + 大小
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.7f),
                        )
                    )
                )
                .padding(horizontal = DesignSystem.Spacing.Small, vertical = DesignSystem.Spacing.ExtraSmall),
        ) {
            // 毛玻璃文字信息
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
            ) {
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = formatFileSize(attachment.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
        }

        // 文件类型角标
        if (!attachment.mime.startsWith("image/")) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(DesignSystem.Spacing.ExtraSmall)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        DesignSystem.Corner.Chip,
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = attachment.type.split("/").last().uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 列表项
// ═══════════════════════════════════════════════════════
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AttachmentListItem(
    attachment: Attachment,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DesignSystem.Corner.Medium)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(DesignSystem.Spacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 缩略图
        if (attachment.mime.startsWith("image/") && attachment.url.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(attachment.url)
                    .crossfade(DesignSystem.Animation.CrossfadeDuration)
                    .build(),
                contentDescription = attachment.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(DesignSystem.Corner.Thumbnail),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(DesignSystem.Corner.Thumbnail)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = getFileTypeIcon(attachment.mime),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.width(DesignSystem.Spacing.Medium))

        // 文件信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${formatFileSize(attachment.size)} · ${attachment.type.split("/").last().uppercase()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 全屏预览
// ═══════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullScreenPreview(
    attachments: List<Attachment>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    onDelete: (Attachment) -> Unit,
) {
    var currentIndex by remember { mutableStateOf(initialIndex) }
    val currentAttachment = attachments[currentIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // 图片
        if (currentAttachment.url.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(currentAttachment.url)
                    .crossfade(DesignSystem.Animation.CrossfadeDuration)
                    .build(),
                contentDescription = currentAttachment.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }

        // 顶部工具栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(DesignSystem.Spacing.Medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White,
                )
            }
            Text(
                text = "${currentIndex + 1} / ${attachments.size}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
            IconButton(onClick = { onDelete(currentAttachment) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Color.White,
                )
            }
        }

        // 底部信息
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(DesignSystem.Spacing.Large),
        ) {
            Text(
                text = currentAttachment.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
            Text(
                text = "${formatFileSize(currentAttachment.size)} • ${formatTimestamp(currentAttachment.created)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
        }

        // 左右滑动按钮
        if (currentIndex > 0) {
            IconButton(
                onClick = { currentIndex-- },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(DesignSystem.Spacing.Medium),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "上一张",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp),
                )
            }
        }
        if (currentIndex < attachments.size - 1) {
            IconButton(
                onClick = { currentIndex++ },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(DesignSystem.Spacing.Medium),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "下一张",
                    tint = Color.White,
                    modifier = Modifier
                        .size(36.dp)
                        .graphicsLayer { scaleX = -1f },
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 工具函数
// ═══════════════════════════════════════════════════════
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "刚刚"
        diff < 3_600_000 -> "${diff / 60_000} 分钟前"
        diff < 86_400_000 -> "${diff / 3_600_000} 小时前"
        diff < 604_800_000 -> "${diff / 86_400_000} 天前"
        else -> {
            val sdf = SimpleDateFormat("MM-dd", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
