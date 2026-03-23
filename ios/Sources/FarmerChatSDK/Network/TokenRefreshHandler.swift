import Foundation

// MARK: - TokenRefreshHandler

/// Thread-safe actor that ensures only one token refresh runs at a time.
/// Additional callers are queued and receive the same result.
actor TokenRefreshHandler {

    // MARK: Private State

    private var isRefreshing = false
    private var waiters: [CheckedContinuation<String, Error>] = []

    // MARK: Public API

    /// Refresh the access token.  If a refresh is already in-flight, this
    /// method suspends until the existing refresh completes and then returns
    /// the same new token to every caller.
    ///
    /// - Parameters:
    ///   - baseUrl: Backend base URL.
    ///   - tokenStore: Store to read/write tokens.
    /// - Returns: The new access token.
    func refreshToken(baseUrl: String, tokenStore: any TokenStore) async throws -> String {
        // Queue behind an in-progress refresh
        if isRefreshing {
            return try await withCheckedThrowingContinuation { continuation in
                waiters.append(continuation)
            }
        }

        isRefreshing = true

        do {
            let newToken = try await performRefresh(baseUrl: baseUrl, tokenStore: tokenStore)
            resumeWaiters(with: .success(newToken))
            isRefreshing = false
            return newToken
        } catch {
            resumeWaiters(with: .failure(error))
            isRefreshing = false
            throw error
        }
    }

    // MARK: Private

    private func resumeWaiters(with result: Result<String, Error>) {
        let captured = waiters
        waiters.removeAll()
        for continuation in captured {
            continuation.resume(with: result)
        }
    }

    private func performRefresh(baseUrl: String, tokenStore: any TokenStore) async throws -> String {
        // Step 1: Try normal token refresh
        do {
            return try await attemptRefreshToken(baseUrl: baseUrl, tokenStore: tokenStore)
        } catch {
            // Step 2: Fall back to send_tokens (guest token acquisition)
            return try await attemptSendTokens(baseUrl: baseUrl, tokenStore: tokenStore)
        }
    }

    // MARK: Step 1 – /api/user/get_new_access_token/

    private func attemptRefreshToken(baseUrl: String, tokenStore: any TokenStore) async throws -> String {
        guard let refreshToken = tokenStore.getRefreshToken(), !refreshToken.isEmpty else {
            throw NetworkError.unauthorized
        }

        let urlString = baseUrl.trimmingCharacters(in: .init(charactersIn: "/"))
            + "/" + APIEndpoints.tokenRefresh

        guard let url = URL(string: urlString) else { throw NetworkError.invalidURL }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let body = ["refresh_token": refreshToken]
        request.httpBody = try JSONEncoder().encode(body)

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.unknown("No HTTP response from token refresh")
        }

        guard httpResponse.statusCode == 200 else {
            throw NetworkError.serverError(httpResponse.statusCode, "Token refresh failed")
        }

        let tokenResponse = try JSONDecoder().decode(TokenResponse.self, from: data)

        guard let newAccess = tokenResponse.accessToken, !newAccess.isEmpty else {
            throw NetworkError.unknown("Empty access token in refresh response")
        }

        tokenStore.saveTokens(
            accessToken: newAccess,
            refreshToken: tokenResponse.refreshToken ?? refreshToken
        )
        return newAccess
    }

    // MARK: Step 2 – /api/user/send_tokens/

    private static let guestApiKey = "Y2K3kW5R9uQ0fL2X8zI7hT3aJ7"

    private func attemptSendTokens(baseUrl: String, tokenStore: any TokenStore) async throws -> String {
        guard let deviceId = tokenStore.getDeviceId(), !deviceId.isEmpty else {
            throw NetworkError.unauthorized
        }
        guard let userId = tokenStore.getUserId(), !userId.isEmpty else {
            throw NetworkError.unauthorized
        }

        let urlString = baseUrl.trimmingCharacters(in: .init(charactersIn: "/"))
            + "/" + APIEndpoints.sendTokens

        guard let url = URL(string: urlString) else { throw NetworkError.invalidURL }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue(TokenRefreshHandler.guestApiKey, forHTTPHeaderField: "API-Key")

        let body: [String: String] = ["device_id": deviceId, "user_id": userId]
        request.httpBody = try JSONEncoder().encode(body)

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.unknown("No HTTP response from send_tokens")
        }

        guard httpResponse.statusCode == 200 else {
            throw NetworkError.unauthorized
        }

        let tokenResponse = try JSONDecoder().decode(TokenResponse.self, from: data)

        guard let newAccess = tokenResponse.accessToken, !newAccess.isEmpty else {
            throw NetworkError.unauthorized
        }

        tokenStore.saveTokens(
            accessToken: newAccess,
            refreshToken: tokenResponse.refreshToken
        )
        return newAccess
    }
}
