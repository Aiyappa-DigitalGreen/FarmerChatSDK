import SwiftUI

// MARK: - WaveformView

/// Displays a live audio waveform from an array of normalised amplitude samples (0.0–1.0).
/// Used in the VoiceInputOverlay during recording.
struct WaveformView: View {

    /// Array of amplitude values in range [0.0, 1.0], one per bar.
    let amplitudes: [Float]

    private let barSpacing: CGFloat = 3
    private let cornerRadius: CGFloat = 2

    var body: some View {
        GeometryReader { geometry in
            let barCount = amplitudes.count
            let totalSpacing = barSpacing * CGFloat(max(barCount - 1, 0))
            let barWidth = barCount > 0
                ? (geometry.size.width - totalSpacing) / CGFloat(barCount)
                : 4

            HStack(alignment: .center, spacing: barSpacing) {
                ForEach(amplitudes.indices, id: \.self) { index in
                    let amplitude = CGFloat(amplitudes[index])
                    let height = max(4, amplitude * geometry.size.height)

                    Capsule()
                        .fill(Color.white.opacity(0.9))
                        .frame(width: max(2, barWidth), height: height)
                        .animation(.easeOut(duration: 0.06), value: amplitudes[index])
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
        }
    }
}

#if DEBUG
#Preview {
    ZStack {
        Color.black.ignoresSafeArea()
        WaveformView(amplitudes: (0..<40).map { _ in Float.random(in: 0.05...1.0) })
            .frame(height: 60)
            .padding(.horizontal, 24)
    }
}
#endif
