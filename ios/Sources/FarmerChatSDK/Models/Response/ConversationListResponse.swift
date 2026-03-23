import Foundation

// MARK: - ConversationListItem
// API returns a plain JSON array of these items — no wrapper object, no pagination fields.

public struct ConversationListItem: Decodable, Identifiable {
    public var id: String { conversationId ?? UUID().uuidString }
    public let conversationId: String?
    public let conversationTitle: String?
    public let createdOn: String?
    public let messageType: String?
    public let grouping: String?
    public let contentProviderLogo: String?

    enum CodingKeys: String, CodingKey {
        case conversationId = "conversation_id"
        case conversationTitle = "conversation_title"
        case createdOn = "created_on"
        case messageType = "message_type"
        case grouping
        case contentProviderLogo = "content_provider_logo"
    }
}
