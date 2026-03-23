import SwiftUI

// MARK: - InlineErrorView

/// A compact error banner with optional retry action, rendered inline in the chat thread.
struct InlineErrorView: View {

    let message: String
    let onRetry: (() -> Void)?

    init(message: String, onRetry: (() -> Void)? = nil) {
        self.message = message
        self.onRetry = onRetry
    }

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: "exclamationmark.triangle.fill")
                .foregroundColor(SDKColors.error)
                .font(.subheadline)
                .padding(.top, 1)

            VStack(alignment: .leading, spacing: 6) {
                Text(message)
                    .font(.subheadline)
                    .foregroundColor(.primary)
                    .multilineTextAlignment(.leading)

                if let onRetry {
                    Button(action: onRetry) {
                        HStack(spacing: 4) {
                            Image(systemName: "arrow.clockwise")
                                .font(.caption)
                            Text("Try again")
                                .font(.caption)
                                .fontWeight(.medium)
                        }
                        .foregroundColor(SDKColors.primary)
                    }
                }
            }

            Spacer()
        }
        .padding(12)
        .background(Color(.systemRed).opacity(0.08))
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color(.systemRed).opacity(0.2), lineWidth: 1)
        )
    }
}

#if DEBUG
#Preview {
    VStack(spacing: 16) {
        InlineErrorView(
            message: "Could not connect to the server. Please check your internet connection.",
            onRetry: {}
        )
        InlineErrorView(message: "Something went wrong.")
    }
    .padding()
}
#endif
