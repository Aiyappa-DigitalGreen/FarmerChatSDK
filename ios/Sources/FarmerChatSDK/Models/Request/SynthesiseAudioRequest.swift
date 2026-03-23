import Foundation

// MARK: - SynthesiseAudioRequest

public struct SynthesiseAudioRequest: Encodable {
    public let messageId: String
    public let text: String
    public let userId: String

    public init(messageId: String, text: String, userId: String) {
        self.messageId = messageId
        self.text = text
        self.userId = userId
    }

    enum CodingKeys: String, CodingKey {
        case messageId = "message_id"
        case text
        case userId = "user_id"
    }
}
