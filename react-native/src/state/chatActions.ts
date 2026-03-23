/**
 * chatActions.ts
 * Discriminated union of all actions for the chat state machine.
 *
 * Public actions are dispatched from UI components.
 * Internal (_-prefixed) actions are dispatched by useChatViewModel only.
 */

import { ChatMessage } from '../models/chatModels';

export type ChatAction =
  // ─── Public: chat entry points ────────────────────────────────────────────

  /** Start a fresh conversation with a user-typed or follow-up question */
  | {
      type: 'INITIALIZE_WITH_QUESTION';
      question: string;
      transcriptionId?: string;
      audioUri?: string;
    }

  /** Show pre-generated Q&A without making a network call */
  | {
      type: 'INITIALIZE_WITH_PREGENERATEDCONTENT';
      question: string;
      answer?: string;
      followUpQuestions?: string[];
    }

  /** User tapped a follow-up question chip */
  | {
      type: 'SEND_FOLLOW_UP_QUESTION';
      question: string;
      followUpQuestionId?: string;
      transcriptionId?: string;
      audioUri?: string;
    }

  /** User attached an image and (optionally) typed a query */
  | {
      type: 'SEND_QUESTION_WITH_IMAGE';
      question: string;
      imageUri: string;
    }

  /** Raw audio file URI + base64 data → transcribe API → send as text query */
  | {
      type: 'TRANSCRIBE_AND_SEND_AUDIO';
      audioUri: string;
      audioBase64: string;
    }

  /** Load history for a specific conversation */
  | {
      type: 'LOAD_CHAT_HISTORY';
      conversationId: string;
      page?: number;
    }

  /** Retry the last failed request */
  | { type: 'RETRY_LAST_REQUEST' }

  /** Dismiss the error banner */
  | { type: 'CLEAR_ERROR' }

  /** Request TTS synthesis for the most recent AI message */
  | { type: 'SYNTHESISE_AUDIO' }

  /** Set audio playback state */
  | { type: 'SET_AUDIO_PLAYING'; isPlaying: boolean }

  /** Clear the current playback URL */
  | { type: 'CLEAR_AUDIO_PLAYBACK_URL' }

  /** Clear all messages from state */
  | { type: 'CLEAR_MESSAGES' }

  // ─── Internal: dispatched only by useChatViewModel ────────────────────────

  | { type: '_UPDATE_MESSAGE_TEXT'; id: string; text: string }
  | { type: '_SET_LOADING'; isLoading: boolean }
  | { type: '_SET_MESSAGES'; messages: ChatMessage[] }
  | { type: '_APPEND_MESSAGE'; message: ChatMessage }
  | { type: '_REPLACE_LAST_LOADING_WITH'; message: ChatMessage }
  | { type: '_SET_ERROR'; errorMessage: string; failedMessageId?: string }
  | { type: '_SET_SUGGESTED_QUESTIONS'; questions: string[]; ids: string[] }
  | { type: '_SET_AUDIO_URL'; url: string }
  | { type: '_SET_CONVERSATION_ID'; conversationId: string }
  | { type: '_SET_HISTORY_NEXT_PAGE'; page: number | undefined }
  | { type: '_SET_INITIAL_HISTORY_LOADED' }
  | { type: '_PREPEND_MESSAGES'; messages: ChatMessage[] }
  | { type: '_SET_SYNTHESISE_AUDIO_LOADING'; isLoading: boolean };
