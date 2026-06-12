package com.flypigs.typechomanager.ui.components.v3

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Visibility
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
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.flypigs.typechomanager.data.model.Post
import com.flypigs.typechomanager.util.extractFirstImageUrl
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 文章卡片 v2 — 精简版
 * 布局：
 *   封面 + 标题（2行）
 *   分类 · 发布时间（一行）
 *   👁 23  💬 1（一行）
 *
 * 支持选中态和长按回调
 */
@Composable
fun ArticleCard(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) DesignSystem.Animation.CardPressScale else 1f,
        animationSpec = tween(durationMillis = DesignSystem.Animation.CardPressDuration),
        label = "cardScale"
    )

    // 选中态边框
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .border(borderWidth, borderColor, DesignSystem.Corner.Card)
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
                    onTap = { onClick() },
                    onLongPress = { onLongClick?.invoke() }
                )
            },
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.Large),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Large),
        ) {
            // 左侧缩略图
            ThumbnailImage(
                imageUrl = post.cover,
                title = post.title,
                category = post.categories.firstOrNull() ?: "",
                text = post.text,
                modifier = Modifier.size(DesignSystem.Component.CardThumbnailSize),
            )

            // 右侧信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.ExtraSmall),
            ) {
                // 标题：最多 2 行
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.ExtraSmall))

                // 分类 · 发布时间
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.ExtraSmall),
                ) {
                    val category = post.categories.firstOrNull()
                    if (!category.isNullOrEmpty()) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall,
                            color = getCategoryColor(category),
                        )
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = formatRelativeTime(post.created),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // 👁 23  💬 1
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Large),
                ) {
                    // 阅读数
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.ExtraSmall),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = post.viewsCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // 评论数
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.ExtraSmall),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = post.commentCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
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
    text: String = "",
) {
    val effectiveUrl = imageUrl?.takeIf { it.isNotEmpty() } ?: extractFirstImageUrl(text)
    if (effectiveUrl != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(effectiveUrl)
                .crossfade(DesignSystem.Animation.CrossfadeDuration)
                .build(),
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
