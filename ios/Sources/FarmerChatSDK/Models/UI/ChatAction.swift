import Foundation

// MARK: - ChatAction

/// All events that can be dispatched to `ChatViewModel`.
public enum ChatAction {
    /// User sends their first question in a new conversation.
    case initializeWithQuestion(
        question: String,
        transcriptionId: String?,
        audioUrl: URL?
    )

    /// The conversation starts with pre-generated content (no API call for the first turn).
    case initializeWithPreGeneratedContent(
        question: String,
        answer: String?,
        followUpQuestions: [String]?
    )

    /// Replace a pre-generated message pair with a live API call.
    case replacePreGeneratedWithQuestion(
        question: String,
        triggerInputType: String?
    )

    /// User taps a follow-up suggestion chip.
    case sendFollowUpQuestion(
        question: String,
        followUpQuestionId: String?,
        transcriptionId: String?,
        audioUrl: URL?
    )

    /// User sends a question with an attached image.
    case sendQuestionWithImage(question: String, imageData: Data)

    /// User sends a voice message (audio file on-device).
    case sendQuestionWithAudio(question: String, audioUrl: URL)

    /// Raw audio data → transcribe API → send as text query.
    case transcribeAndSendAudio(audioData: Data, audioUrl: URL?)

    /// Load paginated chat history for a given conversation.
    case loadChatHistory(conversationId: String, page: Int)

    /// Retry the last failed API action.
    case retryLastRequest

    /// Dismiss the current inline error without retrying.
    case clearError

    /// Clear all messages and reset to empty state.
    case clearMessages

    /// Request server-side text-to-speech for the latest AI message.
    case synthesiseAudio

    /// Clear the audio playback URL (e.g. after playback finishes).
    case clearAudioPlaybackUrl

    /// Update the audio playing flag.
    case setAudioPlaying(Bool)
}
