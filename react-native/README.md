# FarmerChat SDK for React Native

A production-ready React Native TypeScript SDK for integrating FarmerChat AI agricultural assistance into your mobile applications.

## Features

- **AI Chat**: Text, voice, and image-based queries with AI responses
- **Voice Input**: Record audio questions with live waveform visualization
- **Image Analysis**: Analyze crop/plant images for agricultural advice
- **Text-to-Speech**: Listen to AI responses via synthesized audio
- **Follow-up Questions**: Smart follow-up question suggestions
- **Chat History**: Full conversation history with pagination
- **Token Refresh**: Automatic token refresh with fallback to guest token
- **Secure Storage**: Tokens stored securely via Keychain

## Installation

```bash
npm install farmer-chat-sdk
# or
yarn add farmer-chat-sdk
```

### Peer Dependencies

```bash
npm install react react-native
npm install react-native-audio-recorder-player react-native-image-picker react-native-keychain react-native-markdown-display
```

### iOS Setup

```bash
cd ios && pod install
```

Add to `Info.plist`:
```xml
<key>NSMicrophoneUsageDescription</key>
<string>FarmerChat needs microphone access to record voice questions.</string>
<key>NSCameraUsageDescription</key>
<string>FarmerChat needs camera access to analyze crop images.</string>
<key>NSPhotoLibraryUsageDescription</key>
<string>FarmerChat needs photo library access to select crop images.</string>
```

### Android Setup

Add to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## Quick Start

```typescript
import { FarmerChatSDK, ChatFAB } from 'farmer-chat-sdk';

// 1. Configure the SDK once at app startup
FarmerChatSDK.configure({
  baseUrl: 'https://your-farmerchat-server.com',
  accessToken: 'your-access-token',
  refreshToken: 'your-refresh-token',
  userId: 'user-123',
  deviceId: 'device-abc',
  contentProviderId: 'provider-xyz', // optional
});

// 2. Add the ChatFAB anywhere in your app
function MyApp() {
  return (
    <View style={{ flex: 1 }}>
      {/* Your app content */}
      <ChatFAB
        style={{ position: 'absolute', bottom: 24, right: 24 }}
        tintColor="#2E7D32"
      />
    </View>
  );
}
```

## Controlled Modal

```typescript
import { FarmerChatSDK, ChatModal } from 'farmer-chat-sdk';
import { useState } from 'react';

function MyScreen() {
  const [showChat, setShowChat] = useState(false);

  return (
    <>
      <Button title="Open FarmerChat" onPress={() => setShowChat(true)} />
      <ChatModal
        visible={showChat}
        onClose={() => setShowChat(false)}
        conversationId="existing-convo-id" // optional: resume a conversation
      />
    </>
  );
}
```

## API Reference

### `FarmerChatSDK`

| Method | Description |
|--------|-------------|
| `configure(config)` | Initialize SDK with credentials |
| `getConfig()` | Get current config (throws if not configured) |
| `updateTokens(access, refresh)` | Update tokens after external refresh |

### `ChatFAB` Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `conversationId` | `string` | `undefined` | Resume existing conversation |
| `style` | `ViewStyle` | `{}` | Additional FAB container styles |
| `tintColor` | `string` | `'#2E7D32'` | FAB background color |
| `fabSize` | `number` | `56` | FAB diameter in dp |

### `ChatModal` Props

| Prop | Type | Description |
|------|------|-------------|
| `visible` | `boolean` | Controls modal visibility |
| `onClose` | `() => void` | Called when user closes the modal |
| `conversationId` | `string` | Optional: resume existing conversation |

## Architecture

```
SDK Core
├── FarmerChatSDK          - Singleton config holder
├── ChatApiClient          - All REST API calls
├── AuthInterceptor        - Adds auth headers
├── TokenRefreshHandler    - Auto token refresh logic
└── TokenStorage           - Secure keychain storage

State Management
├── useChatViewModel       - Main chat logic + useReducer
├── useHistoryViewModel    - Conversation list logic
├── ChatContext            - React context provider
├── chatReducer            - Pure state reducer
└── chatActions            - Discriminated union of actions

UI Components
├── ChatFAB                - Floating action button (PUBLIC)
├── ChatModal              - Full screen modal (PUBLIC)
├── ChatScreen             - Main chat view
├── ChatHistoryScreen      - Conversation list
├── ChatThread             - Message list (FlatList)
├── UserMessageBubble      - User messages
├── AIMessageBubble        - AI responses with markdown
├── LoadingBubble          - Animated typing indicator
├── FollowUpQuestionsBar   - Horizontal follow-up chips
├── ChatInputBar           - Text/voice/image input
├── VoiceInputOverlay      - Full-screen voice recording
├── WaveformView           - 40-bar amplitude visualization
└── ListenButton           - TTS playback control
```

## License

MIT
