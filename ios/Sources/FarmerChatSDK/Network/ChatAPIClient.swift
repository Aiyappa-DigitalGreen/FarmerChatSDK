import Foundation

// MARK: - ChatAPIClient

/// The central networking class. All 9 chat API calls live here.
/// Handles automatic token refresh on 401 (one retry per request).
final class ChatAPIClient {

    // MARK: Dependencies

    private let baseUrl: String
    private let tokenStore: any TokenStore
    private let authInterceptor: AuthInterceptor
    private let tokenRefreshHandler: TokenRefreshHandler
    private let session: URLSession
    private let jsonDecoder: JSONDecoder

    // MARK: Init

    init(
        baseUrl: String,
        tokenStore: any TokenStore,
        session: URLSession = .shared,
        tokenRefreshHandler: TokenRefreshHandler = TokenRefreshHandler()
    ) {
        self.baseUrl = baseUrl.trimmingCharacters(in: .init(charactersIn: "/"))
        self.tokenStore = tokenStore
        self.authInterceptor = AuthInterceptor(tokenStore: tokenStore)
        self.tokenRefreshHandler = tokenRefreshHandler
        self.session = session

        let decoder = JSONDecoder()
        decoder.keyDecodingStrategy = .convertFromSnakeCase
        self.jsonDecoder = decoder
    }

    // MARK: - Chat API Calls

    func createNewConversation(
        userId: String,
        contentProviderId: String?
    ) async throws -> NewConversationResponse {
        let request = NewConversationRequest(userId: userId, contentProviderId: contentProviderId)
        let urlRequest = try makeRequest(path: APIEndpoints.newConversation, method: "POST", body: request)
        return try await perform(urlRequest)
    }

    func sendTextPrompt(_ request: TextPromptRequest) async throws -> TextPromptResponse {
        let urlRequest = try makeRequest(path: APIEndpoints.getTextPrompt, method: "POST", body: request)
        return try await perform(urlRequest)
    }

    func sendImageAnalysis(_ request: ImageAnalysisRequest) async throws -> ImageAnalysisResponse {
        let urlRequest = try makeRequest(path: APIEndpoints.imageAnalysis, method: "POST", body: request)
        return try await perform(urlRequest)
    }

    func fetchFollowUpQuestions(
        messageId: String,
        useLatestPrompt: Bool = true
    ) async throws -> FollowUpQuestionsResponse {
        let queryItems = [
            URLQueryItem(name: "message_id", value: messageId),
            URLQueryItem(name: "use_latest_prompt", value: useLatestPrompt ? "true" : "false")
        ]
        let urlRequest = try makeRequest(
            path: APIEndpoints.followUpQuestions,
            method: "GET",
            queryItems: queryItems
        )
        return try await perform(urlRequest)
    }

    func trackFollowUpClick(followUpQuestion: String) async throws {
        let body = FollowUpClickRequest(followUpQuestion: followUpQuestion)
        let urlRequest = try makeRequest(path: APIEndpoints.followUpClick, method: "POST", body: body)
        let _: EmptyResponse = try await perform(urlRequest)
    }

    func synthesiseAudio(_ request: SynthesiseAudioRequest) async throws -> SynthesiseAudioResponse {
        let urlRequest = try makeRequest(path: APIEndpoints.synthesiseAudio, method: "POST", body: request)
        return try await perform(urlRequest)
    }

    func fetchChatHistory(
        conversationId: String,
        page: Int
    ) async throws -> ConversationChatHistoryResponse {
        let queryItems = [
            URLQueryItem(name: "conversation_id", value: conversationId),
            URLQueryItem(name: "page", value: String(page))
        ]
        let urlRequest = try makeRequest(
            path: APIEndpoints.chatHistory,
            method: "GET",
            queryItems: queryItems
        )
        return try await perform(urlRequest)
    }

    // API returns a plain JSON array — decode as [ConversationListItem] directly.
    func fetchConversationList(
        userId: String,
        page: Int
    ) async throws -> [ConversationListItem] {
        let queryItems = [
            URLQueryItem(name: "user_id", value: userId),
            URLQueryItem(name: "page", value: String(page))
        ]
        let urlRequest = try makeRequest(
            path: APIEndpoints.conversationList,
            method: "GET",
            queryItems: queryItems
        )
        return try await perform(urlRequest)
    }

    func transcribeAudio(_ request: TranscribeAudioRequest) async throws -> TranscribeAudioResponse {
        let urlRequest = try makeMultipartRequest(audioRequest: request)
        return try await perform(urlRequest)
    }

    // MARK: - Private: Request Building

    private func makeRequest(
        path: String,
        method: String,
        body: (any Encodable)? = nil,
        queryItems: [URLQueryItem]? = nil
    ) throws -> URLRequest {
        var components = URLComponents(string: baseUrl + "/" + path)

        if let queryItems, !queryItems.isEmpty {
            components?.queryItems = queryItems
        }

        guard let url = components?.url else { throw NetworkError.invalidURL }

        var urlRequest = URLRequest(url: url)
        urlRequest.httpMethod = method
        urlRequest.setValue("application/json", forHTTPHeaderField: "Content-Type")
        urlRequest.setValue("application/json", forHTTPHeaderField: "Accept")

        if let body {
            do {
                let encoder = JSONEncoder()
                encoder.keyEncodingStrategy = .convertToSnakeCase
                urlRequest.httpBody = try encoder.encode(body)
            } catch {
                throw NetworkError.requestEncodingFailed(error.localizedDescription)
            }
        }

        return authInterceptor.apply(to: urlRequest)
    }

    private func makeMultipartRequest(audioRequest: TranscribeAudioRequest) throws -> URLRequest {
        guard let url = URL(string: baseUrl + "/" + APIEndpoints.transcribeAudio) else {
            throw NetworkError.invalidURL
        }

        let boundary = "Boundary-\(UUID().uuidString)"
        var urlRequest = URLRequest(url: url)
        urlRequest.httpMethod = "POST"
        urlRequest.setValue(
            "multipart/form-data; boundary=\(boundary)",
            forHTTPHeaderField: "Content-Type"
        )

        urlRequest.httpBody = buildMultipartBody(request: audioRequest, boundary: boundary)
        return authInterceptor.apply(to: urlRequest)
    }

    private func buildMultipartBody(request: TranscribeAudioRequest, boundary: String) -> Data {
        var data = Data()
        let crlf = "\r\n"
        let dashdash = "--"

        func appendString(_ string: String) {
            if let encoded = string.data(using: .utf8) {
                data.append(encoded)
            }
        }

        // Audio file field
        appendString("\(dashdash)\(boundary)\(crlf)")
        appendString("Content-Disposition: form-data; name=\"audio_file\"; filename=\"recording.m4a\"\(crlf)")
        appendString("Content-Type: audio/m4a\(crlf)\(crlf)")
        data.append(request.audioData)
        appendString(crlf)

        // user_id field
        appendString("\(dashdash)\(boundary)\(crlf)")
        appendString("Content-Disposition: form-data; name=\"user_id\"\(crlf)\(crlf)")
        appendString(request.userId)
        appendString(crlf)

        // conversation_id field (optional)
        if let conversationId = request.conversationId {
            appendString("\(dashdash)\(boundary)\(crlf)")
            appendString("Content-Disposition: form-data; name=\"conversation_id\"\(crlf)\(crlf)")
            appendString(conversationId)
            appendString(crlf)
        }

        // language field (optional)
        if let language = request.language {
            appendString("\(dashdash)\(boundary)\(crlf)")
            appendString("Content-Disposition: form-data; name=\"language\"\(crlf)\(crlf)")
            appendString(language)
            appendString(crlf)
        }

        appendString("\(dashdash)\(boundary)\(dashdash)\(crlf)")
        return data
    }

    // MARK: - Private: Response Handling

    private func perform<T: Decodable>(_ urlRequest: URLRequest) async throws -> T {
        do {
            let (data, response) = try await session.data(for: urlRequest)
            return try handleResponse(data: data, response: response, originalRequest: urlRequest)
        } catch let networkError as NetworkError {
            // If 401, attempt token refresh and retry exactly once
            if networkError == .unauthorized {
                let newToken = try await tokenRefreshHandler.refreshToken(
                    baseUrl: baseUrl,
                    tokenStore: tokenStore
                )
                let retried = authInterceptor.applying(newToken: newToken, to: urlRequest)
                let (data, response) = try await session.data(for: retried)
                return try handleResponse(data: data, response: response, originalRequest: retried)
            }
            throw networkError
        } catch {
            // Map URLError to NetworkError
            let urlError = error as? URLError
            switch urlError?.code {
            case .notConnectedToInternet, .networkConnectionLost, .timedOut:
                throw NetworkError.networkUnavailable
            default:
                throw NetworkError.unknown(error.localizedDescription)
            }
        }
    }

    private func handleResponse<T: Decodable>(
        data: Data,
        response: URLResponse,
        originalRequest: URLRequest
    ) throws -> T {
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.unknown("No HTTP response received")
        }

        switch httpResponse.statusCode {
        case 200...299:
            // Success path – EmptyResponse is used for fire-and-forget endpoints
            if let empty = EmptyResponse() as? T {
                return empty
            }
            do {
                return try jsonDecoder.decode(T.self, from: data)
            } catch {
                let preview = String(data: data.prefix(200), encoding: .utf8) ?? "<non-utf8>"
                throw NetworkError.decodingError("\(error.localizedDescription). Body preview: \(preview)")
            }

        case 401:
            throw NetworkError.unauthorized

        case 400...499:
            let body = (try? JSONDecoder().decode(ServerErrorBody.self, from: data))?.resolvedMessage
            throw NetworkError.serverError(httpResponse.statusCode, body)

        case 500...599:
            let body = (try? JSONDecoder().decode(ServerErrorBody.self, from: data))?.resolvedMessage
            throw NetworkError.serverError(httpResponse.statusCode, body ?? "Internal server error")

        default:
            throw NetworkError.serverError(httpResponse.statusCode, nil)
        }
    }
}

// MARK: - EmptyResponse

/// Placeholder decodable used for endpoints that return no meaningful body.
private struct EmptyResponse: Decodable {}
