package com.flypigs.typechomanager.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.flypigs.typechomanager.ui.designsystem.DesignSystem

/**
 * Shared page header: status bar padding + large title.
 * Consistent across all 4 tabs — no TopAppBar bloat.
 */
@Composable
fun PageHeader(title: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = DesignSystem.Spacing.PageHeaderHorizontal, vertical = DesignSystem.Spacing.PageHeaderVertical)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Page header with a subtitle line (e.g. "共 N 项").
 */
@Composable
fun PageHeaderWithSubtitle(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = DesignSystem.Spacing.PageHeaderHorizontal, vertical = DesignSystem.Spacing.PageHeaderVertical)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(DesignSystem.Spacing.TitleSubtitleGap))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
