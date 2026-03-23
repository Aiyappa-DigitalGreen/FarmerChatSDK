import Foundation

// MARK: - FollowUpClickRequest

struct FollowUpClickRequest: Encodable {
    let followUpQuestion: String

    enum CodingKeys: String, CodingKey {
        case followUpQuestion = "follow_up_question"
    }
}
