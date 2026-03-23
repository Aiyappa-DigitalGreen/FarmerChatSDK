import Foundation
import SwiftUI

// MARK: - ChatViewModel

@MainActor
final class ChatViewModel: ObservableObject {

    // MARK: Published State

    @Published private(set) var state = ChatState()

    // MARK: Dependencies

    private let apiClient: ChatAPIClient
    private let tokenStore: any TokenStore
    private let userId: String
    private let deviceId: String
    private let contentProviderId: String?

    // MARK: Private

    private var lastFailedAction: ChatAction?
    private var conversationId: String?

    // MARK: Init

    init(
        apiClient: ChatAPIClient,
        tokenStore: any TokenStore,
        userId: String,
        deviceId: String,
        contentProviderId: String?,
        existingConversationId: String? = nil
    ) {
        self.apiClient = apiClient
        self.tokenStore = tokenStore
        self.userId = userId
        self.deviceId = deviceId
        self.contentProviderId = contentProviderId
        self.conversationId = existingConversationId
        self.state.conversationId = existingConversationId
    }

    // MARK: Dispatch

    func dispatch(_ action: ChatAction) {
        Task { await handleAction(action) }
    }

    // MARK: Private – Action Handler

    private func handleAction(_ action: ChatAction) async {
        switch action {

        case .initializeWithQuestion(let question, let transcriptionId, let audioUrl):
            await sendQuestion(
                question: question,
                transcriptionId: transcriptionId,
                audioUrl: audioUrl,
                triggerInputType: nil
            )

        case .initializeWithPreGeneratedContent(let question, let answer, let followUps):
            let userMsgId = UUID().uuidString
            let aiMsgId = UUID().uuidString
            let userMsg = ChatMessage.userMessage(id: userMsgId, text: question, imageUrl: nil, audioUrl: nil)
            let aiMsg = ChatMessage.aiResponse(
                id: aiMsgId,
                text: answer ?? "",
                followUpQuestions: followUps,
                isPreGenerated: true,
                messageId: nil
            )
            state.messages.append(userMsg)
            state.messages.append(aiMsg)
            if let followUps {
                state.suggestedQuestions = followUps
                state.suggestedQuestionIds = nil
            }

        case .replacePreGeneratedWithQuestion(let question, let triggerInputType):
            // Remove any existing pre-generated messages and send live
            state.messages.removeAll { $0.isPreGenerated }
            await sendQuestion(
                question: question,
                transcriptionId: nil,
                audioUrl: nil,
                triggerInputType: triggerInputType
            )

        case .sendFollowUpQuestion(let question, let followUpQuestionId, let transcriptionId, let audioUrl):
            if let fqId = followUpQuestionId, !fqId.isEmpty {
                _ = try? await apiClient.trackFollowUpClick(followUpQuestion: fqId)
            }
            await sendQuestion(
                question: question,
                transcriptionId: transcriptionId,
                audioUrl: audioUrl,
                triggerInputType: "follow_up"
            )

        case .sendQuestionWithImage(let question, let imageData):
            await sendImageQuestion(question: question, imageData: imageData)

        case .sendQuestionWithAudio(let question, let audioUrl):
            await sendQuestion(
                question: question,
                transcriptionId: nil,
                audioUrl: audioUrl,
                triggerInputType: "audio"
            )

        case .transcribeAndSendAudio(let audioData, let audioUrl):
            await transcribeAndSend(audioData: audioData, audioUrl: audioUrl)

        case .loadChatHistory(let conversationId, let page):
            await loadHistory(conversationId: conversationId, page: page)

        case .retryLastRequest:
            guard let failed = lastFailedAction else { return }
            state.errorMessage = nil
            state.failedMessageId = nil
            lastFailedAction = nil
            await handleAction(failed)

        case .clearError:
            state.errorMessage = nil
            state.failedMessageId = nil

        case .clearMessages:
            state.messages.removeAll()
            state.suggestedQuestions = nil
            state.suggestedQuestionIds = nil
            state.errorMessage = nil
            state.conversationId = nil
            conversationId = nil

        case .synthesiseAudio:
            await performSynthesiseAudio()

        case .clearAudioPlaybackUrl:
            state.audioPlaybackUrl = nil

        case .setAudioPlaying(let isPlaying):
            state.isAudioPlaying = isPlaying
        }
    }

    // MARK: – Text Prompt Flow

    private func sendQuestion(
        question: String,
        transcriptionId: String?,
        audioUrl: URL?,
        triggerInputType: String?
    ) async {
        let userMsgId = UUID().uuidString
        let loadingId = UUID().uuidString

        // 1. Optimistically update UI
        state.messages.append(.userMessage(
            id: userMsgId,
            text: question,
            imageUrl: nil,
            audioUrl: audioUrl
        ))
        state.messages.append(.loadingPlaceholder(id: loadingId))
        state.isLoading = true
        state.errorMessage = nil
        state.suggestedQuestions = nil
        state.suggestedQuestionIds = nil

        do {
            // 2. Ensure we have a conversation
            let convId = try await ensureConversation()

            // 3. Send text prompt — field names must match API exactly
            let request = TextPromptRequest(
                query: question,
                conversationId: convId,
                messageId: UUID().uuidString,
                triggeredInputType: triggerInputType ?? "text",
                transcriptionId: transcriptionId,
                useEntityExtraction: true,
                weatherCtaTriggered: false,
                retry: false
            )
            let response = try await apiClient.sendTextPrompt(request)

            // 4. Resolve follow-up questions — always call dedicated endpoint first
            var followUpTexts: [String]? = nil
            var followUpIds: [String]? = nil

            if let msgId = response.messageId, response.hideFollowUpQuestion != true {
                if let fuResponse = try? await apiClient.fetchFollowUpQuestions(messageId: msgId),
                   let questions = fuResponse.questions, !questions.isEmpty {
                    followUpTexts = questions.compactMap { $0.question }
                    followUpIds = questions.compactMap { $0.followUpQuestionId }
                }
            }
            if (followUpTexts ?? []).isEmpty {
                followUpTexts = response.followUpQuestions?.compactMap { $0.question }
                followUpIds = response.followUpQuestions?.compactMap { $0.followUpQuestionId }
            }

            // 5. Replace loading with AI response
            let aiMsgId = UUID().uuidString
            replaceLoading(
                id: loadingId,
                with: .aiResponse(
                    id: aiMsgId,
                    text: response.response ?? response.translatedResponse ?? "",
                    followUpQuestions: followUpTexts,
                    isPreGenerated: false,
                    messageId: response.messageId
                )
            )

            // 6. Update follow-up suggestions
            state.suggestedQuestions = followUpTexts
            state.suggestedQuestionIds = followUpIds

        } catch {
            handleError(error, loadingId: loadingId, failedAction: .initializeWithQuestion(
                question: question,
                transcriptionId: transcriptionId,
                audioUrl: audioUrl
            ))
        }

        state.isLoading = false
    }

    // MARK: – Image Analysis Flow

    private func sendImageQuestion(question: String, imageData: Data) async {
        let userMsgId = UUID().uuidString
        let loadingId = UUID().uuidString

        state.messages.append(.userMessage(
            id: userMsgId,
            text: question,
            imageUrl: nil,
            audioUrl: nil
        ))
        state.messages.append(.loadingPlaceholder(id: loadingId))
        state.isLoading = true
        state.errorMessage = nil
        state.suggestedQuestions = nil
        state.suggestedQuestionIds = nil

        do {
            let convId = try await ensureConversation()
            let base64 = imageData.base64EncodedString()

            let imageName = "image_\(UUID().uuidString).jpg"
            let request = ImageAnalysisRequest(
                conversationId: convId,
                image: base64,
                triggeredInputType: "image",
                query: question.isEmpty ? nil : question,
                latitude: nil,
                longitude: nil,
                imageName: imageName,
                retry: false
            )
            let response = try await apiClient.sendImageAnalysis(request)

            var followUpTexts: [String]? = nil
            var followUpIds: [String]? = nil

            if let msgId = response.messageId {
                if let fuResponse = try? await apiClient.fetchFollowUpQuestions(messageId: msgId),
                   let questions = fuResponse.questions, !questions.isEmpty {
                    followUpTexts = questions.compactMap { $0.question }
                    followUpIds = questions.compactMap { $0.followUpQuestionId }
                }
            }
            if (followUpTexts ?? []).isEmpty {
                followUpTexts = response.followUpQuestions?.compactMap { $0.question }
                followUpIds = response.followUpQuestions?.compactMap { $0.followUpQuestionId }
            }

            let aiMsgId = UUID().uuidString
            replaceLoading(
                id: loadingId,
                with: .aiResponse(
                    id: aiMsgId,
                    text: response.response,
                    followUpQuestions: followUpTexts,
                    isPreGenerated: false,
                    messageId: response.messageId
                )
            )

            state.suggestedQuestions = followUpTexts
            state.suggestedQuestionIds = followUpIds

        } catch {
            handleError(error, loadingId: loadingId, failedAction: .sendQuestionWithImage(
                question: question,
                imageData: imageData
            ))
        }

        state.isLoading = false
    }

    // MARK: – Audio Transcription Flow

    private func transcribeAndSend(audioData: Data, audioUrl: URL?) async {
        let userMsgId = UUID().uuidString
        let loadingId = UUID().uuidString

        state.messages.append(.userMessage(id: userMsgId, text: "🎤 ...", imageUrl: nil, audioUrl: audioUrl))
        state.messages.append(.loadingPlaceholder(id: loadingId))
        state.isLoading = true
        state.errorMessage = nil
        state.suggestedQuestions = nil
        state.suggestedQuestionIds = nil

        do {
            let convId = try await ensureConversation()

            let transcribeRequest = TranscribeAudioRequest(
                audioData: audioData,
                userId: userId,
                conversationId: convId,
                language: nil
            )
            let transcription = try await apiClient.transcribeAudio(transcribeRequest)

            guard let heardText = transcription.heardInputQuery, !heardText.isEmpty else {  // from "heard_input_query"
                removeLoading(id: loadingId)
                state.errorMessage = "Could not understand audio. Please try again."
                state.isLoading = false
                return
            }

            // Update user bubble with real transcribed text
            if let idx = state.messages.firstIndex(where: { $0.id == userMsgId }) {
                state.messages[idx] = .userMessage(id: userMsgId, text: heardText, imageUrl: nil, audioUrl: audioUrl)
            }

            // Continue with regular text prompt flow using transcribed text
            let request = TextPromptRequest(
                query: heardText,
                conversationId: convId,
                messageId: UUID().uuidString,
                triggeredInputType: "audio",
                transcriptionId: transcription.transcriptionId,
                useEntityExtraction: true,
                weatherCtaTriggered: false,
                retry: false
            )
            let response = try await apiClient.sendTextPrompt(request)

            var followUpTexts: [String]? = nil
            var followUpIds: [String]? = nil

            if let msgId = response.messageId, response.hideFollowUpQuestion != true {
                if let fuResponse = try? await apiClient.fetchFollowUpQuestions(messageId: msgId),
                   let questions = fuResponse.questions, !questions.isEmpty {
                    followUpTexts = questions.compactMap { $0.question }
                    followUpIds = questions.compactMap { $0.followUpQuestionId }
                }
            }
            if (followUpTexts ?? []).isEmpty {
                followUpTexts = response.followUpQuestions?.compactMap { $0.question }
                followUpIds = response.followUpQuestions?.compactMap { $0.followUpQuestionId }
            }

            replaceLoading(
                id: loadingId,
                with: .aiResponse(
                    id: UUID().uuidString,
                    text: response.response ?? response.translatedResponse ?? "",
                    followUpQuestions: followUpTexts,
                    isPreGenerated: false,
                    messageId: response.messageId
                )
            )
            state.suggestedQuestions = followUpTexts
            state.suggestedQuestionIds = followUpIds

        } catch {
            handleError(error, loadingId: loadingId, failedAction: .transcribeAndSendAudio(audioData: audioData, audioUrl: audioUrl))
        }

        state.isLoading = false
    }

    private func removeLoading(id: String) {
        state.messages.removeAll { $0.id == id }
    }

    // MARK: – History Loading

    private func loadHistory(conversationId: String, page: Int) async {
        state.isLoading = true
        do {
            // Ensure guest session before making the history API call
            try await FarmerChatSDK.shared.ensureTokens()

            let response = try await apiClient.fetchChatHistory(
                conversationId: conversationId,
                page: page
            )

            // Map message_type_id to ChatMessage:
            //  1 = user text query   2 = user audio query   3 = AI response
            //  7 = follow-up questions block   11 = user image query
            // API returns newest-first; reverse section order while keeping items
            // within each section in original order (user query before AI response).
            let orderedItems = reverseHistorySections(response.data)

            // Build message list in a loop so type_7 can mutate the preceding AI entry.
            // message_type_id: 1=user text, 2=user audio, 3=AI response,
            //                  7=follow-up questions block, 11=user image
            var mapped: [ChatMessage] = []
            var lastAiFollowUpIds: [String]? = nil

            for item in orderedItems {
                switch item.messageTypeId {
                case 1, 2: // user text / audio query
                    let text = item.queryText ?? item.heardQueryText ?? ""
                    if !text.isEmpty {
                        mapped.append(.userMessage(
                            id: "user-\(item.messageId)",
                            text: text,
                            imageUrl: nil,
                            audioUrl: nil
                        ))
                    }
                case 3: // AI text response — follow-up questions arrive via a separate type_7 item
                    if let responseText = item.responseText, !responseText.isEmpty {
                        mapped.append(.aiResponse(
                            id: "ai-\(item.messageId)",
                            text: responseText,
                            followUpQuestions: nil,
                            isPreGenerated: false,
                            messageId: item.messageId
                        ))
                    }
                case 7: // follow-up questions block — attach to the preceding AI response
                    guard let qs = item.questions, !qs.isEmpty else { break }
                    let texts = qs.map { $0.question }
                    let ids = qs.map { $0.followUpQuestionId }
                    if let lastAiIdx = mapped.indices.last(where: { mapped[$0].isAiResponse }) {
                        mapped[lastAiIdx] = mapped[lastAiIdx].withFollowUpQuestions(texts)
                    }
                    lastAiFollowUpIds = ids
                case 11: // user image query
                    let text = item.queryText ?? ""
                    mapped.append(.userMessage(
                        id: "user-\(item.messageId)",
                        text: text,
                        imageUrl: item.queryMediaFileUrl.flatMap { URL(string: $0) },
                        audioUrl: nil
                    ))
                default:
                    break
                }
            }

            // Set follow-up suggestions from the most recent AI response
            if let lastAi = mapped.last(where: { $0.isAiResponse }),
               let fup = lastAi.aiFollowUpQuestions, !fup.isEmpty {
                state.suggestedQuestions = fup
                state.suggestedQuestionIds = lastAiFollowUpIds
            }

            if page == 1 {
                state.messages = mapped
            } else {
                state.messages = mapped + state.messages
            }

            self.conversationId = conversationId
            state.conversationId = conversationId
            state.isInitialHistoryLoaded = true

            // If a full page (20 items) returned, assume more pages exist
            state.historyNextPage = response.data.count >= 20 ? page + 1 : nil

        } catch {
            state.errorMessage = (error as? NetworkError)?.errorDescription ?? error.localizedDescription
        }
        state.isLoading = false
    }

    // MARK: – History Helpers

    /// API returns sections newest-first. Each section is [user_query, ai_response, follow_ups].
    /// Reverses section order while keeping items within each section in their original order.
    private func reverseHistorySections(
        _ items: [ChatHistoryItem]
    ) -> [ChatHistoryItem] {
        var sections: [[ChatHistoryItem]] = []
        var current: [ChatHistoryItem] = []
        for item in items {
            // User message types (1=text, 2=audio, 11=image) start a new section
            let startsSection = [1, 2, 11].contains(item.messageTypeId)
            if startsSection && !current.isEmpty {
                sections.append(current)
                current = []
            }
            current.append(item)
        }
        if !current.isEmpty { sections.append(current) }
        return sections.reversed().flatMap { $0 }
    }

    // MARK: – Audio Synthesis

    private func performSynthesiseAudio() async {
        // Find the last AI message with a messageId
        guard let lastAI = state.messages.last(where: { $0.aiMessageId != nil }),
              let messageId = lastAI.aiMessageId,
              let text = lastAI.aiResponseText else { return }

        state.isLoadingSynthesiseAudio = true

        do {
            let request = SynthesiseAudioRequest(
                messageId: messageId,
                text: text,
                userId: userId
            )
            let response = try await apiClient.synthesiseAudio(request)

            if let urlString = response.audioUrl, let url = URL(string: urlString) {
                state.audioPlaybackUrl = url
            }
        } catch {
            state.errorMessage = (error as? NetworkError)?.errorDescription ?? error.localizedDescription
        }

        state.isLoadingSynthesiseAudio = false
    }

    // MARK: – Conversation Management

    private func ensureConversation() async throws -> String {
        if let existing = conversationId, !existing.isEmpty {
            return existing
        }
        // Ensure guest session is initialised (calls initialize_user if no tokens are stored).
        // Throws with a user-facing message on device limit or network failure.
        try await FarmerChatSDK.shared.ensureTokens()

        let response = try await apiClient.createNewConversation(
            userId: userId,
            contentProviderId: contentProviderId
        )
        conversationId = response.conversationId
        state.conversationId = response.conversationId
        return response.conversationId
    }

    // MARK: – Private Helpers

    private func replaceLoading(id: String, with newMessage: ChatMessage) {
        if let idx = state.messages.firstIndex(where: { $0.id == id }) {
            state.messages[idx] = newMessage
        } else {
            state.messages.append(newMessage)
        }
    }

    private func handleError(_ error: Error, loadingId: String, failedAction: ChatAction) {
        // Remove the loading placeholder
        state.messages.removeAll { $0.id == loadingId }

        let message: String
        if let networkError = error as? NetworkError {
            message = networkError.errorDescription ?? "An error occurred."
        } else {
            message = error.localizedDescription
        }

        state.errorMessage = message
        state.failedMessageId = loadingId
        lastFailedAction = failedAction
    }
}
