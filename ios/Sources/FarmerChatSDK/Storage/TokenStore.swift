import Foundation

// MARK: - TokenStore

/// Protocol for reading and writing auth tokens and identity data.
public protocol TokenStore: AnyObject {
    func getAccessToken() -> String?
    func getRefreshToken() -> String?
    func getUserId() -> String?
    func getDeviceId() -> String?

    func saveTokens(accessToken: String?, refreshToken: String?)
    func saveUserId(_ userId: String)
    func saveDeviceId(_ deviceId: String)

    /// Returns true if both access and refresh tokens are present.
    func hasTokens() -> Bool

    func clear()
}
