import SwiftUI

// MARK: - ChatHistoryScreen

struct ChatHistoryScreen: View {

    @ObservedObject var viewModel: ChatHistoryViewModel
    let onSelect: (String) -> Void
    let onBack: () -> Void

    // MARK: Body

    var body: some View {
        VStack(spacing: 0) {
            historyNavigationBar
            Divider()

            if viewModel.isLoading && viewModel.items.isEmpty {
                Spacer()
                ProgressView()
                    .tint(SDKColors.primary)
                Spacer()
            } else if let error = viewModel.error, viewModel.items.isEmpty {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.largeTitle)
                        .foregroundColor(SDKColors.error)
                    Text(error)
                        .font(.subheadline)
                        .multilineTextAlignment(.center)
                        .foregroundColor(.secondary)
                        .padding(.horizontal)
                    Button("Retry") {
                        Task { await viewModel.refresh() }
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(SDKColors.primary)
                }
                Spacer()
            } else if viewModel.items.isEmpty {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "bubble.left.and.bubble.right")
                        .font(.largeTitle)
                        .foregroundColor(.secondary)
                    Text("No conversations yet.")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                Spacer()
            } else {
                conversationList
            }
        }
        .onAppear {
            if viewModel.items.isEmpty {
                Task { await viewModel.refresh() }
            }
        }
    }

    // MARK: – Navigation Bar

    private var historyNavigationBar: some View {
        HStack {
            Button(action: onBack) {
                Image(systemName: "chevron.left")
                    .font(.body.weight(.semibold))
                    .foregroundColor(.primary)
                    .frame(width: 44, height: 44)
            }

            Spacer()

            Text("Conversations")
                .font(.headline)
                .fontWeight(.semibold)

            Spacer()

            // Balance spacer
            Color.clear.frame(width: 44, height: 44)
        }
        .padding(.horizontal, 4)
        .frame(height: 56)
        .background(Color(.systemBackground))
    }

    // MARK: – List

    private var conversationList: some View {
        List {
            ForEach(viewModel.items) { item in
                ConversationRowView(item: item)
                    .contentShape(Rectangle())
                    .onTapGesture {
                        if let convId = item.conversationId {
                            onSelect(convId)
                        }
                    }
                    .listRowInsets(EdgeInsets(top: 0, leading: 16, bottom: 0, trailing: 16))
                    .listRowSeparator(.hidden)
                    .padding(.vertical, 6)
            }

            if viewModel.hasMore {
                HStack {
                    Spacer()
                    if viewModel.isLoading {
                        ProgressView()
                            .tint(SDKColors.primary)
                    } else {
                        Button("Load More") {
                            Task { await viewModel.loadNextPage() }
                        }
                        .foregroundColor(SDKColors.primary)
                    }
                    Spacer()
                }
                .listRowSeparator(.hidden)
                .padding(.vertical, 8)
            }
        }
        .listStyle(.plain)
        .refreshable {
            await viewModel.refresh()
        }
    }
}

// MARK: - ConversationRowView

private struct ConversationRowView: View {
    let item: ConversationListItem

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(item.conversationTitle ?? "Conversation")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
                    .lineLimit(1)

                Spacer()

                if let date = item.createdOn {
                    Text(formattedDate(date))
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }

            if let type = item.messageType {
                Text(type.capitalized)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }
        }
        .padding(12)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(10)
    }

    private func formattedDate(_ dateString: String) -> String {
        let isoFormatter = ISO8601DateFormatter()
        isoFormatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]

        guard let date = isoFormatter.date(from: dateString) else { return "" }

        let formatter = DateFormatter()
        if Calendar.current.isDateInToday(date) {
            formatter.dateFormat = "h:mm a"
        } else {
            formatter.dateFormat = "MMM d"
        }
        return formatter.string(from: date)
    }
}
