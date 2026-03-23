import Foundation

// MARK: - TextPromptRequest
// Matches api/chat/get_answer_for_text_query/
// Using camelCase properties — encoder uses .convertToSnakeCase to produce correct JSON keys.

struct TextPromptRequest: Encodable {
    let query: String
    let conversationId: String          // → "conversation_id"
    let messageId: String               // → "message_id"
    let triggeredInputType: String      // → "triggered_input_type": "text" | "audio" | "image" | "follow_up"
    let transcriptionId: String?        // → "transcription_id"
    let useEntityExtraction: Bool       // → "use_entity_extraction"
    let weatherCtaTriggered: Bool       // → "weather_cta_triggered"
    let retry: Bool
}
