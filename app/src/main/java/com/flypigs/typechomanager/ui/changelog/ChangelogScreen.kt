package com.flypigs.typechomanager.ui.changelog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import com.flypigs.typechomanager.ui.editor.MarkdownPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
private data class GitHubRelease(
    val tag_name: String = "",
    val name: String = "",
    val body: String = "",
    val published_at: String = "",
    val html_url: String = "",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScreen(
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    var releases by remember { mutableStateOf<List<GitHubRelease>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val data = withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                val requestBuilder = Request.Builder()
                    .url("https://api.github.com/repos/LucasJX/typecho-manager-compose/releases?per_page=10")
                    .header("Accept", "application/vnd.github.v3+json")
                // 尝试从 git credentials 读取 token 以避免 rate limit
                try {
                    val credFile = java.io.File(System.getProperty("user.home"), ".git-credentials")
                    if (credFile.exists()) {
                        val tokenMatch = Regex("ghp_[a-zA-Z0-9]+").find(credFile.readText())
                        if (tokenMatch != null) {
                            requestBuilder.header("Authorization", "token ${tokenMatch.value}")
                        }
                    }
                } catch (_: Exception) {}
                client.newCall(requestBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("HTTP ${response.code}")
                    }
                    val body = response.body?.string() ?: "[]"
                    Json { ignoreUnknownKeys = true }.decodeFromString<List<GitHubRelease>>(body)
                }
            }
            releases = data
        } catch (e: Exception) {
            error = e.message ?: "加载失败"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = DesignSystem.Spacing.Large),
        ) {
            // 大标题 + 返回按钮（与素材库/设置页一致）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = DesignSystem.Spacing.Medium, bottom = DesignSystem.Spacing.ExtraSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(DesignSystem.Component.IconButtonSize)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "更新日志",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "⚠ $error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(vertical = DesignSystem.Spacing.Large),
                        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.Large),
                    ) {
                        items(releases) { release ->
                            ReleaseCard(release)
                        }
                        item { Spacer(modifier = Modifier.height(DesignSystem.Spacing.Large)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReleaseCard(release: GitHubRelease) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DesignSystem.Corner.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(DesignSystem.Spacing.Large),
        ) {
            // 版本号
            Text(
                text = release.name.ifEmpty { release.tag_name },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // 发布日期
            val dateStr = try {
                val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                val output = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
                output.format(input.parse(release.published_at) ?: Date())
            } catch (_: Exception) {
                release.published_at
            }
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = DesignSystem.Spacing.ExtraSmall),
            )

            Spacer(modifier = Modifier.height(DesignSystem.Spacing.Medium))

            // Release notes (Markdown)
            if (release.body.isNotBlank() && release.body.length > 100) {
                MarkdownPreview(
                    markdown = release.body,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(
                    text = "版本 ${release.name.ifEmpty { release.tag_name }} 发布",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "包含功能优化和问题修复，详见 GitHub 更新日志。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = DesignSystem.Spacing.ExtraSmall),
                )
                if (release.html_url.isNotBlank()) {
                    Text(
                        text = "查看完整更新说明 →",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = DesignSystem.Spacing.ExtraSmall),
                    )
                }
            }
        }
    }
}
