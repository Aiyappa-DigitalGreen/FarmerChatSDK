import Foundation
import AVFoundation

// MARK: - AudioRecorderService

/// Manages AVAudioRecorder lifecycle: permissions, recording, stopping.
@MainActor
class AudioRecorderService: NSObject, ObservableObject {

    // MARK: Published

    @Published private(set) var isRecording = false
    @Published private(set) var duration: TimeInterval = 0

    // MARK: Internal (accessible to WaveformSampler)

    private(set) var avRecorder: AVAudioRecorder?

    // MARK: Private

    private var timer: Timer?
    private var currentFileURL: URL?

    // MARK: Public API

    /// Request microphone permission if needed and start recording.
    /// - Returns: The local file URL where audio will be written.
    /// - Throws: `AudioError` if permission is denied or recording cannot start.
    func startRecording() async throws -> URL {
        try await requestPermission()

        let fileURL = makeRecordingFileURL()
        let settings: [String: Any] = [
            AVFormatIDKey:            Int(kAudioFormatMPEG4AAC),
            AVSampleRateKey:          44_100,
            AVNumberOfChannelsKey:    1,
            AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue
        ]

        let session = AVAudioSession.sharedInstance()
        try session.setCategory(.playAndRecord, mode: .default, options: [.defaultToSpeaker])
        try session.setActive(true)

        let recorder = try AVAudioRecorder(url: fileURL, settings: settings)
        recorder.delegate = self
        recorder.isMeteringEnabled = true

        guard recorder.record() else {
            throw AudioError.recordingFailed
        }

        avRecorder = recorder
        currentFileURL = fileURL
        isRecording = true
        duration = 0

        startTimer()
        return fileURL
    }

    /// Stop recording and return the file URL.
    /// - Returns: File URL, or nil if recording was never started.
    func stopRecording() async -> URL? {
        guard let recorder = avRecorder, isRecording else { return currentFileURL }

        recorder.stop()
        stopTimer()
        isRecording = false

        try? AVAudioSession.sharedInstance().setActive(false)

        return currentFileURL
    }

    /// Cancel and discard the current recording.
    func cancel() {
        avRecorder?.stop()
        avRecorder?.deleteRecording()
        avRecorder = nil

        stopTimer()
        isRecording = false
        duration = 0
        currentFileURL = nil

        try? AVAudioSession.sharedInstance().setActive(false)
    }

    /// Read a recorded file and return base64-encoded data.
    func getBase64(from url: URL) throws -> String {
        let data = try Data(contentsOf: url)
        return data.base64EncodedString()
    }

    // MARK: Private Helpers

    private func requestPermission() async throws {
        let session = AVAudioSession.sharedInstance()

        if session.recordPermission == .granted { return }

        return try await withCheckedThrowingContinuation { continuation in
            session.requestRecordPermission { granted in
                if granted {
                    continuation.resume()
                } else {
                    continuation.resume(throwing: AudioError.permissionDenied)
                }
            }
        }
    }

    private func makeRecordingFileURL() -> URL {
        let tempDir = FileManager.default.temporaryDirectory
        let fileName = "farmerchat_recording_\(Int(Date().timeIntervalSince1970)).m4a"
        return tempDir.appendingPathComponent(fileName)
    }

    private func startTimer() {
        timer = Timer.scheduledTimer(withTimeInterval: 0.5, repeats: true) { [weak self] _ in
            guard let self else { return }
            Task { @MainActor in
                self.duration += 0.5
            }
        }
    }

    private func stopTimer() {
        timer?.invalidate()
        timer = nil
    }
}

// MARK: - AVAudioRecorderDelegate

extension AudioRecorderService: AVAudioRecorderDelegate {
    nonisolated func audioRecorderDidFinishRecording(_ recorder: AVAudioRecorder, successfully flag: Bool) {
        Task { @MainActor in
            isRecording = false
            stopTimer()
        }
    }

    nonisolated func audioRecorderEncodeErrorDidOccur(_ recorder: AVAudioRecorder, error: Error?) {
        Task { @MainActor in
            isRecording = false
            stopTimer()
        }
    }
}

// MARK: - AudioError

enum AudioError: LocalizedError {
    case permissionDenied
    case recordingFailed
    case fileNotFound

    var errorDescription: String? {
        switch self {
        case .permissionDenied:
            return "Microphone access is required for voice input. Please enable it in Settings."
        case .recordingFailed:
            return "Could not start recording. Please try again."
        case .fileNotFound:
            return "Recording file not found."
        }
    }
}
