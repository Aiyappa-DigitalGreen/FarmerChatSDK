# FarmerChat iOS SDK

A Swift Package that embeds the FarmerChat AI chat experience into any iOS application.

## Requirements

- iOS 16.0+
- Swift 5.9+
- Xcode 15+

## Installation

### Swift Package Manager

Add the following to your `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/your-org/FarmerChatSDK-iOS.git", from: "1.0.0")
]
```

Or in Xcode: **File → Add Package Dependencies** and enter the repository URL.

## Quick Start

### 1. Configure the SDK

Call this once, typically in your `AppDelegate` or `App.init`:

```swift
import FarmerChatSDK

let config = FarmerChatConfig(
    baseUrl: "https://api.farmerchat.com",
    accessToken: "your-access-token",
    refreshToken: "your-refresh-token",
    userId: "user-123",
    deviceId: UIDevice.current.identifierForVendor?.uuidString ?? UUID().uuidString,
    contentProviderId: "provider-456",  // optional
    conversationId: nil                 // optional — resume an existing conversation
)

try FarmerChatSDK.shared.configure(config)
```

### 2. Add the Floating Action Button

```swift
import SwiftUI
import FarmerChatSDK

struct ContentView: View {
    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            // Your existing content
            MainView()

            // FarmerChat FAB
            ChatFAB(conversationId: nil)
                .padding(24)
        }
    }
}
```

### 3. Present the Chat Screen Directly

```swift
import SwiftUI
import FarmerChatSDK

struct MyView: View {
    @State private var showChat = false

    var body: some View {
        Button("Open Chat") { showChat = true }
            .fullScreenCover(isPresented: $showChat) {
                ChatContainerView(conversationId: nil, onDismiss: { showChat = false })
            }
    }
}
```

## Public API

### `FarmerChatConfig`

| Parameter | Type | Description |
|-----------|------|-------------|
| `baseUrl` | `String` | Base URL of the FarmerChat backend |
| `accessToken` | `String` | JWT access token |
| `refreshToken` | `String` | JWT refresh token |
| `userId` | `String` | Authenticated user identifier |
| `deviceId` | `String` | Stable device identifier |
| `contentProviderId` | `String?` | Optional content provider scope |
| `conversationId` | `String?` | Optional existing conversation to resume |

### `FarmerChatSDK.shared`

- `configure(_ config: FarmerChatConfig) throws` — Initialize the SDK
- `updateTokens(accessToken:refreshToken:) throws` — Refresh tokens externally

### Views

| View | Description |
|------|-------------|
| `ChatFAB` | Floating action button that opens the chat full-screen |
| `ChatContainerView` | Root chat container (navigation + history + chat screen) |

## Architecture

```
FarmerChatSDK
├── Network Layer      URLSession-based API client with automatic token refresh
├── Models             Codable request/response types + UI state models
├── Storage            Keychain-backed token persistence
├── ViewModels         @MainActor ObservableObject state machines
├── Views              SwiftUI views (ChatFAB → ChatContainerView → ChatScreen)
├── Markdown           Custom markdown parser and renderer
├── Audio              AVFoundation recording + playback services
├── Image              Photo picker, camera, image processing
└── Utilities          Device info, extensions
```

## Token Refresh Flow

1. On 401 response: attempt `POST /api/user/get_new_access_token/` with current refresh token.
2. If that fails: fall back to `POST /api/user/send_tokens/` using `GUEST_USER_API_KEY`.
3. All in-flight requests queue behind a single refresh (no duplicate refresh storms).

## Permissions

Add the following keys to your app's `Info.plist`:

```xml
<key>NSMicrophoneUsageDescription</key>
<string>FarmerChat needs microphone access for voice input.</string>

<key>NSCameraUsageDescription</key>
<string>FarmerChat needs camera access to analyse crop images.</string>

<key>NSPhotoLibraryUsageDescription</key>
<string>FarmerChat needs photo library access to select crop images.</string>
```

## License

Copyright © 2024 FarmerChat. All rights reserved.
