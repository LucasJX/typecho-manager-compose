package com.flypigs.typechomanager.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flypigs.typechomanager.BuildConfig
import com.flypigs.typechomanager.ui.designsystem.DesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToSetup: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    // 退出确认对话框
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("退出登录") },
            text = { Text("确定要退出登录吗？本地配置将被清除。") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout { onNavigateToSetup() }
                }) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("取消")
                }
            },
        )
    }

    // 主题选择对话框
    if (showThemeDialog) {
        ThemePickerDialog(
            currentMode = uiState.themeMode,
            onModeSelected = { mode ->
                viewModel.saveThemeMode(mode)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 标题
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignSystem.Spacing.ExtraLarge)
                        .padding(top = DesignSystem.Spacing.Large)
                ) {
                    Text(
                        text = "设置",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Hero 卡片
            item {
                HeroCard(
                    userName = uiState.username,
                    blogUrl = uiState.blogUrl,
                    isLoading = uiState.isLoading
                )
            }

            // 账号信息
            item {
                SectionHeader(title = "账号")
            }
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignSystem.Spacing.ExtraLarge),
                    shape = DesignSystem.Corner.Card,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.Web,
                            title = "博客地址",
                            subtitle = uiState.blogUrl.ifEmpty { "未配置" }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large))
                        SettingsItem(
                            icon = Icons.Default.Person,
                            title = "用户名",
                            subtitle = uiState.username.ifEmpty { "未配置" }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large))
                        SettingsItem(
                            icon = Icons.Default.Info,
                            title = "API 地址",
                            subtitle = uiState.xmlRpcUrl.ifEmpty { "未配置" }
                        )
                    }
                }
            }

            // 内容管理
            item {
                SectionHeader(title = "内容管理")
            }
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignSystem.Spacing.ExtraLarge),
                    shape = DesignSystem.Corner.Card,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.Description,
                            title = "文章数",
                            subtitle = "${uiState.postCount} 篇"
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large))
                        SettingsItem(
                            icon = Icons.Default.Category,
                            title = "分类数",
                            subtitle = "${uiState.categoryCount} 个"
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large))
                        SettingsItem(
                            icon = Icons.Default.AttachMoney,
                            title = "附件数",
                            subtitle = "${uiState.attachmentCount} 个"
                        )
                    }
                }
            }

            // 应用设置
            item {
                SectionHeader(title = "应用")
            }
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignSystem.Spacing.ExtraLarge),
                    shape = DesignSystem.Corner.Card,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.Palette,
                            title = "主题模式",
                            subtitle = uiState.themeMode,
                            onClick = { showThemeDialog = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large))
                        SettingsItem(
                            icon = Icons.Default.Refresh,
                            title = "清除缓存",
                            subtitle = "清除本地缓存数据",
                            onClick = { /* TODO */ }
                        )
                    }
                }
            }

            // 关于
            item {
                SectionHeader(title = "关于")
            }
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignSystem.Spacing.ExtraLarge),
                    shape = DesignSystem.Corner.Card,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.Info,
                            title = "版本",
                            subtitle = "v${BuildConfig.VERSION_NAME}"
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large))
                        SettingsItem(
                            icon = Icons.Default.Update,
                            title = "检查更新",
                            subtitle = "检查新版本",
                            onClick = { /* TODO */ }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large))
                        SettingsItem(
                            icon = Icons.Default.OpenInBrowser,
                            title = "GitHub",
                            subtitle = "查看源代码",
                            onClick = { /* TODO */ }
                        )
                    }
                }
            }

            // 退出按钮
            item {
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.XXLarge))
                TextButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignSystem.Spacing.ExtraLarge)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.Small))
                    Text(
                        text = "退出登录",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.XXXLarge))
            }
        }
    }
}

@Composable
private fun HeroCard(
    userName: String,
    blogUrl: String,
    isLoading: Boolean
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DesignSystem.Spacing.ExtraLarge, vertical = DesignSystem.Spacing.Medium),
        shape = DesignSystem.Corner.ExtraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.XXLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Small))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                )
            } else {
                Text(
                    text = userName.ifEmpty { "管理员" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (blogUrl.isNotEmpty()) {
                    Text(
                        text = blogUrl,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(
            horizontal = DesignSystem.Spacing.ExtraLarge,
            vertical = DesignSystem.Spacing.Medium
        )
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = if (onClick != null) {
            Modifier.clickable(onClick = onClick)
        } else {
            Modifier
        }
    )
}
