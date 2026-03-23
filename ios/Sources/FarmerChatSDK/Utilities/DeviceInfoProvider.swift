import Foundation
import UIKit
import Darwin

// MARK: - DeviceInfoProvider

/// Builds the URL-encoded `Device-Info` header value expected by the backend.
struct DeviceInfoProvider {

    /// Build and URL-encode the Device-Info JSON object.
    ///
    /// Backend expects a URL-encoded JSON string like:
    /// `%7B%22Build-Version%22%3A%22v2%22%2C%22manufacturer%22%3A%22Apple%22%2C...%7D`
    ///
    /// - Parameter deviceId: The stable device identifier.
    /// - Returns: URL-percent-encoded JSON string, or empty string on failure.
    static func buildHeader(deviceId: String) -> String {
        let info: [String: String] = [
            "Build-Version": "v2",
            "manufacturer":  "Apple",
            "model":         deviceModel(),
            "os_version":    systemVersion(),
            "device_id":     deviceId
        ]

        guard let jsonData = try? JSONSerialization.data(
            withJSONObject: info,
            options: [.sortedKeys]
        ),
              let jsonString = String(data: jsonData, encoding: .utf8) else {
            return ""
        }

        return jsonString.addingPercentEncoding(
            withAllowedCharacters: .urlQueryAllowed
        ) ?? jsonString
    }

    // MARK: Private

    private static func deviceModel() -> String {
        // Use sysctl to get the hardware model string (e.g. "iPhone14,3")
        var size = 0
        sysctlbyname("hw.machine", nil, &size, nil, 0)
        var machine = [CChar](repeating: 0, count: size)
        sysctlbyname("hw.machine", &machine, &size, nil, 0)
        let model = String(cString: machine)
        return model.isEmpty ? UIDevice.current.model : model
    }

    private static func systemVersion() -> String {
        UIDevice.current.systemVersion
    }
}
