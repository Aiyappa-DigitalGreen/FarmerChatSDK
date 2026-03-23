import Foundation
#if canImport(UIKit)
import UIKit
#endif

// MARK: - FarmerChatConfig

/// Immutable configuration object passed to the SDK at startup.
///
/// - Parameter sdkApiKey: Your SDK API key from the FarmerChat developer portal
///   (format: `fc_live_xxx` or `fc_test_xxx`). Sent as `X-SDK-Key` on every API request.
public struct FarmerChatConfig {
    public let sdkApiKey: String
    public let baseUrl: String
    public let contentProviderId: String?
    public let conversationId: String?

    public init(
        sdkApiKey: String,
        baseUrl: String,
        contentProviderId: String? = nil,
        conversationId: String? = nil
    ) {
        self.sdkApiKey = sdkApiKey
        self.baseUrl = baseUrl
        self.contentProviderId = contentProviderId
        self.conversationId = conversationId
    }
}

// MARK: - SDKError

public enum SDKError: LocalizedError {
    case notConfigured
    case invalidBaseUrl(String)
    case invalidApiKey(String)

    public var errorDescription: String? {
        switch self {
        case .notConfigured:
            return "FarmerChatSDK has not been configured. Call FarmerChatSDK.shared.configure(_:) first."
        case .invalidBaseUrl(let url):
            return "The base URL '\(url)' is not a valid HTTP/HTTPS URL."
        case .invalidApiKey(let key):
            return "Invalid SDK API key '\(key.prefix(12))...'. " +
                   "Keys must start with 'fc_live_' or 'fc_test_' followed by at least 16 alphanumeric characters. " +
                   "Obtain your key from the FarmerChat developer portal."
        }
    }
}

// MARK: - FarmerChatSDK

/// Singleton entry point for the FarmerChat SDK.
@MainActor
public final class FarmerChatSDK {

    // MARK: Public

    public static let shared = FarmerChatSDK()

    /// The active configuration, available after `configure(_:)` is called.
    public private(set) var configuration: FarmerChatConfig?

    // MARK: Internal helpers (visible within SDK)

    internal let tokenStore: KeychainTokenStore = KeychainTokenStore()

    // MARK: Init

    private init() {}

    // MARK: Public Methods

    /// Configure the SDK. Must be called before presenting any SDK UI (e.g. in App.init or AppDelegate).
    ///
    /// Tokens are obtained automatically from the backend using the device's `identifierForVendor`.
    /// The network call happens once per fresh install; subsequent launches use the stored tokens.
    ///
    /// - Parameter config: The configuration to apply. Only `baseUrl` is required.
    /// - Throws: `SDKError.invalidBaseUrl` if the URL is malformed.
    public func configure(_ config: FarmerChatConfig) throws {
        // Validate SDK API key format: fc_live_<16+ alphanum> or fc_test_<16+ alphanum>
        guard Self.isValidApiKey(config.sdkApiKey) else {
            throw SDKError.invalidApiKey(config.sdkApiKey)
        }

        let trimmed = config.baseUrl.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty,
              let url = URL(string: trimmed),
              let scheme = url.scheme,
              scheme == "http" || scheme == "https" else {
            throw SDKError.invalidBaseUrl(config.baseUrl)
        }

        configuration = config

        // Use a stable device ID. Prefer what's already saved (survives configure() being
        // called more than once). Fall back to identifierForVendor, then a one-time UUID.
        let deviceId: String
        if let saved = tokenStore.getDeviceId() {
            deviceId = saved
        } else {
            #if canImport(UIKit)
            deviceId = UIDevice.current.identifierForVendor?.uuidString ?? UUID().uuidString
            #else
            deviceId = UUID().uuidString
            #endif
            tokenStore.saveDeviceId(deviceId)
        }

        // Skip the network call if tokens from a previous session are already in Keychain
        guard !tokenStore.hasTokens() else { return }

        Task.detached(priority: .userInitiated) { [tokenStore] in
            do {
                let response = try await GuestAPIClient.initializeUser(
                    baseUrl: config.baseUrl,
                    deviceId: deviceId
                )
                tokenStore.saveTokens(
                    accessToken: response.access_token,
                    refreshToken: response.refresh_token
                )
                if let userId = response.user_id {
                    tokenStore.saveUserId(userId)
                }
            } catch {
                // Tokens will be fetched lazily by ensureTokens() when chat is first opened
            }
        }
    }

    /// Ensures tokens are available, fetching them via initialize_user if needed.
    /// Throws an error with a user-facing message if initialisation fails, so callers
    /// (ChatViewModel) can show a meaningful error instead of a cryptic API failure.
    internal func ensureTokens() async throws {
        guard let config = configuration else { return }
        guard !tokenStore.hasTokens() else { return }

        guard let deviceId = tokenStore.getDeviceId() else {
            throw NSError(domain: "FarmerChatSDK", code: -1,
                          userInfo: [NSLocalizedDescriptionKey: "Unable to start a chat session. Please try again."])
        }

        do {
            let response = try await GuestAPIClient.initializeUser(
                baseUrl: config.baseUrl,
                deviceId: deviceId
            )
            tokenStore.saveTokens(
                accessToken: response.access_token,
                refreshToken: response.refresh_token
            )
            if let userId = response.user_id {
                tokenStore.saveUserId(userId)
            }
        } catch {
            // Re-check: a concurrent call may have already saved tokens
            if tokenStore.hasTokens() { return }
            throw NSError(domain: "FarmerChatSDK", code: -2,
                          userInfo: [NSLocalizedDescriptionKey: buildAuthErrorMessage(error)])
        }
    }

    private func buildAuthErrorMessage(_ error: Error) -> String {
        let msg = error.localizedDescription
        if msg.contains("401") || msg.contains("403") || msg.contains("device") || msg.contains("limit") {
            return "This device has reached its guest account limit. Please contact support."
        }
        if msg.contains("400") || msg.contains("4") {
            return "Unable to start a chat session. Please contact support if this persists."
        }
        return "Please check your internet connection and try again."
    }

    // MARK: Internal Accessors

    /// Resolved base URL from active configuration.
    internal var resolvedBaseUrl: String {
        configuration?.baseUrl ?? ""
    }

    // MARK: Reset

    /// Clear all stored tokens and user data. Forces a fresh initialize_user call
    /// on the next configure (or next chat open).
    public func clearSession() {
        tokenStore.clear()
    }

    // MARK: Key Validation

    /// Returns true if the key matches: fc_live_<16+ alphanum> or fc_test_<16+ alphanum>
    static func isValidApiKey(_ key: String) -> Bool {
        let prefix: String
        if key.hasPrefix("fc_live_") { prefix = "fc_live_" }
        else if key.hasPrefix("fc_test_") { prefix = "fc_test_" }
        else { return false }

        let suffix = key.dropFirst(prefix.count)
        guard suffix.count >= 16 else { return false }
        return suffix.allSatisfy { $0.isLetter || $0.isNumber }
    }
}
