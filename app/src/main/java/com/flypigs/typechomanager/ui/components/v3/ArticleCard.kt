package com.flypigs.typechomanager.ui.components.v3

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.flypigs.typechomanager.data.model.Post
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 文章卡片 - 管理型 + 可读型融合
 * 左侧缩略图 80x80，右侧标题+元数据+底部统计
 * 点击缩放到 0.97
 */
@Composable
fun ArticleCard(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) DesignSystem.Animation.CardPressScale else 1f,
        animationSpec = tween(durationMillis = DesignSystem.Animation.CardPressDuration),
        label = "cardScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = { onClick() }
                )
            },
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.outlineVariant,
                    MaterialTheme.colorScheme.outlineVariant,
                )
            )
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.Large),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Large),
        ) {
            // 左侧缩略图 80x80
            ThumbnailImage(
                imageUrl = post.cover,
                title = post.title,
                category = post.categories.firstOrNull() ?: "",
                modifier = Modifier.size(DesignSystem.Component.CardThumbnailSize),
            )

            // 右侧信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
            ) {
                // 标题：最多 2 行
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // 元数据：分类标签 + 状态
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 分类标签
                    if (post.categories.isNotEmpty()) {
                        CategoryChip(category = post.categories.first())
                    }

                    // 状态圆点 + 文字
                    StatusIndicator(status = post.status)
                }

                // 底部：阅读数 + 编辑时间
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Large),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 阅读数
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = post.viewsCount.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // 编辑时间
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = formatRelativeTime(post.created),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThumbnailImage(
    imageUrl: String?,
    title: String,
    category: String,
    modifier: Modifier = Modifier,
) {
    if (!imageUrl.isNullOrEmpty()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            modifier = modifier.clip(DesignSystem.Corner.Thumbnail),
            contentScale = ContentScale.Crop,
        )
    } else {
        // 无图时显示首字色块（使用分类色）
        Box(
            modifier = modifier
                .clip(DesignSystem.Corner.Thumbnail)
                .background(getCategoryColor(category)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title.firstOrNull()?.toString() ?: "",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun CategoryChip(category: String) {
    Text(
        text = category,
        style = MaterialTheme.typography.labelSmall,
        color = getCategoryColor(category),
        modifier = Modifier
            .clip(DesignSystem.Corner.Chip)
            .background(getCategoryColor(category).copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
private fun StatusIndicator(status: String) {
    val (color, text) = when (status) {
        "publish" -> MaterialTheme.colorScheme.primary to "已发布"
        "draft" -> MaterialTheme.colorScheme.error to "草稿"
        "private" -> MaterialTheme.colorScheme.tertiary to "私密"
        else -> MaterialTheme.colorScheme.onSurfaceVariant to status
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, shape = MaterialTheme.shapes.small)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "生活", "生活杂谈" -> DesignSystem.CategoryColors.Life
        "技术" -> DesignSystem.CategoryColors.Tech
        "ai" -> DesignSystem.CategoryColors.AI
        "工具" -> DesignSystem.CategoryColors.Tools
        "旅行" -> DesignSystem.CategoryColors.Travel
        else -> DesignSystem.BrandColors.Primary
    }
}

private fun formatRelativeTime(date: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - date

    return when {
        diff < 60 * 1000 -> "刚刚"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前"
        else -> SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(date))
    }
}
