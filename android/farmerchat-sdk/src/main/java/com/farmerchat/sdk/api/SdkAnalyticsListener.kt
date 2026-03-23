package com.farmerchat.sdk.api

/**
 * Optional listener for analytics events emitted by the FarmerChat SDK.
 * Implement this interface and pass an instance via [com.farmerchat.sdk.FarmerChatConfig]
 * to receive analytics callbacks.
 */
interface SdkAnalyticsListener {

    /**
     * Called when a new conversation is created.
     * @param conversationId The newly created conversation ID.
     */
    fun onNewConversationCreated(conversationId: String) {}

    /**
     * Called when a text query is sent.
     * @param query The query text.
     * @param conversationId The conversation ID.
     * @param inputType How the query was triggered (e.g. "text", "audio", "follow_up").
     */
    fun onTextQuerySent(query: String, conversationId: String, inputType: String) {}

    /**
     * Called when an image query is sent.
     * @param conversationId The conversation ID.
     */
    fun onImageQuerySent(conversationId: String) {}

    /**
     * Called when a voice query is sent.
     * @param conversationId The conversation ID.
     */
    fun onVoiceQuerySent(conversationId: String) {}

    /**
     * Called when a follow-up question is tapped.
     * @param question The follow-up question text.
     * @param questionId The follow-up question ID if available.
     */
    fun onFollowUpQuestionTapped(question: String, questionId: String?) {}

    /**
     * Called when the TTS (text-to-speech) listen button is tapped.
     * @param messageId The message ID for which audio was requested.
     */
    fun onListenButtonTapped(messageId: String) {}

    /**
     * Called when an API error occurs.
     * @param apiName The name of the API endpoint that failed.
     * @param errorCode The HTTP error code, or null if it was a network/timeout error.
     * @param errorMessage The error message.
     */
    fun onApiError(apiName: String, errorCode: Int?, errorMessage: String?) {}

    /**
     * Called when the chat history screen is opened.
     */
    fun onHistoryOpened() {}

    /**
     * Called when a conversation from history is selected.
     * @param conversationId The selected conversation ID.
     */
    fun onHistoryConversationSelected(conversationId: String) {}
}
