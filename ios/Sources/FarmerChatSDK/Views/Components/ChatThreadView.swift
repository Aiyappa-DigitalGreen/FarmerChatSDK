import SwiftUI

// MARK: - ChatThreadView

/// Scrollable list of all chat messages. Handles auto-scroll, load-more,
/// loading placeholder, and inline errors.
struct ChatThreadView: View {

    let messages: [ChatMessage]
    let isLoading: Bool
    let errorMessage: String?
    let onRetry: () -> Void
    let onFollowUpTapped: (String, String?) -> Void
    let onListenTapped: () -> Void
    let audioPlaybackUrl: URL?
    let isAudioPlaying: Bool
    let onAudioPlayingChanged: (Bool) -> Void
    let onLoadMore: () -> Void

    @State private var scrollProxy: ScrollViewProxy?

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 0) {
                    // Load-more trigger at the top
                    Color.clear
                        .frame(height: 1)
                        .onAppear { onLoadMore() }

                    ForEach(messages) { message in
                        messageView(for: message)
                            .id(message.id)
                    }

                    // Inline error at the bottom
                    if let error = errorMessage {
                        InlineErrorView(message: error, onRetry: onRetry)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .id("error")
                    }
                }
                .padding(.vertical, 12)
            }
            .onAppear { scrollProxy = proxy }
            .onChange(of: messages.count) { _ in
                scrollToBottom(proxy: proxy)
            }
            .onChange(of: isLoading) { _ in
                scrollToBottom(proxy: proxy)
            }
        }
    }

    // MARK: – Message Factory

    @ViewBuilder
    private func messageView(for message: ChatMessage) -> some View {
        switch message {
        case .userMessage(_, let text, let imageUrl, _):
            HStack {
                Spacer(minLength: 60)
                UserMessageBubble(text: text, imageUrl: imageUrl)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 4)

        case .aiResponse(_, let text, let followUps, _, let messageId):
            VStack(alignment: .leading, spacing: 0) {
                AIMessageBubble(text: text)

                ChatResponseActions(
                    onListenTapped: onListenTapped,
                    isAudioPlaying: isAudioPlaying,
                    isLoadingAudio: false
                )
                .padding(.horizontal, 16)
                .padding(.bottom, 4)
            }
            .padding(.vertical, 4)

        case .loadingPlaceholder:
            HStack {
                LoadingBubble()
                    .padding(.horizontal, 16)
                Spacer()
            }
            .padding(.vertical, 4)
        }
    }

    // MARK: – Scroll Helper

    private func scrollToBottom(proxy: ScrollViewProxy) {
        guard let last = messages.last else { return }
        withAnimation(.easeOut(duration: 0.25)) {
            proxy.scrollTo(last.id, anchor: .bottom)
        }
    }
}
