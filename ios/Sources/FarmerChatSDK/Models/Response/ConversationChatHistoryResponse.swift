import Foundation

// MARK: - ConversationChatHistoryResponse
// Matches api/chat/conversation_chat_history/
// Response shape: { "conversation_id": "...", "data": [...] }

struct ConversationChatHistoryResponse: Decodable {
    let conversationId: String          // from "conversation_id"
    let data: [ChatHistoryItem]
}

// MARK: - ChatHistoryItem

struct ChatHistoryItem: Decodable {
    // message_type_id:
    //   1 = user text query
    //   2 = user audio query
    //   3 = AI text response
    //   7 = follow-up questions (attached to prior AI response)
    //  11 = user image query
    let messageTypeId: Int              // from "message_type_id"
    let messageType: String             // from "message_type" (additional string label)
    let messageId: String               // from "message_id"
    let queryText: String?              // from "query_text" — present for user text/image queries
    let heardQueryText: String?         // from "heard_query_text" — transcribed text for audio queries
    let responseText: String?           // from "response_text" — present for AI responses
    let questions: [HistoryFollowUpQuestion]?  // from "questions"
    let queryMediaFileUrl: String?      // from "query_media_file_url" — image URL for image queries
    let contentProviderLogo: String?    // from "content_provider_logo"
    let hideTtsSpeaker: Bool?           // from "hide_tts_speaker"
    let messageInputTime: String?       // from "message_input_time"
}

// MARK: - HistoryFollowUpQuestion

struct HistoryFollowUpQuestion: Decodable {
    let followUpQuestionId: String      // from "follow_up_question_id"
    let question: String
    let sequence: Int
}
