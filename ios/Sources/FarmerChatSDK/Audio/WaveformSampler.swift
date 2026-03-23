import Foundation
import AVFoundation

// MARK: - WaveformSampler

/// Polls an `AVAudioRecorder`'s metering data at a regular interval
/// and publishes a sliding buffer of normalised amplitude samples.
class WaveformSampler: ObservableObject {

    // MARK: Published

    /// Normalised amplitude values in [0.0, 1.0], newest at the end.
    @Published var amplitudes: [Float] = Array(repeating: 0, count: 40)

    // MARK: Private

    private var timer: Timer?
    private let sampleCount: Int
    private let pollInterval: TimeInterval

    // MARK: Init

    /// - Parameters:
    ///   - sampleCount: Number of bars to keep in the buffer (default 40).
    ///   - pollInterval: How often to sample the recorder (default 60 ms).
    init(sampleCount: Int = 40, pollInterval: TimeInterval = 0.06) {
        self.sampleCount = sampleCount
        self.pollInterval = pollInterval
        amplitudes = Array(repeating: 0, count: sampleCount)
    }

    // MARK: Public API

    /// Begin polling the given recorder. Call `stop()` when done.
    func start(recorder: AVAudioRecorder) {
        stop()
        amplitudes = Array(repeating: 0, count: sampleCount)

        timer = Timer.scheduledTimer(withTimeInterval: pollInterval, repeats: true) { [weak self, weak recorder] _ in
            guard let self, let recorder, recorder.isRecording else { return }
            recorder.updateMeters()

            // averagePower ranges from -160 dB (silence) to 0 dB (max)
            let power = recorder.averagePower(forChannel: 0)
            let normalised = Self.normalise(power: power)

            DispatchQueue.main.async {
                var next = self.amplitudes
                next.removeFirst()
                next.append(normalised)
                self.amplitudes = next
            }
        }
    }

    /// Stop polling.
    func stop() {
        timer?.invalidate()
        timer = nil
    }

    // MARK: Private Helpers

    /// Map dB value (-160 … 0) to a normalised Float (0 … 1).
    private static func normalise(power: Float) -> Float {
        let minDb: Float = -80
        let maxDb: Float = 0

        let clamped = max(minDb, min(maxDb, power))
        return (clamped - minDb) / (maxDb - minDb)
    }
}
