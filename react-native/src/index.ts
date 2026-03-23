/**
 * index.ts
 * Public barrel export for the FarmerChat SDK.
 * Only items exported here are part of the public API.
 */

// ─── SDK Configuration ────────────────────────────────────────────────────────

export { FarmerChatSDK } from './config/SDKConfig';
export type { SDKConfiguration } from './config/SDKConfig';

// ─── Public UI Components ─────────────────────────────────────────────────────

export { ChatFAB } from './components/ChatFAB';
export type { ChatFABProps } from './components/ChatFAB';

export { ChatModal } from './components/ChatModal';
export type { ChatModalProps } from './components/ChatModal';

// ─── Models / Types ───────────────────────────────────────────────────────────

// Request types
export type {
  NewConversationRequest,
  TextPromptRequest,
  ImageAnalysisRequest,
  SynthesiseAudioRequest,
  FollowUpQuestionClickRequest,
  TranscribeAudioRequest,
  RefreshTokenRequest,
  SendNewTokenRequest,
  TriggeredInputType,
  AudioEncodingFormat,
} from './models/requests';

// Response types
export type {
  TextPromptResponse,
  FollowUpQuestionOption,
  IntentClassificationOutput,
  ImageAnalysisResponse,
  FollowUpQuestionsResponse,
  Question,
  SynthesiseAudioResponse,
  ConversationChatHistoryResponse,
  ConversationChatHistoryMessageItem,
  ConversationChatHistoryQuestion,
  ConversationListResponse,
  ConversationListItem,
  NewConversationResponse,
  GetVoiceResponse,
  RefreshTokenResponse,
} from './models/responses';

// Chat domain models
export type {
  ChatMessage,
  UserMessage,
  AIResponseMessage,
  LoadingPlaceholder,
  ChatState,
  ChatEntrySource,
  ConversationHistoryDisplayItem,
} from './models/chatModels';

export { initialChatState } from './models/chatModels';

// ─── State / Hooks ────────────────────────────────────────────────────────────

export { ChatProvider, useChatContext } from './state/ChatContext';
export { useChatViewModel } from './state/useChatViewModel';
export { useHistoryViewModel } from './state/useHistoryViewModel';
export type { HistoryViewModel, HistoryViewModelState } from './state/useHistoryViewModel';
export type { ChatAction } from './state/chatActions';
export { chatReducer } from './state/chatReducer';

// ─── Network ──────────────────────────────────────────────────────────────────

export { ChatApiClient } from './network/ChatApiClient';
export {
  NetworkError,
  TokenExpiredError,
  NetworkUnavailableError,
  ApiError,
  ServerError,
} from './network/NetworkError';

// ─── Storage ──────────────────────────────────────────────────────────────────

export { TokenStorage } from './storage/TokenStorage';

// ─── Guest Initialization ─────────────────────────────────────────────────────

export { initializeGuestUser } from './network/GuestApiClient';
export type { InitializeGuestUserResponse } from './network/GuestApiClient';

// ─── Audio ────────────────────────────────────────────────────────────────────

export { AudioRecorderService } from './audio/AudioRecorderService';
export { AudioPlayerService } from './audio/AudioPlayerService';
export { useWaveformSampler } from './audio/useWaveformSampler';

// ─── Image ────────────────────────────────────────────────────────────────────

export { pickFromGallery, takePhoto } from './image/ImagePickerService';
export type { ImagePickerResult } from './image/ImagePickerService';
export { getBase64FromUri, getImageDimensions, uriToFileName } from './image/ImageProcessor';

// ─── Utils ────────────────────────────────────────────────────────────────────

export { buildDeviceInfoHeader } from './utils/deviceInfo';
export { fileToBase64, base64ByteLength } from './utils/base64';
export { groupConversationsByDate, formatDate, formatTime } from './utils/dateUtils';

// ─── Sub-components (advanced use) ───────────────────────────────────────────

export { ChatThread } from './components/chat/ChatThread';
export { UserMessageBubble } from './components/chat/UserMessageBubble';
export { AIMessageBubble } from './components/chat/AIMessageBubble';
export { LoadingBubble } from './components/chat/LoadingBubble';
export { FollowUpQuestionsBar } from './components/chat/FollowUpQuestionsBar';
export { ChatResponseActions } from './components/chat/ChatResponseActions';
export { InlineErrorView } from './components/chat/InlineErrorView';
export { ChatInputBar } from './components/input/ChatInputBar';
export { VoiceInputOverlay } from './components/input/VoiceInputOverlay';
export { WaveformView } from './components/input/WaveformView';
export { PhotoInputSheet } from './components/input/PhotoInputSheet';
export { ListenButton } from './components/audio/ListenButton';
export { SoundWaveAnimation } from './components/audio/SoundWaveAnimation';
export { MarkdownMessage } from './components/markdown/MarkdownMessage';
export { ChatScreen } from './components/screens/ChatScreen';
export { ChatHistoryScreen } from './components/screens/ChatHistoryScreen';

// ─── Constants ────────────────────────────────────────────────────────────────

export {
  PRIMARY_GREEN,
  LIGHT_GREEN,
  SURFACE_COLOR,
  WHITE,
  TEXT_PRIMARY,
  TEXT_SECONDARY,
  ERROR_COLOR,
  BUILD_VERSION,
  WAVEFORM_BAR_COUNT,
} from './config/constants';
