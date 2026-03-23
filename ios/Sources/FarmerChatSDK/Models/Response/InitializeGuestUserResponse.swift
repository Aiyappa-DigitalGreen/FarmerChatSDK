import Foundation

struct InitializeGuestUserResponse: Decodable {
    let access_token: String
    let refresh_token: String
    let user_id: String?
    let created_now: Bool?   // true = new user, false = existing user returned for this device_id
    let country_code: String?
    let country: String?
    let state: String?
}
