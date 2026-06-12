package com.flypigs.typechomanager.ui.components.v3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flypigs.typechomanager.ui.designsystem.DesignSystem

/**
 * FilterChip 组 - 水平排列，高度 36dp，圆角 20dp
 * 筛选标签不显示统计数字，仅显示中文标签
 */
@Composable
fun FilterChipRow(
    filters: List<FilterItem>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Component.ChipGap),
        contentPadding = PaddingValues(horizontal = DesignSystem.Spacing.Large),
    ) {
        items(filters) { filter ->
            val selected = filter.id == selectedFilter
            FilterChip(
                selected = selected,
                onClick = { onFilterSelected(filter.id) },
                label = {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                modifier = Modifier.height(DesignSystem.Component.ChipHeight),
                shape = DesignSystem.Corner.Chip,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

/**
 * 筛选项数据
 */
data class FilterItem(
    val id: String,
    val label: String,
    val count: Int,
)
