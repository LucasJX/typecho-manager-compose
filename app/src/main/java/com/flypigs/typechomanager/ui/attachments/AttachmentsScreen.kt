package com.flypigs.typechomanager.ui.attachments

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.draw.blur
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
import com.flypigs.typechomanager.data.model.Attachment
import com.flypigs.typechomanager.ui.components.v3.AttachmentsSkeleton
import com.flypigs.typechomanager.ui.components.v3.StatBar
import com.flypigs.typechomanager.ui.components.v3.StatItem
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentsScreen(
    onBack: () -> Unit,
    viewModel: AttachmentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 全屏预览
    var showPreview by remember { mutableStateOf(false) }
    var previewIndex by remember { mutableStateOf(0) }

    // 删除确认
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Attachment?>(null) }

    // FAB 可见性
    val fabVisible by remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex < 4
        }
    }

    // 统计数据
    val imageCount = remember(uiState.attachments) {
        uiState.attachments.count { it.mime.startsWith("image/") }
    }
    val totalSize = remember(uiState.attachments) {
        uiState.attachments.sumOf { it.size }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() },
    ) {
        // 骨架屏
        if (uiState.isLoading && uiState.attachments.isEmpty()) {
            AttachmentsSkeleton()
            return@PullToRefreshBox
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(DesignSystem.Component.AttachmentGridColumns),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    start = DesignSystem.Spacing.Large,
                    end = DesignSystem.Spacing.Large,
                    top = DesignSystem.Spacing.Medium,
                    bottom = DesignSystem.Component.FabBottomPadding,
                ),
                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
            ) {
                // ═══════════════════════════════════════════
                // 页面标题（跨列）
                // ═══════════════════════════════════════════
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "附件",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = DesignSystem.Spacing.Small),
                    )
                }

                // ═══════════════════════════════════════════
                // 错误提示（跨列）
                // ═══════════════════════════════════════════
                if (uiState.error != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "⚠ ${uiState.error}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = DesignSystem.Spacing.Small),
                        )
                    }
                }

                // ═══════════════════════════════════════════
                // StatBar（跨列）
                // ═══════════════════════════════════════════
                item(span = { GridItemSpan(maxLineSpan) }) {
                    StatBar(
                        stats = listOf(
                            StatItem(value = uiState.attachments.size, label = "附件"),
                            StatItem(value = imageCount, label = "图片"),
                            StatItem(value = formatFileSize(totalSize).split(" ").first().toIntOrNull() ?: 0, label = formatFileSize(totalSize).split(" ").getOrElse(1) { "KB" }),
                        ),
                    )
                }

                // ═══════════════════════════════════════════
                // 附件网格
                // ═══════════════════════════════════════════
                items(
                    items = uiState.attachments,
                    key = { it.cid },
                ) { attachment ->
                    AttachmentGridItem(
                        attachment = attachment,
                        onClick = {
                            previewIndex = uiState.attachments.indexOf(attachment)
                            showPreview = true
                        },
                        onDelete = {
                            deleteTarget = attachment
                            showDeleteDialog = true
                        },
                    )
                }

                // 底部间距
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    // 全屏预览（支持左右滑动）
    // ═══════════════════════════════════════════════════════
    if (showPreview && uiState.attachments.isNotEmpty()) {
        FullScreenPreview(
            attachments = uiState.attachments,
            initialIndex = previewIndex,
            onDismiss = { showPreview = false },
            onDelete = { attachment ->
                deleteTarget = attachment
                showDeleteDialog = true
            },
        )
    }

    // 删除确认对话框
    if (showDeleteDialog && deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除附件 \"${deleteTarget!!.name}\" 吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAttachment(deleteTarget!!.cid)
                        scope.launch {
                            snackbarHostState.showSnackbar("已删除附件 \"${deleteTarget!!.name}\"")
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
// 附件网格项
// ═══════════════════════════════════════════════════════
@Composable
private fun AttachmentGridItem(
    attachment: Attachment,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(DesignSystem.Corner.Thumbnail)
            .clickable(onClick = onClick),
    ) {
        // 缩略图
        if (attachment.mime.startsWith("image/") && attachment.url.isNotEmpty()) {
            AsyncImage(
                model = attachment.url,
                contentDescription = attachment.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            // 非图片显示图标
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // 底部半透明渐变
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f),
                        )
                    )
                ),
        )

        // 底部信息
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(DesignSystem.Spacing.Small),
        ) {
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
            ) {
                Text(
                    text = formatFileSize(attachment.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Text(
                    text = attachment.type.split("/").last().uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
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
        if (currentAttachment.url != null) {
            AsyncImage(
                model = currentAttachment.url,
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
                    Icons.Default.ArrowBack,
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
                    Icons.Default.ArrowBack,
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
                    Icons.Default.ArrowBack,
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
