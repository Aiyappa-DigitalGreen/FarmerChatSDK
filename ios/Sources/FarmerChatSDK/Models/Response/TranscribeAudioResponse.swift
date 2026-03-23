import Foundation

// MARK: - TranscribeAudioResponse
// Matches: api/chat/transcribe_audio/
// API returns: heard_input_query, confidence_score, error, message_id, transcription_id

public struct TranscribeAudioResponse: Decodable {
    public let heardInputQuery: String?     // "heard_input_query" — the transcribed text
    public let confidenceScore: Double?     // "confidence_score"
    public let error: Bool?
    public let messageId: String?           // "message_id"
    public let transcriptionId: String?     // "transcription_id"
    public let message: String?
    // Decoder uses .convertFromSnakeCase — no explicit CodingKeys needed
}
