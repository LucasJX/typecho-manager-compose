package com.flypigs.typechomanager.ui.postdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.flypigs.typechomanager.ui.designsystem.DesignSystem

/**
 * 文章目录数据
 */
data class TocEntry(
    val level: Int,        // 1-6 for H1-H6
    val title: String,
    val index: Int,        // 在 markdown 中的位置索引（用于滚动定位）
)

/**
 * 从 markdown 中提取标题目录
 */
fun extractTocFromMarkdown(markdown: String): List<TocEntry> {
    val headingRegex = Regex("^(#{1,6})\\s+(.+)$", RegexOption.MULTILINE)
    return headingRegex.findAll(markdown).map { match ->
        val level = match.groupValues[1].length
        val title = match.groupValues[2].trim()
        TocEntry(
            level = level,
            title = title,
            index = match.range.first,
        )
    }.toList()
}

/**
 * 目录底部弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TocBottomSheet(
    tocEntries: List<TocEntry>,
    onTocClick: (TocEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignSystem.Spacing.Large)
                .padding(bottom = DesignSystem.Spacing.Large),
        ) {
            // 标题
            Text(
                text = "目录",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

            // 目录列表
            if (tocEntries.isEmpty()) {
                Text(
                    text = "暂无目录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Small),
                ) {
                    items(tocEntries) { entry ->
                        TocItem(
                            entry = entry,
                            onClick = { onTocClick(entry) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * 单个目录项
 */
@Composable
private fun TocItem(
    entry: TocEntry,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                vertical = DesignSystem.Spacing.Small,
                horizontal = DesignSystem.Spacing.Medium,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 根据标题级别缩进
        val indent = 16.dp * (entry.level - 1)
        Spacer(modifier = Modifier.width(indent))

        // 标题级别指示器
        Icon(
            imageVector = Icons.AutoMirrored.Filled.List,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = when (entry.level) {
                1 -> MaterialTheme.colorScheme.primary
                2 -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )

        Spacer(modifier = Modifier.width(DesignSystem.Spacing.Small))

        // 标题文字
        Text(
            text = entry.title,
            style = when (entry.level) {
                1 -> MaterialTheme.typography.titleMedium
                2 -> MaterialTheme.typography.titleSmall
                else -> MaterialTheme.typography.bodyMedium
            },
            fontWeight = if (entry.level <= 2) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
