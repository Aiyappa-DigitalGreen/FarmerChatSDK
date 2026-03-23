import Foundation

// MARK: - URL Extensions

public extension URL {

    /// Returns true if this URL uses the `https` scheme.
    var isSecure: Bool {
        scheme?.lowercased() == "https"
    }

    /// Append a path component and optional query items to this URL.
    ///
    /// - Parameters:
    ///   - path: Path component to append (without leading slash).
    ///   - queryItems: Optional query parameters.
    /// - Returns: New URL, or nil if construction fails.
    func appending(path: String, queryItems: [URLQueryItem]? = nil) -> URL? {
        var components = URLComponents(url: self.appendingPathComponent(path), resolvingAgainstBaseURL: true)
        if let queryItems, !queryItems.isEmpty {
            components?.queryItems = queryItems
        }
        return components?.url
    }

    /// Safely initialise a URL from a string, returning nil if the string is empty or invalid.
    static func safe(string: String) -> URL? {
        let trimmed = string.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return nil }
        return URL(string: trimmed)
    }
}

// MARK: - String Extensions (URL helpers)

public extension String {

    /// Attempt to create a URL from this string.
    var asURL: URL? {
        URL.safe(string: self)
    }

    /// Percent-encode this string using `.urlQueryAllowed` character set.
    var urlEncoded: String {
        addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? self
    }
}
