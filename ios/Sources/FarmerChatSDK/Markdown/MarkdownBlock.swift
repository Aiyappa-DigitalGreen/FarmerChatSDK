import Foundation

// MARK: - MarkdownBlock

/// A single structural unit produced by `MarkdownParser`.
/// Mirrors the Android `MarkdownBlock` sealed class exactly.
enum MarkdownBlock: Equatable {
    /// # Heading level 1–3
    case header(level: Int, text: String)

    /// Plain paragraph text (may contain inline bold/italic).
    case paragraph(text: String)

    /// - Bullet item; `nested` = true when preceded by spaces.
    case bulletItem(text: String, nested: Bool)

    /// 1. Numbered list item.
    case numberedItem(number: String, text: String)

    /// Horizontal rule (---).
    case divider
}
