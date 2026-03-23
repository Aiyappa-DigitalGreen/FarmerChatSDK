import UIKit
import ImageIO
import CoreLocation

// MARK: - ImageProcessor

/// Static utilities for resizing, compressing, encoding, and extracting
/// EXIF metadata from images.
struct ImageProcessor {

    // MARK: Resize

    /// Resize a UIImage so its longest dimension is at most `maxDimension` points.
    /// Returns the original image if it is already within bounds.
    static func resize(_ image: UIImage, maxDimension: CGFloat = 1024) -> UIImage {
        let size = image.size
        let longest = max(size.width, size.height)

        guard longest > maxDimension else { return image }

        let scale = maxDimension / longest
        let newSize = CGSize(width: size.width * scale, height: size.height * scale)

        let renderer = UIGraphicsImageRenderer(size: newSize)
        return renderer.image { _ in
            image.draw(in: CGRect(origin: .zero, size: newSize))
        }
    }

    // MARK: Compress

    /// Compress a UIImage to JPEG data at the given quality (0.0–1.0).
    static func compress(_ image: UIImage, quality: CGFloat = 0.7) -> Data {
        image.jpegData(compressionQuality: quality) ?? Data()
    }

    // MARK: Base64

    /// Resize, compress, and return a base64-encoded string of the image.
    static func base64(from image: UIImage) -> String {
        let resized = resize(image)
        let data = compress(resized)
        return data.base64EncodedString()
    }

    // MARK: GPS / EXIF

    /// Extract GPS coordinates embedded in a JPEG/HEIC EXIF header.
    ///
    /// - Parameter url: Local file URL to the image.
    /// - Returns: `(latitude, longitude)` as formatted strings, or `nil` if unavailable.
    static func extractGPS(from url: URL) -> (latitude: String, longitude: String)? {
        guard let source = CGImageSourceCreateWithURL(url as CFURL, nil),
              let properties = CGImageSourceCopyPropertiesAtIndex(source, 0, nil) as? [String: Any],
              let gps = properties[kCGImagePropertyGPSDictionary as String] as? [String: Any],
              let lat = gps[kCGImagePropertyGPSLatitude as String] as? Double,
              let lon = gps[kCGImagePropertyGPSLongitude as String] as? Double else {
            return nil
        }

        let latRef = gps[kCGImagePropertyGPSLatitudeRef as String] as? String ?? "N"
        let lonRef = gps[kCGImagePropertyGPSLongitudeRef as String] as? String ?? "E"

        let finalLat = latRef == "S" ? -lat : lat
        let finalLon = lonRef == "W" ? -lon : lon

        return (String(format: "%.6f", finalLat), String(format: "%.6f", finalLon))
    }
}
