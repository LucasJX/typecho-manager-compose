package com.flypigs.typechomanager.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.flypigs.typechomanager.data.model.Post

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNewPost: () -> Unit = {},
    onPostClick: (Int) -> Unit = {},
) {
    val viewModel = androidx.hilt.navigation.compose.hiltViewModel<HomeViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("首页") },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewPost,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("写文章") },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Category filter chips
                if (uiState.categories.isNotEmpty()) {
                    CategoryChipRow(
                        categories = uiState.categories.map { it.name },
                        selectedCategory = uiState.selectedCategorySlug,
                        onCategoryClick = { name -> viewModel.filterByCategory(name) },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                when {
                    uiState.isLoading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("加载中…", style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    uiState.posts.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (uiState.selectedCategorySlug != null) "该分类下暂无文章" else "暂无文章",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(items = uiState.posts, key = { it.cid }) { post ->
                                PostCard(
                                    post = post,
                                    onClick = { onPostClick(post.cid) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChipRow(
    categories: List<String>,
    selectedCategory: String?,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { name ->
            FilterChip(
                selected = name.equals(selectedCategory, ignoreCase = true),
                onClick = { onCategoryClick(name) },
                label = { Text(name) }
            )
        }
    }
}

/**
 * Post card with cover image + excerpt + category + date.
 */
@Composable
fun PostCard(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coverUrl = extractFirstImage(post.text)
    val excerpt = extractExcerpt(post.text, maxLength = 120)

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        // Cover image (if exists)
        if (coverUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(coverUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Title
            Text(
                text = post.title.ifBlank { "(无标题)" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Excerpt
            if (excerpt.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = excerpt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category chip + Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (post.categories.isNotEmpty()) {
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = {
                            Text(post.categories.first(),
                                style = MaterialTheme.typography.labelSmall)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formatTimestamp(post.created),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── Helpers ───────────────────────────────────────────────────

/** Extract the first <img src="..."> URL from HTML. */
fun extractFirstImage(html: String): String? {
    val regex = Regex("""<img[^>]+src=["']([^"']+)["']""")
    return regex.find(html)?.groupValues?.get(1)
}

/** Strip HTML tags and return plain text excerpt. */
fun extractExcerpt(html: String, maxLength: Int = 120): String {
    val text = html
        .replace(Regex("<img[^>]*>"), "") // remove images
        .replace(Regex("<[^>]+>"), "")     // remove all tags
        .replace(Regex("&[a-zA-Z]+;"), " ") // remove HTML entities
        .replace(Regex("\\s+"), " ")        // collapse whitespace
        .trim()
    return if (text.length > maxLength) text.take(maxLength) + "…" else text
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(Date(timestamp * 1000))
    } catch (_: Exception) { "" }
}
