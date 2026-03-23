import SwiftUI

// MARK: - ChatFAB

/// A floating action button that opens the FarmerChat full-screen cover.
///
/// Place this in a `ZStack` at `.bottomTrailing` alignment, typically with
/// `.padding(24)` to keep it off the edges.
///
/// ```swift
/// ZStack(alignment: .bottomTrailing) {
///     MainContentView()
///     ChatFAB()
///         .padding(24)
/// }
/// ```
public struct ChatFAB: View {

    // MARK: State

    @State private var isPresented = false
    @State private var isLoadingTokens = false

    // MARK: Configuration

    private let conversationId: String?

    // MARK: Init

    public init(conversationId: String? = nil) {
        self.conversationId = conversationId
    }

    // MARK: Body

    public var body: some View {
        Button {
            Task { @MainActor in
                isLoadingTokens = true
                await FarmerChatSDK.shared.ensureTokens()
                isLoadingTokens = false
                isPresented = true
            }
        } label: {
            Image(systemName: "message.fill")
                .font(.title2)
                .fontWeight(.semibold)
                .foregroundColor(.white)
                .frame(width: 56, height: 56)
                .background(SDKColors.primary)
                .clipShape(Circle())
                .shadow(color: .black.opacity(0.3), radius: 8, x: 0, y: 4)
        }
        .accessibilityLabel("Open FarmerChat")
        .opacity(isLoadingTokens ? 0.6 : 1.0)
        .sheet(isPresented: $isPresented) {
            ChatContainerView(
                conversationId: conversationId,
                onDismiss: { isPresented = false }
            )
            .presentationDetents([.large])
            .presentationDragIndicator(.visible)
            .presentationCornerRadius(20)
        }
    }
}

// MARK: - SDKColors

/// Centralised brand colour constants.
enum SDKColors {
    /// Primary brand green: #2E7D32
    static let primary = Color(red: 0.18, green: 0.49, blue: 0.20)

    /// Lighter green for backgrounds
    static let primaryContainer = Color(red: 0.88, green: 0.95, blue: 0.88)

    /// User bubble background
    static let userBubble = Color(red: 0.18, green: 0.49, blue: 0.20)

    /// AI bubble background (light neutral)
    static let aiBubble = Color(.systemGray6)

    /// Error red
    static let error = Color(.systemRed)
}

// MARK: - Preview

#if DEBUG
#Preview {
    ZStack(alignment: .bottomTrailing) {
        Color(.systemBackground).ignoresSafeArea()
        ChatFAB()
            .padding(24)
    }
}
#endif
