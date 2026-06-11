package com.flypigs.typechomanager.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flypigs.typechomanager.data.model.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onBack: () -> Unit = {},
    postId: String? = null,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSettingsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        if (postId != null) {
            viewModel.loadPost(postId)
        }
    }

    LaunchedEffect(uiState.isPublished) {
        if (uiState.isPublished) {
            snackbarHostState.showSnackbar(
                if (uiState.status == Post.Companion.Status.DRAFT) "草稿已保存" else "发布成功"
            )
            onBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    // 文章设置底部弹窗
    if (showSettingsSheet) {
        PostSettingsSheet(
            uiState = uiState,
            onDismiss = { showSettingsSheet = false },
            onToggleCategory = { viewModel.toggleCategory(it) },
            onTagsChange = { viewModel.updateTags(it) },
            onStatusChange = { viewModel.setStatus(it) },
            onToggleComment = { viewModel.toggleAllowComment() },
            onTogglePing = { viewModel.toggleAllowPing() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (postId != null) "编辑文章" else "写文章",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 预览切换
                    IconButton(onClick = { viewModel.togglePreview() }) {
                        Icon(
                            if (uiState.isPreview) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (uiState.isPreview) "编辑" else "预览",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // 设置
                    TextButton(onClick = { showSettingsSheet = true }) {
                        Text("设置", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // 底部操作栏
            EditorBottomBar(
                isPublishing = uiState.isPublishing,
                status = uiState.status,
                onSaveDraft = { viewModel.saveDraft() },
                onPublish = { viewModel.publish() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 标题输入
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                placeholder = {
                    Text(
                        "输入标题",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                singleLine = true,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                )
            )

            // 分类标签显示
            if (uiState.selectedCategories.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.selectedCategories.forEach { category ->
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.toggleCategory(category) },
                            label = { Text(category, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 内容区域
            if (uiState.isPreview) {
                // Markdown 预览
                MarkdownPreview(
                    markdown = uiState.content,
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                )
            } else {
                // Markdown 工具栏
                MarkdownToolbar(
                    onInsert = { prefix, suffix ->
                        val currentContent = uiState.content
                        viewModel.updateContent(currentContent + prefix + suffix)
                    }
                )

                // 内容输入
                OutlinedTextField(
                    value = uiState.content,
                    onValueChange = { viewModel.updateContent(it) },
                    placeholder = {
                        Text(
                            "开始写作... 支持 Markdown 语法",
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 24.sp
                    ),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    )
                )
            }
        }
    }
}

@Composable
private fun MarkdownToolbar(
    onInsert: (prefix: String, suffix: String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ToolbarButton("B", "粗体") { onInsert("**", "**") }
        ToolbarButton("I", "斜体") { onInsert("*", "*") }
        ToolbarButton("H1", "标题1") { onInsert("# ", "") }
        ToolbarButton("H2", "标题2") { onInsert("## ", "") }
        ToolbarButton("H3", "标题3") { onInsert("### ", "") }
        ToolbarButton("•", "列表") { onInsert("- ", "") }
        ToolbarButton("1.", "有序列表") { onInsert("1. ", "") }
        ToolbarButton(">", "引用") { onInsert("> ", "") }
        ToolbarButton("</>", "代码") { onInsert("```\n", "\n```") }
        ToolbarButton("`", "行内代码") { onInsert("`", "`") }
        ToolbarButton("🔗", "链接") { onInsert("[", "](url)") }
        ToolbarButton("🖼", "图片") { onInsert("![alt](", ")") }
        ToolbarButton("---", "分割线") { onInsert("\n---\n", "") }
    }
}

@Composable
private fun ToolbarButton(
    text: String,
    description: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.height(36.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EditorBottomBar(
    isPublishing: Boolean,
    status: Post.Companion.Status,
    onSaveDraft: () -> Unit,
    onPublish: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 保存草稿
        OutlinedButton(
            onClick = onSaveDraft,
            enabled = !isPublishing,
            modifier = Modifier.weight(1f)
        ) {
            Text("保存草稿")
        }

        // 发布按钮
        Button(
            onClick = onPublish,
            enabled = !isPublishing,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (isPublishing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    when (status) {
                        Post.Companion.Status.PUBLISH -> "发布"
                        Post.Companion.Status.DRAFT -> "发布草稿"
                        Post.Companion.Status.PRIVATE -> "私密发布"
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostSettingsSheet(
    uiState: EditorUiState,
    onDismiss: () -> Unit,
    onToggleCategory: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onStatusChange: (Post.Companion.Status) -> Unit,
    onToggleComment: () -> Unit,
    onTogglePing: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "文章设置",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 分类选择
            Text(
                "分类",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (uiState.isLoadingCategories) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                // 使用简单的 Column 布局替代 FlowRow
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.categories.forEach { category ->
                        val isSelected = uiState.selectedCategories.contains(category.name)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onToggleCategory(category.name) },
                            label = {
                                Text(
                                    "${category.name} (${category.count})",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 标签输入
            Text(
                "标签",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = uiState.tags,
                onValueChange = onTagsChange,
                placeholder = { Text("用逗号分隔，如：技术, AI, 工具") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 发布状态
            Text(
                "状态",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Post.Companion.Status.entries.forEach { status ->
                    FilterChip(
                        selected = uiState.status == status,
                        onClick = { onStatusChange(status) },
                        label = {
                            Text(
                                when (status) {
                                    Post.Companion.Status.PUBLISH -> "公开"
                                    Post.Companion.Status.DRAFT -> "草稿"
                                    Post.Companion.Status.PRIVATE -> "私密"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 权限控制
            Text(
                "权限",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleComment() }
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = uiState.allowComment,
                    onCheckedChange = { onToggleComment() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("允许评论", style = MaterialTheme.typography.bodyLarge)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTogglePing() }
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = uiState.allowPing,
                    onCheckedChange = { onTogglePing() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("允许被引用", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 确认按钮
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("完成")
            }
        }
    }
}
