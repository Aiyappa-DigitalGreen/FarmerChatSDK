import SwiftUI

// MARK: - UserMessageBubble

/// Right-aligned bubble showing the user's message, with optional image.
struct UserMessageBubble: View {

    let text: String
    let imageUrl: URL?

    var body: some View {
        VStack(alignment: .trailing, spacing: 6) {
            if let imageUrl {
                AsyncImage(url: imageUrl) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .scaledToFill()
                            .frame(maxWidth: 220, maxHeight: 180)
                            .clipped()
                            .cornerRadius(14)
                    case .failure:
                        Image(systemName: "photo")
                            .font(.largeTitle)
                            .foregroundColor(.white.opacity(0.7))
                            .frame(width: 220, height: 140)
                    case .empty:
                        ProgressView()
                            .frame(width: 220, height: 140)
                    @unknown default:
                        EmptyView()
                    }
                }
            }

            if !text.isEmpty {
                Text(text)
                    .font(.body)
                    .foregroundColor(.white)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 10)
                    .background(SDKColors.userBubble)
                    .cornerRadius(18, corners: [.topLeft, .topRight, .bottomLeft])
                    .textSelection(.enabled)
            }
        }
    }
}

// MARK: - RoundedCorner Helper

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
