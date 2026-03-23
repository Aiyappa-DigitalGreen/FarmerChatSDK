/**
 * chatModels.ts
 * Domain models for the chat UI state machine.
 */

// ─── Chat Message discriminated union ────────────────────────────────────────

export type ChatMessage =
  | UserMessage
  | AIResponseMessage
  | LoadingPlaceholder;

export interface UserMessage {
  type: 'userMessage';
  id: string;
  text: string;
  imageUri?: string;
  audioUri?: string;
}

export interface AIResponseMessage {
  type: 'aiResponse';
  id: string;
  text: string;
  followUpQuestions?: string[];
  followUpQuestionIds?: string[];
  /** True when the answer was pre-provided, not fetched live */
  isPreGenerated: boolean;
  messageId?: string;
  hideTtsSpeaker?: boolean;
  contentProviderLogo?: string;
}

export interface LoadingPlaceholder {
  type: 'loadingPlaceholder';
  id: string;
}

// ─── Chat State ───────────────────────────────────────────────────────────────

export interface ChatState {
  messages: ChatMessage[];
  /** Currently visible follow-up question labels */
  suggestedQuestions?: string[];
  /** IDs parallel to suggestedQuestions */
  suggestedQuestionIds?: string[];
  isLoading: boolean;
  errorMessage?: string;
  /** ID of the user message that failed, for retry targeting */
  failedMessageId?: string;
  isLoadingSynthesiseAudio: boolean;
  /** Remote URL of synthesised TTS audio */
  audioPlaybackUrl?: string;
  isAudioPlaying: boolean;
  /** Next page number for history pagination, undefined if no more pages */
  historyNextPage?: number;
  isInitialHistoryLoaded: boolean;
  conversationId?: string;
}

export const initialChatState: ChatState = {
  messages: [],
  isLoading: false,
  isLoadingSynthesiseAudio: false,
  isAudioPlaying: false,
  isInitialHistoryLoaded: false,
};

// ─── Entry Source ─────────────────────────────────────────────────────────────

export type ChatEntrySource =
  | 'text'
  | 'image'
  | 'voice'
  | { type: 'history'; conversationId: string };

// ─── History Item (for display) ───────────────────────────────────────────────

export interface ConversationHistoryDisplayItem {
  conversationId: string;
  title: string;
  dateLabel: string;
  grouping?: string;
}
