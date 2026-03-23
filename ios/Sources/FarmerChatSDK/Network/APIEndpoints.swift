import Foundation

// MARK: - APIEndpoints

/// Centralised list of API path components (without leading slash).
/// These paths are relative to the base URL provided in FarmerChatConfig.
/// All paths include the "api/" prefix required by the farmerchat.farmstack.co backend.
enum APIEndpoints {
    static let newConversation   = "api/chat/new_conversation/"
    static let getTextPrompt     = "api/chat/get_answer_for_text_query/"
    static let imageAnalysis     = "api/chat/image_analysis/"
    static let followUpQuestions = "api/chat/follow_up_questions/"
    static let followUpClick     = "api/chat/follow_up_question_click/"
    static let synthesiseAudio   = "api/chat/synthesise_audio/"
    static let chatHistory       = "api/chat/conversation_chat_history/"
    static let conversationList  = "api/chat/conversation_list/"
    static let transcribeAudio   = "api/chat/transcribe_audio/"
    static let tokenRefresh      = "api/user/get_new_access_token/"
    static let sendTokens        = "api/user/send_tokens/"
    static let initializeUser    = "api/user/initialize_user/"
}
