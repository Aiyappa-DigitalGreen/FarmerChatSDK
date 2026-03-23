import SwiftUI

// MARK: - ChatResponseActions

/// Row of action buttons shown beneath each AI response bubble.
/// Currently contains: Listen (TTS) and Copy.
struct ChatResponseActions: View {

    let onListenTapped: () -> Void
    let isAudioPlaying: Bool
    let isLoadingAudio: Bool

    @State private var isCopied = false

    var body: some View {
        HStack(spacing: 4) {
            // Listen / Stop button
            Button(action: onListenTapped) {
                HStack(spacing: 4) {
                    if isLoadingAudio {
                        ProgressView()
                            .scaleEffect(0.7)
                            .tint(SDKColors.primary)
                    } else {
                        Image(systemName: isAudioPlaying ? "stop.circle" : "speaker.wave.2")
                            .font(.caption)
                    }
                    Text(isAudioPlaying ? "Stop" : "Listen")
                        .font(.caption)
                }
                .foregroundColor(SDKColors.primary)
                .padding(.horizontal, 10)
                .padding(.vertical, 6)
                .background(SDKColors.primaryContainer)
                .cornerRadius(12)
            }
            .disabled(isLoadingAudio)

            Spacer()
        }
    }
}
