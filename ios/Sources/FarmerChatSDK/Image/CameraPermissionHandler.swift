import AVFoundation
import UIKit

// MARK: - CameraPermissionHandler

/// Checks and requests camera (AVCapture) permission.
enum CameraPermissionHandler {

    // MARK: Status

    enum PermissionStatus {
        case granted
        case denied
        case restricted
        case notDetermined
    }

    /// Returns the current camera authorisation status.
    static var status: PermissionStatus {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:     return .granted
        case .denied:         return .denied
        case .restricted:     return .restricted
        case .notDetermined:  return .notDetermined
        @unknown default:     return .notDetermined
        }
    }

    // MARK: Request

    /// Request camera permission if not yet determined.
    /// - Returns: `true` if the user grants permission.
    @discardableResult
    static func requestIfNeeded() async -> Bool {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            return true
        case .notDetermined:
            return await AVCaptureDevice.requestAccess(for: .video)
        default:
            return false
        }
    }

    // MARK: Open Settings

    /// Open the app's Settings page (used when permission is denied).
    static func openSettings() {
        guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
        if UIApplication.shared.canOpenURL(url) {
            UIApplication.shared.open(url)
        }
    }
}
