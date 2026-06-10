package com.flypigs.typechomanager.ui.postdetail

import android.graphics.Color as AndroidColor
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import coil.ImageLoader
import coil.request.ImageRequest
import com.flypigs.typechomanager.data.model.Post
import com.flypigs.typechomanager.data.repository.PostRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PostDetailScreen(
    cid: Int,
    postRepository: PostRepository,
    onBack: () -> Unit = {}
) {
    var post by remember { mutableStateOf<Post?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(cid) {
        try {
            post = postRepository.getPost(cid)
        } catch (e: Exception) {
            error = e.message ?: "Failed to load post"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(post?.title ?: "文章详情", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            post != null -> {
                val p = post!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Title
                    Text(
                        text = p.title.ifBlank { "(无标题)" },
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date + status
                    Text(
                        text = formatTimestamp(p.created),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Categories + Tags
                    if (p.categories.isNotEmpty() || p.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            p.categories.forEach { cat ->
                                AssistChip(
                                    onClick = {},
                                    label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                            p.tags.forEach { tag ->
                                AssistChip(
                                    onClick = {},
                                    label = { Text("#$tag", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // HTML content rendered via AndroidView
                    HtmlContent(html = p.text)

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun HtmlContent(html: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurface
    val linkColor = MaterialTheme.colorScheme.primary

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            TextView(ctx).apply {
                movementMethod = LinkMovementMethod.getInstance()
                setTextColor(textColor)
                setLinkTextColor(linkColor)
                textSize = 16f
                val dip = (ctx.resources.displayMetrics.density * 4).toInt()
                setPadding(0, dip, 0, dip)
            }
        },
        update = { textView ->
            val imageGetter = HtmlCompat.ImageGetter { source ->
                val drawable = android.graphics.drawable.ColorDrawable(AndroidColor.LTGRAY)
                drawable.setBounds(0, 0, 400, 300)
                // Load image asynchronously via Coil
                val request = ImageRequest.Builder(context)
                    .data(source)
                    .target { result ->
                        val bitmap = (result as? android.graphics.drawable.BitmapDrawable)?.bitmap
                        if (bitmap != null) {
                            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                            val width = 800
                            val height = (width / ratio).toInt()
                            val scaled = android.graphics.drawable.BitmapDrawable(
                                context.resources,
                                android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true)
                            )
                            scaled.setBounds(0, 0, width, height)
                            textView.text = HtmlCompat.fromHtml(
                                html, HtmlCompat.FROM_HTML_MODE_COMPACT, scaled, null
                            )
                        }
                    }
                    .build()
                ImageLoader(context).enqueue(request)
                drawable
            }
            textView.text = HtmlCompat.fromHtml(
                html, HtmlCompat.FROM_HTML_MODE_COMPACT, imageGetter, null
            )
        }
    )
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp * 1000))
    } catch (_: Exception) { "" }
}
