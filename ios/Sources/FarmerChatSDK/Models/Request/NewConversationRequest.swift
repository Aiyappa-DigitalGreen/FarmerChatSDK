import Foundation

// MARK: - NewConversationRequest

struct NewConversationRequest: Encodable {
    let userId: String
    let contentProviderId: String?

    enum CodingKeys: String, CodingKey {
        case userId = "user_id"
        case contentProviderId = "content_provider_id"
    }

    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(userId, forKey: .userId)
        try container.encodeIfPresent(contentProviderId, forKey: .contentProviderId)
    }
}
