import Foundation

// MARK: - NetworkError

/// Errors surfaced from the network layer.
public enum NetworkError: LocalizedError, Equatable {
    case unauthorized
    case serverError(Int, String?)
    case decodingError(String)
    case networkUnavailable
    case invalidURL
    case requestEncodingFailed(String)
    case unknown(String)

    public var errorDescription: String? {
        switch self {
        case .unauthorized:
            return "Authentication failed. Please log in again."
        case .serverError(let code, let message):
            if let message {
                return "Server error \(code): \(message)"
            }
            return "Server error \(code)."
        case .decodingError(let detail):
            return "Failed to decode server response: \(detail)"
        case .networkUnavailable:
            return "No internet connection. Please check your network settings."
        case .invalidURL:
            return "The request URL is invalid."
        case .requestEncodingFailed(let detail):
            return "Failed to encode request: \(detail)"
        case .unknown(let detail):
            return "An unexpected error occurred: \(detail)"
        }
    }

    // MARK: Equatable

    public static func == (lhs: NetworkError, rhs: NetworkError) -> Bool {
        switch (lhs, rhs) {
        case (.unauthorized, .unauthorized):
            return true
        case (.serverError(let lc, let lm), .serverError(let rc, let rm)):
            return lc == rc && lm == rm
        case (.decodingError(let l), .decodingError(let r)):
            return l == r
        case (.networkUnavailable, .networkUnavailable):
            return true
        case (.invalidURL, .invalidURL):
            return true
        case (.requestEncodingFailed(let l), .requestEncodingFailed(let r)):
            return l == r
        case (.unknown(let l), .unknown(let r)):
            return l == r
        default:
            return false
        }
    }
}

// MARK: - ServerErrorBody

/// Generic error body returned by the backend.
struct ServerErrorBody: Decodable {
    let detail: String?
    let message: String?
    let error: String?

    var resolvedMessage: String? {
        detail ?? message ?? error
    }
}
