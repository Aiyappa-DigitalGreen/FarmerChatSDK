import SwiftUI

// MARK: - ChatContainerView

/// Root container that owns both the ChatViewModel and ChatHistoryViewModel
/// and manages navigation between the chat screen and history screen.
public struct ChatContainerView: View {

    // MARK: Properties

    private let conversationId: String?
    private let onDismiss: () -> Void

    // MARK: ViewModels

    @StateObject private var chatViewModel: ChatViewModel
    @StateObject private var historyViewModel: ChatHistoryViewModel

    // MARK: Navigation State

    @State private var showHistory = false

    // MARK: Init

    public init(conversationId: String? = nil, onDismiss: @escaping () -> Void) {
        self.conversationId = conversationId
        self.onDismiss = onDismiss

        // Build dependencies from the active SDK configuration
        let tokenStore = FarmerChatSDK.shared.tokenStore
        let baseUrl = FarmerChatSDK.shared.configuration?.baseUrl ?? ""
        let userId = tokenStore.getUserId() ?? ""
        let deviceId = tokenStore.getDeviceId() ?? ""
        let contentProviderId = FarmerChatSDK.shared.configuration?.contentProviderId
        let resolvedConversationId = conversationId
            ?? FarmerChatSDK.shared.configuration?.conversationId

        let apiClient = ChatAPIClient(baseUrl: baseUrl, tokenStore: tokenStore)

        _chatViewModel = StateObject(wrappedValue: ChatViewModel(
            apiClient: apiClient,
            tokenStore: tokenStore,
            userId: userId,
            deviceId: deviceId,
            contentProviderId: contentProviderId,
            existingConversationId: resolvedConversationId
        ))

        _historyViewModel = StateObject(wrappedValue: ChatHistoryViewModel(
            apiClient: apiClient,
            tokenStore: tokenStore,
            userId: userId
        ))
    }

    // MARK: Body

    public var body: some View {
        NavigationStack {
            Group {
                if showHistory {
                    ChatHistoryScreen(
                        viewModel: historyViewModel,
                        onSelect: { selectedConvId in
                            showHistory = false
                            chatViewModel.dispatch(.clearMessages)
                            chatViewModel.dispatch(.loadChatHistory(
                                conversationId: selectedConvId,
                                page: 1
                            ))
                        },
                        onBack: { showHistory = false }
                    )
                } else {
                    ChatScreen(
                        viewModel: chatViewModel,
                        onOpenHistory: {
                            Task { await historyViewModel.refresh() }
                            showHistory = true
                        },
                        onDismiss: onDismiss
                    )
                }
            }
            .navigationBarHidden(true)
        }
        .onAppear {
            // If an existing conversation was provided, load its history
            if let convId = conversationId ?? FarmerChatSDK.shared.configuration?.conversationId,
               !chatViewModel.state.isInitialHistoryLoaded {
                chatViewModel.dispatch(.loadChatHistory(conversationId: convId, page: 1))
            }
        }
    }
}
