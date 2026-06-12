package com.flypigs.typechomanager.ui.profile

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flypigs.typechomanager.ui.components.v3.rememberCountUpState
import com.flypigs.typechomanager.ui.designsystem.DesignSystem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToChangelog: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    BackHandler(onBack = onBack)
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(DesignSystem.Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Large),
        ) {
            // ═══════════════════════════════════════════
            // 页面标题
            // ═══════════════════════════════════════════
            item(key = "page_title") {
                Text(
                    text = "我的",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = DesignSystem.Spacing.Small),
                )
            }

            // ═══════════════════════════════════════════
            // 用户信息区：头像 + Flypigs + Blogga
            // ═══════════════════════════════════════════
            item(key = "user_info") {
                UserInfoSection(
                    username = uiState.username,
                    blogName = uiState.blogName,
                )
            }

            // ═══════════════════════════════════════════
            // 数据概览：文章 / 分类 / 附件（一行三列）
            // ═══════════════════════════════════════════
            item(key = "data_overview") {
                DataOverviewRow(
                    postCount = uiState.postCount,
                    categoryCount = uiState.categoryCount,
                    attachmentCount = uiState.attachmentCount,
                )
            }

            // ═══════════════════════════════════════════
            // 写作热力图（GitHub 风格）
            // ═══════════════════════════════════════════
            item(key = "heatmap") {
                WritingHeatmap(
                    data = uiState.heatmapData,
                )
            }

            // ═══════════════════════════════════════════
            // 我的博客
            // ═══════════════════════════════════════════
            item(key = "my_blog_header") {
                SectionHeader(title = "我的博客")
            }

            item(key = "blog_url") {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "博客地址",
                    subtitle = uiState.blogUrl,
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
            // 应用设置
            // ═══════════════════════════════════════════
            item(key = "app_header") {
                SectionHeader(title = "应用设置")
            }

            item(key = "theme") {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "主题模式",
                    subtitle = when (uiState.themeMode) {
                        com.flypigs.typechomanager.data.model.ThemeMode.SYSTEM -> "跟随系统"
                        com.flypigs.typechomanager.data.model.ThemeMode.LIGHT -> "浅色"
                        com.flypigs.typechomanager.data.model.ThemeMode.DARK -> "深色"
                    },
                    onClick = { viewModel.toggleThemeMode() },
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

            item(key = "pull_refresh") {
                SettingsItem(
                    icon = Icons.Default.Refresh,
                    title = "下拉刷新",
                    subtitle = if (uiState.pullToRefreshEnabled) "已开启" else "已关闭",
                    onClick = { viewModel.togglePullToRefresh() },
                )
            }

            // ═══════════════════════════════════════════
            // 关于
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
                    onClick = onNavigateToChangelog,
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

            // 底部间距
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 用户信息区
// ═══════════════════════════════════════════════════════
@Composable
private fun UserInfoSection(
    username: String,
    blogName: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DesignSystem.Spacing.Large),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Large),
    ) {
        // 头像（首字母）
        val initial = username.firstOrNull()?.uppercase()
            ?: blogName.firstOrNull()?.uppercase()
            ?: "B"
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }

        Column {
            Text(
                text = username.ifEmpty { "Flypigs" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = blogName.ifEmpty { "Blogga" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 数据概览（一行三列卡片）
// ═══════════════════════════════════════════════════════
@Composable
private fun DataOverviewRow(
    postCount: Int,
    categoryCount: Int,
    attachmentCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
    ) {
        DataCard(
            label = "文章",
            count = postCount,
            modifier = Modifier.weight(1f),
        )
        DataCard(
            label = "分类",
            count = categoryCount,
            modifier = Modifier.weight(1f),
        )
        DataCard(
            label = "附件",
            count = attachmentCount,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DataCard(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(88.dp),
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignSystem.Spacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "${rememberCountUpState(count)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.ExtraSmall))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
// 写作热力图（GitHub 贡献图风格）
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

        // 热力图网格（过去 12 周）
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
            data.take(84).forEach { day ->
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
