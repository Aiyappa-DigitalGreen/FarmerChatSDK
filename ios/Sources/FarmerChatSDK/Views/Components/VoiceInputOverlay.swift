import SwiftUI
import AVFoundation

// MARK: - VoiceInputOverlay

/// Full-screen overlay shown during voice recording and transcription.
struct VoiceInputOverlay: View {

    let onDismiss: () -> Void
    let onAudioCaptured: (URL?, String, String?) -> Void

    @StateObject private var recorder = AudioRecorderService()
    @StateObject private var waveformSampler = WaveformSampler()

    @State private var phase: OverlayPhase = .idle
    @State private var recordedFileURL: URL?
    @State private var errorMessage: String?

    enum OverlayPhase {
        case idle, recording, processing, error
    }

    // MARK: Body

    var body: some View {
        ZStack {
            // Scrim
            Color.black.opacity(0.6)
                .ignoresSafeArea()
                .onTapGesture {
                    if phase == .idle { onDismiss() }
                }

            VStack(spacing: 24) {
                Spacer()

                // Title
                Text(titleText)
                    .font(.headline)
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)

                // Waveform / indicator
                if phase == .recording {
                    WaveformView(amplitudes: waveformSampler.amplitudes)
                        .frame(height: 60)
                        .padding(.horizontal, 40)

                    Text(formattedDuration(recorder.duration))
                        .font(.monospacedDigit(.body)())
                        .foregroundColor(.white.opacity(0.8))
                } else if phase == .processing {
                    ProgressView()
                        .tint(.white)
                        .scaleEffect(1.4)
                        .frame(height: 60)
                } else if let error = errorMessage {
                    Text(error)
                        .font(.subheadline)
                        .foregroundColor(.red.opacity(0.9))
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 40)
                }

                // Action buttons
                HStack(spacing: 40) {
                    // Cancel
                    Button(action: handleCancel) {
                        Image(systemName: "xmark")
                            .font(.title2)
                            .foregroundColor(.white)
                            .frame(width: 56, height: 56)
                            .background(Color.white.opacity(0.2))
                            .clipShape(Circle())
                    }

                    // Main record / stop button
                    ListenButton(isListening: phase == .recording) {
                        handleMainButton()
                    }
                    .scaleEffect(1.3)

                    // Placeholder spacer for symmetry
                    Color.clear.frame(width: 56, height: 56)
                }

                Spacer()
            }
            .padding()
        }
        .onAppear { startRecording() }
    }

    // MARK: – Helpers

    private var titleText: String {
        switch phase {
        case .idle:       return "Tap to start recording"
        case .recording:  return "Listening…"
        case .processing: return "Processing your voice…"
        case .error:      return "Something went wrong"
        }
    }

    private func formattedDuration(_ duration: TimeInterval) -> String {
        let minutes = Int(duration) / 60
        let seconds = Int(duration) % 60
        return String(format: "%d:%02d", minutes, seconds)
    }

    // MARK: – Actions

    private func startRecording() {
        phase = .recording
        Task {
            do {
                let url = try await recorder.startRecording()
                recordedFileURL = url
                if let avRecorder = recorder.avRecorder {
                    waveformSampler.start(recorder: avRecorder)
                }
            } catch {
                phase = .error
                errorMessage = error.localizedDescription
            }
        }
    }

    private func handleMainButton() {
        switch phase {
        case .recording:
            stopAndTranscribe()
        case .idle:
            startRecording()
        default:
            break
        }
    }

    private func stopAndTranscribe() {
        waveformSampler.stop()
        phase = .processing

        Task {
            guard let url = await recorder.stopRecording() else {
                phase = .error
                errorMessage = "Recording failed. Please try again."
                return
            }

            // Deliver the raw audio URL — transcription is done server-side
            // The ChatViewModel will call transcribeAudio as needed.
            onAudioCaptured(url, "", nil)
        }
    }

    private func handleCancel() {
        waveformSampler.stop()
        recorder.cancel()
        onDismiss()
    }
}
