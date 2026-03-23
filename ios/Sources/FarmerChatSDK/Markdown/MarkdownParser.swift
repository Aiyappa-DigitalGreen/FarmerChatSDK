import Foundation

// MARK: - MarkdownParser

/// Converts a raw markdown string into an ordered array of `MarkdownBlock` values.
/// Mirrors the Android `parseMarkdownBlocks()` implementation.
struct MarkdownParser {

    // MARK: Block Parser

    /// Parse `text` into a sequence of `MarkdownBlock` values.
    static func parse(_ text: String) -> [MarkdownBlock] {
        let lines = text.components(separatedBy: "\n")
        var blocks: [MarkdownBlock] = []

        for line in lines {
            let trimmed = line.trimmingCharacters(in: .whitespaces)

            // Horizontal rule
            if trimmed == "---" || trimmed == "***" || trimmed == "___" {
                blocks.append(.divider)
                continue
            }

            // Headers: ### ## #
            if trimmed.hasPrefix("###") {
                let content = trimmed.dropFirst(3).trimmingCharacters(in: .whitespaces)
                blocks.append(.header(level: 3, text: content))
                continue
            }
            if trimmed.hasPrefix("##") {
                let content = trimmed.dropFirst(2).trimmingCharacters(in: .whitespaces)
                blocks.append(.header(level: 2, text: content))
                continue
            }
            if trimmed.hasPrefix("#") {
                let content = trimmed.dropFirst(1).trimmingCharacters(in: .whitespaces)
                blocks.append(.header(level: 1, text: content))
                continue
            }

            // Nested bullet: line starts with spaces/tabs then -
            if (line.hasPrefix("  ") || line.hasPrefix("\t")) && trimmed.hasPrefix("- ") {
                let content = String(trimmed.dropFirst(2))
                blocks.append(.bulletItem(text: content, nested: true))
                continue
            }

            // Bullet item: - or * at line start
            if trimmed.hasPrefix("- ") || trimmed.hasPrefix("* ") {
                let content = String(trimmed.dropFirst(2))
                blocks.append(.bulletItem(text: content, nested: false))
                continue
            }

            // Numbered item: "1. " pattern
            if let numberedMatch = parseNumberedItem(trimmed) {
                blocks.append(.numberedItem(number: numberedMatch.number, text: numberedMatch.text))
                continue
            }

            // Skip blank lines
            if trimmed.isEmpty {
                continue
            }

            // Default: paragraph
            blocks.append(.paragraph(text: trimmed))
        }

        return blocks
    }

    // MARK: Inline Formatting

    /// Parse inline bold (`**text**`) and italic (`*text*`) markers into an `AttributedString`.
    static func parseBoldAndItalic(_ text: String) -> AttributedString {
        var result = AttributedString()
        var remaining = text[text.startIndex...]

        // Pattern: **bold**, *italic*, in order of appearance
        let pattern = #"\*\*(.+?)\*\*|\*(.+?)\*"#
        guard let regex = try? NSRegularExpression(pattern: pattern, options: []) else {
            return AttributedString(text)
        }

        let nsString = text as NSString
        let matches = regex.matches(
            in: text,
            options: [],
            range: NSRange(location: 0, length: nsString.length)
        )

        var lastEnd = text.startIndex

        for match in matches {
            // Append plain text before this match
            let matchRange = Range(match.range, in: text)!
            if lastEnd < matchRange.lowerBound {
                result.append(AttributedString(String(text[lastEnd..<matchRange.lowerBound])))
            }

            // Bold group (group 1)
            if match.range(at: 1).location != NSNotFound,
               let boldRange = Range(match.range(at: 1), in: text) {
                var boldAttr = AttributedString(String(text[boldRange]))
                boldAttr.font = .body.bold()
                result.append(boldAttr)
            }
            // Italic group (group 2)
            else if match.range(at: 2).location != NSNotFound,
                    let italicRange = Range(match.range(at: 2), in: text) {
                var italicAttr = AttributedString(String(text[italicRange]))
                italicAttr.font = .body.italic()
                result.append(italicAttr)
            }

            lastEnd = matchRange.upperBound
        }

        // Append any remaining plain text
        if lastEnd < text.endIndex {
            result.append(AttributedString(String(text[lastEnd...])))
        }

        return result.characters.isEmpty ? AttributedString(text) : result
    }

    // MARK: Private Helpers

    private struct NumberedItem {
        let number: String
        let text: String
    }

    private static func parseNumberedItem(_ line: String) -> NumberedItem? {
        // Match "1." or "12." etc.
        guard let dotRange = line.range(of: ".") else { return nil }
        let potential = String(line[line.startIndex..<dotRange.lowerBound])
        guard potential.allSatisfy({ $0.isNumber }), !potential.isEmpty else { return nil }

        let afterDot = String(line[dotRange.upperBound...]).trimmingCharacters(in: .whitespaces)
        guard !afterDot.isEmpty else { return nil }

        return NumberedItem(number: potential, text: afterDot)
    }
}
