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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * 文章卡片 v3 — 内容管理中心风格
 * 固定高度 100dp，布局：
 *   封面 | 标题(2行) + 分类 + 浏览/评论
 *   右上角状态标签：已发布(绿) / 草稿(黄) / 私密(灰)
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
            .height(100.dp)
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
                .height(100.dp)
                .padding(DesignSystem.Spacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
        ) {
            // 左侧封面图（正方形，高度撑满）
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(DesignSystem.Corner.Thumbnail),
            ) {
                ThumbnailImage(
                    imageUrl = post.cover,
                    title = post.title,
                    category = post.categories.firstOrNull() ?: "",
                    text = post.text,
                    modifier = Modifier.size(68.dp),
                )
                // 右上角状态标签
                StatusBadge(
                    status = post.status,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                )
            }

            // 右侧信息
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // 标题：最多 2 行
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // 分类
                val category = post.categories.firstOrNull()
                if (!category.isNullOrEmpty()) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        color = getCategoryColor(category),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // 👁 23  💬 1
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Medium),
                ) {
                    // 阅读数
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = formatCount(post.viewsCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // 评论数
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = formatCount(post.commentCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 状态标签 — 右上角小圆点+文字
 * 已发布 = 绿色，草稿 = 黄色，私密 = 灰色
 */
@Composable
private fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
) {
    val (label, color) = when (status) {
        Post.Companion.Status.PUBLISH.value -> "已发布" to DesignSystem.SemanticColors.Success
        Post.Companion.Status.DRAFT.value -> "草稿" to DesignSystem.SemanticColors.Warning
        Post.Companion.Status.PRIVATE.value -> "私密" to MaterialTheme.colorScheme.onSurfaceVariant
        else -> "草稿" to DesignSystem.SemanticColors.Warning
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 5.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = Color.White,
            maxLines = 1,
        )
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

/** 格式化数字：1234 → 1.2k */
private fun formatCount(count: Int): String {
    return when {
        count >= 10_000 -> String.format("%.1fw", count / 10_000.0)
        count >= 1_000 -> String.format("%.1fk", count / 1_000.0)
        else -> count.toString()
    }
}
