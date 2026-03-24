# FarmerChat SDK

A multi-platform chat SDK extracted from the [FarmerChat](https://farmer.chat) Android app. Integrates as a FAB (Floating Action Button) into any Android, iOS, or React Native app — tapping it opens the full AI chat experience.

---

## SDK Overview

| Platform | Language | Folder | Entry Point |
|----------|----------|--------|-------------|
| Android | Kotlin + Jetpack Compose | `android/` | `FarmerChatFab` Composable |
| iOS | Swift + SwiftUI | `ios/` | `ChatFAB` SwiftUI View |
| React Native | TypeScript | `react-native/` | `<ChatFAB />` Component |

All three SDKs replicate the **complete** FarmerChat chat flow:
- Text, voice, and image (camera/gallery) input
- AI responses with Markdown rendering
- Follow-up question suggestions
- Text-to-Speech (Listen button with sound-wave animation)
- Chat history with pagination
- Token auto-refresh on 401

---

## API Endpoints (All 9 Replicated)

| Method | Path | Purpose |
|--------|------|---------|
| POST | `api/chat/new_conversation/` | Create new conversation |
| POST | `api/chat/get_answer_for_text_query/` | Text/transcribed-audio → AI response |
| POST | `api/chat/image_analysis/` | Image (base64) → AI analysis |
| GET | `api/chat/follow_up_questions/` | Fetch follow-up suggestions |
| POST | `api/chat/follow_up_question_click/` | Track follow-up tap |
| POST | `api/chat/synthesise_audio/` | TTS — text → audio URL |
| GET | `api/chat/conversation_chat_history/` | Load specific conversation |
| GET | `api/chat/conversation_list/` | Paginated past conversations |
| POST | `api/chat/transcribe_audio/` | Voice recording → text |

---

## Android SDK

### Requirements
- Min SDK 29 (Android 10), Target SDK 36
- Kotlin 2.1.0, Compose BOM 2025.02

### Setup

**1. Add the module to your project** (or publish the AAR):
```kotlin
// settings.gradle.kts (in your host app)
includeBuild("/path/to/FarmerChatSDK/android") {
    dependencySubstitution {
        substitute(module("com.farmerchat:sdk")).using(project(":farmerchat-sdk"))
    }
}
```

Or add the AAR directly:
```kotlin
// app/build.gradle.kts
dependencies {
    implementation(files("libs/farmerchat-sdk-1.0.0.aar"))
}
```

**2. Initialize in your Application class:**
```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FarmerChatSdk.initialize(
            context = this,
            config = FarmerChatConfig(
                sdkApiKey = "fc_live_your_key_here",
                baseUrl = "https://your-api-base-url.com/"
            )
        )
    }
}
```

**3. Add the FAB to any Compose screen:**
```kotlin
Scaffold(
    floatingActionButton = { FarmerChatFab() }
) { ... }
```

**4. Or open chat programmatically:**
```kotlin
FarmerChatSdk.openChat(context)
// or with a specific conversation:
FarmerChatSdk.openChat(context, conversationId = "conv_xyz")
```

**5. After your own token refresh:**
```kotlin
FarmerChatSdk.updateTokens(context, newAccessToken, newRefreshToken)
```

### Required Permissions (merge into host app AndroidManifest.xml):
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

### Build:
```bash
cd android/
./gradlew :farmerchat-sdk:assembleRelease
```

---

## iOS SDK

### Requirements
- iOS 16.0+, Swift 5.9+, Xcode 15+

### Setup

**1. Add via Swift Package Manager:**
In Xcode → File → Add Package Dependencies → paste the local path or repo URL.

Or in `Package.swift`:
```swift
.package(path: "/path/to/FarmerChatSDK/ios")
```

**2. Configure in AppDelegate or @main:**
```swift
import FarmerChatSDK

@main
struct MyApp: App {
    init() {
        try? FarmerChatSDK.shared.configure(FarmerChatConfig(
            sdkApiKey: "fc_live_your_key_here",
            baseUrl: "https://your-api-base-url.com/"
        ))
    }

    var body: some Scene {
        WindowGroup { ContentView() }
    }
}
```

**3. Add the FAB to any SwiftUI view:**
```swift
import FarmerChatSDK

struct ContentView: View {
    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            YourMainContent()
            ChatFAB()
                .padding(24)
        }
    }
}
```

**4. After your own token refresh:**
```swift
try? FarmerChatSDK.shared.updateTokens(
    accessToken: newToken,
    refreshToken: newRefreshToken
)
```

### Required `Info.plist` entries:
```xml
<key>NSCameraUsageDescription</key>
<string>Take photos to ask about crops</string>
<key>NSMicrophoneUsageDescription</key>
<string>Record voice questions</string>
<key>NSPhotoLibraryUsageDescription</key>
<string>Choose photos from your library</string>
```

---

## React Native SDK

### Requirements
- React Native ≥ 0.73, React ≥ 18

### Setup

**1. Install (local):**
```bash
npm install /path/to/FarmerChatSDK/react-native
# or with yarn:
yarn add /path/to/FarmerChatSDK/react-native
```

**2. Install native dependencies:**
```bash
npm install react-native-audio-recorder-player react-native-image-picker react-native-keychain react-native-markdown-display
npx pod-install  # iOS
```

**3. Configure once (e.g. in App.tsx):**
```typescript
import { FarmerChatSDK } from 'farmer-chat-sdk'

FarmerChatSDK.configure({
  sdkApiKey: 'fc_live_your_key_here',
  baseUrl: 'https://your-api-base-url.com/',
})
```

**4. Use the FAB in any screen:**
```typescript
import { ChatFAB } from 'farmer-chat-sdk'

export default function HomeScreen() {
  return (
    <View style={{ flex: 1 }}>
      <YourContent />
      <ChatFAB style={{ position: 'absolute', bottom: 24, right: 24 }} />
    </View>
  )
}
```

**5. Or open the modal programmatically:**
```typescript
import { ChatModal } from 'farmer-chat-sdk'

const [chatOpen, setChatOpen] = useState(false)

<ChatModal
  visible={chatOpen}
  onClose={() => setChatOpen(false)}
/>
```

**6. After your own token refresh:**
```typescript
FarmerChatSDK.updateTokens('new_access_token', 'new_refresh_token')
```

### Android `AndroidManifest.xml` additions:
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

### iOS `Info.plist` additions:
```xml
<key>NSCameraUsageDescription</key><string>Take photos</string>
<key>NSMicrophoneUsageDescription</key><string>Record voice</string>
<key>NSPhotoLibraryUsageDescription</key><string>Choose photos</string>
```

---

## UI Customization (Android)

Every visual aspect of the SDK can be customized by passing additional fields to `FarmerChatConfig`. All customization fields are optional — defaults match the FarmerChat green theme.

### Full customization example:
```kotlin
FarmerChatSdk.initialize(
    context = this,
    config = FarmerChatConfig(
        sdkApiKey = "fc_live_your_key_here",
        baseUrl   = "https://your-api-base-url.com/",

        // ── FAB ──────────────────────────────────────────────────────
        fabLabel           = "Ask AI",                   // label next to icon
        fabIcon            = Icons.Filled.SmartToy,      // any Material icon
        fabBackgroundColor = 0xFF1565C0L,                // FAB button color (ARGB)
        fabContentColor    = 0xFFFFFFFFL,                // FAB icon/text color

        // ── Theme ─────────────────────────────────────────────────────
        primaryColor       = 0xFF1565C0L,                // toolbar, active elements

        // ── Chat bubbles ──────────────────────────────────────────────
        userBubbleColor    = 0xFF1565C0L,                // user message background
        userBubbleTextColor = 0xFFFFFFFFL,               // user message text
        aiBubbleColor      = 0xFFE3F2FDL,                // AI response background
        aiBubbleTextColor  = 0xFF0D1117L,                // AI response text

        // ── Chat screen text ──────────────────────────────────────────
        chatTitle    = "My AI Assistant",
        chatSubtitle = "Powered by FarmerChat"
    )
)
```

All color values are standard `0xAARRGGBB` Long constants (same format as Android `Color` constants).

### Per-FAB overrides

You can also override FAB appearance individually without changing the global config:

```kotlin
FarmerChatFab(
    extended       = true,
    label          = "Chat",
    containerColor = Color(0xFF6200EE),    // purple FAB
    contentColor   = Color.White,
    icon           = Icons.Filled.Chat
)
```

### Customization reference table (Android)

| `FarmerChatConfig` field | Type | Default | What it controls |
|--------------------------|------|---------|-----------------|
| `fabLabel` | `String` | `"Chat with FarmerChat"` | FAB button label |
| `fabIcon` | `ImageVector` | `Icons.Filled.Forum` | FAB button icon |
| `fabBackgroundColor` | `Long?` | same as `primaryColor` | FAB background |
| `fabContentColor` | `Long` | `0xFFFFFFFF` | FAB icon + text color |
| `primaryColor` | `Long` | `0xFF2E7D32` | Toolbar, active tints |
| `userBubbleColor` | `Long` | `0xFF2E7D32` | User message bubble background |
| `userBubbleTextColor` | `Long` | `0xFFFFFFFF` | User message text |
| `aiBubbleColor` | `Long` | `0xFFF1F8E9` | AI response bubble background |
| `aiBubbleTextColor` | `Long` | `0xFF1C1B1F` | AI response text |
| `chatTitle` | `String` | `"FarmerChat"` | Top bar title |
| `chatSubtitle` | `String` | `"AI Farm Assistant"` | Top bar subtitle |

---

## UI Customization (iOS)

Pass color and text overrides to `FarmerChatConfig`:

```swift
try? FarmerChatSDK.shared.configure(FarmerChatConfig(
    sdkApiKey: "fc_live_your_key_here",
    baseUrl:   "https://your-api-base-url.com/",

    // FAB
    fabLabel:           "Ask AI",
    fabBackgroundColor: Color(hex: "#1565C0"),
    fabContentColor:    .white,

    // Theme
    primaryColor:       Color(hex: "#1565C0"),

    // Chat bubbles
    userBubbleColor:    Color(hex: "#1565C0"),
    userBubbleTextColor: .white,
    aiBubbleColor:      Color(hex: "#E3F2FD"),
    aiBubbleTextColor:  Color(hex: "#0D1117"),

    // Chat screen
    chatTitle:    "My AI Assistant",
    chatSubtitle: "Powered by FarmerChat"
))
```

---

## UI Customization (React Native)

Pass color and text overrides inside `configure`:

```typescript
FarmerChatSDK.configure({
  sdkApiKey: 'fc_live_your_key_here',
  baseUrl:   'https://your-api-base-url.com/',

  // FAB
  fabLabel:           'Ask AI',
  fabBackgroundColor: '#1565C0',
  fabContentColor:    '#FFFFFF',

  // Theme
  primaryColor:       '#1565C0',

  // Chat bubbles
  userBubbleColor:     '#1565C0',
  userBubbleTextColor: '#FFFFFF',
  aiBubbleColor:       '#E3F2FD',
  aiBubbleTextColor:   '#0D1117',

  // Chat screen
  chatTitle:    'My AI Assistant',
  chatSubtitle: 'Powered by FarmerChat',
})
```

Or pass props directly to `<ChatFAB>`:

```typescript
<ChatFAB
  style={{ position: 'absolute', bottom: 24, right: 24 }}
  label="Ask AI"
  backgroundColor="#1565C0"
  iconColor="#FFFFFF"
/>
```

---

## Chat Entry Points

All 4 entry points work out of the box:

| # | How to trigger | Input |
|---|---------------|-------|
| 1 | Type in chat | Text field → send |
| 2 | Camera icon | Camera or Gallery picker |
| 3 | Mic icon | Voice recording → auto-transcribed |
| 4 | Pass `conversationId` | Loads existing conversation from history |

---

## Excluded Integrations

- **MoEngage** — removed entirely
- **Plotline** — removed entirely
- Analytics callbacks are replaced by `SdkAnalyticsListener` (Android) / optional closures (iOS/RN)

---

## Architecture

```
Host App
  └── FarmerChatFab (tapped)
        └── Full-screen slide-up overlay (Dialog + AnimatedVisibility)
              ├── ChatScreen  ← ChatViewModel
              │     ├── ChatThreadContent (message list — both bubbles left-aligned)
              │     ├── ChatInputOverlays (text / voice / image)
              │     └── ChatResponseActions (TTS / Share / Save)
              └── HistoryScreen  ← HistoryViewModel
                    └── Paginated conversation list
```
