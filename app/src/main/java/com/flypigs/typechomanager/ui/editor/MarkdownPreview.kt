package com.flypigs.typechomanager.ui.editor

import android.graphics.Paint
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.LineBackgroundSpan
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.flypigs.typechomanager.ui.designsystem.DesignSystem
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.spans.HeadingSpan
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin

/**
 * MarkdownPreview — 文章内容阅读组件 (Reader Mode)
 *
 * 排版规范 (.specs/task1-typography.md)：
 * - 正文 16sp / 行高 1.8x / letter-spacing 0.2
 * - H1 24sp bold, H2 20sp bold, H3 18sp w600
 * - 代码 14sp monospace，深色背景
 * - 引用：左侧竖线 + 浅灰背景
 * - H2 底部分割线
 * - 长段落自动拆分（>150字在标点后拆段）
 * - 行宽控制：左右 padding 24dp
 */
@Composable
fun MarkdownPreview(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val rt = DesignSystem.ReaderTypography

    val textColor = colorScheme.onSurface.hashCode()
    val linkColor = colorScheme.primary.hashCode()
    val codeBgColor = colorScheme.surfaceVariant.hashCode()
    val quoteBorderColor = colorScheme.primary.hashCode()
    val dividerColor = colorScheme.outlineVariant.hashCode()

    val markwon = remember(textColor, linkColor, codeBgColor) {
        val density = context.resources.displayMetrics.density

        // ── MarkwonTheme: 字体 + 代码 + 引用 ──
        val theme = MarkwonTheme.builder()
            .textSize((rt.BodySize.value * density).toInt())
            .headingTextSizeMultipliers(rt.HeadingMultipliers)
            .codeBlockTextSize((rt.CodeSize.value * density).toInt())
            .codeBlockBackgroundColor(codeBgColor)
            .codeBlockMargin((DesignSystem.Spacing.Medium.value * density).toInt())
            .codeBackgroundColor(codeBgColor)
            .codeBlockTypeface(Typeface.MONOSPACE)
            .blockQuoteColor(quoteBorderColor)
            .blockQuoteWidth((4 * density).toInt())
            .blockQuoteIndent((16 * density).toInt())
            .listItemIndent((24 * density).toInt())
            .linkColor(linkColor)
            .build()

        val builder = Markwon.builder(context)
        builder.theme(theme)
        builder
            .usePlugin(CorePlugin.create())
            .usePlugin(HtmlPlugin.create())
            .usePlugin(ImagesPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .build()
    }

    // 预处理：长段落自动拆分（在 setMarkdown 之前，避免破坏 span 偏移）
    val processedMarkdown = remember(markdown) { reflowLongParagraphs(markdown) }

    AndroidView(
        factory = { ctx ->
            TextView(ctx).apply {
                val d = ctx.resources.displayMetrics.density
                // 行宽控制：左右 padding 24dp（spec: 20-32dp）
                val hPad = (24 * d).toInt()
                val vPad = (DesignSystem.Spacing.Small.value * d).toInt()
                setPadding(hPad, vPad, hPad, vPad)

                // 基础排版（spec: 16sp / 行高1.8x / letter-spacing 0.2）
                textSize = rt.BodySize.value
                setLineSpacing(rt.BodyLineHeight.value - rt.BodySize.value, 1f)
                letterSpacing = rt.BodyLetterSpacing
                setTextColor(textColor)
                setLinkTextColor(linkColor)
            }
        },
        update = { textView ->
            textView.setTextColor(textColor)
            textView.setLinkTextColor(linkColor)
            markwon.setMarkdown(textView, processedMarkdown)
            // 后处理：H2 分割线（添加 span，不修改文本，安全）
            addH2Dividers(textView, dividerColor)
        },
        modifier = modifier.fillMaxWidth()
    )
}

// ═══════════════════════════════════════════════════════════════
// H2 底部分割线（后处理，添加 LineBackgroundSpan）
// ═══════════════════════════════════════════════════════════════

private fun addH2Dividers(textView: TextView, dividerColor: Int) {
    val spannable = textView.text as? SpannableStringBuilder ?: return
    val density = textView.resources.displayMetrics.density
    val headings = spannable.getSpans(0, spannable.length, HeadingSpan::class.java)
    for (heading in headings) {
        if (heading.level == 2) {
            val end = spannable.getSpanEnd(heading)
            if (end > 0 && end <= spannable.length) {
                spannable.setSpan(
                    H2DividerLineSpan(density, dividerColor),
                    (end - 1).coerceAtLeast(0), end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        }
    }
}

/**
 * 在 H2 最后一行底部绘制 1px 分割线（Material theme outlineVariant 色）
 */
private class H2DividerLineSpan(
    private val density: Float,
    dividerColorArgb: Int,
) : LineBackgroundSpan {
    private val paint = Paint().apply {
        color = dividerColorArgb
        strokeWidth = 1 * density
    }

    override fun drawBackground(
        canvas: android.graphics.Canvas, p: Paint,
        left: Int, right: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int, lineNumber: Int,
    ) {
        // 只在最后一行绘制
        if (text is Spanned) {
            val spanEnd = (text as Spanned).getSpanEnd(this)
            if (end < spanEnd) return
        }
        val y = bottom + 2 * density
        canvas.drawLine(left.toFloat(), y, right.toFloat(), y, paint)
    }
}

// ═══════════════════════════════════════════════════════════════
// 长段落自动拆分（纯字符串操作，setMarkdown 之前调用）
// ═══════════════════════════════════════════════════════════════

private const val REFLOW_THRESHOLD = 150
private const val TARGET_PARAGRAPH_LEN = 100

/**
 * 超过 150 字的段落在标点后自动拆分为多段。
 * 目标：每段不超过 ~100 字。
 */
private fun reflowLongParagraphs(markdown: String): String {
    val paragraphs = markdown.split("\n\n")
    val result = StringBuilder()
    for ((i, para) in paragraphs.withIndex()) {
        if (i > 0) result.append("\n\n")
        val trimmed = para.trim()
        if (trimmed.length > REFLOW_THRESHOLD && !trimmed.startsWith("```") && !trimmed.startsWith("#")) {
            result.append(splitLongParagraph(trimmed))
        } else {
            result.append(para)
        }
    }
    return result.toString()
}

private fun splitLongParagraph(text: String): String {
    val punctuation = Regex("[。！？；…]+")
    val result = StringBuilder()
    var lastSplit = 0
    for (match in punctuation.findAll(text)) {
        if (match.range.first - lastSplit >= TARGET_PARAGRAPH_LEN) {
            result.append(text.substring(lastSplit, match.range.last + 1))
            result.append("\n\n")
            lastSplit = match.range.last + 1
        }
    }
    if (lastSplit < text.length) {
        result.append(text.substring(lastSplit))
    }
    return result.toString()
}
