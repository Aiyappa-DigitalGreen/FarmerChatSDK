import Foundation
import Security

// MARK: - KeychainTokenStore

/// Persists tokens in the iOS Keychain using the Security framework directly.
final class KeychainTokenStore: TokenStore {

    // MARK: Constants

    private enum Keys {
        static let accessToken  = "com.farmerchat.sdk.access_token"
        static let refreshToken = "com.farmerchat.sdk.refresh_token"
        static let userId       = "com.farmerchat.sdk.user_id"
        static let deviceId     = "com.farmerchat.sdk.device_id"
    }

    private let service = "com.farmerchat.sdk"

    // MARK: TokenStore

    func getAccessToken() -> String? {
        read(key: Keys.accessToken)
    }

    func getRefreshToken() -> String? {
        read(key: Keys.refreshToken)
    }

    func getUserId() -> String? {
        read(key: Keys.userId)
    }

    func getDeviceId() -> String? {
        read(key: Keys.deviceId)
    }

    func saveTokens(accessToken: String?, refreshToken: String?) {
        if let accessToken {
            write(value: accessToken, key: Keys.accessToken)
        }
        if let refreshToken {
            write(value: refreshToken, key: Keys.refreshToken)
        }
    }

    func saveUserId(_ userId: String) {
        write(value: userId, key: Keys.userId)
    }

    func saveDeviceId(_ deviceId: String) {
        write(value: deviceId, key: Keys.deviceId)
    }

    func hasTokens() -> Bool {
        read(key: Keys.accessToken) != nil &&
        read(key: Keys.refreshToken) != nil &&
        read(key: Keys.userId) != nil
    }

    func clear() {
        delete(key: Keys.accessToken)
        delete(key: Keys.refreshToken)
        delete(key: Keys.userId)
        delete(key: Keys.deviceId)
    }

    // MARK: Private – Keychain Helpers

    private func write(value: String, key: String) {
        guard let data = value.data(using: .utf8) else { return }

        let query: [CFString: Any] = [
            kSecClass:            kSecClassGenericPassword,
            kSecAttrService:      service,
            kSecAttrAccount:      key,
            kSecAttrAccessible:   kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
            kSecValueData:        data
        ]

        // Try to update first
        let updateAttributes: [CFString: Any] = [
            kSecValueData:      data,
            kSecAttrAccessible: kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
        ]
        let updateStatus = SecItemUpdate(query as CFDictionary, updateAttributes as CFDictionary)

        if updateStatus == errSecItemNotFound {
            // Item doesn't exist — add it
            SecItemAdd(query as CFDictionary, nil)
        }
    }

    private func read(key: String) -> String? {
        let query: [CFString: Any] = [
            kSecClass:            kSecClassGenericPassword,
            kSecAttrService:      service,
            kSecAttrAccount:      key,
            kSecReturnData:       true,
            kSecMatchLimit:       kSecMatchLimitOne
        ]

        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)

        guard status == errSecSuccess,
              let data = result as? Data,
              let string = String(data: data, encoding: .utf8) else {
            return nil
        }
        return string
    }

    private func delete(key: String) {
        let query: [CFString: Any] = [
            kSecClass:       kSecClassGenericPassword,
            kSecAttrService: service,
            kSecAttrAccount: key
        ]
        SecItemDelete(query as CFDictionary)
    }
}
