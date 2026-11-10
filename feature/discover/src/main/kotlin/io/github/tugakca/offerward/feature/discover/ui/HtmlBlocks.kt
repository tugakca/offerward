package io.github.tugakca.offerward.feature.discover.ui

private val blockTag = Regex("<(/?)(p|h[1-6]|ul|ol|blockquote|div|pre|table)\\b[^>]*>", RegexOption.IGNORE_CASE)
private val simpleBlocks = setOf("p", "h1", "h2", "h3", "h4", "h5", "h6")

/**
 * Splits a job description into its top-level blocks (paragraphs, headings, lists, quotes), so
 * each one can be laid out with real space between them. Blocks nested inside lists or quotes
 * stay in their parent. Text outside any block becomes a block of its own.
 */
internal fun splitHtmlBlocks(html: String): List<String> {
    val blocks = mutableListOf<String>()
    var depth = 0
    var topTag = ""
    var start = 0
    for (match in blockTag.findAll(html)) {
        val closing = match.groupValues[1].isNotEmpty()
        val tag = match.groupValues[2].lowercase()
        // An unclosed paragraph or heading ends where the next one starts ("<p>One<p>Two").
        if (!closing && tag in simpleBlocks && depth == 1 && topTag in simpleBlocks) {
            blocks += html.substring(start, match.range.first)
            start = match.range.first
            topTag = tag
            continue
        }
        // A paragraph or heading inside another block is part of it, not a block of its own.
        if (tag in simpleBlocks && (if (closing) tag != topTag || depth != 1 else depth > 0)) continue
        if (closing) {
            depth = (depth - 1).coerceAtLeast(0)
            if (depth == 0) {
                blocks += html.substring(start, match.range.last + 1)
                start = match.range.last + 1
            }
        } else {
            if (depth == 0 && match.range.first > start) {
                blocks += html.substring(start, match.range.first)
                start = match.range.first
            }
            if (depth == 0) topTag = tag
            depth++
        }
    }
    if (start < html.length) blocks += html.substring(start)
    return blocks.filter { it.isNotBlank() }
}

/** Headings get more presence than body text. */
internal fun isHeadingBlock(block: String): Boolean = block.trimStart().startsWith("<h", ignoreCase = true)
