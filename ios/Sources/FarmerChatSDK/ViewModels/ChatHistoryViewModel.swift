import Foundation
import SwiftUI

// MARK: - ChatHistoryViewModel

@MainActor
final class ChatHistoryViewModel: ObservableObject {

    // MARK: Published

    @Published private(set) var items: [ConversationListItem] = []
    @Published private(set) var isLoading = false
    @Published private(set) var error: String?
    @Published private(set) var hasMore = true

    // MARK: Private

    private var currentPage = 1
    private let apiClient: ChatAPIClient
    private let tokenStore: any TokenStore
    private var userId: String

    // MARK: Init

    init(
        apiClient: ChatAPIClient,
        tokenStore: any TokenStore,
        userId: String
    ) {
        self.apiClient = apiClient
        self.tokenStore = tokenStore
        self.userId = userId
    }

    // MARK: Public

    /// Reload from page 1, discarding existing items.
    func refresh() async {
        guard !isLoading else { return }
        currentPage = 1
        hasMore = true
        items = []
        await fetchPage(page: 1)
    }

    /// Load the next page of results (appends to existing list).
    func loadNextPage() async {
        guard !isLoading, hasMore else { return }
        await fetchPage(page: currentPage)
    }

    // MARK: Private

    private func fetchPage(page: Int) async {
        isLoading = true
        error = nil

        do {
            // API returns a plain array — no wrapper, no pagination metadata.
            let newItems = try await apiClient.fetchConversationList(
                userId: userId,
                page: page
            )

            if page == 1 {
                items = newItems
            } else {
                items.append(contentsOf: newItems)
            }

            // Empty page signals end of results.
            hasMore = !newItems.isEmpty
            if hasMore {
                currentPage = page + 1
            }

        } catch let networkError as NetworkError {
            error = networkError.errorDescription
        } catch {
            self.error = error.localizedDescription
        }

        isLoading = false
    }
}
