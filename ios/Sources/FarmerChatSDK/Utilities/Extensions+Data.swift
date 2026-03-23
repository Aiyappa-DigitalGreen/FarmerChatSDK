import Foundation

// MARK: - Data Extensions

public extension Data {

    /// Decode this Data as a UTF-8 string, returning nil on failure.
    var utf8String: String? {
        String(data: self, encoding: .utf8)
    }

    /// Base64-encode and return as a String.
    var base64String: String {
        base64EncodedString()
    }

    /// Decode as JSON into the given `Decodable` type.
    func decoded<T: Decodable>(
        as type: T.Type = T.self,
        decoder: JSONDecoder = JSONDecoder()
    ) throws -> T {
        try decoder.decode(type, from: self)
    }

    // MARK: Multipart helpers

    /// Append a UTF-8 string to this Data object.
    mutating func appendString(_ string: String) {
        if let data = string.data(using: .utf8) {
            append(data)
        }
    }
}
