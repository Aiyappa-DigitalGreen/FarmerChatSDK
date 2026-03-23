/**
 * chatReducer.ts
 * Pure reducer for the chat state machine.
 * All state transitions are immutable.
 */

import { ChatState, initialChatState } from '../models/chatModels';
import { ChatAction } from './chatActions';

export function chatReducer(state: ChatState, action: ChatAction): ChatState {
  switch (action.type) {
    // ─── Public actions handled here (lightweight state changes) ─────────────

    case 'CLEAR_ERROR':
      return {
        ...state,
        errorMessage: undefined,
        failedMessageId: undefined,
      };

    case 'SET_AUDIO_PLAYING':
      return {
        ...state,
        isAudioPlaying: action.isPlaying,
      };

    case 'CLEAR_AUDIO_PLAYBACK_URL':
      return {
        ...state,
        audioPlaybackUrl: undefined,
        isAudioPlaying: false,
      };

    case 'CLEAR_MESSAGES':
      return {
        ...initialChatState,
        conversationId: state.conversationId,
      };

    // ─── Internal state mutations ─────────────────────────────────────────────

    case '_SET_LOADING':
      return { ...state, isLoading: action.isLoading };

    case '_SET_MESSAGES':
      return {
        ...state,
        messages: action.messages,
        isLoading: false,
        errorMessage: undefined,
      };

    case '_APPEND_MESSAGE':
      return {
        ...state,
        messages: [...state.messages, action.message],
      };

    case '_REPLACE_LAST_LOADING_WITH': {
      // Find the last loadingPlaceholder and replace it
      const msgs = [...state.messages];
      const lastLoadingIdx = msgs
        .map((m, i) => ({ type: m.type, i }))
        .filter((x) => x.type === 'loadingPlaceholder')
        .pop()?.i;

      if (lastLoadingIdx !== undefined) {
        msgs[lastLoadingIdx] = action.message;
      } else {
        msgs.push(action.message);
      }

      return {
        ...state,
        messages: msgs,
        isLoading: false,
        errorMessage: undefined,
        failedMessageId: undefined,
      };
    }

    case '_SET_ERROR':
      return {
        ...state,
        isLoading: false,
        errorMessage: action.errorMessage,
        failedMessageId: action.failedMessageId,
        // Remove any orphaned loading bubble on error
        messages: state.messages.filter(
          (m) => m.type !== 'loadingPlaceholder',
        ),
      };

    case '_SET_SUGGESTED_QUESTIONS':
      return {
        ...state,
        suggestedQuestions: action.questions,
        suggestedQuestionIds: action.ids,
      };

    case '_SET_AUDIO_URL':
      return {
        ...state,
        audioPlaybackUrl: action.url,
        isLoadingSynthesiseAudio: false,
      };

    case '_SET_CONVERSATION_ID':
      return { ...state, conversationId: action.conversationId };

    case '_SET_HISTORY_NEXT_PAGE':
      return { ...state, historyNextPage: action.page };

    case '_SET_INITIAL_HISTORY_LOADED':
      return { ...state, isInitialHistoryLoaded: true };

    case '_PREPEND_MESSAGES':
      return {
        ...state,
        messages: [...action.messages, ...state.messages],
      };

    case '_SET_SYNTHESISE_AUDIO_LOADING':
      return { ...state, isLoadingSynthesiseAudio: action.isLoading };

    case '_UPDATE_MESSAGE_TEXT':
      return {
        ...state,
        messages: state.messages.map((m) =>
          m.id === action.id && m.type === 'userMessage'
            ? { ...m, text: action.text }
            : m,
        ),
      };

    // ─── Async actions (side effects handled in useChatViewModel) ─────────────
    // These do NOT mutate state directly — useChatViewModel handles them
    // and dispatches _-prefixed actions above.
    case 'INITIALIZE_WITH_QUESTION':
    case 'INITIALIZE_WITH_PREGENERATEDCONTENT':
    case 'SEND_FOLLOW_UP_QUESTION':
    case 'SEND_QUESTION_WITH_IMAGE':
    case 'TRANSCRIBE_AND_SEND_AUDIO':
    case 'LOAD_CHAT_HISTORY':
    case 'RETRY_LAST_REQUEST':
    case 'SYNTHESISE_AUDIO':
      // These are intercepted by the viewmodel's dispatch wrapper
      // Returning state unchanged here is correct
      return state;

    default: {
      const _exhaustive: never = action;
      void _exhaustive;
      return state;
    }
  }
}
