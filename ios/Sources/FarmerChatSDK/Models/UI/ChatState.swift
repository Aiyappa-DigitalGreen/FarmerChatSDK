import Foundation

// MARK: - ChatState

/// Immutable snapshot of the entire chat screen state.
public struct ChatState {
    /// All messages currently visible in the thread (user + AI + loading).
    public var messages: [ChatMessage] = []

    /// Suggested follow-up question texts shown in the bottom chip bar.
    public var suggestedQuestions: [String]?

    /// IDs corresponding to `suggestedQuestions` (same index mapping).
    public var suggestedQuestionIds: [String]?

    /// True while an API call is in-flight.
    public var isLoading: Bool = false

    /// Non-nil when an error should be shown inline in the thread.
    public var errorMessage: String?

    /// ID of the AI message that failed (used for retry UI).
    public var failedMessageId: String?

    /// True while an audio synthesis request is in-flight.
    public var isLoadingSynthesiseAudio: Bool = false

    /// URL of the latest synthesised audio, ready for playback.
    public var audioPlaybackUrl: URL?

    /// True while audio is actively playing.
    public var isAudioPlaying: Bool = false

    /// The next page number for paginated history loading, nil when all loaded.
    public var historyNextPage: Int?

    /// True after the first page of chat history has been fetched.
    public var isInitialHistoryLoaded: Bool = false

    /// The active conversation ID (set after the first message is sent).
    public var conversationId: String?

    public init() {}
}
