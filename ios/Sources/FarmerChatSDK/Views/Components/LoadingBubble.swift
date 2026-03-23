import SwiftUI

// MARK: - LoadingBubble

/// Animated three-dot typing indicator shown while the AI is responding.
struct LoadingBubble: View {

    @State private var animating = false

    private let dotSize: CGFloat = 8
    private let dotSpacing: CGFloat = 6
    private let animationDelay: Double = 0.18

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: "leaf.circle.fill")
                .resizable()
                .frame(width: 32, height: 32)
                .foregroundColor(SDKColors.primary)
                .padding(.top, 2)

            HStack(spacing: dotSpacing) {
                ForEach(0..<3, id: \.self) { index in
                    Circle()
                        .fill(Color(.systemGray3))
                        .frame(width: dotSize, height: dotSize)
                        .scaleEffect(animating ? 1.3 : 0.7)
                        .opacity(animating ? 1 : 0.4)
                        .animation(
                            .easeInOut(duration: 0.5)
                            .repeatForever(autoreverses: true)
                            .delay(animationDelay * Double(index)),
                            value: animating
                        )
                }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 14)
            .background(SDKColors.aiBubble)
            .cornerRadius(18, corners: [.topLeft, .topRight, .bottomRight])

            Spacer(minLength: 40)
        }
        .onAppear { animating = true }
        .onDisappear { animating = false }
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
