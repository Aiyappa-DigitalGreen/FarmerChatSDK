import SwiftUI

// MARK: - SoundWaveAnimation

/// A compact 5-bar equaliser-style animation used as a status indicator
/// (e.g. inside the FAB when audio is playing).
struct SoundWaveAnimation: View {

    @State private var animating = false

    private let barCount = 5
    private let barWidth: CGFloat = 3
    private let barSpacing: CGFloat = 2
    private let maxBarHeight: CGFloat = 20
    private let minBarHeight: CGFloat = 4

    var body: some View {
        HStack(alignment: .center, spacing: barSpacing) {
            ForEach(0..<barCount, id: \.self) { index in
                Capsule()
                    .fill(Color.white)
                    .frame(width: barWidth, height: animating ? maxBarHeight : minBarHeight)
                    .animation(
                        .easeInOut(duration: 0.4 + Double(index) * 0.06)
                        .repeatForever(autoreverses: true)
                        .delay(Double(index) * 0.08),
                        value: animating
                    )
            }
        }
        .onAppear { animating = true }
        .onDisappear { animating = false }
    }
}

#if DEBUG
#Preview {
    ZStack {
        Color.black
        SoundWaveAnimation()
    }
    .frame(width: 60, height: 40)
    .cornerRadius(8)
}
#endif
