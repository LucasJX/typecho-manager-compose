package com.flypigs.typechomanager.ui.profile

import androidx.activity.compose.BackHandler
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.flypigs.typechomanager.ui.components.v3.ProfileSkeleton
import com.flypigs.typechomanager.ui.components.v3.rememberCountUpState
import com.flypigs.typechomanager.ui.designsystem.DesignSystem

// ═══════════════════════════════════════════════════════════════
// ProfileScreen — Blogga V3 我的页面
//
// 布局 (LazyColumn):
//   1. 标题区 — 渐变图标徽章 + "我的" + 副标题
//   2. 用户信息区 — 大头像 + 用户名 + 博客名 + 同步状态
//   3. 数据概览（3 格渐变图标徽章统计卡）
//   4. 写作热力图
//   5. 设置卡片分组（账号 / 偏好 / 关于）
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToChangelog: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    BackHandler(onBack = onBack)
    val uiState by viewModel.uiState.collectAsState()

    // 入场动画状态
    val enterState = remember { MutableTransitionState(false) }
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            enterState.targetState = true
        }
    }

    // 骨架屏
    if (uiState.isLoading) {
        ProfileSkeleton()
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = DesignSystem.Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.SectionGap),
        ) {
            // ═══════════════════════════════════════════
            // 1. 标题区：渐变图标徽章 + 标题 + 副标题
            // ═══════════════════════════════════════════
            item(key = "header") {
                AnimatedVisibility(
                    visibleState = enterState,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        tween(500),
                        initialOffsetY = { -it / 2 },
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = DesignSystem.Spacing.Large,
                                top = DesignSystem.Spacing.Large,
                                end = DesignSystem.Spacing.Large,
                            ),
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
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = Color.White,
                            )
                        }

                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.Medium))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "我的",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "个人中心与设置",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════
            // 2. 用户信息区：大头像 + 用户名 + 博客名 + 同步状态
            // ═══════════════════════════════════════════
            item(key = "user_info") {
                AnimatedVisibility(
                    visibleState = enterState,
                    enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(
                        tween(500, delayMillis = 100),
                        initialOffsetY = { it / 4 },
                    ),
                ) {
                    UserInfoCard(
                        username = uiState.username,
                        blogName = uiState.blogName,
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                    )
                }
            }

            // ═══════════════════════════════════════════
            // 3. 数据概览（3 格渐变图标徽章统计卡）
            // ═══════════════════════════════════════════
            item(key = "data_overview") {
                AnimatedVisibility(
                    visibleState = enterState,
                    enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(
                        tween(500, delayMillis = 200),
                        initialOffsetY = { it / 4 },
                    ),
                ) {
                    DataOverviewRow(
                        postCount = uiState.postCount,
                        categoryCount = uiState.categoryCount,
                        attachmentCount = uiState.attachmentCount,
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                    )
                }
            }

            // ═══════════════════════════════════════════
            // 4. 写作热力图
            // ═══════════════════════════════════════════
            item(key = "heatmap") {
                AnimatedVisibility(
                    visibleState = enterState,
                    enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(
                        tween(500, delayMillis = 300),
                        initialOffsetY = { it / 4 },
                    ),
                ) {
                    WritingHeatmap(
                        data = uiState.heatmapData,
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                    )
                }
            }

            // ═══════════════════════════════════════════
            // 5. 设置卡片分组
            // ═══════════════════════════════════════════

            // ── 账号设置 ──
            item(key = "settings_account") {
                AnimatedVisibility(
                    visibleState = enterState,
                    enter = fadeIn(tween(500, delayMillis = 400)) + slideInVertically(
                        tween(500, delayMillis = 400),
                        initialOffsetY = { it / 4 },
                    ),
                ) {
                    SettingsGroupCard(
                        title = "账号",
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                    ) {
                        SettingsItem(
                            icon = Icons.Default.Language,
                            iconGradient = Brush.linearGradient(
                                listOf(DesignSystem.BrandColors.Primary, DesignSystem.BrandColors.Secondary)
                            ),
                            label = "博客地址",
                            value = uiState.blogUrl,
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Medium))
                        SettingsItem(
                            icon = Icons.Default.Security,
                            iconGradient = Brush.linearGradient(
                                listOf(DesignSystem.SemanticColors.Success, DesignSystem.SemanticColors.Success.copy(alpha = 0.7f))
                            ),
                            label = "API 地址",
                            value = uiState.apiUrl,
                        )
                    }
                }
            }

            // ── 偏好设置 ──
            item(key = "settings_preference") {
                AnimatedVisibility(
                    visibleState = enterState,
                    enter = fadeIn(tween(500, delayMillis = 500)) + slideInVertically(
                        tween(500, delayMillis = 500),
                        initialOffsetY = { it / 4 },
                    ),
                ) {
                    SettingsGroupCard(
                        title = "偏好",
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                    ) {
                        SettingsItem(
                            icon = Icons.Default.Palette,
                            iconGradient = Brush.linearGradient(
                                listOf(DesignSystem.BrandColors.Tertiary, DesignSystem.BrandColors.Primary)
                            ),
                            label = "主题模式",
                            value = when (uiState.themeMode) {
                                com.flypigs.typechomanager.data.model.ThemeMode.SYSTEM -> "跟随系统"
                                com.flypigs.typechomanager.data.model.ThemeMode.LIGHT -> "浅色"
                                com.flypigs.typechomanager.data.model.ThemeMode.DARK -> "深色"
                            },
                            onClick = { viewModel.toggleThemeMode() },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Medium))
                        SettingsItem(
                            icon = Icons.Default.Star,
                            iconGradient = Brush.linearGradient(
                                listOf(DesignSystem.SemanticColors.Warning, DesignSystem.SemanticColors.Warning.copy(alpha = 0.7f))
                            ),
                            label = "图片质量",
                            value = uiState.imageQuality,
                            onClick = { viewModel.cycleImageQuality() },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Medium))
                        SettingsItem(
                            icon = Icons.Default.Refresh,
                            iconGradient = Brush.linearGradient(
                                listOf(DesignSystem.CategoryColors.Tech, DesignSystem.CategoryColors.Tech.copy(alpha = 0.7f))
                            ),
                            label = "下拉刷新",
                            value = if (uiState.pullToRefreshEnabled) "已开启" else "已关闭",
                            onClick = { viewModel.togglePullToRefresh() },
                        )
                    }
                }
            }

            // ── 关于 ──
            item(key = "settings_about") {
                AnimatedVisibility(
                    visibleState = enterState,
                    enter = fadeIn(tween(500, delayMillis = 600)) + slideInVertically(
                        tween(500, delayMillis = 600),
                        initialOffsetY = { it / 4 },
                    ),
                ) {
                    SettingsGroupCard(
                        title = "关于",
                        modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Large),
                    ) {
                        SettingsItem(
                            icon = Icons.Default.Info,
                            iconGradient = Brush.linearGradient(
                                listOf(DesignSystem.CategoryColors.AI, DesignSystem.CategoryColors.AI.copy(alpha = 0.7f))
                            ),
                            label = "版本号",
                            value = uiState.versionName,
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Medium))
                        SettingsItem(
                            icon = Icons.Default.Update,
                            iconGradient = Brush.linearGradient(
                                listOf(DesignSystem.CategoryColors.Tools, DesignSystem.CategoryColors.Tools.copy(alpha = 0.7f))
                            ),
                            label = "更新日志",
                            showArrow = true,
                            onClick = onNavigateToChangelog,
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = DesignSystem.Spacing.Medium))
                        val context = LocalContext.current
                        SettingsItem(
                            icon = Icons.Default.Code,
                            iconGradient = Brush.linearGradient(
                                listOf(DesignSystem.CategoryColors.Life, DesignSystem.CategoryColors.Life.copy(alpha = 0.7f))
                            ),
                            label = "GitHub",
                            value = "查看源码",
                            showArrow = true,
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/LucasJX/typecho-manager-compose"))
                                )
                            },
                        )
                    }
                }
            }

            // 底部间距
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.Small))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 用户信息卡片：大头像 + 用户名 + 博客名 + 同步状态
// ═══════════════════════════════════════════════════════
@Composable
private fun UserInfoCard(
    username: String,
    blogName: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignSystem.Elevation.Card),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.Large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Large),
        ) {
            // 大头像（渐变背景 + 首字母）
            val initial = username.firstOrNull()?.uppercase()
                ?: blogName.firstOrNull()?.uppercase()
                ?: "B"
            Box(
                modifier = Modifier
                    .size(72.dp)
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
                Text(
                    text = initial,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
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
                    modifier = Modifier.padding(top = DesignSystem.Spacing.ExtraSmall),
                )
                // 同步状态
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = DesignSystem.Spacing.Small),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(DesignSystem.SemanticColors.Success),
                    )
                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.Small))
                    Text(
                        text = "已同步",
                        style = MaterialTheme.typography.bodySmall,
                        color = DesignSystem.SemanticColors.Success,
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 数据概览（3 格渐变图标徽章统计卡）
// ═══════════════════════════════════════════════════════
@Composable
private fun DataOverviewRow(
    postCount: Int,
    categoryCount: Int,
    attachmentCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
    ) {
        ProfileStatCard(
            label = "文章",
            count = postCount,
            icon = Icons.Default.Visibility,
            accentColor = DesignSystem.BrandColors.Primary,
            modifier = Modifier.weight(1f),
        )
        ProfileStatCard(
            label = "分类",
            count = categoryCount,
            icon = Icons.Default.QueryStats,
            accentColor = DesignSystem.SemanticColors.Success,
            modifier = Modifier.weight(1f),
        )
        ProfileStatCard(
            label = "附件",
            count = attachmentCount,
            icon = Icons.Default.Image,
            accentColor = DesignSystem.BrandColors.Tertiary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ProfileStatCard(
    label: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignSystem.Spacing.Medium, vertical = DesignSystem.Spacing.Medium),
        ) {
            // 顶部彩色条纹
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(3.dp)
                    .background(accentColor, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Small))
            // 数字
            Text(
                text = rememberCountUpState(count).toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = DesignSystem.Typography.Headline,
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // 标签 + 图标
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = accentColor,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = DesignSystem.Typography.Label,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "写作热力图",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = DesignSystem.Typography.Title,
            ),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
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

// ═══════════════════════════════════════════════════════
// 设置分组卡片
// ═══════════════════════════════════════════════════════
@Composable
private fun SettingsGroupCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = DesignSystem.Typography.Title,
            ),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = DesignSystem.Spacing.Small),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = DesignSystem.Corner.Card,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = DesignSystem.Elevation.Card),
        ) {
            Column {
                content()
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 设置项：渐变图标徽章 + 标签 + 值 + 箭头
// ═══════════════════════════════════════════════════════
@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconGradient: Brush,
    label: String,
    value: String? = null,
    showArrow: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        },
        supportingContent = value?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconGradient),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White,
                )
            }
        },
        trailingContent = if (showArrow) {
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else null,
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    )
}
