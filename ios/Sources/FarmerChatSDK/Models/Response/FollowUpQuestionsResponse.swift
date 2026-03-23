import Foundation

// MARK: - FollowUpQuestionsResponse
// Matches: api/chat/follow_up_questions/?message_id=xxx
// Decoder uses .convertFromSnakeCase — follow_up_question_id → followUpQuestionId

public struct FollowUpQuestionsResponse: Decodable {
    public let messageId: String?           // "message_id"
    public let sectionMessageId: String?    // "section_message_id"
    public let questions: [FollowUpQuestion]?
}

public struct FollowUpQuestion: Decodable {
    public let followUpQuestionId: String   // "follow_up_question_id"
    public let question: String
    public let sequence: Int?
}
