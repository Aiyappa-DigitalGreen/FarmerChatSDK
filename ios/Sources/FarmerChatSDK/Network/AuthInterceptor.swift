import Foundation

// MARK: - AuthInterceptor

/// Applies authentication headers to every outgoing request.
struct AuthInterceptor {

    private let tokenStore: any TokenStore

    init(tokenStore: any TokenStore) {
        self.tokenStore = tokenStore
    }

    /// Apply standard auth headers to a URLRequest.
    ///
    /// - Parameter request: The request to mutate.
    /// - Returns: The augmented request.
    func apply(to request: URLRequest) -> URLRequest {
        var mutableRequest = request

        // SDK API key — identifies this integration to the FarmerChat backend
        if let sdkApiKey = FarmerChatSDK.shared.configuration?.sdkApiKey, !sdkApiKey.isEmpty {
            mutableRequest.setValue(sdkApiKey, forHTTPHeaderField: "X-SDK-Key")
        }

        // Authorization header
        if let accessToken = tokenStore.getAccessToken(), !accessToken.isEmpty {
            mutableRequest.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        }

        // Build-Version header
        mutableRequest.setValue("v2", forHTTPHeaderField: "Build-Version")

        // Device-Info header
        if let deviceId = tokenStore.getDeviceId() {
            let deviceInfo = DeviceInfoProvider.buildHeader(deviceId: deviceId)
            mutableRequest.setValue(deviceInfo, forHTTPHeaderField: "Device-Info")
        }

        return mutableRequest
    }

    /// Produce a fresh copy of the request with an updated Authorization header
    /// using the new access token (used after a token refresh retry).
    ///
    /// - Parameters:
    ///   - request: Original request.
    ///   - newToken: Fresh access token.
    /// - Returns: Request with updated Authorization header.
    func applying(newToken: String, to request: URLRequest) -> URLRequest {
        var mutableRequest = request
        mutableRequest.setValue("Bearer \(newToken)", forHTTPHeaderField: "Authorization")
        return mutableRequest
    }
}
