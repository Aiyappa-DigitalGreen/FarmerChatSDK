import Foundation

// MARK: - TranscribeAudioRequest

/// Sent as multipart/form-data to the transcription endpoint.
public struct TranscribeAudioRequest {
    public let audioData: Data
    public let userId: String
    public let conversationId: String?
    public let language: String?

    public init(
        audioData: Data,
        userId: String,
        conversationId: String? = nil,
        language: String? = nil
    ) {
        self.audioData = audioData
        self.userId = userId
        self.conversationId = conversationId
        self.language = language
    }
}
