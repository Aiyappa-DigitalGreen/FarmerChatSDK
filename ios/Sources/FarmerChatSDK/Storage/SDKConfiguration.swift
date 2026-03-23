import Foundation

// MARK: - SDKConfiguration

/// Internal accessor for the current SDK configuration.
/// Throws if the SDK has not been configured.
enum SDKConfiguration {

    /// Returns the active `FarmerChatConfig` or throws `SDKError.notConfigured`.
    @MainActor
    static func current() throws -> FarmerChatConfig {
        guard let config = FarmerChatSDK.shared.configuration else {
            throw SDKError.notConfigured
        }
        return config
    }

    /// Convenience: resolve the base URL from the active configuration.
    @MainActor
    static func baseUrl() throws -> String {
        try current().baseUrl
    }

    /// Build an `ChatAPIClient` from the active configuration.
    @MainActor
    static func makeAPIClient() throws -> ChatAPIClient {
        let config = try current()
        return ChatAPIClient(
            baseUrl: config.baseUrl,
            tokenStore: FarmerChatSDK.shared.tokenStore
        )
    }
}
