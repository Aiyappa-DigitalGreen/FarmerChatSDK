import Foundation

// MARK: - ImageAnalysisRequest
// Matches api/chat/image_analysis/
// Using camelCase — encoder uses .convertToSnakeCase to produce correct JSON keys.

struct ImageAnalysisRequest: Encodable {
    let conversationId: String      // → "conversation_id"
    let image: String               // base64-encoded image → "image"
    let triggeredInputType: String  // → "triggered_input_type" (always "image")
    let query: String?              // optional text query alongside the image
    let latitude: String?           // GPS latitude from EXIF
    let longitude: String?          // GPS longitude from EXIF
    let imageName: String           // → "image_name" unique filename e.g. "image_<uuid>.jpg"
    let retry: Bool
}
