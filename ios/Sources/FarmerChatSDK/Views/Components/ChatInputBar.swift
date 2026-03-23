import SwiftUI

// MARK: - ChatInputBar

/// Bottom input area with text field, image preview, camera, mic, and send buttons.
struct ChatInputBar: View {

    @Binding var text: String
    @Binding var selectedImage: UIImage?
    let isLoading: Bool
    let onSend: () -> Void
    let onCameraOpen: () -> Void
    let onMicOpen: () -> Void

    @FocusState private var isFocused: Bool

    var body: some View {
        VStack(spacing: 0) {
            Divider()

            // Image preview strip
            if let image = selectedImage {
                HStack {
                    Image(uiImage: image)
                        .resizable()
                        .scaledToFill()
                        .frame(width: 64, height: 64)
                        .clipped()
                        .cornerRadius(8)

                    Spacer()

                    Button {
                        selectedImage = nil
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.title3)
                            .foregroundColor(.secondary)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
            }

            HStack(alignment: .bottom, spacing: 8) {
                // Camera button
                Button(action: onCameraOpen) {
                    Image(systemName: "camera")
                        .font(.body)
                        .foregroundColor(isLoading ? .secondary : SDKColors.primary)
                        .frame(width: 36, height: 36)
                        .background(isLoading ? Color(.systemGray5) : SDKColors.primaryContainer)
                        .clipShape(Circle())
                }
                .disabled(isLoading)
                .accessibilityLabel("Attach photo")

                // Text field
                ZStack(alignment: .leading) {
                    if text.isEmpty {
                        Text("Ask anything about farming…")
                            .font(.body)
                            .foregroundColor(.secondary)
                            .padding(.horizontal, 12)
                    }
                    TextField("", text: $text, axis: .vertical)
                        .font(.body)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 8)
                        .lineLimit(1...5)
                        .focused($isFocused)
                        .submitLabel(.send)
                        .onSubmit {
                            if !text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                                onSend()
                            }
                        }
                }
                .background(Color(.secondarySystemBackground))
                .cornerRadius(20)

                // Mic or Send button
                if text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && selectedImage == nil {
                    // Mic
                    ListenButton(isListening: false, action: onMicOpen)
                        .disabled(isLoading)
                        .opacity(isLoading ? 0.4 : 1.0)
                } else {
                    // Send
                    Button(action: onSend) {
                        if isLoading {
                            ProgressView()
                                .tint(.white)
                                .frame(width: 36, height: 36)
                                .background(SDKColors.primary.opacity(0.6))
                                .clipShape(Circle())
                        } else {
                            Image(systemName: "arrow.up")
                                .font(.body.weight(.semibold))
                                .foregroundColor(.white)
                                .frame(width: 36, height: 36)
                                .background(SDKColors.primary)
                                .clipShape(Circle())
                        }
                    }
                    .disabled(isLoading)
                    .accessibilityLabel("Send message")
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
        }
        .background(Color(.systemBackground))
    }
}
