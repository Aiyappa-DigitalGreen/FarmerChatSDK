package com.farmerchat.sdk.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ──────────────────────────────────────────────────────────────────────────
// Markdown block model
// ──────────────────────────────────────────────────────────────────────────

private sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class BulletItem(val text: String, val nested: Boolean = false) : MarkdownBlock()
    data class NumberedItem(val index: Int, val text: String) : MarkdownBlock()
    object Divider : MarkdownBlock()
    object Spacer : MarkdownBlock()
}

// ──────────────────────────────────────────────────────────────────────────
// Parser
// ──────────────────────────────────────────────────────────────────────────

private fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = markdown.lines()
    var numberedIndex = 1

    for (line in lines) {
        val trimmed = line.trimEnd()

        when {
            // Horizontal rule
            trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
                blocks.add(MarkdownBlock.Divider)
                numberedIndex = 1
            }

            // Headers
            trimmed.startsWith("### ") -> {
                blocks.add(MarkdownBlock.Header(3, trimmed.removePrefix("### ").trim()))
                numberedIndex = 1
            }
            trimmed.startsWith("## ") -> {
                blocks.add(MarkdownBlock.Header(2, trimmed.removePrefix("## ").trim()))
                numberedIndex = 1
            }
            trimmed.startsWith("# ") -> {
                blocks.add(MarkdownBlock.Header(1, trimmed.removePrefix("# ").trim()))
                numberedIndex = 1
            }

            // Nested bullet (starts with 2+ spaces or tab then -)
            trimmed.matches(Regex("\\s{2,}[-*]\\s.*")) -> {
                val text = trimmed.trimStart().removePrefix("-").removePrefix("*").trim()
                blocks.add(MarkdownBlock.BulletItem(text, nested = true))
                numberedIndex = 1
            }

            // Bullet
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                val text = trimmed.drop(2)
                blocks.add(MarkdownBlock.BulletItem(text, nested = false))
                numberedIndex = 1
            }

            // Numbered list
            trimmed.matches(Regex("\\d+\\.\\s.*")) -> {
                val text = trimmed.substringAfter(". ")
                blocks.add(MarkdownBlock.NumberedItem(numberedIndex++, text))
            }

            // Blank line
            trimmed.isBlank() -> {
                blocks.add(MarkdownBlock.Spacer)
                numberedIndex = 1
            }

            // Paragraph
            else -> {
                blocks.add(MarkdownBlock.Paragraph(trimmed))
                numberedIndex = 1
            }
        }
    }
    return blocks
}

// ──────────────────────────────────────────────────────────────────────────
// Bold/Italic inline parser
// ──────────────────────────────────────────────────────────────────────────

private fun parseBoldAndItalic(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        when {
            // Bold+Italic ***text***
            text.startsWith("***", i) -> {
                val end = text.indexOf("***", i + 3)
                if (end != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic))
                    append(text.substring(i + 3, end))
                    pop()
                    i = end + 3
                } else {
                    append(text[i])
                    i++
                }
            }
            // Bold **text**
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(text.substring(i + 2, end))
                    pop()
                    i = end + 2
                } else {
                    append(text[i])
                    i++
                }
            }
            // Italic *text*
            text.startsWith("*", i) && !text.startsWith("**", i) -> {
                val end = text.indexOf("*", i + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(text.substring(i + 1, end))
                    pop()
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            // Inline code `text`
            text.startsWith("`", i) -> {
                val end = text.indexOf("`", i + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        background = Color(0xFFEEEEEE)
                    ))
                    append(text.substring(i + 1, end))
                    pop()
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            // Strikethrough ~~text~~
            text.startsWith("~~", i) -> {
                val end = text.indexOf("~~", i + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                    append(text.substring(i + 2, end))
                    pop()
                    i = end + 2
                } else {
                    append(text[i])
                    i++
                }
            }
            else -> {
                append(text[i])
                i++
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// Composable
// ──────────────────────────────────────────────────────────────────────────

@Composable
internal fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: Float = 14f
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }
    val bodyFontSize = fontSize.sp

    Column(modifier = modifier.fillMaxWidth()) {
        for (block in blocks) {
            when (block) {
                is MarkdownBlock.Header -> {
                    val style = when (block.level) {
                        1 -> MaterialTheme.typography.headlineMedium
                        2 -> MaterialTheme.typography.headlineSmall
                        else -> MaterialTheme.typography.titleLarge
                    }
                    Text(
                        text = parseBoldAndItalic(block.text),
                        style = style,
                        color = color,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = parseBoldAndItalic(block.text),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = bodyFontSize,
                        color = color
                    )
                }

                is MarkdownBlock.BulletItem -> {
                    val indent = if (block.nested) 24.dp else 8.dp
                    Text(
                        text = buildAnnotatedString {
                            append(if (block.nested) "  • " else "• ")
                            append(parseBoldAndItalic(block.text))
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = bodyFontSize,
                        color = color,
                        modifier = Modifier.padding(start = indent)
                    )
                }

                is MarkdownBlock.NumberedItem -> {
                    Text(
                        text = buildAnnotatedString {
                            pushStyle(SpanStyle(fontWeight = FontWeight.Medium))
                            append("${block.index}. ")
                            pop()
                            append(parseBoldAndItalic(block.text))
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = bodyFontSize,
                        color = color,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                is MarkdownBlock.Divider -> {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        color = color.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                is MarkdownBlock.Spacer -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}
