package com.flypigs.typechomanager.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flypigs.typechomanager.ui.designsystem.DesignSystem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(DesignSystem.Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Large),
        ) {
            // ═══════════════════════════════════════════
            // Hero 卡片（160dp，圆角 28dp，背景渐变）
            // ═══════════════════════════════════════════
            item(key = "hero") {
                HeroCard(
                    blogName = uiState.blogName,
                    blogUrl = uiState.blogUrl,
                )
            }

            // ═══════════════════════════════════════════
            // 账号组
            // ═══════════════════════════════════════════
            item(key = "account_header") {
                SectionHeader(title = "账号")
            }

            item(key = "blog_url") {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "博客地址",
                    subtitle = uiState.blogUrl,
                    onClick = { /* TODO: 编辑 */ },
                )
            }

            item(key = "username") {
                SettingsItem(
                    icon = Icons.Default.AccountCircle,
                    title = "用户名",
                    subtitle = uiState.username,
                    onClick = { /* TODO: 编辑 */ },
                )
            }

            item(key = "api_url") {
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "API 地址",
                    subtitle = uiState.apiUrl,
                    onClick = { /* TODO: 编辑 */ },
                )
            }

            // ═══════════════════════════════════════════
            // 内容管理组
            // ═══════════════════════════════════════════
            item(key = "content_header") {
                SectionHeader(title = "内容管理")
            }

            item(key = "post_count") {
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "文章数",
                    subtitle = "${uiState.postCount} 篇",
                    onClick = null,
                )
            }

            item(key = "category_count") {
                SettingsItem(
                    icon = Icons.Default.Category,
                    title = "分类数",
                    subtitle = "${uiState.categoryCount} 个",
                    onClick = null,
                )
            }

            item(key = "attachment_count") {
                SettingsItem(
                    icon = Icons.Default.AttachFile,
                    title = "附件数",
                    subtitle = "${uiState.attachmentCount} 个",
                    onClick = null,
                )
            }

            item(key = "cache_size") {
                SettingsItem(
                    icon = Icons.Default.Refresh,
                    title = "缓存大小",
                    subtitle = uiState.cacheSize,
                    onClick = { viewModel.clearCache() },
                )
            }

            // ═══════════════════════════════════════════
            // 写作热力图（新增亮点）
            // ═══════════════════════════════════════════
            item(key = "heatmap") {
                WritingHeatmap(
                    data = uiState.heatmapData,
                )
            }

            // ═══════════════════════════════════════════
            // 应用组
            // ═══════════════════════════════════════════
            item(key = "app_header") {
                SectionHeader(title = "应用")
            }

            item(key = "theme") {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "主题模式",
                    subtitle = uiState.themeMode,
                    onClick = { viewModel.toggleThemeMode() },
                )
            }

            item(key = "pull_refresh") {
                SettingsItem(
                    icon = Icons.Default.Refresh,
                    title = "下拉刷新",
                    subtitle = if (uiState.pullToRefreshEnabled) "已开启" else "已关闭",
                    onClick = { viewModel.togglePullToRefresh() },
                )
            }

            item(key = "image_quality") {
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "图片质量",
                    subtitle = uiState.imageQuality,
                    onClick = { viewModel.cycleImageQuality() },
                )
            }

            // ═══════════════════════════════════════════
            // 关于组
            // ═══════════════════════════════════════════
            item(key = "about_header") {
                SectionHeader(title = "关于")
            }

            item(key = "version") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "版本号",
                    subtitle = uiState.versionName,
                    onClick = null,
                )
            }

            item(key = "changelog") {
                SettingsItem(
                    icon = Icons.Default.Update,
                    title = "更新日志",
                    subtitle = null,
                    onClick = { /* TODO: 查看更新日志 */ },
                )
            }

            item(key = "github") {
                SettingsItem(
                    icon = Icons.Default.Code,
                    title = "GitHub",
                    subtitle = "查看源码",
                    onClick = { /* TODO: 打开 GitHub */ },
                )
            }

            item(key = "license") {
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "许可证",
                    subtitle = "MIT License",
                    onClick = null,
                )
            }

            // 底部间距
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// Hero 卡片
// ═══════════════════════════════════════════════════════
@Composable
private fun HeroCard(
    blogName: String,
    blogUrl: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = DesignSystem.Corner.Hero,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.surface,
                        )
                    )
                ),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(DesignSystem.Spacing.Large),
            ) {
                // 头像
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = blogName.firstOrNull()?.toString() ?: "B",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

                Text(
                    text = blogName.ifEmpty { "Blogga" },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = blogUrl,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// Section Header
// ═══════════════════════════════════════════════════════
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = DesignSystem.Spacing.Small),
    )
}

// ═══════════════════════════════════════════════════════
// Settings Item
// ═══════════════════════════════════════════════════════
@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(vertical = DesignSystem.Spacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.width(DesignSystem.Spacing.Large))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 写作热力图（模仿 GitHub 贡献图）
// ═══════════════════════════════════════════════════════
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WritingHeatmap(
    data: List<HeatmapDay>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DesignSystem.Spacing.Medium),
    ) {
        Text(
            text = "写作热力图",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

        // 热力图网格（简化版：显示过去 12 周）
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignSystem.Component.HeatmapHeight)
                .clip(DesignSystem.Corner.Medium)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(DesignSystem.Spacing.Small),
        ) {
            data.take(84).forEach { day -> // 12 周 * 7 天
                val alpha = (day.count.coerceIn(0, 4) / 4f).coerceIn(0.1f, 1f)
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (day.count > 0) {
                                MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                )
            }
        }

        // 图例
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = DesignSystem.Spacing.Small),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "少",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            repeat(5) { level ->
                val alpha = ((level + 1) / 5f).coerceIn(0.1f, 1f)
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .padding(horizontal = 1.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (level > 0) {
                                MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                )
            }
            Text(
                text = "多",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
