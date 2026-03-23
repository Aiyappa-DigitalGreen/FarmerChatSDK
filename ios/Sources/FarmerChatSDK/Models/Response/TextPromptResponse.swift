import Foundation

// MARK: - FollowUpQuestionOption
// Shared by TextPromptResponse and ImageAnalysisResponse.
// Decoder uses .convertFromSnakeCase → "follow_up_question_id" maps to followUpQuestionId.

struct FollowUpQuestionOption: Decodable {
    let followUpQuestionId: String?     // from "follow_up_question_id"
    let question: String?
    let sequence: Int?
}

// MARK: - IntentClassificationOutput
// "confidence" is a string ("high"/"medium"/"low"), NOT a number.

struct IntentClassificationOutput: Decodable {
    let intent: String?
    let confidence: String?             // "high" | "medium" | "low"
    let assetType: String?              // from "asset_type"
    let assetName: String?              // from "asset_name"
    let assetStatus: String?            // from "asset_status"
    let concern: String?
    let stage: String?
    let likelyActivity: String?         // from "likely_activity"
    let rephrasedQuery: String?         // from "rephrased_query"
    let seasonalRelevance: String?      // from "seasonal_relevance"
}

// MARK: - TextPromptResponse
// Matches api/chat/get_answer_for_text_query/

struct TextPromptResponse: Decodable {
    let error: Bool
    let message: String?
    let messageId: String?              // from "message_id"
    let response: String?               // main AI answer text
    let translatedResponse: String?     // from "translated_response"
    let followUpQuestions: [FollowUpQuestionOption]?  // from "follow_up_questions"
    let sectionMessageId: String?       // from "section_message_id"
    let contentProviderLogo: String?    // from "content_provider_logo"
    let hideFollowUpQuestion: Bool?     // from "hide_follow_up_question"
    let hideTtsSpeaker: Bool?           // from "hide_tts_speaker"
    let points: Int?
    let intentClassificationOutput: IntentClassificationOutput?  // from "intent_classification_output"
}
