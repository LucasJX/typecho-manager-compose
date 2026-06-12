package com.flypigs.typechomanager.ui.creator

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flypigs.typechomanager.data.model.Post
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorScreen(
    onWriteArticle: () -> Unit = {},
    onNewDraft: () -> Unit = {},
    onAIClick: () -> Unit = {},
    onDraftClick: (Int) -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: CreatorViewModel = hiltViewModel(),
) {
    BackHandler(onBack = onBack)
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadImage(it) }
    }

    // 显示上传结果
    LaunchedEffect(uiState.uploadResult, uiState.error) {
        val message = uiState.uploadResult ?: uiState.error
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearResult()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = DesignSystem.Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.SectionGap),
        ) {
            // ═══════════════════════════════════════════
            // 1. 标题区 — "创作中心" + "今天写点什么？"
            // ═══════════════════════════════════════════
            item(key = "header") {
                Column(
                    modifier = Modifier.padding(
                        horizontal = DesignSystem.Spacing.Large,
                        top = DesignSystem.Spacing.Large,
                    ),
                ) {
                    Text(
                        text = "创作中心",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = DesignSystem.Typography.Display, // 36sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "今天写点什么？",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = DesignSystem.Typography.Body, // 16sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = DesignSystem.Spacing.Small),
                    )
                }
            }

            // ═══════════════════════════════════════════
            // 2. 大按钮区 — 2×2 网格
            // ═══════════════════════════════════════════
            item(key = "action_grid") {
                Column(
                    modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                    verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                    ) {
                        BigActionButton(
                            label = "写文章",
                            icon = Icons.Default.Create,
                            onClick = onWriteArticle,
                            modifier = Modifier.weight(1f),
                        )
                        BigActionButton(
                            label = "AI辅助",
                            icon = Icons.Default.AutoAwesome,
                            onClick = onAIClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                    ) {
                        BigActionButton(
                            label = "新建草稿",
                            icon = Icons.Default.Description,
                            onClick = onNewDraft,
                            modifier = Modifier.weight(1f),
                        )
                        BigActionButton(
                            label = "上传图片",
                            icon = if (uiState.isUploading) null else Icons.Default.CloudUpload,
                            onClick = { if (!uiState.isUploading) imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            isLoading = uiState.isUploading,
                        )
                    }
                }
            }

            // ═══════════════════════════════════════════
            // 3. 最近草稿
            // ═══════════════════════════════════════════
            if (uiState.recentDrafts.isNotEmpty() || uiState.isLoadingDrafts) {
                item(key = "drafts_header") {
                    Text(
                        text = "最近草稿",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = DesignSystem.Typography.Title, // 20sp
                        ),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                    )
                }

                if (uiState.isLoadingDrafts) {
                    item(key = "drafts_loading") {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignSystem.Spacing.Large),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else {
                    itemsIndexed(
                        items = uiState.recentDrafts,
                        key = { _, draft -> "draft_${draft.cid}" },
                    ) { _, draft ->
                        DraftListItem(
                            draft = draft,
                            onClick = { onDraftClick(draft.cid) },
                            modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 大按钮 — 参考首页 QuickActionButton 风格，但更高更突出
// ═══════════════════════════════════════════════════════
@Composable
private fun BigActionButton(
    label: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(DesignSystem.Component.QuickActionHeight), // 80dp
        shape = DesignSystem.Corner.Button, // 20dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    strokeWidth = 2.dp,
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Text(
                text = if (isLoading) "上传中..." else label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = DesignSystem.Typography.Label,
                ),
                modifier = Modifier.padding(top = DesignSystem.Spacing.ExtraSmall),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 草稿列表项 — 紧凑卡片
// ═══════════════════════════════════════════════════════
@Composable
private fun DraftListItem(
    draft: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = DesignSystem.Corner.Medium, // 16dp
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.Medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = draft.title.ifEmpty { "无标题草稿" },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = DesignSystem.Typography.Body,
                    ),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatDraftTime(draft.modified),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = DesignSystem.Typography.Label,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = DesignSystem.Spacing.ExtraSmall),
                )
            }
        }
    }
}

private fun formatDraftTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "刚刚编辑"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前编辑"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前编辑"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前编辑"
        else -> SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(timestamp))
    }
}
