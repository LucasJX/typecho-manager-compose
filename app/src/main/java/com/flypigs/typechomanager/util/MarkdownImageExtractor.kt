package com.flypigs.typechomanager.util

/**
 * 从 Markdown 文本中提取第一张图片 URL
 * 支持格式: ![alt](url), <img src="url">, 纯图片 URL
 */
fun extractFirstImageUrl(markdown: String): String? {
    if (markdown.isBlank()) return null

    // 1. Markdown 图片: ![alt](url)
    val mdImageRegex = Regex("""!\[.*?\]\((\S+?)\)""")
    mdImageRegex.find(markdown)?.let { return it.groupValues[1] }

    // 2. HTML img 标签: <img src="url">
    val htmlImageRegex = Regex("""<img[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    htmlImageRegex.find(markdown)?.let { return it.groupValues[1] }

    // 3. 纯图片 URL (以常见图片扩展名结尾)
    val urlRegex = Regex("""(https?://\S+\.(?:jpg|jpeg|png|gif|webp|bmp|svg))""", RegexOption.IGNORE_CASE)
    urlRegex.find(markdown)?.let { return it.groupValues[1] }

    return null
}
