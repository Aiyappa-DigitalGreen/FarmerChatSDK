import Foundation

// MARK: - ImageAnalysisResponse
// Matches api/chat/image_analysis/
// Decoder uses .convertFromSnakeCase → snake_case JSON keys map to camelCase Swift properties.

struct ImageAnalysisResponse: Decodable {
    let error: Bool
    let message: String
    let messageId: String               // from "message_id"
    let response: String                // main AI analysis text
    let followUpQuestions: [FollowUpQuestionOption]?  // from "follow_up_questions"
    let contentProviderLogo: String?    // from "content_provider_logo"
    let hideTtsSpeaker: Bool?           // from "hide_tts_speaker"
    let points: Int?
}
