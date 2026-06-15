package com.flypigs.typechomanager.ui.postdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flypigs.typechomanager.ui.designsystem.DesignSystem

/**
 * 阅读进度条 — 显示文章滚动进度
 *
 * 规范：
 * - 高度: 3dp
 * - 颜色: primary
 * - 位置: 顶部应用栏下方
 * - 显示: 滚动时显示，停止滚动后淡出
 */
@Composable
fun ReadingProgressBar(
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val progress by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems <= 1) return@derivedStateOf 0f

            val firstVisibleIndex = listState.firstVisibleItemIndex
            val firstVisibleOffset = listState.firstVisibleItemScrollOffset
            val viewportHeight = listState.layoutInfo.viewportEndOffset.toFloat()

            // 计算已滚动的内容高度
            val scrolledPast = listState.layoutInfo.visibleItemsInfo
                .filter { it.index < firstVisibleIndex }
                .sumOf { it.size }
                .toFloat() + firstVisibleOffset

            // 估算总内容高度
            val averageItemHeight = listState.layoutInfo.visibleItemsInfo
                .map { it.size }
                .average()
                .takeIf { !it.isNaN() } ?: return@derivedStateOf 0f

            val estimatedTotalHeight = averageItemHeight * totalItems
            val totalScrollable = (estimatedTotalHeight - viewportHeight).toFloat()

            if (totalScrollable <= 0f) 1f else (scrolledPast / totalScrollable).coerceIn(0f, 1f)
        }
    }

    val isScrolling by remember {
        derivedStateOf { listState.isScrollInProgress }
    }

    AnimatedVisibility(
        visible = isScrolling,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}
