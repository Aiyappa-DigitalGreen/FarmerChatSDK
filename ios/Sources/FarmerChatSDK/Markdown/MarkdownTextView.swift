import SwiftUI

// MARK: - MarkdownTextView

/// Renders a markdown string as styled SwiftUI views.
/// Uses `MarkdownParser` to decompose the text into `MarkdownBlock` values,
/// then renders each block with the appropriate typography and spacing.
struct MarkdownTextView: View {

    let text: String
    var color: Color = .primary

    private var blocks: [MarkdownBlock] { MarkdownParser.parse(text) }

    // MARK: Body

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            ForEach(Array(blocks.enumerated()), id: \.offset) { index, block in
                blockView(for: block)
                    .padding(.bottom, bottomPadding(for: block, index: index))
            }
        }
    }

    // MARK: Block Renderer

    @ViewBuilder
    private func blockView(for block: MarkdownBlock) -> some View {
        switch block {

        case .header(let level, let text):
            Text(MarkdownParser.parseBoldAndItalic(text))
                .font(font(forHeaderLevel: level))
                .fontWeight(.bold)
                .foregroundColor(color)
                .fixedSize(horizontal: false, vertical: true)

        case .paragraph(let text):
            Text(MarkdownParser.parseBoldAndItalic(text))
                .font(.body)
                .foregroundColor(color)
                .fixedSize(horizontal: false, vertical: true)
                .textSelection(.enabled)

        case .bulletItem(let text, let nested):
            HStack(alignment: .top, spacing: 8) {
                Text(nested ? "◦" : "•")
                    .font(.body)
                    .foregroundColor(color.opacity(0.7))
                    .padding(.top, 1)
                Text(MarkdownParser.parseBoldAndItalic(text))
                    .font(.body)
                    .foregroundColor(color)
                    .fixedSize(horizontal: false, vertical: true)
                    .textSelection(.enabled)
            }
            .padding(.leading, nested ? 16 : 0)

        case .numberedItem(let number, let text):
            HStack(alignment: .top, spacing: 8) {
                Text("\(number).")
                    .font(.body)
                    .fontWeight(.medium)
                    .foregroundColor(color.opacity(0.7))
                    .padding(.top, 1)
                    .frame(minWidth: 20, alignment: .trailing)
                Text(MarkdownParser.parseBoldAndItalic(text))
                    .font(.body)
                    .foregroundColor(color)
                    .fixedSize(horizontal: false, vertical: true)
                    .textSelection(.enabled)
            }

        case .divider:
            Divider()
                .padding(.vertical, 4)
        }
    }

    // MARK: Helpers

    private func font(forHeaderLevel level: Int) -> Font {
        switch level {
        case 1: return .title2
        case 2: return .title3
        default: return .headline
        }
    }

    /// Returns bottom padding for each block, creating a 24pt/12pt rhythm.
    private func bottomPadding(for block: MarkdownBlock, index: Int) -> CGFloat {
        let isLast = index == blocks.count - 1
        if isLast { return 0 }

        switch block {
        case .header:       return 8
        case .paragraph:    return 12
        case .bulletItem:   return 4
        case .numberedItem: return 4
        case .divider:      return 12
        }
    }
}

#if DEBUG
#Preview {
    ScrollView {
        MarkdownTextView(text: """
        # Crop Disease Guide
        ## Identifying Leaf Blight
        ### Early Symptoms
        Look for **yellowing** and *wilting* leaves in the early morning.

        - Water the plants at the base
        - Avoid overhead irrigation
          - Especially during flowering
        - Monitor weekly

        1. Inspect affected leaves
        2. Sample and test soil
        3. Apply fungicide if confirmed

        ---

        Take action early to prevent spread.
        """)
        .padding()
    }
}
#endif
