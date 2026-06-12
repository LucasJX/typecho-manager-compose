package com.flypigs.typechomanager.ui.creator

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flypigs.typechomanager.ui.designsystem.DesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorScreen(
    onWriteArticle: () -> Unit = {},
    onNewDraft: () -> Unit = {},
    onAIAssist: () -> Unit = {},
    onMaterialLibrary: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: CreatorViewModel = hiltViewModel(),
) {
    BackHandler(onBack = onBack)
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(DesignSystem.Spacing.Large),
        ) {
            // 页面标题
            Text(
                text = "创作",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = DesignSystem.Spacing.XXLarge),
            )

            // 2×2 功能入口网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
            ) {
                item {
                    CreatorEntryCard(
                        icon = Icons.Default.Create,
                        title = "写文章",
                        subtitle = "创建新文章",
                        onClick = onWriteArticle,
                    )
                }
                item {
                    CreatorEntryCard(
                        icon = Icons.Default.Description,
                        title = "新建草稿",
                        subtitle = "保存为草稿",
                        onClick = onNewDraft,
                    )
                }
                item {
                    CreatorEntryCard(
                        icon = if (uiState.isUploading) null else Icons.Default.CloudUpload,
                        title = if (uiState.isUploading) "上传中..." else "上传图片",
                        subtitle = "上传到素材库",
                        onClick = { if (!uiState.isUploading) imagePickerLauncher.launch("image/*") },
                        isLoading = uiState.isUploading,
                    )
                }
                item {
                    CreatorEntryCard(
                        icon = Icons.Default.AutoAwesome,
                        title = "AI 辅助",
                        subtitle = "智能创作",
                        onClick = {
                            onAIAssist()
                            // AI 功能尚未实现，给用户反馈
                            snackbarHostState.let { state ->
                                scope.launch {
                                    state.showSnackbar("AI 辅助功能即将上线")
                                }
                            }
                        },
                    )
                }
                item {
                    CreatorEntryCard(
                        icon = Icons.Default.PhotoLibrary,
                        title = "素材库",
                        subtitle = "浏览已有素材",
                        onClick = onMaterialLibrary,
                    )
                }
            }
        }
    }
}

@Composable
private fun CreatorEntryCard(
    icon: ImageVector?,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSystem.Spacing.Large),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Small))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
                modifier = Modifier.padding(bottom = DesignSystem.Spacing.XXLarge),
