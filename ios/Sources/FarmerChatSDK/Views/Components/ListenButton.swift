import SwiftUI

// MARK: - ListenButton

/// A circular microphone button that animates between idle and recording states.
struct ListenButton: View {

    let isListening: Bool
    let action: () -> Void

    @State private var pulse = false

    var body: some View {
        Button(action: action) {
            ZStack {
                if isListening {
                    Circle()
                        .fill(SDKColors.primary.opacity(0.25))
                        .frame(width: 70, height: 70)
                        .scaleEffect(pulse ? 1.4 : 1.0)
                        .opacity(pulse ? 0 : 0.6)
                        .animation(
                            .easeOut(duration: 0.8).repeatForever(autoreverses: false),
                            value: pulse
                        )
                }

                Circle()
                    .fill(isListening ? SDKColors.error : SDKColors.primary)
                    .frame(width: 56, height: 56)

                Image(systemName: isListening ? "stop.fill" : "mic.fill")
                    .font(.title3)
                    .foregroundColor(.white)
            }
        }
        .accessibilityLabel(isListening ? "Stop recording" : "Start voice input")
        .onAppear {
            if isListening { pulse = true }
        }
        .onChange(of: isListening) { listening in
            pulse = listening
        }
    }
}

#if DEBUG
#Preview {
    HStack(spacing: 40) {
        ListenButton(isListening: false, action: {})
        ListenButton(isListening: true, action: {})
    }
    .padding()
}
#endif
