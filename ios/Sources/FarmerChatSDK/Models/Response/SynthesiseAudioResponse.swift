import Foundation

// MARK: - SynthesiseAudioResponse

public struct SynthesiseAudioResponse: Decodable {
    public let audioUrl: String?
    public let messageId: String?
    public let status: String?

    enum CodingKeys: String, CodingKey {
        case audioUrl = "audio_url"
        case messageId = "message_id"
        case status
    }
}
