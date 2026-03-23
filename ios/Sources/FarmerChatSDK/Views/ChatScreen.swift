import SwiftUI
import PhotosUI

// MARK: - ChatScreen

struct ChatScreen: View {

    // MARK: Properties

    @ObservedObject var viewModel: ChatViewModel
    let onOpenHistory: () -> Void
    let onDismiss: () -> Void

    // MARK: Local State

    @State private var inputText = ""
    @State private var selectedImage: UIImage?
    @State private var isShowingVoiceOverlay = false
    @State private var isShowingPhotoPicker = false
    @State private var isShowingCamera = false
    @FocusState private var isTextFieldFocused: Bool
    @StateObject private var audioPlayer = AudioPlayerService()

    // MARK: Body

    var body: some View {
        VStack(spacing: 0) {
            chatNavigationBar
            Divider()

            ChatThreadView(
                messages: viewModel.state.messages,
                isLoading: viewModel.state.isLoading,
                errorMessage: viewModel.state.errorMessage,
                onRetry: { viewModel.dispatch(.retryLastRequest) },
                onFollowUpTapped: { question, id in
                    viewModel.dispatch(.sendFollowUpQuestion(
                        question: question,
                        followUpQuestionId: id,
                        transcriptionId: nil,
                        audioUrl: nil
                    ))
                },
                onListenTapped: { viewModel.dispatch(.synthesiseAudio) },
                audioPlaybackUrl: viewModel.state.audioPlaybackUrl,
                isAudioPlaying: viewModel.state.isAudioPlaying,
                onAudioPlayingChanged: { viewModel.dispatch(.setAudioPlaying($0)) },
                onLoadMore: loadMoreHistory
            )

            if !isShowingVoiceOverlay {
                if let questions = viewModel.state.suggestedQuestions, !questions.isEmpty {
                    FollowUpQuestionsBar(
                        questions: questions,
                        questionIds: viewModel.state.suggestedQuestionIds,
                        onTap: { question, id in
                            viewModel.dispatch(.sendFollowUpQuestion(
                                question: question,
                                followUpQuestionId: id,
                                transcriptionId: nil,
                                audioUrl: nil
                            ))
                        }
                    )
                }

                ChatInputBar(
                    text: $inputText,
                    selectedImage: $selectedImage,
                    isLoading: viewModel.state.isLoading,
                    onSend: sendMessage,
                    onCameraOpen: { isShowingPhotoPicker = true },
                    onMicOpen: { isShowingVoiceOverlay = true }
                )
            }
        }
        .overlay {
            if isShowingVoiceOverlay {
                VoiceInputOverlay(
                    onDismiss: { isShowingVoiceOverlay = false },
                    onAudioCaptured: { audioUrl, transcription, transcriptionId in
                        isShowingVoiceOverlay = false
                        guard !transcription.isEmpty else { return }
                        viewModel.dispatch(.initializeWithQuestion(
                            question: transcription,
                            transcriptionId: transcriptionId,
                            audioUrl: audioUrl
                        ))
                    }
                )
                .transition(.opacity)
            }
        }
        .sheet(isPresented: $isShowingPhotoPicker) {
            ImagePickerView(
                sourceType: .photoLibrary,
                onImageSelected: { image in
                    selectedImage = image
                    isShowingPhotoPicker = false
                }
            )
        }
        .sheet(isPresented: $isShowingCamera) {
            ImagePickerView(
                sourceType: .camera,
                onImageSelected: { image in
                    selectedImage = image
                    isShowingCamera = false
                }
            )
        }
        .onChange(of: viewModel.state.audioPlaybackUrl) { newUrl in
            if let url = newUrl {
                audioPlayer.play(url: url)
                viewModel.dispatch(.setAudioPlaying(true))
            }
        }
        .onChange(of: audioPlayer.isPlaying) { isPlaying in
            viewModel.dispatch(.setAudioPlaying(isPlaying))
            if !isPlaying {
                viewModel.dispatch(.clearAudioPlaybackUrl)
            }
        }
    }

    // MARK: – Navigation Bar

    private var chatNavigationBar: some View {
        HStack {
            Button(action: onDismiss) {
                Image(systemName: "xmark")
                    .font(.body.weight(.semibold))
                    .foregroundColor(.primary)
                    .frame(width: 44, height: 44)
            }

            Spacer()

            HStack(spacing: 6) {
                Image(systemName: "leaf.fill")
                    .foregroundColor(SDKColors.primary)
                Text("FarmerChat")
                    .font(.headline)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
            }

            Spacer()

            Button(action: onOpenHistory) {
                Image(systemName: "clock.arrow.circlepath")
                    .font(.body)
                    .foregroundColor(.primary)
                    .frame(width: 44, height: 44)
            }
        }
        .padding(.horizontal, 4)
        .frame(height: 56)
        .background(Color(.systemBackground))
    }

    // MARK: – Actions

    private func sendMessage() {
        let trimmed = inputText.trimmingCharacters(in: .whitespacesAndNewlines)

        if let image = selectedImage {
            guard !trimmed.isEmpty else { return }
            let data = ImageProcessor.compress(ImageProcessor.resize(image))
            inputText = ""
            selectedImage = nil
            isTextFieldFocused = false
            viewModel.dispatch(.sendQuestionWithImage(question: trimmed, imageData: data))
            return
        }

        guard !trimmed.isEmpty else { return }
        inputText = ""
        isTextFieldFocused = false
        viewModel.dispatch(.initializeWithQuestion(
            question: trimmed,
            transcriptionId: nil,
            audioUrl: nil
        ))
    }

    private func loadMoreHistory() {
        guard let convId = viewModel.state.conversationId,
              let nextPage = viewModel.state.historyNextPage else { return }
        viewModel.dispatch(.loadChatHistory(conversationId: convId, page: nextPage))
    }
}
