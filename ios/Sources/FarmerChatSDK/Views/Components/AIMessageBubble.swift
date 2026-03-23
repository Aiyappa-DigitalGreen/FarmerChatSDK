import SwiftUI

// MARK: - AIMessageBubble

/// Left-aligned bubble displaying the AI's markdown-formatted response.
struct AIMessageBubble: View {

    let text: String

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            // Avatar
            Image(systemName: "leaf.circle.fill")
                .resizable()
                .frame(width: 32, height: 32)
                .foregroundColor(SDKColors.primary)
                .padding(.top, 2)

            VStack(alignment: .leading, spacing: 0) {
                MarkdownTextView(text: text)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 10)
                    .background(SDKColors.aiBubble)
                    .cornerRadius(18, corners: [.topLeft, .topRight, .bottomRight])
            }

            Spacer(minLength: 40)
        }
        .padding(.horizontal, 16)
    }
}

private extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCornerShape(radius: radius, corners: corners))
    }
}

private struct RoundedCornerShape: Shape {
    var radius: CGFloat
    var corners: UIRectCorner

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(
            roundedRect: rect,
            byRoundingCorners: corners,
            cornerRadii: CGSize(width: radius, height: radius)
        )
        return Path(path.cgPath)
    }
}
