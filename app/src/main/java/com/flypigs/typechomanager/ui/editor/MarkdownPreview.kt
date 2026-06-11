package com.flypigs.typechomanager.ui.editor

import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.syntax.SyntaxHighlightPlugin

/**
 * Markdown 预览组件，使用 Markwon 渲染 Markdown 内容
 */
@Composable
fun MarkdownPreview(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val markwon = remember {
        Markwon.builder(context)
            .usePlugin(HtmlPlugin.create())
            .usePlugin(ImagesPlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(SyntaxHighlightPlugin.create())
            .build()
    }

    AndroidView(
        factory = { ctx ->
            TextView(ctx).apply {
                setPadding(16.dpToPx(ctx), 8.dpToPx(ctx), 16.dpToPx(ctx), 8.dpToPx(ctx))
                textSize = 16f
                lineHeight = (textSize * 1.5f).toInt()
            }
        },
        update = { textView ->
            markwon.setMarkdown(textView, markdown)
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

private fun Int.dpToPx(context: android.content.Context): Int =
    (this * context.resources.displayMetrics.density).toInt()
