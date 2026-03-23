import Foundation
import AVFoundation

// MARK: - AudioPlayerService

/// Plays audio from a remote or local URL using AVPlayer.
@MainActor
class AudioPlayerService: NSObject, ObservableObject {

    // MARK: Published

    @Published private(set) var isPlaying = false
    @Published private(set) var currentTime: TimeInterval = 0
    @Published private(set) var duration: TimeInterval = 0

    // MARK: Private

    private var player: AVPlayer?
    private var playerItem: AVPlayerItem?
    private var timeObserver: Any?
    private var endObserver: NSObjectProtocol?
    private var currentURL: URL?

    // MARK: Public API

    /// Start playing audio from the given URL (remote or local).
    func play(url: URL) {
        // If same URL is already playing, do nothing
        if currentURL == url, isPlaying { return }

        stop()
        currentURL = url

        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.playback, mode: .default, options: [])
            try session.setActive(true)
        } catch {
            // Continue even if session setup fails
        }

        playerItem = AVPlayerItem(url: url)
        player = AVPlayer(playerItem: playerItem)

        // Observe duration once item is ready
        playerItem?.addObserver(self, forKeyPath: "status", options: [.new], context: nil)

        // Periodic time observer (every 0.5s)
        let interval = CMTime(seconds: 0.5, preferredTimescale: CMTimeScale(NSEC_PER_SEC))
        timeObserver = player?.addPeriodicTimeObserver(forInterval: interval, queue: .main) { [weak self] time in
            guard let self else { return }
            Task { @MainActor in
                self.currentTime = time.seconds
            }
        }

        // End-of-playback observer
        endObserver = NotificationCenter.default.addObserver(
            forName: .AVPlayerItemDidPlayToEndTime,
            object: playerItem,
            queue: .main
        ) { [weak self] _ in
            guard let self else { return }
            Task { @MainActor in
                self.isPlaying = false
                self.currentTime = 0
                self.player?.seek(to: .zero)
            }
        }

        player?.play()
        isPlaying = true
    }

    /// Stop playback.
    func stop() {
        player?.pause()
        removeObservers()
        player = nil
        playerItem = nil
        isPlaying = false
        currentTime = 0
        duration = 0
        currentURL = nil
    }

    /// Pause playback.
    func pause() {
        player?.pause()
        isPlaying = false
    }

    /// Resume paused playback.
    func resume() {
        player?.play()
        isPlaying = true
    }

    // MARK: KVO

    nonisolated override func observeValue(
        forKeyPath keyPath: String?,
        of object: Any?,
        change: [NSKeyValueChangeKey: Any]?,
        context: UnsafeMutableRawPointer?
    ) {
        guard keyPath == "status",
              let item = object as? AVPlayerItem,
              item.status == .readyToPlay else { return }

        let dur = item.duration.seconds
        Task { @MainActor in
            if dur.isFinite && !dur.isNaN {
                self.duration = dur
            }
        }
    }

    // MARK: Private Helpers

    private func removeObservers() {
        if let observer = timeObserver {
            player?.removeTimeObserver(observer)
            timeObserver = nil
        }
        if let observer = endObserver {
            NotificationCenter.default.removeObserver(observer)
            endObserver = nil
        }
        playerItem?.removeObserver(self, forKeyPath: "status")
    }

    deinit {
        // Removing observers in deinit without MainActor can be tricky;
        // player/playerItem will be released and observers auto-invalidated.
    }
}
