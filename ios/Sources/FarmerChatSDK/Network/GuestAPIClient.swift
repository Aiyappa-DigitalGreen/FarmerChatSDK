import Foundation

// MARK: - GuestAPIClient

/// Standalone URLSession client for guest user initialization.
/// Operates independently of the main authenticated API client — no tokens required.
internal enum GuestAPIClient {

    private static let apiKey = "Y2K3kW5R9uQ0fL2X8zI7hT3aJ7"

    /// POST api/user/initialize_user/
    /// Obtains access_token, refresh_token, and user_id for a device on first install.
    static func initializeUser(
        baseUrl: String,
        deviceId: String
    ) async throws -> InitializeGuestUserResponse {
        let base = baseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        let urlString = base + "/" + APIEndpoints.initializeUser

        guard let url = URL(string: urlString) else {
            throw URLError(.badURL)
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Api-Key \(apiKey)", forHTTPHeaderField: "Authorization")
        request.timeoutInterval = 30

        let encoder = JSONEncoder()
        request.httpBody = try encoder.encode(InitializeGuestUserRequest(device_id: deviceId))

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw URLError(.badServerResponse)
        }
        guard httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }

        return try JSONDecoder().decode(InitializeGuestUserResponse.self, from: data)
    }
}
