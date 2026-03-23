import Foundation

// MARK: - NewConversationResponse

public struct NewConversationResponse: Decodable {
    public let conversationId: String
    public let message: String?
    public let showPopup: Bool?

    enum CodingKeys: String, CodingKey {
        case conversationId = "conversation_id"
        case message
        case showPopup = "show_popup"
    }
}
